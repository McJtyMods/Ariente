package mcjty.ariente.blocks.storagecube.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.storagecube.PacketStorageCubeAction;
import mcjty.ariente.blocks.storagecube.PacketStorageCubeData;
import mcjty.ariente.blocks.storagecube.StorageCubePanel;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.gui.client.InventoryRenderer;
import mcjty.ariente.network.ArienteMessages;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.hologui.gui.HoloGuiRenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static mcjty.ariente.blocks.storagecube.PacketStorageCubeAction.Action.*;
import static mcjty.ariente.gui.InventoryLayout.*;
import static mcjty.ariente.gui.client.InventoryRenderer.count;
import static mcjty.ariente.gui.client.InventoryRenderer.item;
import static mcjty.hologui.api.IGuiTile.TAG_HELP;

@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class StorageCubeClient {

    private static final double STORAGE_Y = .7;
    private static final double SORT_Y = -.2;
    private static final double SORT_HEIGHT = .55;
    private static final double SORT_NAME_X = 2.6;
    private static final double SORT_NAME_WIDTH = 1;
    private static final double SORT_COUNT_X = 3.72;
    private static final double SORT_COUNT_WIDTH = 1.25;
    private static final Map<Integer, State> VIEWS = new HashMap<>();
    // Gestures belong to one active panel; snapshots remain available for every visible panel.
    private static State state;

    private static boolean pointsAt(HoloGuiEntity holo) {
        Minecraft mc = Minecraft.getInstance();
        return mc.hitResult instanceof EntityHitResult hit && hit.getEntity() == holo;
    }

    private static State view(StorageCubeTile tile, HoloGuiEntity holo) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.isAlive() || mc.player.isSpectator()
                || mc.level != tile.getLevel() || !holo.isAlive() || tile.isRemoved() || tile.isLocked()
                || mc.player.distanceToSqr(holo) > 64 || mc.player.distanceToSqr(tile.getBlockPos().getCenter()) > 64) {
            return null;
        }
        State view = VIEWS.get(holo.getId());
        if (view == null || view.tile != tile || view.holo != holo) {
            if (view != null) {
                unsubscribe(view);
            }
            view = new State(tile, holo);
            VIEWS.put(holo.getId(), view);
            send(new PacketStorageCubeAction(tile.getBlockPos(), holo.getId(), SUBSCRIBE));
        }
        return view;
    }

    private static void deactivate() {
        if (state != null) {
            state.selected = -1;
            state.pressed = false;
            state.fastScrollDirection = 0;
        }
        state = null;
    }

    private static void unsubscribe(State view) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null && mc.level == view.tile.getLevel()) {
            send(new PacketStorageCubeAction(view.tile.getBlockPos(), view.holo.getId(), CLOSE));
        }
        if (state == view) {
            deactivate();
        }
    }

    private static void send(PacketStorageCubeAction packet) {
        ArienteMessages.INSTANCE.sendToServer(packet);
    }

    public static void receive(PacketStorageCubeData packet) {
        State view = VIEWS.get(packet.entityId());
        if (view == null || !view.tile.getBlockPos().equals(packet.pos()) || view.size != packet.size()
                || view.tile.getLevel() != Minecraft.getInstance().level) {
            return;
        }
        for (PacketStorageCubeData.Entry entry : packet.changes()) {
            if (entry.slot() >= 0 && entry.slot() < view.stacks.length) {
                view.stacks[entry.slot()] = entry.stack();
            }
        }
        view.loaded = true;
        view.revision = packet.revision();
        if (packet.reply()) {
            view.pending = false;
            view.selected = -1;
        } else if (view.selected >= 0 && !ItemStack.matches(view.selection, view.stacks[view.selected])) {
            view.selected = -1;
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
                deactivate();
                return;
            }
            State view = view((StorageCubeTile) mc.level.getBlockEntity(holo.getGuiTile()), holo);
            if (view != state) {
                deactivate();
                state = view;
            }
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
        state.pressOrigin = -1;
        double x = state.holo.getCursorX();
        double y = state.holo.getCursorY();
        PacketStorageCubeAction.Action sort = targeted ? sortAt(x, y) : null;
        if (sort != null) {
            state.selected = -1;
            state.column = 0;
            send(new PacketStorageCubeAction(state.tile.getBlockPos(), state.holo.getId(), sort,
                    -1, -1, 0, state.revision));
            state.pending = true;
            state.pendingTicks = 0;
            return;
        }
        int direction = targeted ? scrollDirection(x, y) : 0;
        if (direction != 0) {
            state.selected = -1;
            state.fastScrollDirection = direction;
            scroll(direction, 3);
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

    private static PacketStorageCubeAction.Action sortAt(double x, double y) {
        if (y < SORT_Y || y >= SORT_Y + SORT_HEIGHT) {
            return null;
        }
        if (x >= SORT_NAME_X && x < SORT_NAME_X + SORT_NAME_WIDTH) {
            return SORT_NAME;
        }
        if (x >= SORT_COUNT_X && x < SORT_COUNT_X + SORT_COUNT_WIDTH) {
            return SORT_COUNT;
        }
        return null;
    }

    private static int scrollDirection(double x, double y) {
        return y >= STORAGE_Y && y < STORAGE_Y + 3 * CELL
                ? x >= -.2 && x < GRID_X ? -1 : x >= GRID_X + 9 * CELL && x <= 8.2 ? 1 : 0 : 0;
    }

    private static void scroll(int direction, int columns) {
        state.column = Math.max(0, Math.min(state.maxColumn(), state.column + direction * columns));
        state.scrollTicks = 0;
    }

    @SubscribeEvent
    public static void blockAttack(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide && state != null && state.pressed) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        for (Iterator<State> iterator = VIEWS.values().iterator(); iterator.hasNext();) {
            State view = iterator.next();
            if (mc.player == null || mc.level != view.tile.getLevel() || !view.holo.isAlive() || view.tile.isRemoved()
                    || mc.player.distanceToSqr(view.holo) > 64 || mc.player.distanceToSqr(view.tile.getBlockPos().getCenter()) > 64
                    || !mc.player.isAlive() || mc.player.isSpectator() || TAG_HELP.equals(view.holo.getTag()) || !view.holo.getGuiId().isEmpty()
                    || view.pending && ++view.pendingTicks > 60) {
                unsubscribe(view);
                iterator.remove();
            } else if (++view.ticks % 20 == 0) {
                send(new PacketStorageCubeAction(view.tile.getBlockPos(), view.holo.getId(), SUBSCRIBE));
            }
        }
        if (state == null) {
            return;
        }
        if (mc.screen != null) {
            state.selected = -1;
            state.pressed = false;
            state.fastScrollDirection = 0;
            return;
        }
        boolean targeted = pointsAt(state.holo);
        double x = state.holo.getCursorX();
        double y = state.holo.getCursorY();
        int direction = targeted ? scrollDirection(x, y) : 0;
        if (direction != state.scrollDirection) {
            state.scrollDirection = direction;
            state.scrollTicks = 0;
        }
        if (state.fastScrollDirection != direction || !state.pressed || !mc.options.keyAttack.isDown()) {
            state.fastScrollDirection = 0;
        }
        if (direction != 0 && ++state.scrollTicks >= (state.fastScrollDirection != 0 ? 2 : 8)) {
            scroll(direction, 1);
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
        int playerSlot = playerSlotAt(x, y);
        return playerSlot < 0 ? -1 : view.size + playerSlot;
    }

    public static void render(StorageCubeTile tile, PoseStack poses, MultiBufferSource buffer, IHoloGuiEntity entity, double x, double y) {
        if (!(entity instanceof HoloGuiEntity holo)) {
            return;
        }
        State view = view(tile, holo);
        text(poses, buffer, .4, -.3, "Storage", .46f, 0xffb8f4ff);
        sortButton(poses, buffer, SORT_NAME_X, SORT_NAME_WIDTH, "A-Z", sortAt(x, y) == SORT_NAME, view);
        sortButton(poses, buffer, SORT_COUNT_X, SORT_COUNT_WIDTH, "Count", sortAt(x, y) == SORT_COUNT, view);
        text(poses, buffer, 5.15, -.2, tile.getItems().getSlots() + " stacks", .3f, 0xff9acde0);
        if (view == null || !view.loaded) {
            text(poses, buffer, 1, 2, "Loading inventory - stay near the cube", .35f, 0xffdddddd);
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
        int leftColor = view.column > 0 ? view.fastScrollDirection == -1 ? 0x8fffe1 : 0xb8e9ff : 0x486078;
        int rightColor = view.column < view.maxColumn() ? view.fastScrollDirection == 1 ? 0x8fffe1 : 0xb8e9ff : 0x486078;
        HoloGuiRenderTools.renderLine(poses, buffer, .18, 1.55, 0, 1.85, .04, leftColor);
        HoloGuiRenderTools.renderLine(poses, buffer, 0, 1.85, .18, 2.15, .04, leftColor);
        HoloGuiRenderTools.renderLine(poses, buffer, 7.82, 1.55, 8, 1.85, .04, rightColor);
        HoloGuiRenderTools.renderLine(poses, buffer, 8, 1.85, 7.82, 2.15, .04, rightColor);
        poses.popPose();
        text(poses, buffer, .45, 3.25, "Columns " + (view.column + 1) + "-" + (view.column + 9)
                + " / " + ((view.size + 2) / 3), .3f, 0xff9acde0);
        text(poses, buffer, 3.7, 3.25, "Hover: scroll  Click/hold: faster", .26f, 0xff9acde0);
        InventoryRenderer.header(poses, buffer, "Drag or sneak-click to move");
        if (view.selected >= 0 && pointsAt(holo)) {
            poses.pushPose();
            poses.translate(0, 0, .015);
            item(poses, buffer, x - .35, y - .35, view.selection);
            count(poses, buffer, x - .35, y - .35, view.amount);
            poses.popPose();
        }
    }

    private static void sortButton(PoseStack poses, MultiBufferSource buffer, double x, double width, String label,
                                   boolean hover, State view) {
        boolean enabled = view != null && view.loaded && !view.pending;
        HoloGuiRenderTools.renderControl(poses, buffer, x, SORT_Y, width, SORT_HEIGHT, enabled && hover);
        HoloGuiRenderTools.renderControlText(poses, buffer, x + .08, SORT_Y, width - .16, SORT_HEIGHT,
                label, enabled ? hover ? 0xe2fbff : 0x9edce9 : 0x486078, .32f);
    }

    private static void cell(PoseStack poses, MultiBufferSource buffer, State view, int slot, double x, double y, boolean hover) {
        ItemStack stack = view.stacks[slot];
        int count = stack.getCount() - (view.selected == slot ? view.amount : 0);
        InventoryRenderer.cell(poses, buffer, x, y, stack, count, hover);
    }

    private static void text(PoseStack poses, MultiBufferSource buffer, double x, double y, String text, float scale, int color) {
        HoloGuiRenderTools.renderText(poses, buffer, x, y, text, color, scale);
    }

    public static void tooltip(StorageCubeTile tile, IHoloGuiEntity holo, PoseStack poses, MultiBufferSource buffer, double x, double y) {
        State view = VIEWS.get(holo.getEntity().getId());
        if (view == null || view.tile != tile || view.selected >= 0) {
            return;
        }
        PacketStorageCubeAction.Action sort = sortAt(x, y);
        if (sort != null) {
            poses.pushPose();
            poses.translate(0, 0, .015);
            HoloGuiRenderTools.renderControl(poses, buffer, 2.5, .45, 4.4, .5, false);
            HoloGuiRenderTools.renderControlText(poses, buffer, 2.6, .45, 4.2, .5,
                    sort == SORT_NAME ? "Sort storage: A-Z" : "Sort storage: largest stacks first", 0xb8f4ff, .28f);
            poses.popPose();
            return;
        }
        int slot = slotAt(view, x, y);
        if (slot < 0 || view.stacks[slot].isEmpty()) {
            return;
        }
        poses.pushPose();
        poses.scale(.004f, .004f, .004f);
        poses.mulPose(Axis.YP.rotationDegrees(180));
        poses.mulPose(Axis.ZP.rotationDegrees(180));
        poses.translate(0, 0, -25);
        HoloGuiRenderTools.renderToolTip(poses, buffer, view.stacks[slot], (int) (x * 30 - 120),
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
        private int fastScrollDirection;
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
