package mcjty.ariente.blocks.utility;

import mcjty.ariente.api.AlarmType;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class AlarmTile extends GenericTileEntity implements ITickable {

    public static final PropertyEnum<AlarmType> ALARM = PropertyEnum.create("alarm", AlarmType.class, AlarmType.values());

    private AlarmType alarmType = AlarmType.SAFE;
    private int soundTicker = 0;

    @Override
    public void update() {
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

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType;
        markDirtyClient();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        AlarmType type = alarmType;
        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            AlarmType newType = alarmType;
            if (newType != type) {
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
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
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
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
