package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Files;
import java.util.UUID;

/** Opt-in regression check in an isolated save, including actual client/server passenger ticks. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxCartClientChecks {

    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static volatile UUID cartId;
    private static boolean started;
    private static int ticks;
    private static int totalTicks;
    private static int samples;
    private static double maxPanelDrift;
    private static double minStep = Double.MAX_VALUE;
    private static double maxStep;
    private static int courseSides;
    private static int cornerSamples;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.fluxClientCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null
                || mc.getOverlay() != null) {
            return;
        }
        require(failure == null, "Server fixture failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.setScreen(null);
            mc.mouseHandler.releaseMouse();
            var uuid = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                try {
                    var server = mc.getSingleplayerServer();
                    var level = server.overworld();
                    level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
                    level.setDayTime(6000);
                    level.getEntitiesOfClass(FluxLevitatorEntity.class, new AABB(-3, 198, -161, 3, 206, 161))
                            .forEach(FluxLevitatorEntity::discard);
                    for (BlockPos pos : BlockPos.betweenClosed(-2, 199, -160, 2, 204, 160)) {
                        level.setBlock(pos, pos.getY() == 199 ? Blocks.WHITE_CONCRETE.defaultBlockState()
                                : Blocks.AIR.defaultBlockState(), 2);
                    }
                    var beam = Registration.FLUX_BEAM.get().defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
                    for (int z = -160; z <= 160; z++) {
                        level.setBlock(new BlockPos(0, 200, z), beam, 2);
                    }
                    var player = server.getPlayerList().getPlayer(uuid);
                    player.setGameMode(GameType.CREATIVE);
                    player.teleportTo(level, .5, 200.0625, .5, 0, 36);
                    var cart = FluxLevitatorEntity.create(level, .5, 200.0625, .5);
                    cartId = cart.getUUID();
                    level.addFreshEntity(cart);
                    player.startRiding(cart, true);
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
        require(++totalTicks < 1200, "Client check timed out waiting for the mounted cart");
        if (!(mc.player.getVehicle() instanceof FluxLevitatorEntity cart) || !cart.getUUID().equals(cartId)) {
            return;
        }
        ticks++;
        if (ticks == 40) {
            changeSpeed(20);
        } else if (ticks == 130) {
            changeSpeed(0);
        } else if (ticks == 160) {
            changeSpeed(-20);
        } else if (ticks == 290) {
            changeSpeed(80);
        } else if (ticks == 345) {
            aimAtStopButton(cart, mc);
        } else if (ticks == 350) {
            mc.gameMode.attack(mc.player, cart.getHoloGuiFront().getEntity());
        } else if (ticks == 365) {
            require(cart.getSpeed() == 0, "Clicking the moving panel must stop the cart on the server");
        } else if (ticks == 380) {
            require(samples > 200, "Both panels must have been checked while moving");
            require(maxPanelDrift < .00001, "Mounted panels must interpolate with the camera; drift=" + maxPanelDrift);
            require(minStep > .55 && maxStep < .65, "Steady travel must have no stalls or jumps: " + minStep + ".." + maxStep);
            String result = "Mounted panel samples=" + samples + ", maximum within-frame drift=" + maxPanelDrift
                    + ", steady movement per tick=" + minStep + ".." + maxStep + "\n";
            try (var screenshot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                screenshot.writeToFile(mc.gameDirectory.toPath().resolve("flux-cart.png"));
                Files.writeString(mc.gameDirectory.toPath().resolve("flux-cart-check.txt"), result);
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
            Ariente.setup.getLogger().info("FLUX CART CLIENT CHECK: {}", result);
            createCornerCourse(mc);
        } else if (ticks > 410 && ticks < 730) {
            checkCornerPanel(cart.getHoloGuiFront(), cart);
            checkCornerPanel(cart.getHoloGuiBack(), cart);
            if (cart.getZ() < 1) {
                courseSides |= 1;
            }
            if (cart.getX() > 23) {
                courseSides |= 2;
            }
            if (cart.getZ() > 15) {
                courseSides |= 4;
            }
            if (cart.getX() < 9) {
                courseSides |= 8;
            }
        } else if (ticks == 740) {
            require(courseSides == 15 && cornerSamples > 500, "The mounted cart must traverse all four sides and bends");
            Ariente.setup.getLogger().info("FLUX CART CORNER CHECK PASSED: {} panel samples", cornerSamples);
            mc.stop();
        }
        if ((ticks > 80 && ticks < 125) || (ticks > 205 && ticks < 280) || (ticks > 320 && ticks < 345)) {
            checkPanel(cart.getHoloGuiFront(), mc);
            checkPanel(cart.getHoloGuiBack(), mc);
            if (ticks < 280) {
                double step = cart.position().distanceTo(new Vec3(cart.xOld, cart.yOld, cart.zOld));
                minStep = Math.min(minStep, step);
                maxStep = Math.max(maxStep, step);
            }
        }
        if (ticks > 345 && ticks < 350) {
            var panel = (HoloGuiEntity) cart.getHoloGuiFront().getEntity();
            for (float partialTicks : new float[]{0, .25f, .5f, .75f, 1}) {
                panel.updateCursor(mc.player, partialTicks);
                require(Math.abs(panel.getCursorX() - 4.5) < .01 && Math.abs(panel.getCursorY() - 2.5) < .01,
                        "The cursor must stay on the stop button throughout each moving frame");
            }
        }
    }

    private static void checkPanel(IHoloGuiEntity holo, Minecraft mc) {
        require(holo != null, "Both mounted panels must be present on the client");
        var panel = holo.getEntity();
        Vec3 currentOffset = panel.position().subtract(mc.player.position());
        // EntityRenderDispatcher interpolates xOld; Camera interpolates the player's xo.
        Vec3 previousOffset = new Vec3(panel.xOld - mc.player.xo, panel.yOld - mc.player.yo, panel.zOld - mc.player.zo);
        maxPanelDrift = Math.max(maxPanelDrift, previousOffset.distanceTo(currentOffset));
        samples++;
    }

    private static void changeSpeed(int speed) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        server.execute(() -> ((FluxLevitatorEntity) server.overworld().getEntity(cartId)).changeSpeed(speed));
    }

    private static void createCornerCourse(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        server.execute(() -> {
            try {
                var level = server.overworld();
                level.getEntitiesOfClass(FluxLevitatorEntity.class, new AABB(7, 198, -1, 26, 206, 18))
                        .forEach(FluxLevitatorEntity::discard);
                for (BlockPos pos : BlockPos.betweenClosed(7, 199, -1, 25, 204, 17)) {
                    level.setBlock(pos, pos.getY() == 199 ? Blocks.WHITE_CONCRETE.defaultBlockState()
                            : Blocks.AIR.defaultBlockState(), 2);
                }
                var eastWest = Registration.FLUX_BEAM.get().defaultBlockState();
                var northSouth = eastWest.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
                for (int i = 1; i < 16; i++) {
                    level.setBlock(new BlockPos(8 + i, 200, 0), eastWest, 2);
                    level.setBlock(new BlockPos(8 + i, 200, 16), eastWest, 2);
                    level.setBlock(new BlockPos(8, 200, i), northSouth, 2);
                    level.setBlock(new BlockPos(24, 200, i), northSouth, 2);
                }
                var bend = Registration.FLUX_BEND_BEAM.get().defaultBlockState();
                level.setBlock(new BlockPos(8, 200, 0), bend.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST), 2);
                level.setBlock(new BlockPos(24, 200, 0), bend.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH), 2);
                level.setBlock(new BlockPos(24, 200, 16), bend.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST), 2);
                level.setBlock(new BlockPos(8, 200, 16), bend.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH), 2);
                var player = server.getPlayerList().getPlayer(uuid);
                player.stopRiding();
                player.teleportTo(level, 10.5, 200.0625, .5, -90, 36);
                var cart = FluxLevitatorEntity.create(level, 10.5, 200.0625, .5);
                cartId = cart.getUUID();
                level.addFreshEntity(cart);
                player.startRiding(cart, true);
                cart.changeSpeed(20);
            } catch (Throwable error) {
                failure = error;
            }
        });
    }

    private static void checkCornerPanel(IHoloGuiEntity holo, FluxLevitatorEntity cart) {
        require(holo != null, "Both panels must stay mounted through bends");
        var panel = holo.getEntity();
        require(panel.getVehicle() == cart && panel.position().distanceTo(cart.position()) < 2,
                "Panels must remain beside the cart through bends");
        require(Math.abs(Mth.wrapDegrees(panel.getYRot() - panel.yRotO)) <= 91,
                "Panel rotation must take the short route around each bend: " + panel.yRotO + " -> " + panel.getYRot());
        cornerSamples++;
    }

    private static void aimAtStopButton(FluxLevitatorEntity cart, Minecraft mc) {
        var panel = (HoloGuiEntity) cart.getHoloGuiFront().getEntity();
        // Invert the panel's widget transform for the center of the stop button (4.5, 2.5).
        double factor = 1 - (1 - panel.getScale()) * (.4 / .25);
        double offset = .8 + (1 - panel.getScale()) * (3.3 / .25);
        Vec3 right = panel.getLookAngle().cross(new Vec3(0, 1, 0));
        Vec3 target = panel.position().add(right.scale((4.5 + offset) * factor / 10 - .5))
                .add(0, 1 - (2.5 + offset) * factor / 10, 0);
        Vec3 direction = target.subtract(mc.player.getEyePosition());
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance())));
        mc.player.yRotO = mc.player.getYRot();
        mc.player.xRotO = mc.player.getXRot();
        mc.player.setYHeadRot(mc.player.getYRot());
        mc.player.yHeadRotO = mc.player.getYRot();
        panel.updateCursor(mc.player, 1);
        require(Math.abs(panel.getCursorX() - 4.5) < .01 && Math.abs(panel.getCursorY() - 2.5) < .01,
                "The cursor must align with the rendered stop button: " + panel.getCursorX() + ", " + panel.getCursorY()
                        + "; panel=" + panel.position() + "; eye=" + mc.player.getEyePosition() + "; target=" + target
                        + "; look=" + mc.player.getViewVector(1));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
