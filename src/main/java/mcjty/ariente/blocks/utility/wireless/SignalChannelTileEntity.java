package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.api.ISignalChannel;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class SignalChannelTileEntity extends GenericTileEntity implements ICityEquipment, ISignalChannel {

    protected int channel = -1;
    protected int powerOutput = 0;

    private int desiredChannel; // Only used for city AI

    public SignalChannelTileEntity(BlockEntityType<?> type) {
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
    public int getRedstoneOutput(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
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
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    public void readRestorableFromNBT(CompoundTag tagCompound) {
        CompoundTag info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            channel = info.getInt("channel");
            desiredChannel = info.getInt("desired");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        writeRestorableToNBT(tagCompound);
        super.saveAdditional(tagCompound);
    }

    public void writeRestorableToNBT(CompoundTag tagCompound) {
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("channel", channel);
        info.putInt("desired", desiredChannel);
    }

    public static boolean onBlockActivatedInt(Level world, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(SignalChannelTileEntity.isRedstoneChannelItem(stack.getItem())) {
            setChannel(world, pos, player, stack);
        }
        return true;
    }

    public static void setChannel(Level world, BlockPos pos, Player player, ItemStack stack) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof SignalChannelTileEntity) {
            if(!world.isClientSide) {
                SignalChannelTileEntity rcte = (SignalChannelTileEntity)te;
                CompoundTag tagCompound = stack.getOrCreateTag();
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
                Logging.message(player, ChatFormatting.YELLOW + "Channel set to " + channel + "!");
            }
        }
    }

    /**
     * Returns the signal strength at one input of the block
     */
    protected int getInputStrength(Level world, BlockPos pos, Direction side) {
        int power = world.getSignal(pos.relative(side), side);
        if (power < 15) {
            // Check if there is no redstone wire there. If there is a 'bend' in the redstone wire it is
            // not detected with world.getRedstonePower().
            // Not exactly pretty, but it's how vanilla redstone repeaters do it.
            BlockState blockState = world.getBlockState(pos.relative(side));
            Block b = blockState.getBlock();
            if (b == Block.REDSTONE_WIRE) {
                power = Math.max(power, blockState.getValue(RedstoneWireBlock.POWER));
            }
        }

        return power;
    }

    @Override
    public void checkRedstone(Level world, BlockPos pos) {
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
    public void setup(ICityAI cityAI, Level world, boolean firstTime) {

    }
}
