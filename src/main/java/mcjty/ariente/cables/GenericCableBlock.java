package mcjty.ariente.cables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.ariente.setup.Registration;
import mcjty.lib.client.ModelProperty;
import mcjty.lib.compat.theoneprobe.TOPInfoProvider;
import mcjty.lib.compat.waila.WailaInfoProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GenericCableBlock extends Block implements WailaInfoProvider, TOPInfoProvider, EntityBlock {
    public static final ModelProperty<BlockState> FACADEID = new ModelProperty<>();

    // Properties that indicate if there is the same block in a certain direction.
    public static final EnumProperty<ConnectorType> NORTH = EnumProperty.<ConnectorType>create("north", ConnectorType.class);
    public static final EnumProperty<ConnectorType> SOUTH = EnumProperty.<ConnectorType>create("south", ConnectorType.class);
    public static final EnumProperty<ConnectorType> WEST = EnumProperty.<ConnectorType>create("west", ConnectorType.class);
    public static final EnumProperty<ConnectorType> EAST = EnumProperty.<ConnectorType>create("east", ConnectorType.class);
    public static final EnumProperty<ConnectorType> UP = EnumProperty.<ConnectorType>create("up", ConnectorType.class);
    public static final EnumProperty<ConnectorType> DOWN = EnumProperty.<ConnectorType>create("down", ConnectorType.class);

    public static final EnumProperty<CableColor> COLOR = EnumProperty.<CableColor>create("color", CableColor.class);

    private static VoxelShape[] shapeCache = null;

    private static final VoxelShape SHAPE_CABLE_NORTH = Shapes.box(.4, .4, 0, .6, .6, .4);
    private static final VoxelShape SHAPE_CABLE_SOUTH = Shapes.box(.4, .4, .6, .6, .6, 1);
    private static final VoxelShape SHAPE_CABLE_WEST = Shapes.box(0, .4, .4, .4, .6, .6);
    private static final VoxelShape SHAPE_CABLE_EAST = Shapes.box(.6, .4, .4, 1, .6, .6);
    private static final VoxelShape SHAPE_CABLE_UP = Shapes.box(.4, .6, .4, .6, 1, .6);
    private static final VoxelShape SHAPE_CABLE_DOWN = Shapes.box(.4, 0, .4, .6, .4, .6);

    private static final VoxelShape SHAPE_BLOCK_NORTH = Shapes.box(.2, .2, 0, .8, .8, .1);
    private static final VoxelShape SHAPE_BLOCK_SOUTH = Shapes.box(.2, .2, .9, .8, .8, 1);
    private static final VoxelShape SHAPE_BLOCK_WEST = Shapes.box(0, .2, .2, .1, .8, .8);
    private static final VoxelShape SHAPE_BLOCK_EAST = Shapes.box(.9, .2, .2, 1, .8, .8);
    private static final VoxelShape SHAPE_BLOCK_UP = Shapes.box(.2, .9, .2, .8, 1, .8);
    private static final VoxelShape SHAPE_BLOCK_DOWN = Shapes.box(.2, 0, .2, .8, .1, .8);

    public GenericCableBlock(MapColor material) {
        super(Properties.of().mapColor(material)
                .sound(SoundType.METAL)
                .strength(1.0f)
        );
        makeShapes();
        registerDefaultState(defaultBlockState().setValue(COLOR, CableColor.NEGARITE));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetCableTileEntity(pos, state);
    }

    private void makeShapes() {
        if (shapeCache == null) {
            int length = ConnectorType.values().length;
            shapeCache = new VoxelShape[length * length * length * length * length * length];

            for (ConnectorType up : ConnectorType.VALUES) {
                for (ConnectorType down : ConnectorType.VALUES) {
                    for (ConnectorType north : ConnectorType.VALUES) {
                        for (ConnectorType south : ConnectorType.VALUES) {
                            for (ConnectorType east : ConnectorType.VALUES) {
                                for (ConnectorType west : ConnectorType.VALUES) {
                                    int idx = calculateShapeIndex(north, south, west, east, up, down);
                                    shapeCache[idx] = makeShape(north, south, west, east, up, down);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private VoxelShape makeShape(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType up, ConnectorType down) {
        VoxelShape shape = Shapes.box(.4, .4, .4, .6, .6, .6);
        shape = combineShape(shape, north, SHAPE_CABLE_NORTH, SHAPE_BLOCK_NORTH);
        shape = combineShape(shape, south, SHAPE_CABLE_SOUTH, SHAPE_BLOCK_SOUTH);
        shape = combineShape(shape, west, SHAPE_CABLE_WEST, SHAPE_BLOCK_WEST);
        shape = combineShape(shape, east, SHAPE_CABLE_EAST, SHAPE_BLOCK_EAST);
        shape = combineShape(shape, up, SHAPE_CABLE_UP, SHAPE_BLOCK_UP);
        shape = combineShape(shape, down, SHAPE_CABLE_DOWN, SHAPE_BLOCK_DOWN);
        return shape;
    }

    private VoxelShape combineShape(VoxelShape shape, ConnectorType connectorType, VoxelShape cableShape, VoxelShape blockShape) {
        if (connectorType == ConnectorType.CABLE) {
            return Shapes.join(shape, cableShape, BooleanOp.OR);
        } else if (connectorType == ConnectorType.BLOCK) {
            return Shapes.join(shape, blockShape, BooleanOp.OR);
        } else {
            return shape;
        }
    }

    private int calculateShapeIndex(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType up, ConnectorType down) {
        int l = ConnectorType.values().length;
        return ((((south.ordinal() * l + north.ordinal()) * l + west.ordinal()) * l + east.ordinal()) * l + up.ordinal()) * l + down.ordinal();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (getMimicBlock(world, pos) != null) {
            // In mimic mode we use original block
            return getMimicBlock(world, pos).getShape(world, pos, context);
        }
        CableColor color = state.getValue(COLOR);
        ConnectorType north = getConnectorType(color, world, pos, Direction.NORTH);
        ConnectorType south = getConnectorType(color, world, pos, Direction.SOUTH);
        ConnectorType west = getConnectorType(color, world, pos, Direction.WEST);
        ConnectorType east = getConnectorType(color, world, pos, Direction.EAST);
        ConnectorType up = getConnectorType(color, world, pos, Direction.UP);
        ConnectorType down = getConnectorType(color, world, pos, Direction.DOWN);
        int index = calculateShapeIndex(north, south, west, east, up, down);
        return shapeCache[index];
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(getItem(state.getValue(COLOR)));
    }

    protected Item getItem(CableColor color) {
        switch (color) {
            case NEGARITE:
                return Registration.NETCABLE_NEGARITE.get();
            case POSIRITE:
                return Registration.NETCABLE_POSIRITE.get();
            case COMBINED:
                return Registration.NETCABLE_COMBINED.get();
            case DATA:
                return Registration.NETCABLE_DATA.get();
        };
        return Items.AIR;
    }

    @Nullable
    protected BlockState getMimicBlock(BlockGetter blockAccess, BlockPos pos) {
        BlockEntity te = blockAccess.getBlockEntity(pos);
        if (te instanceof IFacadeSupport) {
            return ((IFacadeSupport) te).getMimicBlock();
        } else {
            return null;
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide) {
            PowerSenderSupport.fixNetworks(world, pos);
        }
        BlockState blockState = calculateState(world, pos, state);
        if (state != blockState) {
            world.setBlockAndUpdate(pos, blockState);
        }
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, world, pos, newState, isMoving);
        if (!state.is(newState.getBlock()) && !world.isClientSide) {
            PowerSenderSupport.fixNetworks(world, pos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    public int getUpDownMask(BlockState state, BlockGetter world, BlockPos pos) {
        CableColor color = state.getValue(COLOR);
        int mask = 0;
        ConnectorType up = getConnectorType(color, world, pos, Direction.UP);
        if (up != ConnectorType.NONE) {
            mask |= 1 << 1;
        }
        ConnectorType down = getConnectorType(color, world, pos, Direction.DOWN);
        if (down != ConnectorType.NONE) {
            mask |= 1;  // 1 << 0
        }
        return mask;
    }

    public int getEastWestMask(BlockState state, BlockGetter world, BlockPos pos) {
        CableColor color = state.getValue(COLOR);
        int mask = 0;
        ConnectorType west = getConnectorType(color, world, pos, Direction.WEST);
        if (west != ConnectorType.NONE) {
            mask |= 1 << 1;
        }
        ConnectorType east = getConnectorType(color, world, pos, Direction.EAST);
        if (east != ConnectorType.NONE) {
            mask |= 1; // 1 << 0;
        }
        return mask;
    }

    public int getNorthSouthMask(BlockState state, BlockGetter world, BlockPos pos) {
        CableColor color = state.getValue(COLOR);
        int mask = 0;
        ConnectorType north = getConnectorType(color, world, pos, Direction.NORTH);
        if (north != ConnectorType.NONE) {
            mask |= 1 << 1;
        }
        ConnectorType south = getConnectorType(color, world, pos, Direction.SOUTH);
        if (south != ConnectorType.NONE) {
            mask |= 1; // 1 << 0;
        }
        return mask;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = calculateState(world, pos, defaultBlockState());
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world, BlockPos current, BlockPos offset) {
        return calculateState(world, current, state);
    }

    @Nonnull
    public BlockState calculateState(BlockGetter world, BlockPos pos, BlockState state) {
        CableColor color = state.getValue(COLOR);
        ConnectorType north = getConnectorType(color, world, pos, Direction.NORTH);
        ConnectorType south = getConnectorType(color, world, pos, Direction.SOUTH);
        ConnectorType west = getConnectorType(color, world, pos, Direction.WEST);
        ConnectorType east = getConnectorType(color, world, pos, Direction.EAST);
        ConnectorType up = getConnectorType(color, world, pos, Direction.UP);
        ConnectorType down = getConnectorType(color, world, pos, Direction.DOWN);

        return state
                .setValue(NORTH, north)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                .setValue(EAST, east)
                .setValue(UP, up)
                .setValue(DOWN, down);
    }

    // @todo 1.14

    protected abstract ConnectorType getConnectorType(@Nonnull CableColor thisColor, BlockGetter world, BlockPos pos, Direction facing);
}
