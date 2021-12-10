package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class FieldMarkerTile extends GenericTileEntity {

    private BlockPos autoFieldTile = null;

    public static final VoxelShape FLAT_SHAPE = VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);


    public FieldMarkerTile() {
        super(Registration.FIELD_MARKER_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .tileEntitySupplier(FieldMarkerTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }

            @Nonnull
            @Override
            public PartSlot getSlotFromState(World world, BlockPos pos, BlockState newState) {
                return PartSlot.DOWN;
            }

            @Override
            public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
                return FLAT_SHAPE;
            }
        };
    }

    @Override
    public void setChanged() {
        // Make sure to mark the MultipartTE as dirty
        level.getBlockEntity(worldPosition).setChanged();
    }

    @Override
    public void markDirtyQuick() {
        // Make sure to mark the MultipartTE as dirty
        TileEntity te = level.getBlockEntity(worldPosition);
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
        this.level = multipartTile.getLevel();
        this.worldPosition = multipartTile.getBlockPos();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        AutoFieldTile auto = null;
        for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
            BlockPos p = pos.relative(facing);
            TileEntity te = MultipartHelper.getTileEntity(world, p, PartSlot.DOWN);
            if (te instanceof FieldMarkerTile) {
                BlockPos tile = ((FieldMarkerTile) te).getAutoFieldTile();
                if (tile != null) {
                    TileEntity tileEntity = world.getBlockEntity(tile);
                    if (tileEntity instanceof AutoFieldTile) {
                        auto = (AutoFieldTile) tileEntity;
                        break;
                    }
                }
            }
        }
        if (auto != null) {
            autoFieldTile = auto.getBlockPos();
            auto.addFieldMarker(pos);
            markDirtyQuick();
        }
    }

    @Override
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
        if (autoFieldTile != null) {
            TileEntity tileEntity = world.getBlockEntity(autoFieldTile);
            if (tileEntity instanceof AutoFieldTile) {
                ((AutoFieldTile)tileEntity).removeFieldMarker(pos);
            }
        }
    }

    public BlockPos getAutoFieldTile() {
        return autoFieldTile;
    }

    @Override
    public void load(CompoundNBT tagCompound) {
        super.load(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        if (info.contains("autofield")) {
            autoFieldTile = BlockPosTools.read(info, "autofield");
        }
    }

    @Override
    public void saveAdditional(CompoundNBT tagCompound) {
        if (autoFieldTile != null) {
            BlockPosTools.write(getOrCreateInfo(tagCompound), "autofield", autoFieldTile);
        }
        super.saveAdditional(tagCompound);
    }
}
