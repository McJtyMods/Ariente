package mcjty.ariente.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.gui.client.InventoryRenderer;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.IImage;
import mcjty.hologui.gui.HoloGuiRenderTools;
import mcjty.hologui.gui.components.HoloPlayerSlots;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.ariente.gui.InventoryLayout.*;

/** A fixed player grid; filtered items retain their actual inventory positions. */
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
            if (filter.test(stack, slot)) {
                return Pair.of(stack, slot);
            }
        }
        return Pair.of(ItemStack.EMPTY, -1);
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        InventoryRenderer.header(poses, buffer, hint);
        int hovered = getSelectedPair(player, cursorX, cursorY).getRight();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row == 3 ? column : 9 + row * 9 + column;
                ItemStack stack = player.getInventory().getItem(slot);
                boolean visible = filter.test(stack, slot);
                double slotX = GRID_X + column * CELL;
                double slotY = row == 3 ? HOTBAR_Y : PLAYER_Y + row * CELL;
                InventoryRenderer.cell(poses, buffer, slotX, slotY, visible ? stack : ItemStack.EMPTY,
                        visible ? stack.getCount() : 0, hovered == slot || visible && selected == slot);
                IImage image = visible ? overlay.apply(stack, slot) : null;
                if (image != null) {
                    HoloGuiRenderTools.renderImage(poses, buffer, slotX + .1, slotY + .1,
                            image.getU(), image.getV(), 16, 16, image.getWidth(), image.getHeight(), image.getImage());
                }
            }
        }
    }
}
