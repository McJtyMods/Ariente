package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.blocks.BlockProperties;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.NBTTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.*;

public class SignalReceiverTile extends SignalChannelTileEntity implements ITickableTileEntity {

    private static final VoxelShape BLOCK_AABB = VoxelShapes.box(1.0D/16.0, 1.0D/16.0, 15.0D/16.0, 15.0D/16.0, 15.0D/16.0, 1.0D);

    public SignalReceiverTile() {
        super(Registration.SIGNAL_RECEIVER_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header(), parameter("channel", NBTTools.intGetter("channel", -1)))
                .topDriver(DRIVER)
                .tileEntitySupplier(SignalReceiverTile::new)
        ) {
            @Override
            protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(BlockProperties.POWER);
            }

            @Override
            public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
                return BLOCK_AABB;
            }
        };
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        SignalChannelTileEntity.onBlockActivatedInt(level, worldPosition, player, hand);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            checkStateServer();
        }
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getInt("rs");
    }

    @Override
    public CompoundNBT save(CompoundNBT tagCompound) {
        super.save(tagCompound);
        tagCompound.putInt("rs", powerOutput);
        return tagCompound;
    }
}
