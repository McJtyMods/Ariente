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
import mcjty.lib.varia.ItemStackTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

import static mcjty.ariente.blocks.BlockProperties.LOCKED;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class WirelessLockTile extends SignalChannelTileEntity implements ILockable, IGuiTile, ITickableTileEntity {

    private boolean locked = false;
    private int horizontalRange = 5;
    private int verticalRange = 3;

    private static final VoxelShape BLOCK_AABB = VoxelShapes.create(1.0D/16.0, 1.0D/16.0, 15.0D/16.0, 15.0D/16.0, 15.0D/16.0, 1.0D);

    public WirelessLockTile() {
        super(Registration.WIRELESS_LOCK_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header(), parameter("channel", ItemStackTools.intGetter("channel", -1)))
                .topDriver(DRIVER)
                .tileEntitySupplier(WirelessLockTile::new)
        ) {
            @Override
            protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
                super.fillStateContainer(builder);
                builder.add(LOCKED);
            }

            @Override
            public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
                return BLOCK_AABB;
            }
        };
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGui(world, pos, player);
        return ActionResultType.SUCCESS;
    }


    @Override
    public void tick() {
        if (!world.isRemote) {
            if (channel != -1) {
                RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
                RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
                if (ch != null) {
                    setLocked(ch.getValue() <= 0);
                } else {
                    setLocked(true);
                }
            }
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        boolean locked = isLocked();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            boolean newLocked = isLocked();
            if (newLocked != locked) {
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
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
        if (world == null) {
            // Safety because this can actually happen during worldgen/load
            return;
        }
        if (!world.isRemote) {
            for (int dx = -horizontalRange ; dx <= horizontalRange ; dx++) {
                for (int dy = -verticalRange ; dy <= verticalRange ; dy++) {
                    for (int dz = -horizontalRange ; dz <= horizontalRange ; dz++) {
                        BlockPos p = pos.add(dx, dy, dz);
                        TileEntity te = world.getTileEntity(p);
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
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        locked = tagCompound.getBoolean("locked");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        tagCompound.putBoolean("locked", locked);
        return super.write(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
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
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
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
