package mcjty.ariente.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.gui.client.InventoryRenderer;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.IImage;
import mcjty.hologui.gui.HoloGuiRenderTools;
import mcjty.hologui.gui.components.HoloPlayerSlots;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.ariente.gui.InventoryLayout.*;

/** A full player grid; filters disable items without hiding them. */
public class PlayerInventoryPanel extends HoloPlayerSlots {

    private final String hint;

    public PlayerInventoryPanel(String hint) {
        super(GRID_X, PLAYER_Y, WIDTH, HEIGHT);
        this.hint = hint;
        exactView();
        withAmount();
        fullBright();
    }

    @Override
    protected Pair<ItemStack, Integer> getSelectedPair(Player player, double cursorX, double cursorY) {
        int slot = playerSlotAt(cursorX, cursorY);
        if (slot >= 0) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && filter.test(stack, slot)) {
                return Pair.of(stack, slot);
            }
        }
        return Pair.of(ItemStack.EMPTY, -1);
    }

    @Override
    protected ItemStack getSelectedStack(Player player, double cursorX, double cursorY) {
        int slot = playerSlotAt(cursorX, cursorY);
        return slot < 0 ? ItemStack.EMPTY : player.getInventory().getItem(slot);
    }

    @Override
    public void hitClient(Player player, IHoloGuiEntity entity, double cursorX, double cursorY) {
        if (getSelectedPair(player, cursorX, cursorY).getRight() < 0) {
            setSelection(-1);
            return;
        }
        super.hitClient(player, entity, cursorX, cursorY);
    }

    @Override
    public void render(PoseStack poses, RenderBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        InventoryRenderer.header(poses, buffer, hint);
        int hovered = getSelectedPair(player, cursorX, cursorY).getRight();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row == 3 ? column : 9 + row * 9 + column;
                ItemStack stack = player.getInventory().getItem(slot);
                boolean enabled = !stack.isEmpty() && filter.test(stack, slot);
                double slotX = GRID_X + column * CELL;
                double slotY = row == 3 ? HOTBAR_Y : PLAYER_Y + row * CELL;
                InventoryRenderer.cell(poses, buffer, slotX, slotY, stack, stack.getCount(),
                        enabled && (hovered == slot || selected == slot), enabled || stack.isEmpty());
                IImage image = enabled ? overlay.apply(stack, slot) : null;
                if (image != null) {
                    HoloGuiRenderTools.renderImage(poses, buffer, slotX + .1, slotY + .1,
                            image.getU(), image.getV(), 16, 16, image.getWidth(), image.getHeight(), image.getImage());
                }
            }
        }
    }
}
