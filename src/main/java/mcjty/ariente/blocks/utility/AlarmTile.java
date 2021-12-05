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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.Constants;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class AlarmTile extends GenericTileEntity implements ITickableTileEntity, IAlarmTile {

    public static final EnumProperty<AlarmType> ALARM = EnumProperty.create("alarm", AlarmType.class, AlarmType.values());

    private static final VoxelShape BLOCK_AABB = VoxelShapes.box(1.0D/16.0, 1.0D/16.0, 15.0D/16.0, 15.0D/16.0, 15.0D/16.0, 1.0D);

    private AlarmType alarmType = AlarmType.SAFE;
    private int soundTicker = 0;

    public AlarmTile() {
        super(Registration.ALARM_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(AlarmTile::new)
        ) {
            @Override
            protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(ALARM);
            }

            @SuppressWarnings({"NullableProblems", "deprecation"})
            @Override
            public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
                return BLOCK_AABB;
            }
        };
    }


    @Override
    public void tick() {
        if (!level.isClientSide) {
            if (alarmType == AlarmType.ALERT) {
                soundTicker--;
                if (soundTicker >= 0) {
                    return;
                }
                soundTicker = 60;
                level.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), ModSounds.alarm, SoundCategory.BLOCKS, 0.1f, 1.0f);
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

        if (level.isClientSide) {
            // If needed send a render update.
            AlarmType newType = alarmType;
            if (newType != type) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        }
    }

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.with(ALARM, alarmType);
//    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        alarmType = AlarmType.values()[tagCompound.getInt("alarm")];
    }

    @Override
    public CompoundNBT save(CompoundNBT tagCompound) {
        tagCompound.putInt("alarm", alarmType.ordinal());
        return super.save(tagCompound);
    }
}
