package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FieldMarkerTile extends GenericTileEntity {

    private BlockPos autoFieldTile = null;

    @Override
    public void markDirty() {
        // Make sure to mark the MultipartTE as dirty
        world.getTileEntity(pos).markDirty();
    }

    @Override
    public void markDirtyQuick() {
        // Make sure to mark the MultipartTE as dirty
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof MultipartTE) {
            ((MultipartTE) te).markDirtyQuick();
        }
    }

    public void setAutoFieldTile(BlockPos autoFieldTile) {
        this.autoFieldTile = autoFieldTile;
        markDirtyQuick();
    }

    @Override
    public void onPartAdded(PartSlot slot, BlockState state, TileEntity multipartTile) {
        this.world = multipartTile.getWorld();
        this.pos = multipartTile.getPos();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        AutoFieldTile auto = null;
        for (Direction facing : Direction.HORIZONTALS) {
            BlockPos p = pos.offset(facing);
            TileEntity te = MultipartHelper.getTileEntity(world, p, PartSlot.DOWN);
            if (te instanceof FieldMarkerTile) {
                BlockPos tile = ((FieldMarkerTile) te).getAutoFieldTile();
                if (tile != null) {
                    TileEntity tileEntity = world.getTileEntity(tile);
                    if (tileEntity instanceof AutoFieldTile) {
                        auto = (AutoFieldTile) tileEntity;
                        break;
                    }
                }
            }
        }
        if (auto != null) {
            autoFieldTile = auto.getPos();
            auto.addFieldMarker(pos);
            markDirtyQuick();
        }
    }

    @Override
    public void onBlockBreak(World world, BlockPos pos, BlockState state) {
        if (autoFieldTile != null) {
            TileEntity tileEntity = world.getTileEntity(autoFieldTile);
            if (tileEntity instanceof AutoFieldTile) {
                ((AutoFieldTile)tileEntity).removeFieldMarker(pos);
            }
        }
    }

    public BlockPos getAutoFieldTile() {
        return autoFieldTile;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        autoFieldTile = BlockPosTools.readFromNBT(tagCompound, "autofield");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        if (autoFieldTile != null) {
            BlockPosTools.writeToNBT(tagCompound, "autofield", autoFieldTile);
        }
    }
}
