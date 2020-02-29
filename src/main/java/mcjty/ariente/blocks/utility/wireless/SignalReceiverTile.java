package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.blocks.BlockProperties;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.ItemStackTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class SignalReceiverTile extends SignalChannelTileEntity implements ITickableTileEntity {

    public SignalReceiverTile() {
        super(Registration.SIGNAL_RECEIVER_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.signal_receiver")
                .infoExtendedParameter(ItemStackTools.intGetter("channel", -1))
                .topDriver(DRIVER)
                .tileEntitySupplier(SignalReceiverTile::new)
        ) {
            @Override
            protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
                super.fillStateContainer(builder);
                builder.add(BlockProperties.POWER);
            }
        };
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        SignalChannelTileEntity.onBlockActivatedInt(world, pos, player, hand);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        setRedstoneState(checkOutput());
    }

    public int checkOutput() {
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
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
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putInt("rs", powerOutput);
        return tagCompound;
    }
}
