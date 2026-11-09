package mcjty.ariente.blocks.purifier;

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

import static mcjty.ariente.gui.InventoryLayout.CELL;

/** A machine slot with the same icon depth and count placement as the player inventory. */
public class PurifierSlot extends HoloSlots {

    private final ResourceHandler<ItemResource> items;

    public PurifierSlot(ResourceHandler<ItemResource> items) {
        super(4 - CELL / 2, 1.2, CELL, CELL);
        this.items = items;
        itemHandler(items);
        exactView();
    }

    @Override

    public void render(PoseStack poses, RenderBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        ItemStack stack = ItemUtil.getStack(items, 0);
        InventoryRenderer.cell(poses, buffer, x, y, stack, stack.getCount(), selected == 0 || isInside(cursorX, cursorY));
    }
}
