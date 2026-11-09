package mcjty.ariente.blocks.utility;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.gui.client.InventoryRenderer;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.components.HoloSlots;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.ariente.gui.InventoryLayout.CELL;

/** Fixed machine slots using the same geometry and rendering as the carried inventory. */
public class RecyclerSlots extends HoloSlots {

    private final ResourceHandler<ItemResource> items;
    private final int first;
    private final int columns;
    private final int rows;

    public RecyclerSlots(double x, double y, ResourceHandler<ItemResource> items, int first, int columns, int rows) {
        super(x, y, columns * CELL, rows * CELL);
        this.items = items;
        this.first = first;
        this.columns = columns;
        this.rows = rows;
        itemHandler(items);
        exactView();
    }

    @Override
    protected Pair<ItemStack, Integer> getSelectedPair(Player player, double cursorX, double cursorY) {
        int column = (int) Math.floor((cursorX - x) / CELL);
        int row = (int) Math.floor((cursorY - y) / CELL);
        if (column < 0 || column >= columns || row < 0 || row >= rows) {
            return Pair.of(ItemStack.EMPTY, -1);
        }
        int slot = first + row * columns + column;
        return Pair.of(ItemUtil.getStack(items, slot), slot);
    }

    @Override

    public void render(PoseStack poses, RenderBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        int hovered = getSelectedPair(player, cursorX, cursorY).getRight();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slot = first + row * columns + column;
                ItemStack stack = ItemUtil.getStack(items, slot);
                InventoryRenderer.cell(poses, buffer, x + column * CELL, y + row * CELL, stack, stack.getCount(),
                        selected == slot || hovered == slot);
            }
        }
    }
}
