package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.api.ISignalChannel;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class SignalChannelTileEntity extends GenericTileEntity implements ICityEquipment, ISignalChannel {

    protected int channel = -1;
    protected int powerOutput = 0;

    private int desiredChannel; // Only used for city AI

    public SignalChannelTileEntity(TileEntityType<?> type) {
        super(type);
    }

    static boolean isRedstoneChannelItem(Item item) {
        return (item instanceof BlockItem &&
                (((BlockItem)item).getBlock() == Registration.SIGNAL_TRANSMITTER.get()
                        || ((BlockItem)item).getBlock() == Registration.SIGNAL_RECEIVER.get()
                        || ((BlockItem)item).getBlock() == Registration.WIRELESS_LOCK.get()
                        || ((BlockItem)item).getBlock() == Registration.WIRELESS_BUTTON.get()));
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
        setChanged();
        BlockState state = level.getBlockState(worldPosition);
        Direction outputSide = getFacing(state).getOpposite();
        getLevel().neighborChanged(this.worldPosition.relative(outputSide), this.getBlockState().getBlock(), this.worldPosition);
        markDirtyClient();
    }

    private Direction getFacing(BlockState state) {
        return BaseBlock.getFrontDirection(RotationType.ROTATION, state);
    }

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.with(POWER, getPowerOutput() > 0);
//    }

    @Override
    public int getRedstoneOutput(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        if (side == getFacing(state)) {
            return getPowerOutput();
        } else {
            return 0;
        }
    }

    @Override
    public int getChannel(boolean initialize) {
        if(initialize && channel == -1) {
            RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(level);
            setChannel(redstoneChannels.newChannel());
        }
        return channel;
    }

    @Override
    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    @Override
    public void load(CompoundNBT tagCompound) {
        super.load(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        CompoundNBT info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            channel = info.getInt("channel");
            desiredChannel = info.getInt("desired");
        }
    }

    @Override
    public void saveAdditional(CompoundNBT tagCompound) {
        writeRestorableToNBT(tagCompound);
        super.saveAdditional(tagCompound);
    }

    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("channel", channel);
        info.putInt("desired", desiredChannel);
    }

    public static boolean onBlockActivatedInt(World world, BlockPos pos, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(SignalChannelTileEntity.isRedstoneChannelItem(stack.getItem())) {
            setChannel(world, pos, player, stack);
        }
        return true;
    }

    public static void setChannel(World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof SignalChannelTileEntity) {
            if(!world.isClientSide) {
                SignalChannelTileEntity rcte = (SignalChannelTileEntity)te;
                CompoundNBT tagCompound = stack.getOrCreateTag();
                int channel;
                if(!player.isShiftKeyDown()) {
                    channel = rcte.getChannel(true);
                    tagCompound.putInt("channel", channel);
                } else {
                    if (tagCompound.contains("channel")) {
                        channel = tagCompound.getInt("channel");
                    } else {
                        channel = -1;
                    }
                    if(channel == -1) {
                        RedstoneChannels redstoneChannels = RedstoneChannels.getChannels(world);
                        channel = redstoneChannels.newChannel();
                        tagCompound.putInt("channel", channel);
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
    protected int getInputStrength(World world, BlockPos pos, Direction side) {
        int power = world.getSignal(pos.relative(side), side);
        if (power < 15) {
            // Check if there is no redstone wire there. If there is a 'bend' in the redstone wire it is
            // not detected with world.getRedstonePower().
            // Not exactly pretty, but it's how vanilla redstone repeaters do it.
            BlockState blockState = world.getBlockState(pos.relative(side));
            Block b = blockState.getBlock();
            if (b == Blocks.REDSTONE_WIRE) {
                power = Math.max(power, blockState.getValue(RedstoneWireBlock.POWER));
            }
        }

        return power;
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        Direction inputSide = getFacing(world.getBlockState(pos)).getOpposite();
        int power = getInputStrength(world, pos, inputSide);
        setPowerInput(power);
    }

    @Override
    public int getDesiredChannel() {
        return desiredChannel;
    }

    @Override
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
    public void setup(ICityAI cityAI, World world, boolean firstTime) {

    }
}
