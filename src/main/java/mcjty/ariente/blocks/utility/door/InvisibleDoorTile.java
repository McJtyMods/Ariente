package mcjty.ariente.blocks.utility.door;

import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InvisibleDoorTile extends ArienteBlockEntity implements ILockable {

    public InvisibleDoorTile(BlockPos pos, BlockState state) {
        super(Registration.INVISIBLE_DOOR_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of().mapColor(MapColor.METAL)
                        .dynamicShape())
                .tileEntitySupplier(InvisibleDoorTile::new)
        ) {
            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(BlockStateProperties.OPEN);
            }

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

            @Deprecated
            @Override
            public boolean isPathfindable(BlockState state, PathComputationType pathType) {
                return InvisibleDoorTile.isPathfindable(state, pathType);
            }
        };
    }

    public static boolean isPathfindable(BlockState state, PathComputationType pathType) {
        return pathType == PathComputationType.LAND && state.getValue(BlockStateProperties.OPEN);
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

}
