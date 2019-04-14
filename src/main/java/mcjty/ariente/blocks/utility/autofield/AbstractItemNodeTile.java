package mcjty.ariente.blocks.utility.autofield;

import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.components.IPlayerInventory;
import mcjty.hologui.api.components.ISlots;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.PartPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

public abstract class AbstractItemNodeTile extends AbstractNodeTile {

    public static final int FILTER_AMOUNT = 12;

    @Nullable
    public IItemHandler getConnectedItemHandler(PartPos partPos) {
        IBlockState state = MultipartHelper.getBlockState(world, pos, partPos.getSlot());
        if (state != null && state.getBlock() == getBlockType()) {
            NodeOrientation orientation = state.getValue(ORIENTATION);
            EnumFacing mainDirection = orientation.getMainDirection();
            TileEntity otherTe = world.getTileEntity(pos.offset(mainDirection));
            if (otherTe != null && otherTe.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, mainDirection.getOpposite())) {
                return otherTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, mainDirection.getOpposite());
            }
        }
        return null;
    }

    protected void addToFilter(EntityPlayer player, IHoloGuiEntity entity, SimpleItemHandler filter) {
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

    protected void removeFromFilter(EntityPlayer player, IHoloGuiEntity entity, SimpleItemHandler filter) {
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
