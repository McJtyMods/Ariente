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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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

    private static final VoxelShape AABB_DOWN_NW = Shapes.box(0.0+A, 0, 0.0+A,      0.5-A, T, 0.5-A);
    private static final VoxelShape AABB_DOWN_NE = Shapes.box(0.5+A, 0, 0.0+A,      1.0-A, T, 0.5-A);
    private static final VoxelShape AABB_DOWN_SW = Shapes.box(0.0+A, 0, 0.5+A,      0.5-A, T, 1.0-A);
    private static final VoxelShape AABB_DOWN_SE = Shapes.box(0.5+A, 0, 0.5+A,      1.0-A, T, 1.0-A);
    private static final VoxelShape AABB_UP_NW = Shapes.box(0.0+A, 1-T, 0.0+A,      0.5-A, 1, 0.5-A);
    private static final VoxelShape AABB_UP_NE = Shapes.box(0.5+A, 1-T, 0.0+A,      1.0-A, 1, 0.5-A);
    private static final VoxelShape AABB_UP_SW = Shapes.box(0.0+A, 1-T, 0.5+A,      0.5-A, 1, 1.0-A);
    private static final VoxelShape AABB_UP_SE = Shapes.box(0.5+A, 1-T, 0.5+A,      1.0-A, 1, 1.0-A);
    private static final VoxelShape AABB_NORTH_DE = Shapes.box(0.5+A, 0.0+A, 0,     1.0-A, 0.5-A, T);
    private static final VoxelShape AABB_NORTH_DW = Shapes.box(0.0+A, 0.0+A, 0,     0.5-A, 0.5-A, T);
    private static final VoxelShape AABB_NORTH_UE = Shapes.box(0.5+A, 0.5+A, 0,     1.0-A, 1.0-A, T);
    private static final VoxelShape AABB_NORTH_UW = Shapes.box(0.0+A, 0.5+A, 0,     0.5-A, 1.0-A, T);
    private static final VoxelShape AABB_SOUTH_DE = Shapes.box(0.5+A, 0.0+A, 1-T,   1.0-A, 0.5-A, 1);
    private static final VoxelShape AABB_SOUTH_DW = Shapes.box(0.0+A, 0.0+A, 1-T,   0.5-A, 0.5-A, 1);
    private static final VoxelShape AABB_SOUTH_UE = Shapes.box(0.5+A, 0.5+A, 1-T,   1.0-A, 1.0-A, 1);
    private static final VoxelShape AABB_SOUTH_UW = Shapes.box(0.0+A, 0.5+A, 1-T,   0.5-A, 1.0-A, 1);
    private static final VoxelShape AABB_WEST_DN = Shapes.box(0, 0.0+A, 0.0+A,      T, 0.5-A, 0.5-A);
    private static final VoxelShape AABB_WEST_DS = Shapes.box(0, 0.0+A, 0.5+A,      T, 0.5-A, 1.0-A);
    private static final VoxelShape AABB_WEST_UN = Shapes.box(0, 0.5+A, 0.0+A,      T, 1.0-A, 0.5-A);
    private static final VoxelShape AABB_WEST_US = Shapes.box(0, 0.5+A, 0.5+A,      T, 1.0-A, 1.0-A);
    private static final VoxelShape AABB_EAST_DN = Shapes.box(1-T, 0.0+A, 0.0+A,    1, 0.5-A, 0.5-A);
    private static final VoxelShape AABB_EAST_DS = Shapes.box(1-T, 0.0+A, 0.5+A,    1, 0.5-A, 1.0-A);
    private static final VoxelShape AABB_EAST_UN = Shapes.box(1-T, 0.5+A, 0.0+A,    1, 1.0-A, 0.5-A);
    private static final VoxelShape AABB_EAST_US = Shapes.box(1-T, 0.5+A, 0.5+A,    1, 1.0-A, 1.0-A);

    public AbstractNodeTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static BlockState getStateForPlacement(Block block, Direction facing, double hitX, double hitY, double hitZ) {
        NodeOrientation orientation = getOrientationFromPlacement(facing, hitX, hitY, hitZ);
        // Since this is a multipart we can use state that isn't convertable to metadata
        return block.defaultBlockState().setValue(ORIENTATION, orientation);
    }

    @Override
    public void setChanged() {
        // Make sure to mark the MultipartTE as dirty
        level.getBlockEntity(worldPosition).setChanged();
    }

    @Override
    public void markDirtyQuick() {
        // Make sure to mark the MultipartTE as dirty
        ((GenericTileEntity)level.getBlockEntity(worldPosition)).markDirtyQuick();
    }

    public DyeColor[] getFilters() {
        return filters;
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        AutoFieldTile.notifyField(world, pos);
    }

    protected void notifyField() {
        markDirtyClient();
        AutoFieldTile.notifyField(level, worldPosition);
    }

    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        AutoFieldTile.notifyField(world, pos);
    }

    public static VoxelShape getVoxelShape(BlockState state, BlockGetter world, BlockPos pos) {
        NodeOrientation orientation = state.getValue(ORIENTATION);
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
        return Shapes.empty();
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGuiEntity(level, worldPosition, player, state.getValue(ORIENTATION).getSlot().name(), 1.0);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onPartAdded(PartSlot slot, BlockState state, BlockEntity multipartTile) {
        this.level = multipartTile.getLevel();
        // @todo 1.18 this.worldPosition = multipartTile.getBlockPos();
        AutoFieldTile.notifyField(level, worldPosition);
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

    public void readRestorableFromNBT(CompoundTag tagCompound) {
        CompoundTag info = tagCompound.getCompound("Info");
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

    public void writeRestorableToNBT(CompoundTag tagCompound) {
        CompoundTag info = getOrCreateInfo(tagCompound);
        for (int i = 0 ; i < filters.length ; i++) {
            if (filters[i] != null) {
                info.putInt("f" + i, filters[i].ordinal());
            }
        }
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        writeRestorableToNBT(tagCompound);
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
        BlockState state = MultipartHelper.getBlockState(level, worldPosition, partPos.getSlot());
        if (state != null && state.getBlock() == getBlockState().getBlock()) {
            NodeOrientation orientation = state.getValue(ORIENTATION);
            Direction mainDirection = orientation.getMainDirection();
            BlockEntity otherTe = level.getBlockEntity(worldPosition.relative(mainDirection));
            if (otherTe != null) {
                return otherTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, mainDirection.getOpposite());
            }
        }
        return LazyOptional.empty();
    }
}
