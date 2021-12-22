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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nonnull;

public class FieldMarkerTile extends GenericTileEntity {

    private BlockPos autoFieldTile = null;

    public static final VoxelShape FLAT_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);


    public FieldMarkerTile(BlockPos pos, BlockState state) {
        super(Registration.FIELD_MARKER_TILE.get(), pos, state);
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
            public PartSlot getSlotFromState(Level world, BlockPos pos, BlockState newState) {
                return PartSlot.DOWN;
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
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
        BlockEntity te = level.getBlockEntity(worldPosition);
        if (te instanceof MultipartTE) {
            ((MultipartTE) te).markDirtyQuick();
        }
    }

    public void setAutoFieldTile(BlockPos autoFieldTile) {
        this.autoFieldTile = autoFieldTile;
        markDirtyQuick();
    }

    @Override
    public void onPartAdded(PartSlot slot, BlockState state, BlockEntity multipartTile) {
        this.level = multipartTile.getLevel();
        // @todo 1.18 this.worldPosition = multipartTile.getBlockPos();
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        AutoFieldTile auto = null;
        for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
            BlockPos p = pos.relative(facing);
            BlockEntity te = MultipartHelper.getTileEntity(world, p, PartSlot.DOWN);
            if (te instanceof FieldMarkerTile) {
                BlockPos tile = ((FieldMarkerTile) te).getAutoFieldTile();
                if (tile != null) {
                    BlockEntity tileEntity = world.getBlockEntity(tile);
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
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        if (autoFieldTile != null) {
            BlockEntity tileEntity = world.getBlockEntity(autoFieldTile);
            if (tileEntity instanceof AutoFieldTile) {
                ((AutoFieldTile)tileEntity).removeFieldMarker(pos);
            }
        }
    }

    public BlockPos getAutoFieldTile() {
        return autoFieldTile;
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        if (info.contains("autofield")) {
            autoFieldTile = BlockPosTools.read(info, "autofield");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        if (autoFieldTile != null) {
            BlockPosTools.write(getOrCreateInfo(tagCompound), "autofield", autoFieldTile);
        }
        super.saveAdditional(tagCompound);
    }
}
