package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IFluxShipPilot;
import mcjty.ariente.cables.CableColor;
import mcjty.ariente.cables.GenericCableBlock;
import mcjty.ariente.cables.GenericCableTileEntity;
import mcjty.ariente.client.textures.SurfaceTextures;
import mcjty.ariente.entities.fluxship.FluxShipEnergy;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.CameraType;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.UUID;

import static mcjty.ariente.blocks.BlockProperties.WORKING;

/** Real atlas, chunk-model, combined-network, battery synchronization and distant exhaust verification. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipChargingClientChecks {

    private static final BlockPos PAD = new BlockPos(0, 199, 0);
    private static UUID shipId;
    private static volatile boolean ready;
    private static boolean started;
    private static int ticks;
    private static ArmorStand observer;

    @SubscribeEvent
    public static void supplyTick(TickEvent.ServerTickEvent event) {
        if (Boolean.getBoolean("ariente.shipChargerCheck") && ready && event.phase == TickEvent.Phase.END) {
            ServerLevel level = event.getServer().overworld();
            if (level.getBlockEntity(PAD.below()) instanceof IPowerBlob connector) {
                // Ariente power is production per tick, rather than a persistent network battery.
                PowerSystem.getPowerSystem(level).addPower(connector.getCableId(), 2000, PowerType.NEGARITE);
                PowerSystem.getPowerSystem(level).addPower(connector.getCableId(), 2000, PowerType.POSIRITE);
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.shipChargerCheck") || event.phase != TickEvent.Phase.END
                || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null || mc.getOverlay() != null) {
            return;
        }
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.options.renderDistance().set(12);
            mc.options.graphicsMode().set(Boolean.getBoolean("ariente.chargerFabulous") ? GraphicsStatus.FABULOUS : GraphicsStatus.FANCY);
            mc.levelRenderer.allChanged();
            mc.setScreen(null);
            mc.mouseHandler.releaseMouse();
            UUID playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> setup(mc, playerId));
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        FluxShipEntity ship = (FluxShipEntity) mc.level.getEntitiesOfClass(FluxShipEntity.class,
                new AABB(-300, 180, -300, 300, 260, 500), entity -> entity.getUUID().equals(shipId)).stream().findFirst().orElse(null);
        require(ticks < 400, "Timed out waiting for the charger scene");
        if (ticks == 35) {
            mc.getSingleplayerServer().execute(() -> {
                var level = mc.getSingleplayerServer().overworld();
                Ariente.setup.getLogger().info("CHARGER SERVER STATE: {} power={} connector={}", level.getBlockState(PAD),
                        PowerReceiverSupport.getPowerAvailable(level, PAD, true), level.getBlockEntity(PAD.below()));
            });
            checkTop(mc, true);
            screenshot(mc, "charger-powered");
            mc.getSingleplayerServer().execute(() -> mc.getSingleplayerServer().overworld().removeBlock(PAD.below(), false));
        } else if (ticks == 55) {
            checkTop(mc, false);
            screenshot(mc, "charger-unpowered");
            mc.getSingleplayerServer().execute(() -> {
                ServerLevel level = mc.getSingleplayerServer().overworld();
                supply(level);
                FluxShipEntity serverShip = (FluxShipEntity) level.getEntity(shipId);
                serverShip.setPos(.5, 200.0625, .5);
                serverShip.setDeltaMovement(Vec3.ZERO);
                serverShip.setCharge(120_000);
            });
        } else if (ticks == 90) {
            require(ship != null && ship.getCharge() > 120_000 && ship.getCharge() < 180_000,
                    "A ship on the pad must receive and synchronize power from a combined connector: " + (ship == null ? null : ship.getCharge()));
            screenshot(mc, "charger-docked");
            UUID playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                var server = mc.getSingleplayerServer();
                var pilot = server.getPlayerList().getPlayer(playerId);
                FluxShipEntity serverShip = (FluxShipEntity) server.overworld().getEntity(shipId);
                serverShip.setPos(.5, 202, .5);
                pilot.startRiding(serverShip, true);
            });
        } else if (ticks == 115) {
            mc.player.setYRot(0);
            mc.player.setXRot(5);
        } else if (ticks == 125) {
            require(ship != null && mc.player.getVehicle() == ship && ship.getChargeRatio() > .1f && ship.getChargeRatio() < .2f,
                    "The cockpit must receive its partial battery charge");
            screenshot(mc, "ship-battery-low");
            mc.getSingleplayerServer().execute(() -> ((FluxShipEntity) mc.getSingleplayerServer().overworld().getEntity(shipId)).setCharge(0));
        } else if (ticks == 155) {
            require(ship != null && ship.getCharge() == 0, "The empty battery must reach the cockpit");
            screenshot(mc, "ship-battery-empty");
            UUID playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                ServerLevel level = mc.getSingleplayerServer().overworld();
                level.getServer().getPlayerList().getPlayer(playerId).stopRiding();
                FluxShipEntity serverShip = (FluxShipEntity) level.getEntity(shipId);
                serverShip.setCharge(FluxShipEnergy.CAPACITY);
                serverShip.setPos(.5, 208, .5);
                serverShip.setYRot(0);
                serverShip.setXRot(0);
                PatrolPilot pilot = new PatrolPilot(level);
                pilot.setPos(serverShip.position());
                level.addFreshEntity(pilot);
                pilot.startRiding(serverShip, true);
            });
            observer = new ArmorStand(mc.level, 0, 210, -24);
            mc.setCameraEntity(observer);
        }
        if (observer != null && ship != null) {
            Vec3 offset = ticks < 235 ? new Vec3(12, 6, -24) : new Vec3(40, 24, -140);
            observer.setPos(ship.position().add(offset));
            observer.setYRot((float) Math.toDegrees(Math.atan2(offset.x, -offset.z)));
            observer.yHeadRot = observer.yHeadRotO = observer.getYRot();
            observer.setXRot((float) Math.toDegrees(Math.atan2(offset.y, offset.horizontalDistance())));
            observer.setOldPosAndRot();
        }
        if (ticks == 215 || ticks == 270) {
            require(ship != null && ship.getExhaustStrength(1) > .4f, "Slower patrol flight must retain a clear exhaust");
            screenshot(mc, ticks == 215 ? "ship-patrol-exhaust-near" : "ship-patrol-exhaust-far");
        } else if (ticks == 280) {
            Ariente.setup.getLogger().info("FLUX SHIP CHARGER CLIENT CHECKS PASSED: animated/fullbright and unpowered tops, combined-network charging, synced low/empty cockpit bars, slow patrol exhaust at 27 and 147 blocks");
            mc.setCameraEntity(mc.player);
            mc.stop();
        }
    }

    private static void setup(Minecraft mc, UUID playerId) {
        var server = mc.getSingleplayerServer();
        ServerLevel level = server.overworld();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        level.setDayTime(Boolean.getBoolean("ariente.chargerDaytime") ? 6000 : 18000);
        level.setWeatherParameters(6000, 0, false, false);
        level.getEntitiesOfClass(FluxShipEntity.class, new AABB(-100, 180, -100, 100, 260, 100)).forEach(Entity::discard);
        for (BlockPos pos : BlockPos.betweenClosed(-12, 199, -12, 12, 199, 12)) {
            level.setBlock(pos, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), 2);
        }
        level.setBlockAndUpdate(PAD, Registration.CHARGER.get().defaultBlockState());
        supply(level);
        var player = server.getPlayerList().getPlayer(playerId);
        player.stopRiding();
        player.setGameMode(GameType.CREATIVE);
        player.teleportTo(level, 2.5, 201, -2.5, 34, 36);
        FluxShipEntity ship = FluxShipEntity.create(level, 6.5, 200.0625, .5);
        ship.setCharge(120_000);
        level.addFreshEntity(ship);
        shipId = ship.getUUID();
        ready = true;
    }

    private static void supply(ServerLevel level) {
        level.setBlockAndUpdate(PAD.below(), Registration.CONNECTOR.get().defaultBlockState().setValue(GenericCableBlock.COLOR, CableColor.COMBINED));
        int id = PowerSystem.getPowerSystem(level).allocateId();
        ((IPowerBlob) level.getBlockEntity(PAD.below())).fillCableId(id);
        ((GenericCableTileEntity) level.getBlockEntity(PAD.below())).setMimicBlock(Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
        PowerSystem.getPowerSystem(level).addPower(id, 2_000_000, PowerType.NEGARITE);
        PowerSystem.getPowerSystem(level).addPower(id, 2_000_000, PowerType.POSIRITE);
    }

    private static void checkTop(Minecraft mc, boolean powered) {
        var state = mc.level.getBlockState(PAD);
        require(state.is(Registration.CHARGER.get()) && state.getValue(WORKING) == powered,
                "The charger block's power state must synchronize: " + state + ", expected=" + powered);
        var model = mc.getBlockRenderer().getBlockModel(state);
        var quads = model.getQuads(state, Direction.UP, RandomSource.create(0), ModelData.EMPTY, RenderType.cutout());
        ResourceLocation glow = new ResourceLocation(Ariente.MODID, "block/machines/charger_glow");
        if (powered) {
            require(SurfaceTextures.get(glow) != null && SurfaceTextures.get(glow).light() == 15, "Charger glow metadata must be loaded");
            require(quads.stream().anyMatch(quad -> quad.getSprite().contents().name().equals(glow)
                            && !quad.isShade() && !quad.hasAmbientOcclusion() && (quad.getVertices()[6] & 0xffff) == 240),
                    "The powered top must emit an unshaded fullbright glow quad in the actual chunk model");
        } else {
            require(quads.isEmpty(), "An unpowered top must have no emissive layer");
        }
    }

    private static void screenshot(Minecraft mc, String name) {
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            if (name.startsWith("ship-patrol-exhaust")) {
                int yellow = 0;
                int cyan = 0;
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int pixel = image.getPixelRGBA(x, y);
                        int red = pixel & 255;
                        int green = pixel >> 8 & 255;
                        int blue = pixel >> 16 & 255;
                        if (red > 180 && green > 150 && blue < 130) {
                            yellow++;
                        }
                        if (red < 170 && green > 180 && blue > 180) {
                            cyan++;
                        }
                    }
                }
                require(yellow >= 3 && cyan >= 3, "Both exhaust colors must actually reach the framebuffer at " + name
                        + ": yellow=" + yellow + ", cyan=" + cyan);
            }
            image.writeToFile(mc.gameDirectory.toPath().resolve(name + ".png"));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static class PatrolPilot extends ArmorStand implements IFluxShipPilot {

        private PatrolPilot(ServerLevel level) {
            super(level, 0, 208, 0);
        }

        @Override
        public void controlShip(FluxShipEntity ship) {
            ship.flyTowards(this, new Vec3(0, 0, 1), 1.25);
        }
    }
}
