package mcjty.ariente.blocks.utility;

import mcjty.ariente.api.AlarmType;
import mcjty.ariente.api.IAlarmTile;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.util.Constants;

public class AlarmTile extends GenericTileEntity implements ITickableTileEntity, IAlarmTile {

    public static final EnumProperty<AlarmType> ALARM = EnumProperty.create("alarm", AlarmType.class, AlarmType.values());

    private AlarmType alarmType = AlarmType.SAFE;
    private int soundTicker = 0;

    public AlarmTile() {
        super(Registration.ALARM_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.alarm")
                .tileEntitySupplier(AlarmTile::new)
        ) {
            @Override
            protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
                super.fillStateContainer(builder);
                builder.add(ALARM);
            }
        };
    }


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

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.withProperty(ALARM, alarmType);
//    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        alarmType = AlarmType.values()[tagCompound.getInt("alarm")];
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        tagCompound.putInt("alarm", alarmType.ordinal());
        return super.write(tagCompound);
    }

    // @todo 1.14
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        switch (alarmType) {
//            case DEAD:
//                probeInfo.text(TextStyleClass.INFO + "City eliminated!");
//                break;
//            case SAFE:
//                probeInfo.text(TextStyleClass.INFO + "City ok");
//                break;
//            case ALERT:
//                probeInfo.text(TextStyleClass.INFO + "City Alert!");
//                break;
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
////        if (isWorking()) {
////            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
////        }
//    }
}
