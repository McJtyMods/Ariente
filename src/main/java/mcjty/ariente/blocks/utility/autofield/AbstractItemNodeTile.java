package mcjty.ariente.blocks.utility.autofield;

import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.components.IPlayerInventory;
import mcjty.hologui.api.components.ISlots;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class AbstractItemNodeTile extends AbstractNodeTile {

    public static final int FILTER_AMOUNT = 12;

    public AbstractItemNodeTile(TileEntityType<?> type) {
        super(type);
    }

    protected void addToFilter(PlayerEntity player, IHoloGuiEntity entity, SimpleItemHandler filter) {
        entity.findComponent("playerSlots").ifPresent(component -> {
            if (component instanceof IPlayerInventory) {
                int selected = ((IPlayerInventory) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = player.inventory.getStackInSlot(selected);
                    if (!extracted.isEmpty()) {
                        ItemHandlerHelper.insertItem(filter, extracted, false);
                        notifyField();
                    }
                }
            }
        });
    }

    protected void removeFromFilter(PlayerEntity player, IHoloGuiEntity entity, SimpleItemHandler filter) {
        entity.findComponent("slots").ifPresent(component -> {
            if (component instanceof ISlots) {
                int selected = ((ISlots) component).getSelected();
                if (selected != -1) {
                    filter.setItemStack(selected, ItemStack.EMPTY);
                    notifyField();
                }
            }
        });
    }
}
