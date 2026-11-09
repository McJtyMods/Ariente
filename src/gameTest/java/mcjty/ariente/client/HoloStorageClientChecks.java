package mcjty.ariente.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LightTexture;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.patchouli.client.book.gui.GuiBookEntry;
import org.joml.Matrix4f;

import java.nio.file.Files;
import java.util.UUID;

import static mcjty.hologui.setup.Registration.HOLOGUI_ENTITY_TYPE;

/** Real input gestures, hover scrolling and rendering, in an opt-in isolated client world. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class HoloStorageClientChecks {

    private static final BlockPos POS = new BlockPos(0, 200, 0);
    private static boolean started;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static int ticks;
    private static String capture;
    private static ItemStack[] inventoryBeforeSort;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.holoStorageClientCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.gui.overlay() != null) {
            return;
        }
        require(failure == null, "Server check failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.options.hideGui = false;
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
                StorageCubeTile tile = (StorageCubeTile) level.getBlockEntity(POS);
                tile.getItems().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 64));
                tile.getItems().setStackInSlot(1, new ItemStack(Items.OAK_LOG, 32));
                tile.getItems().setStackInSlot(10, new ItemStack(Items.DIAMOND, 12));
                tile.getItems().setStackInSlot(20, new ItemStack(Items.COBBLESTONE, 64));
                tile.getItems().setStackInSlot(35, new ItemStack(Items.GOLD_INGOT, 16));
                tile.getItems().setStackInSlot(60, new ItemStack(Items.GLASS, 20));
                tile.getItems().setStackInSlot(90, new ItemStack(Items.ENDER_PEARL, 8));
                tile.getItems().setStackInSlot(119, new ItemStack(Items.NETHER_STAR));
                player.setGameMode(GameType.CREATIVE);
                player.getInventory().clearContent();
                player.getInventory().setItem(9, new ItemStack(Items.GOLD_INGOT, 32));
                player.getInventory().setItem(10, new ItemStack(Items.APPLE, 16));
                player.getInventory().setItem(18, new ItemStack(Items.REDSTONE, 64));
                player.getInventory().setItem(26, new ItemStack(Registration.CONSTRUCTOR.get(), 64));
                player.getInventory().setItem(27, new ItemStack(Items.DIAMOND_PICKAXE));
                player.getInventory().setItem(34, new ItemStack(Items.EMERALD, 32));
                player.getInventory().setItem(35, new ItemStack(Items.AMETHYST_SHARD, 48));
                player.getInventory().setItem(8, new ItemStack(Items.BREAD, 64));
                player.teleportTo(level, .5, 200, -2.5, 0, 15);
                player.containerMenu.broadcastChanges();
                ready = true;
            });
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 20) {
            aim(mc, Vec3.atCenterOf(POS));
        } else if (ticks == 40) {
            capture = "holo-storage-cube";
        } else if (ticks == 50) {
            server(mc, () -> Ariente.guiHandler.openHoloGui(mc.getSingleplayerServer().overworld(), POS,
                    mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));
        } else if (ticks == 70) {
            aimGui(mc, .8, 1.1);
        } else if (ticks == 90) {
            aimGui(mc, 4, 4);
        } else if (ticks == 100) {
            capture = "holo-storage-panel";
        } else if (ticks == 105) {
            mc.options.keyShift.setDown(true);
        } else if (ticks == 108) {
            aimGui(mc, .8, 1.1);
        } else if (ticks == 110) {
            press(mc);
        } else if (ticks == 112) {
            mc.options.keyAttack.setDown(false);
            mc.options.keyShift.setDown(false);
        } else if (ticks == 130) {
            server(mc, () -> {
                StorageCubeTile tile = serverTile(mc);
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                require(tile.getItems().getStackInSlot(0).isEmpty() && player.getInventory().getItem(0).getCount() == 64,
                        "Sneak-click must extract into player inventory");
            });
            aimGui(mc, .8, 5.45);
        } else if (ticks == 140) {
            press(mc);
        } else if (ticks == 150) {
            aimGui(mc, .8, 2.7);
        } else if (ticks == 160) {
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 185) {
            server(mc, () -> {
                require(serverTile(mc).getItems().getStackInSlot(2).getCount() == 32,
                        "Holding, aiming and releasing must drag from player inventory into the cube");
            });
            aimGui(mc, 7.9, 1.5);
        } else if (ticks == 470) {
            capture = "holo-storage-scrolled";
        } else if (ticks == 478) {
            mc.options.keyShift.setDown(true);
        } else if (ticks == 485) {
            aimGui(mc, 7.2, 2.7);
        } else if (ticks == 490) {
            press(mc);
        } else if (ticks == 492) {
            mc.options.keyAttack.setDown(false);
            mc.options.keyShift.setDown(false);
        } else if (ticks == 515) {
            server(mc, () -> require(serverTile(mc).getItems().getStackInSlot(119).isEmpty(),
                    "Hover scrolling without clicking must make the final storage slot accessible"));
        } else if (ticks == 525) {
            mc.options.keyShift.setDown(true);
        } else if (ticks == 528) {
            aimGui(mc, 7.2, 8.15);
        } else if (ticks == 530) {
            press(mc);
        } else if (ticks == 532) {
            mc.options.keyAttack.setDown(false);
            mc.options.keyShift.setDown(false);
        } else if (ticks == 550) {
            server(mc, () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                require(player.getInventory().getItem(8).isEmpty(), "The separated hotbar must use its new hit coordinates");
            });
            aimGui(mc, 4, 4);
        } else if (ticks == 560) {
            HoloGuiEntity holo = gui(mc);
            InputEvent.InteractionKeyMappingTriggered input = new InputEvent.InteractionKeyMappingTriggered(1,
                    mc.options.keyUse, InteractionHand.MAIN_HAND);
            MinecraftForge.EVENT_BUS.post(input);
            require(!input.isCanceled(), "Right-click must reach HoloGui's normal close handler");
            mc.gameMode.interact(mc.player, holo, InteractionHand.MAIN_HAND);
        } else if (ticks == 575) {
            require(mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(5)).isEmpty(),
                    "Right-click must dismiss the hologram");
        } else if (ticks == 580) {
            server(mc, () -> Ariente.guiHandler.openHoloGui(mc.getSingleplayerServer().overworld(), POS,
                    mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));
        } else if (ticks == 600) {
            aimControl(mc, "pin");
        } else if (ticks == 610) {
            pressControl(mc);
        } else if (ticks == 630) {
            require(gui(mc).isPinned(), "Clicking the pin must synchronize its selected state from the server");
            mc.player.setYRot(mc.player.getYRot() + 180);
        } else if (ticks == 740) {
            require(gui(mc).isPinned(), "A pinned storage panel must remain after looking away beyond the normal timeout");
            aimGui(mc, 4, 4);
        } else if (ticks == 750) {
            capture = "holo-storage-pinned";
        } else if (ticks == 760) {
            aimControl(mc, "pin");
        } else if (ticks == 770) {
            pressControl(mc);
        } else if (ticks == 790) {
            require(!gui(mc).isPinned(), "Clicking the selected pin must unpin on both sides");
            mc.player.setYRot(mc.player.getYRot() + 180);
        } else if (ticks == 900) {
            require(mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(5)).isEmpty(),
                    "An unpinned panel must close normally when looking away");
            server(mc, () -> Ariente.guiHandler.openHoloGui(mc.getSingleplayerServer().overworld(), POS,
                    mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));
        } else if (ticks == 920) {
            aimControl(mc, "pin");
        } else if (ticks == 930) {
            pressControl(mc);
        } else if (ticks == 950) {
            require(gui(mc).isPinned(), "The second panel must be pinned before testing X");
            aimControl(mc, "close");
        } else if (ticks == 960) {
            pressControl(mc);
        } else if (ticks == 980) {
            require(mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(5)).isEmpty(),
                    "The standard X must close even a pinned storage panel");
            server(mc, () -> Ariente.guiHandler.openHoloGui(mc.getSingleplayerServer().overworld(), POS,
                    mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));
        } else if (ticks == 1000) {
            aimControl(mc, "help");
        } else if (ticks == 1010) {
            pressControl(mc);
        } else if (ticks == 1035) {
            require(mc.gui.screen() instanceof GuiBookEntry book
                    && book.getEntry().getId().equals(new ResourceLocation(Ariente.MODID, "construction/holo_storage")),
                    "The question mark must open the correct Patchouli entry");
            mc.gui.setScreen(null);
        } else if (ticks == 1040) {
            UUID playerId = mc.player.getUUID();
            server(mc, () -> {
                var level = mc.getSingleplayerServer().overworld();
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId);
                level.getEntitiesOfClass(HoloGuiEntity.class, player.getBoundingBox().inflate(5)).forEach(HoloGuiEntity::discard);
                StorageCubeTile tile = serverTile(mc);
                for (int slot = 0; slot < tile.getItems().getSlots(); slot++) {
                    tile.getItems().setStackInSlot(slot, ItemStack.EMPTY);
                }
                tile.getItems().setStackInSlot(6, new ItemStack(Items.DIAMOND, 64).setHoverName(Component.literal("Zulu")));
                tile.getItems().setStackInSlot(45, new ItemStack(Items.IRON_INGOT, 16).setHoverName(Component.literal("Alpha")));
                tile.getItems().setStackInSlot(80, new ItemStack(Items.OAK_LOG, 32).setHoverName(Component.literal("beta")));
                tile.getItems().setStackInSlot(119, new ItemStack(Items.EMERALD, 2).setHoverName(Component.literal("alpha")));
                inventoryBeforeSort = new ItemStack[36];
                for (int slot = 0; slot < 36; slot++) {
                    inventoryBeforeSort[slot] = player.getInventory().getItem(slot).copy();
                }
                var holo = Ariente.guiHandler.openHoloGuiEntity(level, POS, player, "default", 1);
                holo.setPinned(true);
            });
        } else if (ticks == 1060) {
            aimGui(mc, 3.1, -.1);
        } else if (ticks == 1070) {
            press(mc);
        } else if (ticks == 1072) {
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 1090) {
            server(mc, () -> require(serverTile(mc).getItems().getStackInSlot(0).is(Items.EMERALD)
                            && serverTile(mc).getItems().getStackInSlot(3).is(Items.DIAMOND),
                    "The A-Z button must reorder real storage contents by item name"));
            aimGui(mc, 4.3, -.1);
        } else if (ticks == 1100) {
            press(mc);
        } else if (ticks == 1102) {
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 1120) {
            server(mc, () -> {
                StorageCubeTile tile = serverTile(mc);
                require(tile.getItems().getStackInSlot(0).is(Items.DIAMOND)
                                && tile.getItems().getStackInSlot(1).is(Items.OAK_LOG)
                                && tile.getItems().getStackInSlot(2).is(Items.IRON_INGOT)
                                && tile.getItems().getStackInSlot(3).is(Items.EMERALD),
                        "Count sorting puts the largest stacks first");
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                for (int slot = 0; slot < 36; slot++) {
                    require(ItemStack.matches(inventoryBeforeSort[slot], player.getInventory().getItem(slot)),
                            "Sort controls must not rearrange the player inventory");
                }
            });
            aimGui(mc, 4, 4);
        } else if (ticks == 1125) {
            capture = "storage-sorted-count";
        } else if (ticks == 1130) {
            server(mc, () -> {
                StorageCubeTile tile = serverTile(mc);
                for (int slot = 0; slot < tile.getItems().getSlots(); slot++) {
                    ItemStack stack = new ItemStack(Items.COBBLESTONE);
                    stack.getOrCreateTag().putInt("ScrollSlot", slot);
                    tile.getItems().setStackInSlot(slot, stack);
                }
            });
        } else if (ticks == 1140) {
            aimGui(mc, 7.9, 1.5);
        } else if (ticks == 1145) {
            press(mc);
        } else if (ticks == 1155) {
            capture = "storage-fast-scroll";
        } else if (ticks == 1165) {
            mc.options.keyAttack.setDown(false);
            aimGui(mc, 4, 4);
        } else if (ticks == 1170) {
            mc.options.keyShift.setDown(true);
        } else if (ticks == 1172) {
            aimGui(mc, .8, 1.1);
        } else if (ticks == 1175) {
            press(mc);
        } else if (ticks == 1177) {
            mc.options.keyAttack.setDown(false);
            mc.options.keyShift.setDown(false);
        } else if (ticks == 1195) {
            server(mc, () -> {
                var items = serverTile(mc).getItems();
                int emptySlot = -1;
                for (int slot = 0; slot < items.getSlots(); slot++) {
                    if (items.getStackInSlot(slot).isEmpty()) {
                        require(emptySlot < 0, "Fast scrolling must not transfer or lose extra stacks");
                        emptySlot = slot;
                    }
                }
                require(emptySlot >= 30, "Click-and-hold must reach columns much faster than hover scrolling; transferred slot=" + emptySlot);
            });
        } else if (ticks == 1205) {
            require(failure == null, "Client checks failed: " + failure);
            try {
                Files.writeString(mc.gameDirectory.toPath().resolve("holo-storage-check.txt"),
                        "PASS: rendering, drag, sneak transfer, hover/click/hold scrolling, lowered inventory and hotbar, A-Z/count sorting, right-click close, pin/unpin synchronization and timeout, pinned X close, Patchouli help\n");
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
            mc.stop();
        }
    }

    private static void pressControl(Minecraft mc) {
        InputEvent.InteractionKeyMappingTriggered input = new InputEvent.InteractionKeyMappingTriggered(0,
                mc.options.keyAttack, InteractionHand.MAIN_HAND);
        MinecraftForge.EVENT_BUS.post(input);
        require(!input.isCanceled(), "Standard window controls must receive normal clicks, without inventory gestures");
        mc.gameMode.attack(mc.player, gui(mc));
    }

    private static StorageCubeTile serverTile(Minecraft mc) {
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

    private static void press(Minecraft mc) {
        mc.options.keyAttack.setDown(true);
        InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(0,
                mc.options.keyAttack, InteractionHand.MAIN_HAND);
        MinecraftForge.EVENT_BUS.post(event);
        require(event.isCanceled() && !event.shouldSwingHand(), "Storage panels must consume inventory clicks");
    }

    private static void aimControl(Minecraft mc, String name) {
        var control = gui(mc).getGui(mc.player).findChild("hologui." + name).orElseThrow();
        aimGui(mc, control.getX() + control.getW() / 2, control.getY() + control.getH() / 2);
    }

    private static void aimGui(Minecraft mc, double x, double y) {
        HoloGuiEntity holo = gui(mc);
        Vec3 right = holo.getLookAngle().cross(new Vec3(0, 1, 0));
        aim(mc, holo.position().add(right.scale((x + .8) / 10 - .5)).add(0, 1 - (y + .8) / 10, 0));
    }

    private static HoloGuiEntity gui(Minecraft mc) {
        return mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(5))
                .stream().findFirst().orElseThrow();
    }

    private static void aim(Minecraft mc, Vec3 target) {
        Vec3 delta = target.subtract(mc.player.getEyePosition());
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
        mc.player.yRotO = mc.player.getYRot();
        mc.player.xRotO = mc.player.getXRot();
    }

    @SubscribeEvent
    public static void screenshot(RenderGuiEvent.Post event) {
        if (capture == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        try (var screenshot = Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget())) {
            screenshot.writeToFile(mc.gameDirectory.toPath().resolve(capture + ".png"));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
        if ("holo-storage-panel".equals(capture)) {
            captureWindow(mc, ModGuis.GUI_ARMOR, false);
            captureWindow(mc, ModGuis.GUI_LEVITATOR, true);
        }
        capture = null;
    }

    private static void captureWindow(Minecraft mc, String guiId, boolean mounted) {
        HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), mc.level);
        holo.setGuiId(guiId);
        holo.setYRot(180);
        holo.yRotO = 180;
        if (mounted) {
            holo.setScale(.75f);
            holo.startRiding(EntityType.PIG.create(mc.level), true);
        }
        require(holo.getGui(mc.player).findChild("hologui.pin").isEmpty(), "Equipment and mounted panels omit pin");
        require(holo.getGui(mc.player).findChild("hologui.close").isPresent() != mounted,
                "Only unmounted panels expose X");
        var target = new TextureTarget(768, 768, true, Minecraft.ON_OSX);
        var oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var oldSorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();
        try {
            RenderSystem.setProjectionMatrix(new Matrix4f().ortho(-.6f, .6f, -.6f, .6f, .1f, 10), oldSorting);
            for (int angle : new int[]{0, 35}) {
                target.setClearColor(.025f, .05f, .09f, 1);
                RenderSystem.depthMask(true);
                target.clear(Minecraft.ON_OSX);
                target.bindWrite(true);
                PoseStack poses = new PoseStack();
                poses.translate(0, 0, -2);
                poses.mulPose(Axis.YP.rotationDegrees(angle));
                poses.translate(0, -.5, 0);
                var buffers = RenderBufferSource.immediate(new BufferBuilder(4096));
                mc.getEntityRenderDispatcher().getRenderer(holo).render(holo, 0, 0, poses, buffers, LightTexture.FULL_BRIGHT);
                buffers.endBatch();
                try (var screenshot = Screenshot.takeScreenshot(target)) {
                    screenshot.writeToFile(mc.gameDirectory.toPath().resolve("window-" + guiId + "-" + angle + ".png"));
                }
            }
        } catch (Exception error) {
            throw new IllegalStateException(error);
        } finally {
            target.destroyBuffers();
            RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
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
