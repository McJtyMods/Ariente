package mcjty.ariente.blocks.utility;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class BlueprintItemHandler implements ResourceHandler<ItemResource> {

    private final Level world;
    private final BlockPos pos;

    @SuppressWarnings("FieldCanBeLocal")
    private List<BlockPos> storageList = null;

    public BlueprintItemHandler(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    public List<BlockPos> getStorages() {
        // @todo optimize!
        storageList = new ArrayList<>();
        for (Direction value : OrientationTools.DIRECTION_VALUES) {
            BlockPos offset = pos.relative(value);
            BlockEntity te = world.getBlockEntity(offset);
            if (te instanceof BlueprintStorageTile) {
                storageList.add(offset);
            }
        }
        return storageList;
    }

    public int getSlots() {
        return getStorages().size() * BlueprintStorageTile.BLUEPRINTS;
    }

    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        int slots = getSlots();
        if (slot >= slots ) {
            return ItemStack.EMPTY;
        }
        BlockPos pos = getStorages().get(slot / BlueprintStorageTile.BLUEPRINTS);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof BlueprintStorageTile) {
            BlueprintStorageTile storage = (BlueprintStorageTile) te;
            return ItemUtil.getStack(storage.getItems(), BlueprintStorageTile.SLOT_BLUEPRINT + slot % BlueprintStorageTile.BLUEPRINTS);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return 64;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return false;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (index < 0 || index >= size()) return 0;
        var storage = (BlueprintStorageTile) world.getBlockEntity(getStorages().get(index / BlueprintStorageTile.BLUEPRINTS));
        return storage == null ? 0 : storage.getItems().extract(
                BlueprintStorageTile.SLOT_BLUEPRINT + index % BlueprintStorageTile.BLUEPRINTS, resource, amount, transaction);
    }
}
