package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.security.IKeyCardSlot;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.varia.Logging;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Set;

public class WirelessLockTile extends SignalChannelTileEntity {

    private boolean locked = false;
    private int prevIn = -1;

    public WirelessLockTile() {
    }

    @Override
    public void setChannel(int channel) {
        super.setChannel(channel);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean locked = isLocked();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            boolean newLocked = isLocked();
            if (newLocked != locked) {
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (locked == this.locked) {
            return;
        }
        if (channel != -1) {
            this.locked = locked;
            RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(locked ? 15 : 0);
        }
        markDirtyClient();
    }

    public static boolean onBlockActivatedWithToggle(World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (SignalChannelTileEntity.isRedstoneChannelItem(stack.getItem())) {
            setChannel(world, pos, player, stack);
        } else {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof WirelessLockTile) {
                world.playSound(null, pos, ModSounds.buzzOk, SoundCategory.BLOCKS, 1.0f, 1.0f);
                ((WirelessLockTile) te).toggleLock();
            }
        }
        return true;
    }

    public void toggleLock() {
        if (!world.isRemote) {
            setLocked(!locked);
        }
    }


    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(POWER, !locked);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getInteger("prevIn");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("prevIn", prevIn);
        return tagCompound;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        locked = tagCompound.getBoolean("locked");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setBoolean("locked", locked);
    }

}
