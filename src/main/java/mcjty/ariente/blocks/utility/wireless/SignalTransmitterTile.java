package mcjty.ariente.blocks.utility.wireless;

import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalTransmitterTile extends SignalChannelTileEntity {

    private int prevIn = -1;

    public SignalTransmitterTile() {
    }

    @Override
    public void setChannel(int channel) {
        super.setChannel(channel);
        update();
    }

    public void update() {
        if (getWorld().isRemote) {
            return;
        }

        if (channel == -1) {
            return;
        }

        if (powerLevel != prevIn) {
            prevIn = powerLevel;
            markDirtyClient();
            RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(powerLevel);
            channels.save();
        }
    }

    @Override
    public BlockState getActualState(BlockState state) {
        return state.withProperty(POWER, powerLevel > 0);
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getInteger("prevIn");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("prevIn", prevIn);
        return tagCompound;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isRemote) {
            // @todo double check
            update();
        }
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        super.checkRedstone(world, pos);
        update();
    }
}
