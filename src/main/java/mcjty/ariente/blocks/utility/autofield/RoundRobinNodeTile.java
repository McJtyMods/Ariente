package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.*;

public class RoundRobinNodeTile extends GenericTileEntity {

    public static final PropertyEnum<NodeOrientation> ORIENTATION = PropertyEnum.create("orientation", NodeOrientation.class, NodeOrientation.values());

    public static BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        NodeOrientation orientation = getOrientationFromPlacement(facing, hitX, hitY, hitZ);
        // Since this is a multipart we can use state that isn't convertable to metadata
        return ModBlocks.roundRobinNode.getDefaultState().withProperty(ORIENTATION, orientation);
    }

    private int index = 0;

    private static final float T = 0.1f;
    private static final float A = 0.05F;

    private static final AxisAlignedBB AABB_DOWN_NW = new AxisAlignedBB(0.0+A, 0, 0.0+A,      0.5-A, T, 0.5-A);
    private static final AxisAlignedBB AABB_DOWN_NE = new AxisAlignedBB(0.5+A, 0, 0.0+A,      1.0-A, T, 0.5-A);
    private static final AxisAlignedBB AABB_DOWN_SW = new AxisAlignedBB(0.0+A, 0, 0.5+A,      0.5-A, T, 1.0-A);
    private static final AxisAlignedBB AABB_DOWN_SE = new AxisAlignedBB(0.5+A, 0, 0.5+A,      1.0-A, T, 1.0-A);
    private static final AxisAlignedBB AABB_UP_NW = new AxisAlignedBB(0.0+A, 1-T, 0.0+A,      0.5-A, 1, 0.5-A);
    private static final AxisAlignedBB AABB_UP_NE = new AxisAlignedBB(0.5+A, 1-T, 0.0+A,      1.0-A, 1, 0.5-A);
    private static final AxisAlignedBB AABB_UP_SW = new AxisAlignedBB(0.0+A, 1-T, 0.5+A,      0.5-A, 1, 1.0-A);
    private static final AxisAlignedBB AABB_UP_SE = new AxisAlignedBB(0.5+A, 1-T, 0.5+A,      1.0-A, 1, 1.0-A);
    private static final AxisAlignedBB AABB_NORTH_DE = new AxisAlignedBB(0.5+A, 0.0+A, 0,     1.0-A, 0.5-A, T);
    private static final AxisAlignedBB AABB_NORTH_DW = new AxisAlignedBB(0.0+A, 0.0+A, 0,     0.5-A, 0.5-A, T);
    private static final AxisAlignedBB AABB_NORTH_UE = new AxisAlignedBB(0.5+A, 0.5+A, 0,     1.0-A, 1.0-A, T);
    private static final AxisAlignedBB AABB_NORTH_UW = new AxisAlignedBB(0.0+A, 0.5+A, 0,     0.5-A, 1.0-A, T);
    private static final AxisAlignedBB AABB_SOUTH_DE = new AxisAlignedBB(0.5+A, 0.0+A, 1-T,   1.0-A, 0.5-A, 1);
    private static final AxisAlignedBB AABB_SOUTH_DW = new AxisAlignedBB(0.0+A, 0.0+A, 1-T,   0.5-A, 0.5-A, 1);
    private static final AxisAlignedBB AABB_SOUTH_UE = new AxisAlignedBB(0.5+A, 0.5+A, 1-T,   1.0-A, 1.0-A, 1);
    private static final AxisAlignedBB AABB_SOUTH_UW = new AxisAlignedBB(0.0+A, 0.5+A, 1-T,   0.5-A, 1.0-A, 1);
    private static final AxisAlignedBB AABB_WEST_DN = new AxisAlignedBB(0, 0.0+A, 0.0+A,      T, 0.5-A, 0.5-A);
    private static final AxisAlignedBB AABB_WEST_DS = new AxisAlignedBB(0, 0.0+A, 0.5+A,      T, 0.5-A, 1.0-A);
    private static final AxisAlignedBB AABB_WEST_UN = new AxisAlignedBB(0, 0.5+A, 0.0+A,      T, 1.0-A, 0.5-A);
    private static final AxisAlignedBB AABB_WEST_US = new AxisAlignedBB(0, 0.5+A, 0.5+A,      T, 1.0-A, 1.0-A);
    private static final AxisAlignedBB AABB_EAST_DN = new AxisAlignedBB(1-T, 0.0+A, 0.0+A,    1, 0.5-A, 0.5-A);
    private static final AxisAlignedBB AABB_EAST_DS = new AxisAlignedBB(1-T, 0.0+A, 0.5+A,    1, 0.5-A, 1.0-A);
    private static final AxisAlignedBB AABB_EAST_UN = new AxisAlignedBB(1-T, 0.5+A, 0.0+A,    1, 1.0-A, 0.5-A);
    private static final AxisAlignedBB AABB_EAST_US = new AxisAlignedBB(1-T, 0.5+A, 0.5+A,    1, 1.0-A, 1.0-A);

    @Override
    public Block getBlockType() {
        return ModBlocks.roundRobinNode;
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

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        AutoFieldTile.notifyField(world, pos);
    }

    @Override
    public void onBlockBreak(World world, BlockPos pos, BlockState state) {
        AutoFieldTile.notifyField(world, pos);
    }

    public static AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess world, BlockPos pos) {
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
        return Block.NULL_AABB;
    }

    @Override
    public boolean onBlockActivated(BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        Ariente.guiHandler.openHoloGuiEntity(world, pos, player, state.getValue(ORIENTATION).getSlot().name(), 1.0);
        return true;
    }

    @Override
    public void onPartAdded(PartSlot slot, BlockState state, TileEntity multipartTile) {
        this.world = multipartTile.getWorld();
        this.pos = multipartTile.getPos();
        AutoFieldTile.notifyField(world, pos);
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
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        index = tagCompound.getInteger("index");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("index", index);
        return super.writeToNBT(tagCompound);
    }

    public int fetchIndex() {
        markDirty();
        return index++;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
    }
}
