package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IEvent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.components.IIconChoice;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.*;
import static mcjty.hologui.api.Icons.*;

public abstract class AbstractNodeTile extends GenericTileEntity implements IGuiTile {

    public static final EnumProperty<NodeOrientation> ORIENTATION = EnumProperty.create("orientation", NodeOrientation.class, NodeOrientation.values());

    protected DyeColor[] filters = new DyeColor[] { null, null, null, null };

    private static final float T = 0.2f;
    private static final float A = 0.14F;

    private static final VoxelShape AABB_DOWN_NW = VoxelShapes.create(0.0+A, 0, 0.0+A,      0.5-A, T, 0.5-A);
    private static final VoxelShape AABB_DOWN_NE = VoxelShapes.create(0.5+A, 0, 0.0+A,      1.0-A, T, 0.5-A);
    private static final VoxelShape AABB_DOWN_SW = VoxelShapes.create(0.0+A, 0, 0.5+A,      0.5-A, T, 1.0-A);
    private static final VoxelShape AABB_DOWN_SE = VoxelShapes.create(0.5+A, 0, 0.5+A,      1.0-A, T, 1.0-A);
    private static final VoxelShape AABB_UP_NW = VoxelShapes.create(0.0+A, 1-T, 0.0+A,      0.5-A, 1, 0.5-A);
    private static final VoxelShape AABB_UP_NE = VoxelShapes.create(0.5+A, 1-T, 0.0+A,      1.0-A, 1, 0.5-A);
    private static final VoxelShape AABB_UP_SW = VoxelShapes.create(0.0+A, 1-T, 0.5+A,      0.5-A, 1, 1.0-A);
    private static final VoxelShape AABB_UP_SE = VoxelShapes.create(0.5+A, 1-T, 0.5+A,      1.0-A, 1, 1.0-A);
    private static final VoxelShape AABB_NORTH_DE = VoxelShapes.create(0.5+A, 0.0+A, 0,     1.0-A, 0.5-A, T);
    private static final VoxelShape AABB_NORTH_DW = VoxelShapes.create(0.0+A, 0.0+A, 0,     0.5-A, 0.5-A, T);
    private static final VoxelShape AABB_NORTH_UE = VoxelShapes.create(0.5+A, 0.5+A, 0,     1.0-A, 1.0-A, T);
    private static final VoxelShape AABB_NORTH_UW = VoxelShapes.create(0.0+A, 0.5+A, 0,     0.5-A, 1.0-A, T);
    private static final VoxelShape AABB_SOUTH_DE = VoxelShapes.create(0.5+A, 0.0+A, 1-T,   1.0-A, 0.5-A, 1);
    private static final VoxelShape AABB_SOUTH_DW = VoxelShapes.create(0.0+A, 0.0+A, 1-T,   0.5-A, 0.5-A, 1);
    private static final VoxelShape AABB_SOUTH_UE = VoxelShapes.create(0.5+A, 0.5+A, 1-T,   1.0-A, 1.0-A, 1);
    private static final VoxelShape AABB_SOUTH_UW = VoxelShapes.create(0.0+A, 0.5+A, 1-T,   0.5-A, 1.0-A, 1);
    private static final VoxelShape AABB_WEST_DN = VoxelShapes.create(0, 0.0+A, 0.0+A,      T, 0.5-A, 0.5-A);
    private static final VoxelShape AABB_WEST_DS = VoxelShapes.create(0, 0.0+A, 0.5+A,      T, 0.5-A, 1.0-A);
    private static final VoxelShape AABB_WEST_UN = VoxelShapes.create(0, 0.5+A, 0.0+A,      T, 1.0-A, 0.5-A);
    private static final VoxelShape AABB_WEST_US = VoxelShapes.create(0, 0.5+A, 0.5+A,      T, 1.0-A, 1.0-A);
    private static final VoxelShape AABB_EAST_DN = VoxelShapes.create(1-T, 0.0+A, 0.0+A,    1, 0.5-A, 0.5-A);
    private static final VoxelShape AABB_EAST_DS = VoxelShapes.create(1-T, 0.0+A, 0.5+A,    1, 0.5-A, 1.0-A);
    private static final VoxelShape AABB_EAST_UN = VoxelShapes.create(1-T, 0.5+A, 0.0+A,    1, 1.0-A, 0.5-A);
    private static final VoxelShape AABB_EAST_US = VoxelShapes.create(1-T, 0.5+A, 0.5+A,    1, 1.0-A, 1.0-A);

    public AbstractNodeTile(TileEntityType<?> type) {
        super(type);
    }

    public static BlockState getStateForPlacement(Block block, Direction facing, double hitX, double hitY, double hitZ) {
        NodeOrientation orientation = getOrientationFromPlacement(facing, hitX, hitY, hitZ);
        // Since this is a multipart we can use state that isn't convertable to metadata
        return block.getDefaultState().with(ORIENTATION, orientation);
    }

    @Override
    public void markDirty() {
        // Make sure to mark the MultipartTE as dirty
        world.getTileEntity(pos).markDirty();
    }

    @Override
    public void markDirtyQuick() {
        // Make sure to mark the MultipartTE as dirty
        ((GenericTileEntity)world.getTileEntity(pos)).markDirtyQuick();
    }

    public DyeColor[] getFilters() {
        return filters;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        AutoFieldTile.notifyField(world, pos);
    }

    protected void notifyField() {
        markDirtyClient();
        AutoFieldTile.notifyField(world, pos);
    }

    @Override
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
        AutoFieldTile.notifyField(world, pos);
    }

    public static VoxelShape getVoxelShape(BlockState state, IBlockReader world, BlockPos pos) {
        NodeOrientation orientation = state.get(ORIENTATION);
        switch (orientation) {
            case DOWN_NE: return AABB_DOWN_NE;
            case DOWN_NW: return AABB_DOWN_NW;
            case DOWN_SE: return AABB_DOWN_SE;
            case DOWN_SW: return AABB_DOWN_SW;
            case UP_NE: return AABB_UP_NE;
            case UP_NW: return AABB_UP_NW;
            case UP_SE: return AABB_UP_SE;
            case UP_SW: return AABB_UP_SW;
            case NORTH_DE: return AABB_NORTH_DE;
            case NORTH_DW: return AABB_NORTH_DW;
            case NORTH_UE: return AABB_NORTH_UE;
            case NORTH_UW: return AABB_NORTH_UW;
            case SOUTH_DE: return AABB_SOUTH_DE;
            case SOUTH_DW: return AABB_SOUTH_DW;
            case SOUTH_UE: return AABB_SOUTH_UE;
            case SOUTH_UW: return AABB_SOUTH_UW;
            case WEST_DN: return AABB_WEST_DN;
            case WEST_DS: return AABB_WEST_DS;
            case WEST_UN: return AABB_WEST_UN;
            case WEST_US: return AABB_WEST_US;
            case EAST_DN: return AABB_EAST_DN;
            case EAST_DS: return AABB_EAST_DS;
            case EAST_UN: return AABB_EAST_UN;
            case EAST_US: return AABB_EAST_US;
        }
        return VoxelShapes.empty();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGuiEntity(world, pos, player, state.get(ORIENTATION).getSlot().name(), 1.0);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onPartAdded(PartSlot slot, BlockState state, TileEntity multipartTile) {
        this.world = multipartTile.getWorld();
        this.pos = multipartTile.getPos();
        AutoFieldTile.notifyField(world, pos);
    }

    public static NodeOrientation getOrientationFromPlacement(Direction side, double hitX, double hitY, double hitZ) {
        side = side.getOpposite();
        NodeOrientation facing;
        switch (side) {
            case DOWN:
                if (hitX < .5) {    // W
                    facing = hitZ < 0.5 ? DOWN_NW : DOWN_SW;
                } else {            // E
                    facing = hitZ < 0.5 ? DOWN_NE : DOWN_SE;
                }
                break;
            case UP:
                if (hitX < .5) {    // W
                    facing = hitZ < 0.5 ? UP_NW : UP_SW;
                } else {            // E
                    facing = hitZ < 0.5 ? UP_NE : UP_SE;
                }
                break;
            case NORTH:
                if (hitX < .5) {    // W
                    facing = hitY < 0.5 ? NORTH_DW : NORTH_UW;
                } else {            // E
                    facing = hitY < 0.5 ? NORTH_DE : NORTH_UE;
                }
                break;
            case SOUTH:
                if (hitX < .5) {    // W
                    facing = hitY < 0.5 ? SOUTH_DW : SOUTH_UW;
                } else {            // E
                    facing = hitY < 0.5 ? SOUTH_DE : SOUTH_UE;
                }
                break;
            case WEST:
                if (hitZ < .5) {    // N
                    facing = hitY < 0.5 ? WEST_DN : WEST_UN;
                } else {            // S
                    facing = hitY < 0.5 ? WEST_DS : WEST_US;
                }
                break;
            case EAST:
                if (hitZ < .5) {    // N
                    facing = hitY < 0.5 ? EAST_DN : EAST_UN;
                } else {            // S
                    facing = hitY < 0.5 ? EAST_DS : EAST_US;
                }
                break;
            default:
                facing = DOWN_NW;
                break;
        }
        return facing;
    }

    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        CompoundNBT info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            for (int i = 0; i < filters.length; i++) {
                if (tagCompound.contains("f" + i)) {
                    filters[i] = DyeColor.values()[tagCompound.getInt("f" + i)];
                } else {
                    filters[i] = null;
                }
            }
        }
    }

    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        CompoundNBT info = getOrCreateInfo(tagCompound);
        for (int i = 0 ; i < filters.length ; i++) {
            if (filters[i] != null) {
                info.putInt("f" + i, filters[i].ordinal());
            }
        }
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        writeRestorableToNBT(tagCompound);
        return super.write(tagCompound);
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//    }

    protected Pair<String, String> getSlotTag(String tag) {
        String[] split = StringUtils.split(tag, ":");
        final String slot = split[0];
        final String t = split.length > 1 ? split[1] : TAG_MAIN;
        return Pair.of(slot, t);
    }

    @Override
    public void syncToClient() {
//        markDirtyClient();
    }

    protected IIconChoice addColors(IGuiComponentRegistry registry, IIconChoice iconChoice, boolean negative) {
        return iconChoice
                .addImage(registry.image(GRAY_CROSS))
                .addImage(registry.image(negative ? NOT_COLOR_WHITE : COLOR_WHITE))
                .addImage(registry.image(negative ? NOT_COLOR_ORANGE : COLOR_ORANGE))
                .addImage(registry.image(negative ? NOT_COLOR_MAGENTA : COLOR_MAGENTA))
                .addImage(registry.image(negative ? NOT_COLOR_LIGHT_BLUE : COLOR_LIGHT_BLUE))
                .addImage(registry.image(negative ? NOT_COLOR_YELLOW : COLOR_YELLOW))
                .addImage(registry.image(negative ? NOT_COLOR_LIME : COLOR_LIME))
                .addImage(registry.image(negative ? NOT_COLOR_PINK : COLOR_PINK))
                .addImage(registry.image(negative ? NOT_COLOR_GRAY : COLOR_GRAY))
                .addImage(registry.image(negative ? NOT_COLOR_SILVER : COLOR_SILVER))
                .addImage(registry.image(negative ? NOT_COLOR_CYAN : COLOR_CYAN))
                .addImage(registry.image(negative ? NOT_COLOR_PURPLE : COLOR_PURPLE))
                .addImage(registry.image(negative ? NOT_COLOR_BLUE : COLOR_BLUE))
                .addImage(registry.image(negative ? NOT_COLOR_BROWN : COLOR_BROWN))
                .addImage(registry.image(negative ? NOT_COLOR_GREEN : COLOR_GREEN))
                .addImage(registry.image(negative ? NOT_COLOR_RED : COLOR_RED))
                .addImage(registry.image(negative ? NOT_COLOR_BLACK : COLOR_BLACK));
    }

    protected void addFilterChoice(IGuiComponentRegistry registry, IPanel panel, int i, boolean negative) {
        IIconChoice iconChoice = registry.iconChoice(i * 0.9 - .5, -.7, 1, 1);
        panel.add(addColors(registry, iconChoice, negative)
                .getter(player -> filters[i] == null ? 0 : filters[i].ordinal() + 1)
                .hitEvent(changeColor(filters, i))
        );
    }

    protected IEvent changeColor(DyeColor[] filters, int i) {
        return (component, player, entity, x, y) -> {
            if (filters[i] == null) {
                filters[i] = DyeColor.WHITE;
            } else if (filters[i] == DyeColor.BLACK) {
                filters[i] = null;
            } else {
                filters[i] = DyeColor.values()[filters[i].ordinal() + 1];
            }
            markDirtyClient();
        };
    }

    @Nonnull
    public LazyOptional<IItemHandler> getConnectedItemHandler(PartPos partPos) {
        BlockState state = MultipartHelper.getBlockState(world, pos, partPos.getSlot());
        if (state != null && state.getBlock() == getBlockState().getBlock()) {
            NodeOrientation orientation = state.get(ORIENTATION);
            Direction mainDirection = orientation.getMainDirection();
            TileEntity otherTe = world.getTileEntity(pos.offset(mainDirection));
            if (otherTe != null) {
                return otherTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, mainDirection.getOpposite());
            }
        }
        return LazyOptional.empty();
    }
}
