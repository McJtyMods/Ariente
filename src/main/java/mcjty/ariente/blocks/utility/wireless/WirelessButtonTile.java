package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.blocks.BlockProperties;
import mcjty.ariente.blocks.MountedPanelShapes;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.NBTTools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.Level;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.*;

public class WirelessButtonTile extends SignalChannelTileEntity {

    private boolean locked = false;
    private int prevIn = -1;

    private static final MountedPanelShapes SHAPES = new MountedPanelShapes(2, 3);

    public WirelessButtonTile(BlockPos pos, BlockState state) {
        super(Registration.WIRELESS_BUTTON_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header(), parameter("channel", NBTTools.intGetter("channel", -1)))
                .topDriver(DRIVER)
                .tileEntitySupplier(WirelessButtonTile::new)
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
        onBlockActivatedWithToggle(level, worldPosition, player, hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setChannel(int channel) {
        super.setChannel(channel);
        publish();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        boolean locked = isLocked();

        super.onDataPacket(net, packet);

        if (level.isClientSide) {
            // If needed send a render update.
            boolean newLocked = isLocked();
            if (newLocked != locked) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (level == null || level.isClientSide) {
            return;
        }
        this.locked = locked;
        getChannel(true);
        publish();
        markDirtyClient();
    }

    private void publish() {
        if (level != null && !level.isClientSide) {
            setVisualState(BlockProperties.POWER, !locked);
            if (channel != -1) {
                RedstoneChannels channels = RedstoneChannels.getChannels(level);
                channels.getOrCreateChannel(channel).setValue(locked ? 15 : 0);
                channels.save();
            }
        }
    }

    @Override
    public void tickServer() {
        setVisualState(BlockProperties.POWER, !locked);
    }

    public static boolean onBlockActivatedWithToggle(Level world, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (SignalChannelTileEntity.isRedstoneChannelItem(stack.getItem())) {
            setChannel(world, pos, player, stack);
        } else {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof WirelessButtonTile) {
                if (!world.isClientSide) {
                    world.playSound(null, pos, ModSounds.buzzOk.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                ((WirelessButtonTile) te).toggleLock();
            }
        }
        return true;
    }

    public void toggleLock() {
        if (!level.isClientSide) {
            setLocked(!locked);
        }
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        prevIn = tagCompound.getInt("prevIn");
        CompoundTag info = tagCompound.getCompound("Info");
        if (info.contains("locked")) {
            locked = info.getBoolean("locked");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        tagCompound.putInt("prevIn", prevIn);
        getOrCreateInfo(tagCompound).putBoolean("locked", locked);
    }

}
