package mcjty.ariente.blocks.utility.autofield;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import java.util.Objects;
import javax.annotation.Nonnull;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.multipart.MultipartHelper;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FieldMarkerTile extends ArienteBlockEntity {

    private BlockPos autoFieldTile = null;

    public static final VoxelShape FLAT_SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    public FieldMarkerTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.FIELD_MARKER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
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
        BlockEntity parent = level == null ? null : level.getBlockEntity(worldPosition);
        if (parent instanceof MultipartTE) {
            parent.setChanged();
        } else {
            super.setChanged();
        }
    }

    @Override
    public void markDirtyQuick() {
        // Make sure to mark the MultipartTE as dirty
        BlockEntity parent = level == null ? null : level.getBlockEntity(worldPosition);
        if (parent instanceof MultipartTE multipart) {
            multipart.markDirtyQuick();
        } else {
            super.markDirtyQuick();
        }
    }

    public void setAutoFieldTile(BlockPos autoFieldTile) {
        if (!Objects.equals(this.autoFieldTile, autoFieldTile)) {
            this.autoFieldTile = autoFieldTile;
            markDirtyQuick();
        }
    }

    @Override
    public void onPartAdded(PartSlot slot, BlockState state, BlockEntity multipartTile) {
        this.level = multipartTile.getLevel();
        connectToField(level, worldPosition);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        connectToField(world, pos);
    }

    private void connectToField(Level world, BlockPos pos) {
        AutoFieldTile auto = null;
        for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
            BlockPos p = pos.relative(facing);
            if (world.hasChunkAt(p) && world.getBlockEntity(p) instanceof AutoFieldTile field) {
                auto = field;
                break;
            }
            BlockEntity te = MultipartHelper.getBlockEntity(world, p, PartSlot.DOWN);
            if (te instanceof FieldMarkerTile) {
                BlockPos tile = ((FieldMarkerTile) te).getAutoFieldTile();
                if (tile != null) {
                    BlockEntity tileEntity = world.hasChunkAt(tile) ? world.getBlockEntity(tile) : null;
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
            BlockEntity tileEntity = world.hasChunkAt(autoFieldTile) ? world.getBlockEntity(autoFieldTile) : null;
            if (tileEntity instanceof AutoFieldTile) {
                ((AutoFieldTile)tileEntity).removeFieldMarker(pos);
            }
        }
    }

    public BlockPos getAutoFieldTile() {
        if (autoFieldTile != null && level != null && !level.isClientSide() && level.hasChunkAt(autoFieldTile)
                && !(level.getBlockEntity(autoFieldTile) instanceof AutoFieldTile)) {
            setAutoFieldTile(null);
        }
        return autoFieldTile;
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        CompoundTag info = tagCompound.getCompoundOrEmpty("Info");
        autoFieldTile = info.contains("autofield") ? BlockPosTools.read(info, "autofield") : null;
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        if (autoFieldTile != null) {
            BlockPosTools.write(getOrCreateInfo(tagCompound), "autofield", autoFieldTile);
        }
        super.saveAdditional(tagCompound, provider);
    }
}
