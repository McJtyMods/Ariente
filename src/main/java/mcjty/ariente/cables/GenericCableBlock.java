package mcjty.ariente.cables;

import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.lib.compat.theoneprobe.TOPInfoProvider;
import mcjty.lib.compat.waila.WailaInfoProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
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


    public static final AxisAlignedBB AABB_EMPTY = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    public static final AxisAlignedBB AABB_CENTER = new AxisAlignedBB(.4, .4, .4, .6, .6, .6);

    public static final AxisAlignedBB AABBS[] = new AxisAlignedBB[]{
            new AxisAlignedBB(.4, 0, .4, .6, .4, .6),
            new AxisAlignedBB(.4, .6, .4, .6, 1, .6),
            new AxisAlignedBB(.4, .4, 0, .6, .6, .4),
            new AxisAlignedBB(.4, .4, .6, .6, .6, 1),
            new AxisAlignedBB(0, .4, .4, .4, .6, .6),
            new AxisAlignedBB(.6, .4, .4, 1, .6, .6)
    };

    public static final AxisAlignedBB AABBS_CONNECTOR[] = new AxisAlignedBB[]{
            new AxisAlignedBB(.2, 0, .2, .8, .1, .8),
            new AxisAlignedBB(.2, .9, .2, .8, 1, .8),
            new AxisAlignedBB(.2, .2, 0, .8, .8, .1),
            new AxisAlignedBB(.2, .2, .9, .8, .8, 1),
            new AxisAlignedBB(0, .2, .2, .1, .8, .8),
            new AxisAlignedBB(.9, .2, .2, 1, .8, .8)
    };


    public GenericCableBlock(Material material) {
        super(Properties.create(material)
                .sound(SoundType.METAL)
                .hardnessAndResistance(1.0f)
                .harvestTool(ToolType.PICKAXE)
                .harvestLevel(0)
            );
        // @todo 1.14
//        setRegistryName(name);
        setDefaultState(getDefaultState().with(COLOR, CableColor.NEGARITE));
    }

//    public static boolean activateBlock(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
//        return block.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
//    }

//    public static Collection<IProperty<?>> getPropertyKeys(BlockState state) {
//        return state.getPropertyKeys();
//    }

    // @todo 1.14
//    @Override
//    public ItemStack getItem(World worldIn, BlockPos pos, BlockState state) {
//        ItemStack item = super.getItem(worldIn, pos, state);
//        return updateColorInStack(item, state.getValue(COLOR));
//    }

    protected ItemStack updateColorInStack(ItemStack item, CableColor color) {
        if (color != null) {
            CompoundNBT tag = item.getOrCreateTag();
            CompoundNBT display = new CompoundNBT();
            // @todo 1.14 check
            String unlocname = getTranslationKey() + "_" + color.getName() + ".name";
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
    protected BlockState getMimicBlock(IBlockReader blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
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
//    public RayTraceResult collisionRayTrace(BlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {
//        if (getMimicBlock(world, pos) != null) {
//            // In mimic mode we use original raytrace mode
//            return originalCollisionRayTrace(blockState, world, pos, start, end);
//        }
//        Vec3d vec3d = start.subtract(pos.getX(), pos.getY(), pos.getZ());
//        Vec3d vec3d1 = end.subtract(pos.getX(), pos.getY(), pos.getZ());
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
//    private RayTraceResult checkIntersect(BlockPos pos, Vec3d vec3d, Vec3d vec3d1, AxisAlignedBB boundingBox) {
//        RayTraceResult raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
//        return raytraceresult == null ? null : new RayTraceResult(raytraceresult.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), raytraceresult.sideHit, pos);
//    }
//
//    protected RayTraceResult originalCollisionRayTrace(BlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {
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
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            PowerSenderSupport.fixNetworks(world, pos);
        }
    }


    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onReplaced(state, world, pos, newState, isMoving);
        if (!world.isRemote) {
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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(COLOR, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }


    public int getUpDownMask(BlockState state, IWorldReader world, BlockPos pos) {
        CableColor color = state.get(COLOR);
        int mask = 0;
        ConnectorType up = getConnectorType(color, world, pos, Direction.UP);
        if (up != ConnectorType.NONE) {
            mask |= 1 << 1;
        }
        ConnectorType down = getConnectorType(color, world, pos, Direction.DOWN);
        if (down != ConnectorType.NONE) {
            mask |= 1 << 0;
        }
        return mask;
    }

    public int getEastWestMask(BlockState state, IWorldReader world, BlockPos pos) {
        CableColor color = state.get(COLOR);
        int mask = 0;
        ConnectorType west = getConnectorType(color, world, pos, Direction.WEST);
        if (west != ConnectorType.NONE) {
            mask |= 1 << 1;
        }
        ConnectorType east = getConnectorType(color, world, pos, Direction.EAST);
        if (east != ConnectorType.NONE) {
            mask |= 1 << 0;
        }
        return mask;
    }

    public int getNorthSouthMask(BlockState state, IWorldReader world, BlockPos pos) {
        CableColor color = state.get(COLOR);
        int mask = 0;
        ConnectorType north = getConnectorType(color, world, pos, Direction.NORTH);
        if (north != ConnectorType.NONE) {
            mask |= 1 << 1;
        }
        ConnectorType south = getConnectorType(color, world, pos, Direction.SOUTH);
        if (south != ConnectorType.NONE) {
            mask |= 1 << 0;
        }
        return mask;
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
//                .withProperty(NORTH, north)
//                .withProperty(SOUTH, south)
//                .withProperty(WEST, west)
//                .withProperty(EAST, east)
//                .withProperty(UP, up)
//                .withProperty(DOWN, down);
//    }

    protected abstract ConnectorType getConnectorType(@Nonnull CableColor thisColor, IBlockReader world, BlockPos pos, Direction facing);
}
