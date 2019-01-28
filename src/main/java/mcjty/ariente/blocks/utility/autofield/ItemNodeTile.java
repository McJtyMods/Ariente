package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static mcjty.ariente.blocks.ModBlocks.*;
import static mcjty.ariente.blocks.utility.autofield.NodeOrientation.*;

public class ItemNodeTile extends GenericTileEntity {

    public static final PropertyEnum<NodeOrientation> ORIENTATION = PropertyEnum.create("orientation", NodeOrientation.class, NodeOrientation.values());

    private NodeOrientation orientation;

    public static IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        NodeOrientation orientation = getOrientationFromPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
        // orientation doesn't get saved to metadata, but it doesn't need to. It only needs to be available until onLoad() runs.
        return ModBlocks.itemNode.getDefaultState().withProperty(ORIENTATION, orientation);
    }

    public static AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        NodeOrientation orientation  = state.getValue(ORIENTATION);
        // @todo bounding box should differ based on which part we are
        EnumFacing facing = orientation.getMainDirection();
        switch (facing) {
            case UP:
                return LIGHT_BLOCK_DOWN;
            case DOWN:
                return LIGHT_BLOCK_UP;
            case SOUTH:
                return LIGHT_BLOCK_NORTH;
            case NORTH:
                return LIGHT_BLOCK_SOUTH;
            case EAST:
                return LIGHT_BLOCK_WEST;
            case WEST:
                return LIGHT_BLOCK_EAST;
        }
        return Block.NULL_AABB;
    }


    public static NodeOrientation getOrientationFromPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {

        // @todo remove these!

        float dx = Math.abs(0.5f - hitX);
        float dy = Math.abs(0.5f - hitY);
        float dz = Math.abs(0.5f - hitZ);

        side = side.getOpposite();
//        System.out.println("LogicSlabBlock.getStateForPlacement");
//        System.out.println("  side = " + side);
        NodeOrientation facing;
        switch (side) {
            case DOWN:
                if (dx < dz) {
                    facing = hitZ < 0.5 ? DOWN_NW : DOWN_SW;
                } else {
                    facing = hitX < 0.5 ? DOWN_NE : DOWN_SE;
                }
                break;
            case UP:
                if (dx < dz) {
                    facing = hitZ < 0.5 ? UP_NW : UP_SW;
                } else {
                    facing = hitX < 0.5 ? UP_NE : UP_SE;
                }
                break;
            case NORTH:
                if (dx < dy) {
                    facing = hitY < 0.5 ? NORTH_DW : NORTH_DE;
                } else {
                    facing = hitX < 0.5 ? NORTH_UW : NORTH_UE;
                }
                break;
            case SOUTH:
                if (dx < dy) {
                    facing = hitY < 0.5 ? SOUTH_DW : SOUTH_DE;
                } else {
                    facing = hitX < 0.5 ? SOUTH_UW : SOUTH_UE;
                }
                break;
            case WEST:
                if (dy < dz) {
                    facing = hitZ < 0.5 ? WEST_DN : WEST_DS;
                } else {
                    facing = hitY < 0.5 ? WEST_UN : WEST_US;
                }
                break;
            case EAST:
                if (dy < dz) {
                    facing = hitZ < 0.5 ? EAST_DN : EAST_DS;
                } else {
                    facing = hitY < 0.5 ? EAST_UN : EAST_US;
                }
                break;
            default:
                facing = DOWN_NW;
                break;
        }
        return facing;
    }


    public void setOrientation(NodeOrientation orientation) {
        if(orientation != this.orientation) {
            this.orientation = orientation;
            markDirtyClient();
        }
    }

    public NodeOrientation getOrientation() {
        return orientation;
    }

    @Override
    public void onLoad() {
        if(orientation == null) {
            IBlockState state = getWorld().getBlockState(getPos());
            if(state.getBlock() == ModBlocks.itemNode) {
                setOrientation(state.getValue(ORIENTATION));
            }
        }
        super.onLoad();
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        orientation = NodeOrientation.VALUES[tagCompound.getInteger("orientation")];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("orientation", orientation.ordinal());
        return super.writeToNBT(tagCompound);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
    }
}
