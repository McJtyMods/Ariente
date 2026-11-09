package mcjty.ariente.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LightTexture;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.nio.file.Files;
import java.util.UUID;

/** Two owners' panels on one real client: independent snapshots, pixels and reopen behavior. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class StoragePanelsClientChecks {

    private static final BlockPos POS = new BlockPos(0, 200, 0);
    private static boolean started;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static volatile int firstId;
    private static volatile int secondId;
    private static int ticks;
    private static int capture;
    private static int captured;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.storagePanelsCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.gui.overlay() != null) {
            return;
        }
        require(failure == null, "Server fixture failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.gui.setScreen(null);
            mc.mouseHandler.releaseMouse();
            UUID id = mc.player.getUUID();
            server(mc, () -> {
                var server = mc.getSingleplayerServer();
                var level = server.overworld();
                var player = server.getPlayerList().getPlayer(id);
                level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                level.setDayTime(6000);
                for (BlockPos pos : BlockPos.betweenClosed(-4, 199, -5, 4, 204, 3)) {
                    level.setBlock(pos, (pos.getY() == 199 ? Blocks.GRAY_CONCRETE : Blocks.AIR).defaultBlockState(), 2);
                }
                level.getEntitiesOfClass(HoloGuiEntity.class, new AABB(-5, 198, -5, 5, 207, 5)).forEach(HoloGuiEntity::discard);
                level.setBlockAndUpdate(POS, Registration.HOLO_STORAGE_3.get().defaultBlockState());
                var items = tile(mc).getItems();
                for (int slot = 0; slot < items.getSlots(); slot++) {
                    items.setStackInSlot(slot, ItemStack.EMPTY);
                }
                player.getInventory().clearContent();
                player.setGameMode(GameType.CREATIVE);
                player.teleportTo(level, .5, 200, -2.5, 0, 0);
                var peer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "StoragePeer"));
                peer.setPos(.5, 200, -2.5);
                HoloGuiEntity first = (HoloGuiEntity) Ariente.guiHandler.openHoloGuiEntity(level, POS, player, "default", 1);
                HoloGuiEntity second = (HoloGuiEntity) Ariente.guiHandler.openHoloGuiEntity(level, POS, peer, "default", 1);
                require(first != second, "Different players must have separate panels");
                first.setPinned(true);
                second.setPinned(true);
                first.moveTo(-.15, first.getY(), -.6, 0, 0);
                second.moveTo(1.15, second.getY(), -.6, 0, 0);
                firstId = first.getId();
                secondId = second.getId();
                ready = true;
            });
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 20) {
            mc.player.setYRot(0);
            mc.player.setXRot(0);
        } else if (ticks == 60) {
            server(mc, () -> {
                tile(mc).getItems().setStackInSlot(0, new ItemStack(Items.DIAMOND, 32));
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                player.getInventory().setItem(9, new ItemStack(Items.APPLE, 12));
                player.containerMenu.broadcastChanges();
            });
        } else if (ticks == 100) {
            capture = 1;
        } else if (ticks == 110) {
            require(captured == 1, "Both visible panels must pass their item and player-slot pixel checks");
            server(mc, () -> {
                var level = mc.getSingleplayerServer().overworld();
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var peer = (HoloGuiEntity) level.getEntity(secondId);
                var peerPosition = peer.position();
                player.teleportTo(level, 2.5, 200, -2.5, 90, 0);
                var reopened = (HoloGuiEntity) Ariente.guiHandler.openHoloGuiEntity(level, POS, player, "default", 1);
                require(reopened.getId() == firstId && reopened.isPinned(), "Reopening must preserve entity identity and pin");
                require(Math.abs(reopened.getX() - 1.5) < .001 && reopened.getYRot() == 90,
                        "Reopening must place the panel in front of the new player position");
                require(peer.position().equals(peerPosition), "A different owner's panel must stay put");
                reopened.close();
                tile(mc).getItems().setStackInSlot(0, new ItemStack(Items.EMERALD, 20));
                player.teleportTo(level, .5, 200, -2.5, 0, 0);
            });
        } else if (ticks == 160) {
            capture = 2;
        } else if (ticks == 175) {
            require(captured == 2 && failure == null, "The remaining panel must keep rendering updates after another panel closes");
            try {
                Files.writeString(mc.gameDirectory.toPath().resolve("storage-panels-check.txt"),
                        "PASS: separate owners, concurrent storage/player deltas and rendered items, closed-panel isolation, repositioned pinned panel identity\n");
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
            mc.stop();
        }
    }

    private static StorageCubeTile tile(Minecraft mc) {
        return (StorageCubeTile) mc.getSingleplayerServer().overworld().getBlockEntity(POS);
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

    @SubscribeEvent
    public static void screenshot(RenderGuiEvent.Post event) {
        if (capture == 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        try (var screenshot = Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget())) {
            screenshot.writeToFile(mc.gameDirectory.toPath().resolve("storage-panels-world-" + capture + ".png"));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
        if (capture == 1) {
            capturePanel(mc, firstId, false);
        }
        capturePanel(mc, secondId, capture == 2);
        captured = capture;
        capture = 0;
    }

    private static void capturePanel(Minecraft mc, int id, boolean emerald) {
        var holo = (HoloGuiEntity) mc.level.getEntity(id);
        require(holo != null, "Expected panel is present on the client");
        var target = new TextureTarget(768, 768, true, Minecraft.ON_OSX);
        var projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var sorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();
        try {
            RenderSystem.setProjectionMatrix(new Matrix4f().ortho(-.6f, .6f, -.6f, .6f, .1f, 10), sorting);
            target.setClearColor(.025f, .05f, .09f, 1);
            RenderSystem.depthMask(true);
            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);
            PoseStack poses = new PoseStack();
            poses.translate(0, -.5, -2);
            poses.rotate(Axis.YP.rotationDegrees(holo.getViewYRot(mc.getFrameTime()) - 180));
            var buffers = RenderBufferSource.immediate(new BufferBuilder(4096));
            mc.getEntityRenderDispatcher().getRenderer(holo).render(holo, 0, 0, poses, buffers, LightTexture.FULL_BRIGHT);
            buffers.endBatch();
            try (var screenshot = Screenshot.takeScreenshot(target)) {
                screenshot.writeToFile(mc.gameDirectory.toPath().resolve("storage-panel-" + id + "-" + capture + ".png"));
                int itemPixels = 0;
                int playerPixels = 0;
                for (int y = -16; y <= 16; y++) {
                    for (int x = -16; x <= 16; x++) {
                        int item = screenshot.getPixelRGBA(164 + x, 182 + y);
                        if ((item & 255) < 160 && (item >> 8 & 255) > 165
                                && (emerald ? (item >> 16 & 255) < 140 : (item >> 16 & 255) > 140)) {
                            itemPixels++;
                        }
                        int player = screenshot.getPixelRGBA(164 + x, 460 + y);
                        if ((player & 255) > 160 && (player >> 8 & 255) < 130) {
                            playerPixels++;
                        }
                    }
                }
                require(itemPixels > 25 && playerPixels > 25,
                        "Every panel must render the updated stored item and local player inventory; panel=" + id
                                + "; stored pixels=" + itemPixels + "; player pixels=" + playerPixels);
            }
        } catch (Exception error) {
            throw new IllegalStateException(error);
        } finally {
            target.destroyBuffers();
            RenderSystem.setProjectionMatrix(projection, sorting);
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            mc.gameRenderer.mainRenderTarget().bindWrite(true);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
