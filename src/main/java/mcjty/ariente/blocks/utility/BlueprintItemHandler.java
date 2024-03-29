package mcjty.ariente.blocks.utility;

import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlueprintItemHandler implements IItemHandler {

    private final Level world;
    private final BlockPos pos;

    @SuppressWarnings("FieldCanBeLocal")
    private List<BlockPos> storageList = null;

    public BlueprintItemHandler(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
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


    @Override
    public int getSlots() {
        return getStorages().size() * BlueprintStorageTile.BLUEPRINTS;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        int slots = getSlots();
        if (slot >= slots ) {
            return ItemStack.EMPTY;
        }
        BlockPos pos = getStorages().get(slot / BlueprintStorageTile.BLUEPRINTS);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof BlueprintStorageTile) {
            BlueprintStorageTile storage = (BlueprintStorageTile) te;
            return storage.getItems().getStackInSlot(BlueprintStorageTile.SLOT_BLUEPRINT + slot % BlueprintStorageTile.BLUEPRINTS);
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int slots = getSlots();
        if (slot >= slots ) {
            return ItemStack.EMPTY;
        }
        BlockPos pos = getStorages().get(slot / BlueprintStorageTile.BLUEPRINTS);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof BlueprintStorageTile) {
            BlueprintStorageTile storage = (BlueprintStorageTile) te;
            return storage.getItems().extractItem(BlueprintStorageTile.SLOT_BLUEPRINT + slot % BlueprintStorageTile.BLUEPRINTS, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        int slots = getSlots();
        if (slot >= slots ) {
            return 0;
        }
        BlockPos pos = getStorages().get(slot / BlueprintStorageTile.BLUEPRINTS);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof BlueprintStorageTile) {
            BlueprintStorageTile storage = (BlueprintStorageTile) te;
            return storage.getItems().getSlotLimit(BlueprintStorageTile.SLOT_BLUEPRINT + slot % BlueprintStorageTile.BLUEPRINTS);
        }
        return 0;
    }
}
