package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

import static mcjty.ariente.blocks.utility.autofield.AbstractNodeTile.ORIENTATION;
import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.*;

public class RoundRobinNodeTile extends GenericTileEntity {

    private int index = 0;

    private static final float T = 0.1f;
    private static final float A = 0.05F;

    private static final AABB AABB_DOWN_NW = new AABB(0.0+A, 0, 0.0+A,      0.5-A, T, 0.5-A);
    private static final AABB AABB_DOWN_NE = new AABB(0.5+A, 0, 0.0+A,      1.0-A, T, 0.5-A);
    private static final AABB AABB_DOWN_SW = new AABB(0.0+A, 0, 0.5+A,      0.5-A, T, 1.0-A);
    private static final AABB AABB_DOWN_SE = new AABB(0.5+A, 0, 0.5+A,      1.0-A, T, 1.0-A);
    private static final AABB AABB_UP_NW = new AABB(0.0+A, 1-T, 0.0+A,      0.5-A, 1, 0.5-A);
    private static final AABB AABB_UP_NE = new AABB(0.5+A, 1-T, 0.0+A,      1.0-A, 1, 0.5-A);
    private static final AABB AABB_UP_SW = new AABB(0.0+A, 1-T, 0.5+A,      0.5-A, 1, 1.0-A);
    private static final AABB AABB_UP_SE = new AABB(0.5+A, 1-T, 0.5+A,      1.0-A, 1, 1.0-A);
    private static final AABB AABB_NORTH_DE = new AABB(0.5+A, 0.0+A, 0,     1.0-A, 0.5-A, T);
    private static final AABB AABB_NORTH_DW = new AABB(0.0+A, 0.0+A, 0,     0.5-A, 0.5-A, T);
    private static final AABB AABB_NORTH_UE = new AABB(0.5+A, 0.5+A, 0,     1.0-A, 1.0-A, T);
    private static final AABB AABB_NORTH_UW = new AABB(0.0+A, 0.5+A, 0,     0.5-A, 1.0-A, T);
    private static final AABB AABB_SOUTH_DE = new AABB(0.5+A, 0.0+A, 1-T,   1.0-A, 0.5-A, 1);
    private static final AABB AABB_SOUTH_DW = new AABB(0.0+A, 0.0+A, 1-T,   0.5-A, 0.5-A, 1);
    private static final AABB AABB_SOUTH_UE = new AABB(0.5+A, 0.5+A, 1-T,   1.0-A, 1.0-A, 1);
    private static final AABB AABB_SOUTH_UW = new AABB(0.0+A, 0.5+A, 1-T,   0.5-A, 1.0-A, 1);
    private static final AABB AABB_WEST_DN = new AABB(0, 0.0+A, 0.0+A,      T, 0.5-A, 0.5-A);
    private static final AABB AABB_WEST_DS = new AABB(0, 0.0+A, 0.5+A,      T, 0.5-A, 1.0-A);
    private static final AABB AABB_WEST_UN = new AABB(0, 0.5+A, 0.0+A,      T, 1.0-A, 0.5-A);
    private static final AABB AABB_WEST_US = new AABB(0, 0.5+A, 0.5+A,      T, 1.0-A, 1.0-A);
    private static final AABB AABB_EAST_DN = new AABB(1-T, 0.0+A, 0.0+A,    1, 0.5-A, 0.5-A);
    private static final AABB AABB_EAST_DS = new AABB(1-T, 0.0+A, 0.5+A,    1, 0.5-A, 1.0-A);
    private static final AABB AABB_EAST_UN = new AABB(1-T, 0.5+A, 0.0+A,    1, 1.0-A, 0.5-A);
    private static final AABB AABB_EAST_US = new AABB(1-T, 0.5+A, 0.5+A,    1, 1.0-A, 1.0-A);

    public RoundRobinNodeTile() {
        super(Registration.ROUND_ROBIN_TILE.get());
    }

    // @todo 1.14
//    @Override
//    public Block getBlockType() {
//        return ModBlocks.roundRobinNode;
//    }

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

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        AutoFieldTile.notifyField(world, pos);
    }

    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        AutoFieldTile.notifyField(world, pos);
    }

    public static AABB getBoundingBox(BlockState state, Level world, BlockPos pos) {
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
        return null; // @todo 1.14 Block.NULL_AABB;
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGuiEntity(level, worldPosition, player, state.getValue(ORIENTATION).getSlot().name(), 1.0);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onPartAdded(PartSlot slot, BlockState state, BlockEntity multipartTile) {
        this.level = multipartTile.getLevel();
        this.worldPosition = multipartTile.getBlockPos();
        AutoFieldTile.notifyField(level, worldPosition);
    }

    public static NodeOrientation getOrientationFromPlacement(Direction side, float hitX, float hitY, float hitZ) {
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

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        index = tagCompound.getInt("index");
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        tagCompound.putInt("index", index);
    }

    public int fetchIndex() {
        setChanged();
        return index++;
    }
}
