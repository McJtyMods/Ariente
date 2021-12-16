package mcjty.ariente.blocks.utility.door;

import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InvisibleDoorTile extends GenericTileEntity implements ILockable {

    public InvisibleDoorTile() {
        super(Registration.INVISIBLE_DOOR_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of(Material.METAL)
                        .dynamicShape())
                .tileEntitySupplier(InvisibleDoorTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
                return InvisibleDoorTile.getCollisionShape(state, worldIn, pos);
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return Shapes.empty();
            }

            @Nullable
            @Override
            public PathNodeType getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos, @Nullable MobEntity entity) {
                return InvisibleDoorTile.getAiPathNodeType(state, world, pos);
            }
        };
    }

    @Nonnull
    public static PathNodeType getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof InvisibleDoorTile) {
            DoorMarkerTile door = ((InvisibleDoorTile) te).findDoorMarker();
            if (door.isOpen()) {
                return PathNodeType.OPEN;
            }
        }
        return PathNodeType.BLOCKED;
    }

    public static VoxelShape getCollisionShape(BlockState blockState, BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof InvisibleDoorTile) {
            DoorMarkerTile door = ((InvisibleDoorTile) te).findDoorMarker();
            if (door != null && door.isOpen()) {
                return DoorMarkerTile.OPEN_BLOCK_AABB;
            }
        }
        return DoorMarkerTile.CLOSED_BLOCK_AABB;
    }

    @Override
    public boolean isLocked() {
        // An invisible door block can never be broken
        return true;
//        DoorMarkerTile tile = findDoorMarker();
//        if (tile.isLocked()) {
//            return true;
//        }
//        return false;
    }

    public DoorMarkerTile findDoorMarker() {
        DoorMarkerTile doorMarkerTile = null;
        // Find the parent door marker
        BlockPos p = worldPosition.below();
        for (int i = 0; i < UtilityConfiguration.MAX_DOOR_HEIGHT.get() ; i++) {
            BlockEntity marker = getLevel().getBlockEntity(p);
            if (marker instanceof DoorMarkerTile) {
                doorMarkerTile = (DoorMarkerTile) marker;
                break;
            }
            p = p.below();
        }
        return doorMarkerTile;
    }

// @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        // @todo Should be pass 0 but it flickers then if an entity comes into view
//        return pass == 0;
//    }
}
