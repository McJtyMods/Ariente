package mcjty.ariente.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.AutoConstructorTile;
import mcjty.ariente.blocks.utility.BlueprintStorageTile;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LightTexture;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.joml.Matrix4f;

import java.nio.file.Files;

import static mcjty.ariente.gui.InventoryLayout.*;
import static mcjty.hologui.setup.Registration.HOLOGUI_ENTITY_TYPE;

/** Real server updates drive dimmed/enabled inventories; no client data is injected. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class ConstructionInventoryClientChecks {

    private static final BlockPos POS = new BlockPos(0, 200, 0);
    private static boolean started;
    private static volatile boolean ready;
    private static int ticks;
    private static boolean capture;
    private static boolean complete;
    private static int stage;
    private static volatile Throwable failure;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        var mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.constructionInventoryCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.gui.overlay() != null) {
            return;
        }
        require(failure == null, "Server fixture failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.gui.setScreen(null);
            mc.mouseHandler.releaseMouse();
            var id = mc.player.getUUID();
            server(() -> {
                var level = mc.getSingleplayerServer().overworld();
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                player.teleportTo(level, .5, 200, -2.5, 0, 0);
                level.setBlockAndUpdate(POS, Registration.BLUEPRINT_STORAGE.get().defaultBlockState());
                var storage = (BlueprintStorageTile) level.getBlockEntity(POS);
                storage.getItems().setStackInSlot(0, BlueprintItem.makeBluePrint(new ItemStack(Registration.DISPLAY_BASE.get())));
                storage.markDirtyClient();
                level.setBlockAndUpdate(POS.east(), Registration.CONSTRUCTOR.get().defaultBlockState());
                level.setBlockAndUpdate(POS.west(), Registration.AUTO_CONSTRUCTOR.get().defaultBlockState());
                var autoItems = machineItems(POS.west(), true);
                for (int slot = 0; slot < autoItems.getSlots(); slot++) {
                    autoItems.setStackInSlot(slot, ItemStack.EMPTY);
                }
                autoItems.setStackInSlot(0, new ItemStack(Registration.INGOT_SILVER.get(), 64));
                autoItems.setStackInSlot(8, new ItemStack(Items.GLASS, 16));
                autoItems.setStackInSlot(17, new ItemStack(Registration.CIRCUIT.get(), 16));
                player.getInventory().clearContent();
                player.getInventory().setItem(9, new ItemStack(Items.GOLD_INGOT, 32));
                player.getInventory().setItem(10, new ItemStack(Registration.INGOT_SILVER.get(), 32));
                player.getInventory().setItem(18, new ItemStack(Items.GLASS, 16));
                player.getInventory().setItem(26, new ItemStack(Registration.CIRCUIT.get(), 16));
                player.getInventory().setItem(27, new ItemStack(Items.DIAMOND_PICKAXE));
                player.getInventory().setItem(34, new ItemStack(Registration.INGOT_SILVER.get(), 32));
                player.getInventory().setItem(35, new ItemStack(Registration.INGOT_SILVER.get(), 64));
                player.getInventory().setItem(8, BlueprintItem.makeBluePrint(new ItemStack(Registration.DISPLAY_BASE.get())));
                player.inventoryMenu.broadcastChanges();
                ready = true;
            });
        } else if (complete) {
            mc.stop();
        } else if (ready) {
            ticks++;
            if (ticks == 80 || stage == 1 && ticks == 110 || stage == 2 && ticks == 140) {
                capture = true;
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) throws Exception {
        if (!capture) {
            return;
        }
        capture = false;
        var mc = Minecraft.getInstance();
        var storage = (BlueprintStorageTile) mc.level.getBlockEntity(POS);
        boolean populated = stage != 1;
        require(!storage.getItems().getStackInSlot(0).isEmpty() == populated, "Blueprint packets reach the real client, including clearing slots");
        require(!machineItems(POS.west(), false).getStackInSlot(0).isEmpty() == populated, "Ingredient packets reach the real client");
        var target = new TextureTarget(900, 900, true, Minecraft.ON_OSX);
        var projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var sorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();
        try {
            RenderSystem.setProjectionMatrix(new Matrix4f().ortho(-.65f, .65f, -.65f, .65f, .1f, 10), sorting);
            String[] names = {"blueprints", "constructor", "auto-constructor"};
            BlockPos[] positions = {POS, POS.east(), POS.west()};
            for (int i = 0; i < names.length; i++) {
                var holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), mc.level);
                holo.setGuiTile(positions[i]);
                holo.setTag(i == 2 ? AutoConstructorTile.TAG_INGREDIENTS : "main");
                holo.setYRot(180);
                holo.yRotO = 180;
                var root = holo.getGui(mc.player);
                require(root != null, "Missing client panel: " + names[i]);
                var inventory = (IPlayerSlots) root.findChild("playerslots").orElseThrow();
                root.hitClient(mc.player, holo, GRID_X + CELL / 2, PLAYER_Y + CELL / 2);
                require(inventory.getSelected() == -1, "Dimmed gold is never selectable: " + names[i]);
                int allowed = i == 0 ? 8 : 35;
                double allowedY = allowed < 9 ? HOTBAR_Y + CELL / 2 : PLAYER_Y + 2.5 * CELL;
                root.hitClient(mc.player, holo, GRID_X + 8.5 * CELL, allowedY);
                require(inventory.getSelected() == (i == 0 || populated ? allowed : -1),
                        "Eligibility follows synchronized blueprints: " + names[i]);
                inventory.setSelection(-1);
                for (int angle : new int[] {0, 35}) {
                    target.setClearColor(.025f, .05f, .09f, 1);
                    RenderSystem.depthMask(true);
                    target.clear(Minecraft.ON_OSX);
                    target.bindWrite(true);
                    var poses = new PoseStack();
                    poses.translate(0, 0, -2);
                    poses.mulPose(Axis.YP.rotationDegrees(angle));
                    poses.translate(0, -.5, 0);
                    var buffers = RenderBufferSource.immediate(new BufferBuilder(4096));
                    mc.getEntityRenderDispatcher().getRenderer(holo).render(holo, 0, 0, poses, buffers, LightTexture.FULL_BRIGHT);
                    buffers.endBatch();
                    try (var screenshot = Screenshot.takeScreenshot(target)) {
                        screenshot.writeToFile(mc.gameDirectory.toPath().resolve(names[i] + "-" + stage + "-" + angle + ".png"));
                    }
                    if (i == 1 && stage == 0 && angle == 0) {
                        poses.pushPose();
                        poses.translate(0, .5, .01);
                        root.findChild("outputslots").orElseThrow().renderTooltip(poses, buffers, mc.player, holo, 1.7, 1.2);
                        buffers.endBatch();
                        poses.popPose();
                        try (var screenshot = Screenshot.takeScreenshot(target)) {
                            screenshot.writeToFile(mc.gameDirectory.toPath().resolve("constructor-tooltip.png"));
                        }
                    }
                }
            }
            if (stage == 2) {
                Files.writeString(mc.gameDirectory.toPath().resolve("construction-inventory-check.txt"),
                        "PASS: real blueprint/ingredient packets, cleared/restored slots, enabled/disabled clicks, full inventory renders at front/35 degrees\n");
                complete = true;
            } else {
                boolean clear = stage == 0;
                server(() -> {
                    var serverStorage = (BlueprintStorageTile) mc.getSingleplayerServer().overworld().getBlockEntity(POS);
                    serverStorage.getItems().setStackInSlot(0, clear ? ItemStack.EMPTY
                            : BlueprintItem.makeBluePrint(new ItemStack(Registration.DISPLAY_BASE.get())));
                    machineItems(POS.west(), true).setStackInSlot(0, clear ? ItemStack.EMPTY : new ItemStack(Registration.INGOT_SILVER.get(), 5));
                });
                stage++;
            }
        } finally {
            target.destroyBuffers();
            RenderSystem.setProjectionMatrix(projection, sorting);
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            mc.gameRenderer.mainRenderTarget().bindWrite(true);
        }
    }

    private static IItemHandlerModifiable machineItems(BlockPos pos, boolean server) {
        var mc = Minecraft.getInstance();
        var level = server ? mc.getSingleplayerServer().overworld() : mc.level;
        return (IItemHandlerModifiable) level.getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElseThrow(IllegalStateException::new);
    }

    private static void server(Runnable action) {
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure = error;
            }
        });
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
