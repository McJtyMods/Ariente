package mcjty.ariente.blocks.utility;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class BlueprintItemHandler implements IItemHandler {

    private final World world;
    private final BlockPos pos;

    private List<BlockPos> storageList = null;

    public BlueprintItemHandler(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public List<BlockPos> getStorages() {
        // @todo optimize!
        storageList = new ArrayList<>();
        for (Direction value : Direction.VALUES) {
            BlockPos offset = pos.offset(value);
            TileEntity te = world.getTileEntity(offset);
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
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof BlueprintStorageTile) {
            BlueprintStorageTile storage = (BlueprintStorageTile) te;
            return storage.getStackInSlot(BlueprintStorageTile.SLOT_BLUEPRINT + slot % BlueprintStorageTile.BLUEPRINTS);
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
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof BlueprintStorageTile) {
            BlueprintStorageTile storage = (BlueprintStorageTile) te;
            return storage.getItemHandler().extractItem(BlueprintStorageTile.SLOT_BLUEPRINT + slot % BlueprintStorageTile.BLUEPRINTS, amount, simulate);
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
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof BlueprintStorageTile) {
            BlueprintStorageTile storage = (BlueprintStorageTile) te;
            return storage.getItemHandler().getSlotLimit(BlueprintStorageTile.SLOT_BLUEPRINT + slot % BlueprintStorageTile.BLUEPRINTS);
        }
        return 0;
    }
}
