package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.blocks.BlockProperties;
import mcjty.ariente.blocks.MountedPanelShapes;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.NBTTools;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.*;

public class SignalTransmitterTile extends SignalChannelTileEntity {

    private int prevIn = -1;
    private boolean publishPending = true;

    private static final MountedPanelShapes SHAPES = new MountedPanelShapes(2, 3);

    public SignalTransmitterTile(BlockPos pos, BlockState state) {
        super(Registration.SIGNAL_TRANSMITTER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header(), parameter("channel", NBTTools.intGetter("channel", -1)))
                .topDriver(DRIVER)
                .tileEntitySupplier(SignalTransmitterTile::new)
        ) {
            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(BlockProperties.POWER);
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return SHAPES.get(state.getValue(BlockStateProperties.FACING));
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        SignalChannelTileEntity.onBlockActivatedInt(level, worldPosition, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setChannel(int channel) {
        prevIn = -1;
        super.setChannel(channel);
        update();
    }

    @Override
    public void tickServer() {
        checkRedstone(level, worldPosition);
    }

    public void update() {
        if (level == null || level.isClientSide) {
            return;
        }

        setVisualState(BlockProperties.POWER, powerLevel > 0);
        if (channel == -1) {
            return;
        }

        if (publishPending || powerLevel != prevIn) {
            publishPending = false;
            prevIn = powerLevel;
            markDirtyClient();
            RedstoneChannels channels = RedstoneChannels.getChannels(getLevel());
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(powerLevel);
            channels.save();
        }
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        prevIn = tagCompound.getInt("prevIn");
        publishPending = true;
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        tagCompound.putInt("prevIn", prevIn);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isClientSide) {
            // @todo double check
            update();
        }
    }

    @Override
    public void checkRedstone(Level world, BlockPos pos) {
        super.checkRedstone(world, pos);
        update();
    }
}
