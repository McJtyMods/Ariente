package mcjty.ariente.cables;

import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.ariente.setup.Registration;
import mcjty.lib.compat.theoneprobe.TOPInfoProvider;
import mcjty.lib.compat.waila.WailaInfoProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GenericCableBlock extends Block implements WailaInfoProvider, TOPInfoProvider {

    // Properties that indicate if there is the same block in a certain direction.
    public static final EnumProperty<ConnectorType> NORTH = EnumProperty.<ConnectorType>create("north", ConnectorType.class);
    public static final EnumProperty<ConnectorType> SOUTH = EnumProperty.<ConnectorType>create("south", ConnectorType.class);
    public static final EnumProperty<ConnectorType> WEST = EnumProperty.<ConnectorType>create("west", ConnectorType.class);
    public static final EnumProperty<ConnectorType> EAST = EnumProperty.<ConnectorType>create("east", ConnectorType.class);
    public static final EnumProperty<ConnectorType> UP = EnumProperty.<ConnectorType>create("up", ConnectorType.class);
    public static final EnumProperty<ConnectorType> DOWN = EnumProperty.<ConnectorType>create("down", ConnectorType.class);

    public static final ModelProperty<BlockState> FACADEID = new ModelProperty<>();
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


    public GenericCableBlock(Material material) {
        super(Properties.of(material)
                .sound(SoundType.METAL)
                .strength(1.0f)
                .harvestTool(ToolType.PICKAXE)
                .harvestLevel(0)
        );
        makeShapes();
        registerDefaultState(defaultBlockState().setValue(COLOR, CableColor.NEGARITE));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new NetCableTileEntity();
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
            return Shapes.join(shape, cableShape, IBooleanFunction.OR);
        } else if (connectorType == ConnectorType.BLOCK) {
            return Shapes.join(shape, blockShape, IBooleanFunction.OR);
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

    //    public static boolean activateBlock(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
//        return block.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
//    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter worldIn, BlockPos pos, BlockState state) {
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

    protected ItemStack updateColorInStack(ItemStack item, CableColor color) {
        if (color != null) {
            CompoundTag tag = item.getOrCreateTag();
            CompoundTag display = new CompoundTag();
            // @todo 1.14 check
            String unlocname = getDescriptionId() + "_" + color.getSerializedName() + ".name";
            display.putString("LocName", unlocname);
            tag.put("display", display);
        }
        return item;
    }

// @todo 1.14
//    @Override
//    public int damageDropped(BlockState state) {
//        return state.getValue(COLOR).ordinal();
//    }

//    public void initModel() {
//        ResourceLocation name = getRegistryName();
//        for (CableColor color : CableColor.VALUES) {
//            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), color.ordinal(), new ModelResourceLocation(new ResourceLocation(name.getResourceDomain(), name.getResourcePath()+"item"), "color=" + color.name()));
//        }
//    }

//    public void initItemModel() {
//    }

    @Nullable
    protected BlockState getMimicBlock(BlockGetter blockAccess, BlockPos pos) {
        BlockEntity te = blockAccess.getBlockEntity(pos);
        if (te instanceof IFacadeSupport) {
            return ((IFacadeSupport) te).getMimicBlock();
        } else {
            return null;
        }
    }

    // @todo 1.14
//    public void initColorHandler(BlockColors blockColors) {
//        blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
//            BlockState mimicBlock = getMimicBlock(world, pos);
//            return mimicBlock != null ? blockColors.colorMultiplier(mimicBlock, world, pos, tintIndex) : -1;
//        }, this);
//    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public AxisAlignedBB getSelectedBoundingBox(BlockState state, World worldIn, BlockPos pos) {
//        return AABB_EMPTY;
//    }

    // @todo 1.14
//    @Nullable
//    @Override
//    public RayTraceResult collisionRayTrace(BlockState blockState, World world, BlockPos pos, Vector3d start, Vector3d end) {
//        if (getMimicBlock(world, pos) != null) {
//            // In mimic mode we use original raytrace mode
//            return originalCollisionRayTrace(blockState, world, pos, start, end);
//        }
//        Vector3d vec3d = start.subtract(pos.getX(), pos.getY(), pos.getZ());
//        Vector3d vec3d1 = end.subtract(pos.getX(), pos.getY(), pos.getZ());
//        RayTraceResult rc = checkIntersect(pos, vec3d, vec3d1, AABB_CENTER);
//        if (rc != null) {
//            return rc;
//        }
//        CableColor color = blockState.getValue(COLOR);
//
//        for (Direction facing : Direction.VALUES) {
//            ConnectorType type = getConnectorType(color, world, pos, facing);
//            if (type != ConnectorType.NONE) {
//                rc = checkIntersect(pos, vec3d, vec3d1, AABBS[facing.ordinal()]);
//                if (rc != null) {
//                    return rc;
//                }
//            }
//            if (type == ConnectorType.BLOCK) {
//                rc = checkIntersect(pos, vec3d, vec3d1, AABBS_CONNECTOR[facing.ordinal()]);
//                if (rc != null) {
//                    return rc;
//                }
//            }
//        }
//        return null;
//    }

    // @todo 1.14
//    private RayTraceResult checkIntersect(BlockPos pos, Vector3d vec3d, Vector3d vec3d1, AxisAlignedBB boundingBox) {
//        RayTraceResult raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
//        return raytraceresult == null ? null : new RayTraceResult(raytraceresult.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), raytraceresult.sideHit, pos);
//    }
//
//    protected RayTraceResult originalCollisionRayTrace(BlockState blockState, World world, BlockPos pos, Vector3d start, Vector3d end) {
//        return super.collisionRayTrace(blockState, world, pos, start, end);
//    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    @Optional.Method(modid = "waila")
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        return currenttip;
//    }
//
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof GenericCableTileEntity) {
//            GenericCableTileEntity cableTileEntity = (GenericCableTileEntity) te;
//            int cableId = cableTileEntity.getCableId();
//            probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + cableId);
//            PowerSystem powerSystem = PowerSystem.getPowerSystem(world);
//            int tickCounter = powerSystem.getTickCounter();
//            PowerBlob blob = powerSystem.getPowerBlob(cableId);
//            CableColor cableColor = cableTileEntity.getCableColor();
//            if (cableColor.equals(CableColor.NEGARITE) || cableColor.equals(CableColor.COMBINED)) {
//                PowerAmount negariteAmount = blob.getPowerAmount(PowerType.NEGARITE);
//                if (negariteAmount.getLastUsedTick() >= tickCounter - 1) {
//                    probeInfo.text(TextStyleClass.LABEL + "Negarite:" + TextFormatting.GREEN + " +" + negariteAmount.getPrevTotalAdded()
//                        + TextFormatting.RED + " -" + negariteAmount.getPrevTotalConsumed());
//                }
//            }
//            if (cableColor.equals(CableColor.POSIRITE) || cableColor.equals(CableColor.COMBINED)) {
//                PowerAmount posiriteAmount = blob.getPowerAmount(PowerType.POSIRITE);
//                if (posiriteAmount.getLastUsedTick() >= tickCounter - 1) {
//                    probeInfo.text(TextStyleClass.LABEL + "Posirite:" + TextFormatting.GREEN + " +" + posiriteAmount.getPrevTotalAdded()
//                            + TextFormatting.RED + " -" + posiriteAmount.getPrevTotalConsumed());
//                }
//            }
//        }
//    }

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
        if (!world.isClientSide) {
            PowerSenderSupport.fixNetworks(world, pos);
        }
    }

// @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
//        return false;
//    }
//
//    @Override
//    public boolean isBlockNormalCube(BlockState blockState) {
//        return false;
//    }
//
//    @Override
//    public boolean isOpaqueCube(BlockState blockState) {
//        return false;
//    }
//
//    @Override
//    public boolean isFullCube(BlockState state) {
//        return false;
//    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }


    public int getUpDownMask(BlockState state, IWorldReader world, BlockPos pos) {
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

    public int getEastWestMask(BlockState state, IWorldReader world, BlockPos pos) {
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

    public int getNorthSouthMask(BlockState state, IWorldReader world, BlockPos pos) {
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
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = calculateState(world, pos, defaultBlockState());
        System.out.println("state = " + state);
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, Level world, BlockPos current, BlockPos offset) {
        return calculateState(world, current, state);
    }

    @Nonnull
    public BlockState calculateState(Level world, BlockPos pos, BlockState state) {
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

//    public BlockState getStateInternal(BlockState state, IBlockAccess world, BlockPos pos) {
//        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
//        CableColor color = state.getValue(COLOR);
//
//        ConnectorType north = getConnectorType(color, world, pos, Direction.NORTH);
//        ConnectorType south = getConnectorType(color, world, pos, Direction.SOUTH);
//        ConnectorType west = getConnectorType(color, world, pos, Direction.WEST);
//        ConnectorType east = getConnectorType(color, world, pos, Direction.EAST);
//        ConnectorType up = getConnectorType(color, world, pos, Direction.UP);
//        ConnectorType down = getConnectorType(color, world, pos, Direction.DOWN);
//
//        return extendedBlockState
//                .with(NORTH, north)
//                .with(SOUTH, south)
//                .with(WEST, west)
//                .with(EAST, east)
//                .with(UP, up)
//                .with(DOWN, down);
//    }

    protected abstract ConnectorType getConnectorType(@Nonnull CableColor thisColor, BlockGetter world, BlockPos pos, Direction facing);
}
