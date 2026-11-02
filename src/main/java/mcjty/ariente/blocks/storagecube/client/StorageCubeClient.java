package mcjty.ariente.blocks.storagecube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.storagecube.PacketStorageCubeAction;
import mcjty.ariente.blocks.storagecube.PacketStorageCubeData;
import mcjty.ariente.blocks.storagecube.StorageCubePanel;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.network.ArienteMessages;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.hologui.gui.HoloGuiRenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

import static mcjty.ariente.blocks.storagecube.PacketStorageCubeAction.Action.*;
import static mcjty.hologui.api.IGuiTile.TAG_HELP;

@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class StorageCubeClient {

    private static final double CELL = .8;
    private static final double GRID_X = .4;
    private static final double STORAGE_Y = .7;
    private static final double PLAYER_Y = 4.45;
    private static final double HOTBAR_Y = PLAYER_Y + 3 * CELL + .3;
    private static State state;

    private static boolean pointsAt(HoloGuiEntity holo) {
        Minecraft mc = Minecraft.getInstance();
        return mc.hitResult instanceof EntityHitResult hit && hit.getEntity() == holo;
    }

    private static State open(StorageCubeTile tile, HoloGuiEntity holo) {
        if (state == null || state.tile != tile || state.holo != holo) {
            close();
            state = new State(tile, holo);
            send(new PacketStorageCubeAction(tile.getBlockPos(), holo.getId(), SUBSCRIBE));
        }
        return state;
    }

    private static void close() {
        if (state != null && Minecraft.getInstance().getConnection() != null) {
            send(new PacketStorageCubeAction(state.tile.getBlockPos(), state.holo.getId(), CLOSE));
        }
        state = null;
    }

    private static void send(PacketStorageCubeAction packet) {
        ArienteMessages.INSTANCE.sendToServer(packet);
    }

    public static void receive(PacketStorageCubeData packet) {
        if (state == null || state.holo.getId() != packet.entityId() || !state.tile.getBlockPos().equals(packet.pos())
                || state.size != packet.size()) {
            return;
        }
        for (PacketStorageCubeData.Entry entry : packet.changes()) {
            if (entry.slot() >= 0 && entry.slot() < state.stacks.length) {
                state.stacks[entry.slot()] = entry.stack();
            }
        }
        state.loaded = true;
        state.revision = packet.revision();
        if (packet.reply()) {
            state.pending = false;
            state.selected = -1;
        } else if (state.selected >= 0 && !ItemStack.matches(state.selection, state.stacks[state.selected])) {
            state.selected = -1;
        }
    }

    @SubscribeEvent
    public static void input(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || !(event.isAttack() || event.isUseItem())) {
            return;
        }
        boolean targeted = mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof HoloGuiEntity holo
                && holo.getGuiTile() != null && mc.level.getBlockEntity(holo.getGuiTile()) instanceof StorageCubeTile;
        if (!targeted && (state == null || state.selected < 0 && !state.pressed)) {
            return;
        }
        if (targeted) {
            HoloGuiEntity holo = (HoloGuiEntity) ((EntityHitResult) mc.hitResult).getEntity();
            IGuiComponent<?> gui = holo.getGui(mc.player);
            IGuiComponent<?> hovered = gui == null ? null : gui.findHoveringWidget(holo.getCursorX(), holo.getCursorY());
            if (event.isUseItem() || TAG_HELP.equals(holo.getTag())
                    || hovered != null && !(hovered instanceof StorageCubePanel)) {
                // Let HoloGui handle its usual right-click close and standard help controls.
                close();
                return;
            }
            open((StorageCubeTile) mc.level.getBlockEntity(holo.getGuiTile()), holo);
        }
        if (!event.isAttack()) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        if (state == null || state.pending || !state.loaded) {
            return;
        }
        if (state.pressed) {
            return;
        }
        state.pressed = true;
        double x = state.holo.getCursorX();
        double y = state.holo.getCursorY();
        if (targeted && x >= 7.35 && y < .2 && y >= -.55) {
            send(new PacketStorageCubeAction(state.tile.getBlockPos(), state.holo.getId(), DISMISS));
            state.holo.discard();
            close();
            return;
        }
        int slot = targeted ? slotAt(state, x, y) : -1;
        state.pressOrigin = slot;
        if (slot < 0) {
            state.selected = -1;
        } else if (mc.player.isShiftKeyDown()) {
            transfer(slot, -1, 64, true);
        } else if (state.selected >= 0) {
            if (slot == state.selected) {
                state.selected = -1;
            } else {
                transfer(state.selected, slot, state.amount, false);
            }
        } else if (!state.stacks[slot].isEmpty()) {
            state.selected = slot;
            state.selection = state.stacks[slot].copy();
            state.amount = state.selection.getCount();
        }
    }

    private static void transfer(int source, int target, int amount, boolean quick) {
        if (state.pending || state.stacks[source].isEmpty()) {
            return;
        }
        send(new PacketStorageCubeAction(state.tile.getBlockPos(), state.holo.getId(), quick ? QUICK_MOVE : TRANSFER,
                source, target, amount, state.revision));
        state.pending = true;
        state.pendingTicks = 0;
    }

    @SubscribeEvent
    public static void blockAttack(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide && state != null && state.pressed) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || state == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level != state.tile.getLevel() || !state.holo.isAlive() || state.tile.isRemoved()
                || mc.player.distanceToSqr(state.holo) > 64 || !mc.player.isAlive() || TAG_HELP.equals(state.holo.getTag())) {
            close();
            return;
        }
        if (mc.screen != null) {
            state.selected = -1;
            state.pressed = false;
            return;
        }
        if (++state.ticks % 20 == 0) {
            send(new PacketStorageCubeAction(state.tile.getBlockPos(), state.holo.getId(), SUBSCRIBE));
        }
        if (state.pending && ++state.pendingTicks > 60) {
            close();
            return;
        }
        boolean targeted = pointsAt(state.holo);
        double x = state.holo.getCursorX();
        double y = state.holo.getCursorY();
        int direction = targeted && y >= STORAGE_Y && y < STORAGE_Y + 3 * CELL
                ? x >= -.2 && x < GRID_X ? -1 : x >= GRID_X + 9 * CELL && x <= 8.2 ? 1 : 0 : 0;
        if (direction != state.scrollDirection) {
            state.scrollDirection = direction;
            state.scrollTicks = 0;
        }
        if (direction != 0 && ++state.scrollTicks >= 8) {
            state.column = Math.max(0, Math.min(state.maxColumn(), state.column + direction));
            state.scrollTicks = 0;
        }
        if (state.pressed && !mc.options.keyAttack.isDown()) {
            state.pressed = false;
            int target = targeted ? slotAt(state, x, y) : -1;
            if (state.selected >= 0 && state.pressOrigin == state.selected && target >= 0 && target != state.selected) {
                transfer(state.selected, target, state.amount, false);
            } else if (!targeted) {
                state.selected = -1;
            }
        }
    }

    private static int slotAt(State view, double x, double y) {
        int column = (int) Math.floor((x - GRID_X) / CELL);
        if (column < 0 || column >= 9) {
            return -1;
        }
        int row = (int) Math.floor((y - STORAGE_Y) / CELL);
        if (row >= 0 && row < 3) {
            int slot = (column + view.column) * 3 + row;
            return slot < view.size ? slot : -1;
        }
        row = (int) Math.floor((y - PLAYER_Y) / CELL);
        if (row >= 0 && row < 3) {
            return view.size + 9 + row * 9 + column;
        }
        if (y >= HOTBAR_Y && y < HOTBAR_Y + CELL) {
            return view.size + column;
        }
        return -1;
    }

    public static void render(StorageCubeTile tile, PoseStack poses, MultiBufferSource buffer, IHoloGuiEntity entity, double x, double y) {
        if (!(entity instanceof HoloGuiEntity holo)) {
            return;
        }
        if (pointsAt(holo)) {
            open(tile, holo);
        }
        State view = state != null && state.holo == holo ? state : null;
        text(poses, buffer, .4, -.3, "Holo Storage  " + tile.getItems().getSlots() + " stacks", .46f, 0xffb8f4ff);
        text(poses, buffer, 7.4, -.3, "X", .5f, 0xfff6c797);
        if (view == null || !view.loaded) {
            text(poses, buffer, 1, 2, "Look at the panel to load inventory", .35f, 0xffdddddd);
            return;
        }
        int hover = slotAt(view, x, y);
        for (int c = 0; c < 9; c++) {
            for (int row = 0; row < 3; row++) {
                int slot = (c + view.column) * 3 + row;
                if (slot < view.size) {
                    cell(poses, buffer, view, slot, GRID_X + c * CELL, STORAGE_Y + row * CELL, hover == slot);
                }
            }
            for (int row = 0; row < 4; row++) {
                int slot = view.size + (row == 3 ? c : 9 + row * 9 + c);
                cell(poses, buffer, view, slot, GRID_X + c * CELL, row == 3 ? HOTBAR_Y : PLAYER_Y + row * CELL, hover == slot);
            }
        }
        HoloGuiRenderTools.renderFill(poses, buffer, -.15, STORAGE_Y, .48, 3 * CELL,
                view.scrollDirection == -1 ? 0x996acfff : 0x44477caa);
        HoloGuiRenderTools.renderFill(poses, buffer, 7.67, STORAGE_Y, .48, 3 * CELL,
                view.scrollDirection == 1 ? 0x996acfff : 0x44477caa);
        poses.pushPose();
        poses.translate(0, 0, .003);
        int leftColor = view.column > 0 ? 0xb8e9ff : 0x486078;
        int rightColor = view.column < view.maxColumn() ? 0xb8e9ff : 0x486078;
        HoloGuiRenderTools.renderLine(poses, buffer, .18, 1.55, 0, 1.85, .04, leftColor);
        HoloGuiRenderTools.renderLine(poses, buffer, 0, 1.85, .18, 2.15, .04, leftColor);
        HoloGuiRenderTools.renderLine(poses, buffer, 7.82, 1.55, 8, 1.85, .04, rightColor);
        HoloGuiRenderTools.renderLine(poses, buffer, 8, 1.85, 7.82, 2.15, .04, rightColor);
        poses.popPose();
        text(poses, buffer, .45, 3.25, "Columns " + (view.column + 1) + "-" + (view.column + 9)
                + " / " + ((view.size + 2) / 3) + "    Hover arrows to scroll", .3f, 0xff9acde0);
        text(poses, buffer, .4, 3.85, "Inventory    Drag or sneak-click to move", .3f, 0xffb8f4ff);
        if (view.selected >= 0 && pointsAt(holo)) {
            poses.pushPose();
            poses.translate(0, 0, .015);
            item(poses, buffer, x - .35, y - .35, view.selection);
            count(poses, buffer, x - .35, y - .35, view.amount);
            poses.popPose();
        }
    }

    private static void cell(PoseStack poses, MultiBufferSource buffer, State view, int slot, double x, double y, boolean hover) {
        HoloGuiRenderTools.renderBorder(poses, buffer, x, y, CELL, CELL, hover ? 190 : 75, 160, 200, 170);
        ItemStack stack = view.stacks[slot];
        int count = stack.getCount() - (view.selected == slot ? view.amount : 0);
        if (count > 0) {
            item(poses, buffer, x, y, stack);
            if (count > 1) {
                count(poses, buffer, x, y, count);
            }
        }
    }

    private static void text(PoseStack poses, MultiBufferSource buffer, double x, double y, String text, float scale, int color) {
        HoloGuiRenderTools.renderText(poses, buffer, x, y, text, color, scale);
    }

    private static void item(PoseStack poses, MultiBufferSource buffer, double x, double y, ItemStack stack) {
        // Use the same coordinates as the slot border. The legacy item helper's different
        // X/Y scales accumulated a downward offset in the lower inventory rows.
        poses.pushPose();
        poses.translate((x + .36) * .1 - .42, .42 - (y + .34) * .1, .004);
        poses.scale(.063f, .063f, .000063f);
        var renderer = Minecraft.getInstance().getItemRenderer();
        renderer.render(stack, ItemDisplayContext.GUI, false, poses, buffer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, renderer.getModel(stack, null, null, 1));
        poses.popPose();
    }

    private static void count(PoseStack poses, MultiBufferSource buffer, double x, double y, int amount) {
        String label = Integer.toString(amount);
        var font = Minecraft.getInstance().font;
        // renderText has a .2-unit inset relative to the slot borders. Include it when
        // aligning the count inside the lower-right corner, slightly over the icon.
        double labelX = x + CELL - .06 - font.width(label) * .03 - .2;
        double labelY = y + CELL - .05 - font.lineHeight * .03 - .2;
        poses.pushPose();
        poses.translate(0, 0, .01);
        text(poses, buffer, labelX, labelY, label, .3f, 0xffffffff);
        poses.popPose();
    }

    public static void tooltip(StorageCubeTile tile, PoseStack poses, MultiBufferSource buffer, double x, double y) {
        if (state == null || state.tile != tile || state.selected >= 0) {
            return;
        }
        int slot = slotAt(state, x, y);
        if (slot < 0 || state.stacks[slot].isEmpty()) {
            return;
        }
        poses.pushPose();
        poses.scale(.004f, .004f, .004f);
        poses.mulPose(Axis.YP.rotationDegrees(180));
        poses.mulPose(Axis.ZP.rotationDegrees(180));
        poses.translate(0, 0, -25);
        HoloGuiRenderTools.renderToolTip(poses, buffer, state.stacks[slot], (int) (x * 30 - 120),
                (int) (y * 30 - 95), (stack, lines) -> {});
        poses.popPose();
    }

    private static class State {

        private final StorageCubeTile tile;
        private final HoloGuiEntity holo;
        private final int size;
        private final ItemStack[] stacks;
        private boolean loaded;
        private long revision;
        private int column;
        private int ticks;
        private int scrollDirection;
        private int scrollTicks;
        private int selected = -1;
        private ItemStack selection = ItemStack.EMPTY;
        private int amount;
        private boolean pressed;
        private int pressOrigin = -1;
        private boolean pending;
        private int pendingTicks;

        private State(StorageCubeTile tile, HoloGuiEntity holo) {
            this.tile = tile;
            this.holo = holo;
            size = tile.getItems().getSlots();
            stacks = new ItemStack[size + 36];
            Arrays.fill(stacks, ItemStack.EMPTY);
        }

        private int maxColumn() {
            return Math.max(0, (size + 2) / 3 - 9);
        }
    }
}
