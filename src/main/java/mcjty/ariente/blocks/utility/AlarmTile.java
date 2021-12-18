package mcjty.ariente.blocks.utility;

import mcjty.ariente.api.AlarmType;
import mcjty.ariente.api.IAlarmTile;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
// @todo 1.18 import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class AlarmTile extends GenericTileEntity implements /* @todo 1.18 ITickableTileEntity, */ IAlarmTile {

    public static final EnumProperty<AlarmType> ALARM = EnumProperty.create("alarm", AlarmType.class, AlarmType.values());

    private static final VoxelShape BLOCK_AABB = Shapes.box(1.0D/16.0, 1.0D/16.0, 15.0D/16.0, 15.0D/16.0, 15.0D/16.0, 1.0D);

    private AlarmType alarmType = AlarmType.SAFE;
    private int soundTicker = 0;

    public AlarmTile(BlockPos pos, BlockState state) {
        super(Registration.ALARM_TILE.get(), pos, state);
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
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(ALARM);
            }

            @SuppressWarnings({"NullableProblems", "deprecation"})
            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return BLOCK_AABB;
            }
        };
    }


    //@Override
    public void tickServer() {
        if (alarmType == AlarmType.ALERT) {
            soundTicker--;
            if (soundTicker >= 0) {
                return;
            }
            soundTicker = 60;
            level.playSound(null, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), ModSounds.alarm, SoundSource.BLOCKS, 0.1f, 1.0f);
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        AlarmType type = alarmType;
        super.onDataPacket(net, packet);

        if (level.isClientSide) {
            // If needed send a render update.
            AlarmType newType = alarmType;
            if (newType != type) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS + Block.UPDATE_NEIGHBORS);
            }
        }
    }

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.with(ALARM, alarmType);
//    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        alarmType = AlarmType.values()[tagCompound.getInt("alarm")];
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        tagCompound.putInt("alarm", alarmType.ordinal());
        super.saveAdditional(tagCompound);
    }
}
