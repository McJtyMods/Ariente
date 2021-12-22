package mcjty.ariente.blocks.utility.autofield;

import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.components.IPlayerInventory;
import mcjty.hologui.api.components.ISlots;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class AbstractItemNodeTile extends AbstractNodeTile {

    public static final int FILTER_AMOUNT = 12;

    public AbstractItemNodeTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected void addToFilter(Player player, IHoloGuiEntity entity, SimpleItemHandler filter) {
        entity.findComponent("playerSlots").ifPresent(component -> {
            if (component instanceof IPlayerInventory) {
                int selected = ((IPlayerInventory) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = player.getInventory().getItem(selected);
                    if (!extracted.isEmpty()) {
                        ItemHandlerHelper.insertItem(filter, extracted, false);
                        notifyField();
                    }
                }
            }
        });
    }

    protected void removeFromFilter(Player player, IHoloGuiEntity entity, SimpleItemHandler filter) {
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
