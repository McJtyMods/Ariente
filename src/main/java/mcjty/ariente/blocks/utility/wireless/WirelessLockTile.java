package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.blocks.utility.door.DoorMarkerTile;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.NBTTools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.Connection;
import net.minecraft.world.level.block.state.StateDefinition;
// @todo 1.18 import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.Map;

import static mcjty.ariente.blocks.BlockProperties.LOCKED;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.*;

public class WirelessLockTile extends SignalChannelTileEntity implements ILockable, IGuiTile /* @todo 1.18, ITickableTileEntity */ {

    private boolean locked = false;
    private int horizontalRange = 5;
    private int verticalRange = 3;

    private static final VoxelShape BLOCK_AABB = Shapes.box(1.0D/16.0, 1.0D/16.0, 15.0D/16.0, 15.0D/16.0, 15.0D/16.0, 1.0D);

    public WirelessLockTile(BlockPos pos, BlockState state) {
        super(Registration.WIRELESS_LOCK_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header(), parameter("channel", NBTTools.intGetter("channel", -1)))
                .topDriver(DRIVER)
                .tileEntitySupplier(WirelessLockTile::new)
        ) {
            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(LOCKED);
            }

            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return BLOCK_AABB;
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }


    //@Override
    public void tickServer() {
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(getLevel());
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            if (ch != null) {
                setLocked(ch.getValue() <= 0);
            } else {
                setLocked(true);
            }
        }
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


    @Override
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (locked == this.locked) {
            return;
        }
        this.locked = locked;
        doLock(locked);
        markDirtyClient();
    }


    private void doLock(boolean l) {
        if (level == null) {
            // Safety because this can actually happen during worldgen/load
            return;
        }
        if (!level.isClientSide) {
            for (int dx = -horizontalRange ; dx <= horizontalRange ; dx++) {
                for (int dy = -verticalRange ; dy <= verticalRange ; dy++) {
                    for (int dz = -horizontalRange ; dz <= horizontalRange ; dz++) {
                        BlockPos p = worldPosition.offset(dx, dy, dz);
                        BlockEntity te = level.getBlockEntity(p);
                        if (te instanceof DoorMarkerTile) { // @todo generalize!
                            ((DoorMarkerTile) te).setLocked(l);
                        }
                    }
                }
            }
        }
    }

    public int getHorizontalRange() {
        return horizontalRange;
    }

    public void setHorizontalRange(int horizontalRange) {
        doLock(false);
        this.horizontalRange = horizontalRange;
        markDirtyQuick();
        doLock(true);
    }

    public int getVerticalRange() {
        return verticalRange;
    }

    public void setVerticalRange(int verticalRange) {
        doLock(false);
        this.verticalRange = verticalRange;
        markDirtyQuick();
        doLock(true);
    }

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.with(LOCKED, locked);
//    }


    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        doLock(false);
        super.onReplaced(world, pos, state, newstate);
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = super.save();
        data.put("vertical", verticalRange);
        data.put("horizontal", horizontalRange);
        return data;
    }

    @Override
    public void load(Map<String, Object> data) {
        super.load(data);
        if (data.get("horizontal") instanceof Integer) {
            setHorizontalRange((Integer) data.get("horizontal"));
        }
        if (data.get("vertical") instanceof Integer) {
            setVerticalRange((Integer) data.get("vertical"));
        }
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        locked = tagCompound.getBoolean("locked");
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        tagCompound.putBoolean("locked", locked);
        super.saveAdditional(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(CompoundTag tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            if (info.contains("vertical")) {
                verticalRange = info.getInt("vertical");
            }
            if (info.contains("horizontal")) {
                horizontalRange = info.getInt("horizontal");
            }
        }
    }

    @Override
    public void writeRestorableToNBT(CompoundTag tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("vertical", verticalRange);
        info.putInt("horizontal", horizontalRange);
    }

    private void changeHorizontalRange(int dy) {
        int h = horizontalRange + dy;
        if (h < 1) {
            h = 1;
        } else if (h > 20) {
            h = 20;
        }
        setHorizontalRange(h);
    }


    private void changeVerticalRange(int dy) {
        int h = verticalRange + dy;
        if (h < 1) {
            h = 1;
        } else if (h > 10) {
            h = 10;
        }
        setVerticalRange(h);
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (isLocked()) {
            return Ariente.guiHandler.createNoAccessPanel();
        }
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 1, 1, 1).text("Horizontal").color(registry.color(StyledColor.LABEL)))
                .add(registry.number(3, 2, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p,h) -> getHorizontalRange()))

                .add(registry.iconButton(1, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> changeHorizontalRange(-8)))
                .add(registry.iconButton(2, 2, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> changeHorizontalRange(-1)))
                .add(registry.iconButton(5, 2, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> changeHorizontalRange(1)))
                .add(registry.iconButton(6, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> changeHorizontalRange(8)))


                .add(registry.text(0, 4, 1, 1).text("Vertical").color(registry.color(StyledColor.LABEL)))
                .add(registry.number(3, 5, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p,h) -> getVerticalRange()))

                .add(registry.iconButton(1, 5, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> changeVerticalRange(-8)))
                .add(registry.iconButton(2, 5, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> changeVerticalRange(-1)))
                .add(registry.iconButton(5, 5, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> changeVerticalRange(1)))
                .add(registry.iconButton(6, 5, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> changeVerticalRange(8)))
                ;
    }

    @Override
    public void syncToClient() {

    }

}
