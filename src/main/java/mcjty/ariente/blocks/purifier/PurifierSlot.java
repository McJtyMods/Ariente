package mcjty.ariente.blocks.purifier;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.gui.client.InventoryRenderer;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.gui.components.HoloSlots;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import static mcjty.ariente.gui.InventoryLayout.CELL;

/** A machine slot with the same icon depth and count placement as the player inventory. */
public class PurifierSlot extends HoloSlots {

    private final IItemHandler items;

    public PurifierSlot(IItemHandler items) {
        super(4 - CELL / 2, 1.2, CELL, CELL);
        this.items = items;
        itemHandler(items);
        exactView();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack poses, MultiBufferSource buffer, Player player, IHoloGuiEntity holo, double cursorX, double cursorY) {
        ItemStack stack = items.getStackInSlot(0);
        InventoryRenderer.cell(poses, buffer, x, y, stack, stack.getCount(), selected == 0 || isInside(cursorX, cursorY));
    }
}
