package mcjty.ariente.blocks.utility;

import mcjty.ariente.api.AlarmType;
import mcjty.ariente.api.IAlarmTile;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class AlarmTile extends GenericTileEntity implements ITickableTileEntity, IAlarmTile {

    public static final EnumProperty<AlarmType> ALARM = EnumProperty.create("alarm", AlarmType.class, AlarmType.values());

    private AlarmType alarmType = AlarmType.SAFE;
    private int soundTicker = 0;

    @Override
    public void tick() {
        if (!world.isRemote) {
            if (alarmType == AlarmType.ALERT) {
                soundTicker--;
                if (soundTicker >= 0) {
                    return;
                }
                soundTicker = 60;
                world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), ModSounds.alarm, SoundCategory.BLOCKS, 0.1f, 1.0f);
            }
        }
    }

    @Override
    public AlarmType getAlarmType() {
        return alarmType;
    }

    @Override
    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType;
        markDirtyClient();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        AlarmType type = alarmType;
        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            AlarmType newType = alarmType;
            if (newType != type) {
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        }
    }

    @Override
    public BlockState getActualState(BlockState state) {
        return state.withProperty(ALARM, alarmType);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        alarmType = AlarmType.values()[tagCompound.getInteger("alarm")];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("alarm", alarmType.ordinal());
        return super.writeToNBT(tagCompound);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        switch (alarmType) {
            case DEAD:
                probeInfo.text(TextStyleClass.INFO + "City eliminated!");
                break;
            case SAFE:
                probeInfo.text(TextStyleClass.INFO + "City ok");
                break;
            case ALERT:
                probeInfo.text(TextStyleClass.INFO + "City Alert!");
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (isWorking()) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }
}
