package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.ai.CityAI;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.ICityEquipment;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SignalChannelTileEntity extends GenericTileEntity implements ICityEquipment {

    public static final PropertyBool POWER = PropertyBool.create("power");

    protected int channel = -1;
    protected int powerOutput = 0;

    private int desiredChannel; // Only used for city AI

    static boolean isRedstoneChannelItem(Item item) {
        return (item instanceof ItemBlock &&
                (((ItemBlock)item).getBlock() == ModBlocks.signalTransmitterBlock
                        || ((ItemBlock)item).getBlock() == ModBlocks.signalReceiverBlock
                        || ((ItemBlock)item).getBlock() == ModBlocks.wirelessLockBlock
                        || ((ItemBlock)item).getBlock() == ModBlocks.wirelessButtonBlock));
    }

//    @Override
//    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
//        boolean powered = getPowerOutput() > 0;
//
//        super.onDataPacket(net, packet);
//
//        if (world.isRemote) {
//            // If needed send a render update.
//            boolean newPowered = getPowerOutput() > 0;
//            if (newPowered != powered) {
//                world.markBlockRangeForRenderUpdate(pos, pos);
//            }
//        }
//    }


    public int getPowerOutput() {
        return powerOutput;
    }

    protected void setRedstoneState(int newout) {
        if (powerOutput == newout) {
            return;
        }
        powerOutput = newout;
        markDirty();
        IBlockState state = world.getBlockState(pos);
        EnumFacing outputSide = getFacing(state).getOpposite();
        getWorld().neighborChanged(this.pos.offset(outputSide), this.getBlockType(), this.pos);
        markDirtyClient();
    }

    private EnumFacing getFacing(IBlockState state) {
        return BaseBlock.getFrontDirection(BaseBlock.RotationType.ROTATION, state);
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(POWER, getPowerOutput() > 0);
    }

    @Override
    public int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side == getFacing(state)) {
            return getPowerOutput();
        } else {
            return 0;
        }
    }

    public int getChannel(boolean initialize) {
        if(initialize && channel == -1) {
            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
            setChannel(redstoneChannels.newChannel());
            redstoneChannels.save();
        }
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
        desiredChannel = tagCompound.getInteger("desired");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
        tagCompound.setInteger("desired", desiredChannel);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        probeInfo.text(TextFormatting.GREEN + "Channel: " + getChannel(false));
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
        currenttip.add(TextFormatting.GREEN + "Channel: " + getChannel(false));
    }

    public static boolean onBlockActivated(World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if(SignalChannelTileEntity.isRedstoneChannelItem(stack.getItem())) {
            setChannel(world, pos, player, stack);
        }
        return true;
    }

    public static void setChannel(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SignalChannelTileEntity) {
            if(!world.isRemote) {
                SignalChannelTileEntity rcte = (SignalChannelTileEntity)te;
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    stack.setTagCompound(tagCompound);
                }
                int channel;
                if(!player.isSneaking()) {
                    channel = rcte.getChannel(true);
                    tagCompound.setInteger("channel", channel);
                } else {
                    if (tagCompound.hasKey("channel")) {
                        channel = tagCompound.getInteger("channel");
                    } else {
                        channel = -1;
                    }
                    if(channel == -1) {
                        RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                        channel = redstoneChannels.newChannel();
                        redstoneChannels.save();
                        tagCompound.setInteger("channel", channel);
                    }
                    rcte.setChannel(channel);
                }
                Logging.message(player, TextFormatting.YELLOW + "Channel set to " + channel + "!");
            }
        }
    }

    /**
     * Returns the signal strength at one input of the block
     */
    protected int getInputStrength(World world, BlockPos pos, EnumFacing side) {
        int power = world.getRedstonePower(pos.offset(side), side);
        if (power < 15) {
            // Check if there is no redstone wire there. If there is a 'bend' in the redstone wire it is
            // not detected with world.getRedstonePower().
            // Not exactly pretty, but it's how vanilla redstone repeaters do it.
            IBlockState blockState = world.getBlockState(pos.offset(side));
            Block b = blockState.getBlock();
            if (b == Blocks.REDSTONE_WIRE) {
                power = Math.max(power, blockState.getValue(BlockRedstoneWire.POWER));
            }
        }

        return power;
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        EnumFacing inputSide = getFacing(world.getBlockState(pos)).getOpposite();
        int power = getInputStrength(world, pos, inputSide);
        setPowerInput(power);
    }

    public int getDesiredChannel() {
        return desiredChannel;
    }

    public void setDesiredChannel(int desiredChannel) {
        this.desiredChannel = desiredChannel;
        markDirtyQuick();
    }

    @Nullable
    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();
        data.put("channel", channel);
        return data;
    }

    @Override
    public void load(Map<String, Object> data) {
        if (data.get("channel") instanceof Integer) {
            desiredChannel = (Integer) data.get("channel");
        }
    }

    @Override
    public void setup(CityAI cityAI, World world, boolean firstTime) {

    }
}
