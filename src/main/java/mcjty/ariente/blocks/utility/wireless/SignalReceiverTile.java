package mcjty.ariente.blocks.utility.wireless;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import mcjty.ariente.blocks.BlockProperties;
import mcjty.ariente.blocks.MountedPanelShapes;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.*;

public class SignalReceiverTile extends SignalChannelTileEntity {

    private static final MountedPanelShapes SHAPES = new MountedPanelShapes(2, 3);

    public SignalReceiverTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.SIGNAL_RECEIVER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header(), parameter("channel", (stack -> { var tag = stack.getOrDefault(mcjty.ariente.setup.Registration.MACHINE_DATA.get(), CustomData.EMPTY).copyTag().getCompoundOrEmpty("Info"); return Integer.toString(tag.contains("channel") ? tag.getIntOr("channel", 0) : -1); })))
                .topDriver(DRIVER)
                .tileEntitySupplier(SignalReceiverTile::new)
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

            @Override
            public boolean isSignalSource(BlockState state) {
                return true;
            }

            @Override
            public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
                if (world.getBlockEntity(pos) instanceof SignalReceiverTile receiver) {
                    return receiver.getRedstoneOutput(state, world, pos, side);
                }
                return 0;
            }

            @Override
            public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
                return getSignal(state, world, pos, side);
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        SignalChannelTileEntity.onBlockActivatedInt(level, worldPosition, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        checkStateServer();
    }

    protected void checkStateServer() {
        setRedstoneState(checkOutput());
    }

    public int checkOutput() {
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(getLevel());
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            if (ch != null) {
                return ch.getValue();
            }
        }
        return 0;
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        powerOutput = tagCompound.getIntOr("rs", 0);
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.saveAdditional(tagCompound, provider);
        tagCompound.putInt("rs", powerOutput);
    }
}
