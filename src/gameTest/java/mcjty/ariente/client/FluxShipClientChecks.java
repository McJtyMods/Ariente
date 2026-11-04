package mcjty.ariente.client;

import com.mojang.blaze3d.platform.InputConstants;
import mcjty.ariente.Ariente;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.entities.fluxship.FluxShipClient;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipLaser;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Matrix4f;

import java.util.UUID;

/** Opt-in integration check using the real keyboard, network, passenger and render paths. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipClientChecks {

    private static boolean started;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static volatile UUID shipId;
    private static int ticks;
    private static int totalTicks;
    private static float observedRoll;
    private static float turnLookOffset;
    private static double startingAltitude;
    private static float maxCockpitYawDrift;
    private static int cockpitSamples;
    private static double maxCockpitProjectionDrift;
    private static Matrix4f pilotBasePose;
    private static float pilotBodyBeforeRender;
    private static float pilotHeadBeforeRender;
    private static float pilotPitchBeforeRender;
    private static int pilotRenderSamples;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforePilot(RenderPlayerEvent.Pre event) {
        if (checkingPilot(event.getEntity())) {
            pilotBasePose = new Matrix4f(event.getPoseStack().last().pose());
            pilotBodyBeforeRender = event.getEntity().yBodyRot;
            pilotHeadBeforeRender = event.getEntity().yHeadRot;
            pilotPitchBeforeRender = event.getEntity().getXRot();
        }
    }

    @SubscribeEvent
    public static void pilotInCabin(RenderLivingEvent.Pre<?, ?> event) {
        if (checkingPilot(event.getEntity())) {
            var player = Minecraft.getInstance().player;
            var ship = (FluxShipEntity) player.getVehicle();
            float heading = ship.getFlightYaw(event.getPartialTick());
            require(Math.abs(Mth.wrapDegrees(player.yBodyRot - heading)) < .001
                            && Math.abs(Mth.wrapDegrees(player.yHeadRot - heading)) < .001 && player.getXRot() == 0,
                    "The pilot model must face straight ahead while the third-person camera looks around");
            Vector3f up = new Matrix4f(pilotBasePose).invert().mul(event.getPoseStack().last().pose())
                    .transformDirection(new Vector3f(0, 1, 0));
            Vec3 cabinUp = ship.rotateCabinOffset(new Vec3(0, 1, 0), event.getPartialTick());
            // Cabin offsets use Minecraft's lookup-table trig; render rotations use quaternion trig.
            require(new Vec3(up.x, up.y, up.z).distanceTo(cabinUp) < .0005,
                    "The pilot model must bank and pitch with the cabin: actual=" + up + ", expected=" + cabinUp
                            + ", tick=" + ticks + ", partialTick=" + event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void seatedPilot(RenderLivingEvent.Post<?, ?> event) {
        if (checkingPilot(event.getEntity())) {
            require(event.getRenderer().getModel().riding, "The pilot model must use the seated pose");
            pilotRenderSamples++;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterPilot(RenderPlayerEvent.Post event) {
        if (checkingPilot(event.getEntity())) {
            require(event.getEntity().yBodyRot == pilotBodyBeforeRender && event.getEntity().yHeadRot == pilotHeadBeforeRender
                            && event.getEntity().getXRot() == pilotPitchBeforeRender
                            && event.getPoseStack().last().pose().equals(pilotBasePose, .000001f),
                    "Pilot rendering must restore the camera angles and pose afterward");
        }
    }

    private static boolean checkingPilot(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        return Boolean.getBoolean("ariente.shipClientCheck") && entity == mc.player
                && !mc.options.getCameraType().isFirstPerson() && entity.getVehicle() instanceof FluxShipEntity;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void camera(ViewportEvent.ComputeCameraAngles event) {
        if (Boolean.getBoolean("ariente.shipClientCheck")) {
            observedRoll = event.getRoll();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.shipClientCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.getOverlay() != null) {
            return;
        }
        require(failure == null, "Server fixture failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.fov().set(70);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.options.graphicsMode().set(Boolean.getBoolean("ariente.shipExhaustFabulous") ? GraphicsStatus.FABULOUS : GraphicsStatus.FANCY);
            mc.levelRenderer.allChanged();
            mc.setScreen(null);
            mc.mouseHandler.releaseMouse();
            UUID playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                try {
                    var server = mc.getSingleplayerServer();
                    var level = server.overworld();
                    level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
                    level.setDayTime(Boolean.getBoolean("ariente.shipExhaustNight") ? 18000 : 6000);
                    level.setWeatherParameters(6000, 0, false, false);
                    boolean tutorial = Boolean.getBoolean("ariente.shipTutorialCheck");
                    if (tutorial) {
                        checkTutorialSpawners(level);
                    }
                    level.getEntitiesOfClass(FluxShipEntity.class, new AABB(-100, 180, -100, 100, 260, 100))
                            .forEach(FluxShipEntity::discard);
                    if (!tutorial) {
                        for (BlockPos pos : BlockPos.betweenClosed(-40, 199, -40, 40, 199, 60)) {
                            level.setBlock(pos, (pos.getX() % 8 == 0 || pos.getZ() % 8 == 0 ? Blocks.LIGHT_BLUE_CONCRETE
                                    : Blocks.WHITE_CONCRETE).defaultBlockState(), 2);
                        }
                    }
                    var player = server.getPlayerList().getPlayer(playerId);
                    player.stopRiding();
                    player.setGameMode(GameType.CREATIVE);
                    double center = tutorial ? 128.5 : .5;
                    player.teleportTo(level, center, 201, center, 0, 5);
                    var ship = FluxShipEntity.create(level, center, 201, center);
                    shipId = ship.getUUID();
                    level.addFreshEntity(ship);
                    player.startRiding(ship, true);
                    ready = true;
                } catch (Throwable error) {
                    failure = error;
                }
            });
            return;
        }
        if (!ready) {
            return;
        }
        require(++totalTicks < 700, "Timed out waiting for the ship");
        if (!(mc.player.getVehicle() instanceof FluxShipEntity ship) || !ship.getUUID().equals(shipId)) {
            if (ticks >= 345) {
                require(Math.abs(observedRoll) < .01, "Dismounting must restore an upright camera");
                Ariente.setup.getLogger().info("FLUX SHIP CLIENT CHECKS PASSED: keyboard packets, telemetry, bank/pitch, free look, camera settings, seated pilot and dismount; {} cockpit samples, yaw drift={}, projection drift={}, pilot renders={}",
                        cockpitSamples, maxCockpitYawDrift, maxCockpitProjectionDrift, pilotRenderSamples);
                mc.stop();
            }
            return;
        }
        ticks++;
        FluxShipWeaponChecks.observeSpawns();
        if (ticks >= 95 && ticks < 115 || ticks >= 145 && ticks < 165) {
            float start = Mth.wrapDegrees(mc.player.getViewYRot(0) - ship.yRotO);
            Vector3f initialProjection = cockpitProjection(mc, ship, 0);
            for (float partialTick : new float[]{0, .25f, .5f, .75f, 1}) {
                float relative = Mth.wrapDegrees(mc.player.getViewYRot(partialTick)
                        - Mth.rotLerp(partialTick, ship.yRotO, ship.getYRot()));
                maxCockpitYawDrift = Math.max(maxCockpitYawDrift, Math.abs(Mth.wrapDegrees(relative - start)));
                maxCockpitProjectionDrift = Math.max(maxCockpitProjectionDrift,
                        initialProjection.distance(cockpitProjection(mc, ship, partialTick)));
                cockpitSamples++;
            }
        }
        if (ticks == 25) {
            FluxShipLightingChecks.run(ship);
            FluxShipSoundChecks.begin(ship);
            FluxShipWeaponChecks.begin(ship);
            require(ship.getHoloGui() instanceof HoloGuiEntity panel && panel.getVehicle() == ship
                            && panel.getGui(mc.player).findChild("ship_speed").isPresent()
                            && panel.getGui(mc.player).findChild("ship_altitude").isPresent(),
                    "The mounted cockpit instruments must reach the client");
            mc.player.setYRot(ship.getYRot());
            mc.player.setXRot(5);
        } else if (ticks == 28) {
            screenshot(mc, "ship-cockpit");
        } else if (ticks == 30) {
            key(GLFW.GLFW_KEY_W, true);
        } else if (ticks == 40) {
            key(GLFW.GLFW_KEY_V, true);
        } else if (ticks == 60) {
            screenshot(mc, "ship-firing-cockpit");
        } else if (ticks == 70) {
            key(GLFW.GLFW_KEY_V, false);
        } else if (ticks == 79) {
            FluxShipWeaponChecks.released(ship);
        } else if (ticks == 80) {
            FluxShipSoundChecks.cruise(ship);
            require(ship.getSpeed() > 95 && ship.getSpeed() < 104.1,
                    "Forward key packets must accelerate the server ship toward 104 blocks per second");
            turnLookOffset = Mth.wrapDegrees(mc.player.getYRot() - ship.getYRot());
            key(GLFW.GLFW_KEY_D, true);
        } else if (ticks == 115) {
            FluxShipSoundChecks.recovered(ship);
            FluxShipExhaustChecks.hiddenFromPilot(ship);
            require(cockpitSamples >= 100 && maxCockpitYawDrift < .02,
                    "The pilot and hull must interpolate together throughout each turning frame; drift=" + maxCockpitYawDrift);
            require(ship.getBank(1) > 8 && observedRoll > 8, "A moving right turn must bank the hull and first-person camera");
            require(Math.abs(Mth.wrapDegrees(mc.player.getYRot() - ship.getYRot() - turnLookOffset)) < 1,
                    "Turning must preserve the pilot's mouse-look offset");
            checkCameraOptions(mc, ship);
            screenshot(mc, "ship-banked-cockpit");
        } else if (ticks == 120) {
            FluxShipWeaponChecks.beforeFreeLook();
            key(GLFW.GLFW_KEY_V, true);
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            mc.player.setYRot(ship.getYRot() + 70);
            mc.player.setXRot(25);
        } else if (ticks == 125) {
            require(Math.abs(observedRoll) < .01, "Third-person cameras must remain upright");
            FluxShipExhaustChecks.flying(ship);
            screenshot(mc, "ship-banked-exterior");
        } else if (ticks == 130) {
            key(GLFW.GLFW_KEY_V, false);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.player.setYRot(ship.getYRot());
            mc.player.setXRot(5);
            startingAltitude = ship.getY();
            key(GLFW.GLFW_KEY_UP, true);
        } else if (ticks == 165) {
            require(cockpitSamples >= 200 && maxCockpitProjectionDrift < .003,
                    "The dashboard must stay steady in view during combined turns and climbs; projection drift=" + maxCockpitProjectionDrift);
            require(ship.getXRot() < -2 && ship.getY() > startingAltitude + 5,
                    "Rise key packets must climb and pitch the ship up");
            screenshot(mc, "ship-climbing-cockpit");
        } else if (ticks == 166) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            mc.player.setYRot(ship.getYRot() - 55);
            mc.player.setXRot(-25);
        } else if (ticks == 169) {
            require(pilotRenderSamples >= 5, "Both third-person cameras must render the seated, tilted pilot");
            screenshot(mc, "ship-climbing-pilot");
        } else if (ticks == 170) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.player.setYRot(ship.getYRot());
            mc.player.setXRot(5);
            key(GLFW.GLFW_KEY_W, false);
            key(GLFW.GLFW_KEY_D, false);
            key(GLFW.GLFW_KEY_UP, false);
        } else if (ticks == 230) {
            FluxShipSoundChecks.braking(ship);
            FluxShipExhaustChecks.braking(ship);
        } else if (ticks == 335) {
            FluxShipWeaponChecks.complete(ship);
            FluxShipSoundChecks.stopped(ship);
            FluxShipExhaustChecks.stopped(ship);
            require(ship.getSpeed() == 0 && Math.abs(ship.getBank(1)) < .1
                            && Math.abs(ship.getXRot()) < .1 && Math.abs(observedRoll) < .1,
                    "Releasing keys must brake, hover and level both ship and camera: speed=" + ship.getSpeed()
                            + ", bank=" + ship.getBank(1) + ", pitch=" + ship.getXRot() + ", roll=" + observedRoll);
            screenshot(mc, "ship-hovering-cockpit");
        } else if (ticks == 345) {
            key(GLFW.GLFW_KEY_LEFT_SHIFT, true);
        }
        if (ticks > 345) {
            key(GLFW.GLFW_KEY_LEFT_SHIFT, false);
        }
    }

    private static void checkTutorialSpawners(ServerLevel level) {
        int count = 0;
        for (int[] coordinates : new int[][]{{4, 4}, {8, 4}, {4, 7}, {10, 7}, {11, 8}, {5, 12}, {6, 12}}) {
            for (var blockEntity : level.getChunk(coordinates[0], coordinates[1]).getBlockEntities().values()) {
                if (blockEntity instanceof SpawnerBlockEntity spawner) {
                    String id = spawner.saveWithoutMetadata().getCompound("SpawnData").getCompound("entity").getString("id");
                    require(id.equals("arienteworld:soldier"), "The saved Tutorial spawner must migrate on load: " + id);
                    require(spawner.getSpawner().getOrCreateDisplayEntity(level, level.random, spawner.getBlockPos()) != null,
                            "The migrated Tutorial spawner must render its soldier");
                    count++;
                }
            }
        }
        require(count == 7, "The installed Tutorial fixture must include all seven legacy soldier spawners; found=" + count);
        Ariente.setup.getLogger().info("TUTORIAL SPAWNER MIGRATION CHECK PASSED: {} saved spawners", count);
    }

    private static Vector3f cockpitProjection(Minecraft mc, FluxShipEntity ship, float partialTick) {
        Vec3 center = FluxShipLaser.panelCenter(ship, partialTick);
        Vec3 relative = center.subtract(mc.player.getEyePosition(partialTick));
        var angles = new ViewportEvent.ComputeCameraAngles(mc.gameRenderer, mc.gameRenderer.getMainCamera(), partialTick,
                mc.player.getViewYRot(partialTick), mc.player.getViewXRot(partialTick), 0);
        FluxShipClient.cameraAngles(angles);
        Vector3f view = new Quaternionf().rotationZ(angles.getRoll() * Mth.DEG_TO_RAD)
                .rotateX(angles.getPitch() * Mth.DEG_TO_RAD)
                .rotateY((angles.getYaw() + 180) * Mth.DEG_TO_RAD)
                .transform(new Vector3f((float) relative.x, (float) relative.y, (float) relative.z));
        return new Vector3f(view.x / -view.z, view.y / -view.z, 0);
    }

    private static void checkCameraOptions(Minecraft mc, FluxShipEntity ship) {
        var camera = mc.gameRenderer.getMainCamera();
        float yaw = Mth.rotLerp(1, ship.yRotO, ship.getYRot());
        var sideways = new ViewportEvent.ComputeCameraAngles(mc.gameRenderer, camera, 1, yaw + 90, 0, 0);
        FluxShipClient.cameraAngles(sideways);
        require(sideways.getPitch() > 8 && Math.abs(sideways.getRoll()) < 1,
                "Looking sideways must project the ship bank into camera pitch");
        UtilityConfiguration.FLUX_SHIP_CAMERA_TILT.set(0.0);
        var disabled = new ViewportEvent.ComputeCameraAngles(mc.gameRenderer, camera, 1, yaw, 7, 0);
        FluxShipClient.cameraAngles(disabled);
        require(disabled.getPitch() == 7 && disabled.getRoll() == 0, "The client setting must disable camera tilt");
        UtilityConfiguration.FLUX_SHIP_CAMERA_TILT.set(1.0);
    }

    private static void key(int code, boolean down) {
        KeyMapping.set(InputConstants.Type.KEYSYM.getOrCreate(code), down);
    }

    private static void screenshot(Minecraft mc, String name) {
        try (var screenshot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            screenshot.writeToFile(mc.gameDirectory.toPath().resolve(name + ".png"));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
