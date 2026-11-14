package mcjty.ariente.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.items.TeleportationCardItem;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.teleport.client.WarperPreviewCache;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.nio.file.Files;

/** Opt-in real networking, HoloGui selection, miniature rendering and player travel. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class WarperClientChecks {

    private static final BlockPos SOURCE = new BlockPos(0, 200, 0);
    private static final BlockPos TARGET = new BlockPos(64, 200, 0);
    private static boolean started;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static int ticks;
    private static int capture;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.warperClientCheck") || event.phase != TickEvent.Phase.END) {
            return;
        }
        // The opt-in disposable fixture can contain the companion's experimental dimension settings.
        if (mc.player == null && mc.screen != null && mc.getOverlay() == null) {
            for (var child : mc.screen.children()) {
                if (child instanceof Button button && (button.getMessage().getString().equals("Proceed")
                        || button.getMessage().getString().equals("Continue")
                        || button.getMessage().getString().equals("I know what I'm doing!"))) {
                    button.onPress();
                    return;
                }
            }
        }
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.getOverlay() != null) {
            return;
        }
        require(failure == null, "Server fixture failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.hideGui = false;
            mc.setScreen(null);
            mc.mouseHandler.releaseMouse();
            server(mc, () -> {
                var level = mc.getSingleplayerServer().overworld();
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, mc.getSingleplayerServer());
                level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, mc.getSingleplayerServer());
                level.setDayTime(6000);
                player.setGameMode(GameType.CREATIVE);
                player.getInventory().clearContent();
                for (BlockPos pos : BlockPos.betweenClosed(-6, 199, -7, 7, 205, 7)) {
                    level.setBlock(pos, (pos.getY() == 199 ? Blocks.GRAY_CONCRETE : Blocks.AIR).defaultBlockState(), 2);
                }
                for (BlockPos pos : BlockPos.betweenClosed(48, 199, -16, 79, 208, 15)) {
                    var block = pos.getY() == 199 ? ((pos.getX() + pos.getZ()) % 4 == 0 ? Blocks.STONE_BRICKS : Blocks.GRASS_BLOCK) : Blocks.AIR;
                    if (pos.getY() < 204 && (pos.getX() == 58 || pos.getX() == 70) && Math.abs(pos.getZ()) == 6) {
                        block = Blocks.QUARTZ_PILLAR;
                    }
                    if (pos.getY() == 204 && Math.abs(pos.getZ()) == 6 && pos.getX() >= 58 && pos.getX() <= 70) {
                        block = Blocks.OAK_LEAVES;
                    }
                    level.setBlock(pos, block.defaultBlockState(), 2);
                }
                level.setBlockAndUpdate(SOURCE, Registration.WARPER.get().defaultBlockState());
                level.setBlockAndUpdate(TARGET, Registration.WARPER.get().defaultBlockState());
                var target = (WarperTile) level.getBlockEntity(TARGET);
                target.setWarperName("Garden landing");
                var card = new ItemStack(Registration.TELEPORTATION_CARD.get());
                TeleportationCardItem.encode(card, target);
                player.setItemInHand(InteractionHand.MAIN_HAND, card);
                player.teleportTo(level, .5, 200, -3.5, 0, 0);
                TeleportationCardItem.sync(player);
                Registration.TELEPORTATION_CARD.get().use(level, player, InteractionHand.MAIN_HAND);
                ready = true;
            });
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 30) {
            aimGui(mc, gui(mc), 2, 1.9);
        } else if (ticks == 35) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 70) {
            var card = mc.player.getMainHandItem();
            require(TeleportationCardItem.selected(card) != null, "Actual HoloGui hit selects the card destination on the server");
            require(WarperPreviewCache.get(TeleportationCardItem.id(card), TeleportationCardItem.selected(card)) != null,
                    "The separately transmitted cross-chunk preview reaches the client");
            require(TeleportationCardItem.destinationName(card, 0).equals("Garden landing"), "The card renders the stored Warper name");
            capture = 1;
        } else if (ticks == 90) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(SOURCE), Direction.NORTH, SOURCE, false));
            server(mc, () -> mc.getSingleplayerServer().overworld().getEntitiesOfClass(HoloGuiEntity.class,
                    mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()).getBoundingBox().inflate(6)).forEach(HoloGuiEntity::discard));
        } else if (ticks == 125) {
            var tile = (WarperTile) mc.level.getBlockEntity(SOURCE);
            require(tile.getDestination() != null && tile.getDestinationScene().getLongArray("Blocks").length > 0,
                    "Actual card-on-block interaction configures the Warper and syncs its scene");
            require(tile.getDestinationDescription().equals("Garden landing (64, 200, 0)"), "The source GUI has the named destination on the client");
            capture = 2;
        } else if (ticks == 130) {
            server(mc, () -> {
                var level = mc.getSingleplayerServer().overworld();
                var tile = (WarperTile) level.getBlockEntity(SOURCE);
                tile.setWarperName("Departure");
                tile.setDestinationPinned(true);
                tile.setCharges(UtilityConfiguration.WARPER_MAX_CHARGES.get());
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                tile.onBlockActivated(tile.getBlockState(), player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(SOURCE), Direction.NORTH, SOURCE, false));
            });
        } else if (ticks == 145) {
            capture = 3;
        } else if (ticks == 150) {
            server(mc, () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                player.teleportTo(mc.getSingleplayerServer().overworld(), .5, 201, .5, 0, 0);
            });
        } else if (ticks == 180) {
            require(mc.player.distanceToSqr(64.5, 201, .5) < 2, "A real player stepping onto the configured pad reaches the destination");
            try {
                Files.writeString(mc.gameDirectory.toPath().resolve("warper-client-check.txt"),
                        "PASS: real HoloGui hit, names, separate preview packet, card-on-Warper interaction, terrain rendering and player travel\n");
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
            Ariente.setup.getLogger().info("WARPER CLIENT CHECK PASSED");
            mc.stop();
        }
    }

    private static void server(Minecraft mc, Runnable action) {
        mc.getSingleplayerServer().execute(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure = error;
            }
        });
    }

    private static HoloGuiEntity gui(Minecraft mc) {
        return mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(6)).stream().findFirst().orElseThrow();
    }

    private static void aimGui(Minecraft mc, HoloGuiEntity holo, double x, double y) {
        Vec3 right = holo.getLookAngle().cross(new Vec3(0, 1, 0));
        Vec3 target = holo.position().add(right.scale((x + .8) / 10 - .5)).add(0, 1 - (y + .8) / 10, 0);
        Vec3 direction = target.subtract(mc.player.getEyePosition());
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance())));
        mc.player.yRotO = mc.player.getYRot();
        mc.player.xRotO = mc.player.getXRot();
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        if (capture == 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        TextureTarget target = new TextureTarget(900, 900, true, Minecraft.ON_OSX);
        Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var sorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();
        try {
            float span = capture == 2 ? 2.2f : .6f;
            RenderSystem.setProjectionMatrix(new Matrix4f().ortho(-span, span, -span, span, .1f, 20), sorting);
            target.setClearColor(.025f, .05f, .09f, 1);
            RenderSystem.depthMask(true);
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);
            PoseStack poses = new PoseStack();
            var buffers = MultiBufferSource.immediate(new BufferBuilder(65536));
            if (capture != 2) {
                var holo = gui(mc);
                poses.translate(0, -.5, -3);
                poses.mulPose(Axis.YP.rotationDegrees(holo.getViewYRot(mc.getFrameTime()) - 180));
                mc.getEntityRenderDispatcher().getRenderer(holo).render(holo, 0, 0, poses, buffers, LightTexture.FULL_BRIGHT);
            } else {
                var tile = (WarperTile) mc.level.getBlockEntity(SOURCE);
                poses.translate(0, -1, -5);
                poses.mulPose(Axis.XP.rotationDegrees(20));
                poses.mulPose(Axis.YP.rotationDegrees(40));
                poses.translate(-.5, 0, -.5);
                mc.getBlockRenderer().renderSingleBlock(tile.getBlockState(), poses, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                mc.getBlockEntityRenderDispatcher().getRenderer(tile).render(tile, 0, poses, buffers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
            buffers.endBatch();
            try (var screenshot = Screenshot.takeScreenshot(target)) {
                screenshot.writeToFile(mc.gameDirectory.toPath().resolve(capture == 1 ? "warper-card.png" : capture == 2 ? "warper-miniature.png" : "warper-gui.png"));
            }
        } catch (Exception error) {
            throw new IllegalStateException(error);
        } finally {
            target.destroyBuffers();
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection, sorting);
            mc.getMainRenderTarget().bindWrite(true);
        }
        capture = 0;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
