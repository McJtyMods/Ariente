package mcjty.ariente.blocks.utility.wireless;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.blocks.utility.door.DoorMarkerTile;
import mcjty.hologui.api.StyledColor;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.Map;

import static mcjty.hologui.api.Icons.*;

public class WirelessLockTile extends SignalChannelTileEntity implements ILockable, IGuiTile, ITickable {

    public static final PropertyBool LOCKED = PropertyBool.create("locked");

    private boolean locked = false;
    private int horizontalRange = 5;
    private int verticalRange = 3;

    public WirelessLockTile() {
    }

    @Override
    public void update() {
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
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean locked = isLocked();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            boolean newLocked = isLocked();
            if (newLocked != locked) {
                world.markBlockRangeForRenderUpdate(pos, pos);
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

    @Override
    public BlockState getActualState(BlockState state) {
        return state.withProperty(LOCKED, locked);
    }

    @Override
    public void onBlockBreak(World world, BlockPos pos, BlockState state) {
        doLock(false);
        super.onBlockBreak(world, pos, state);
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
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        locked = tagCompound.getBoolean("locked");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        tagCompound.setBoolean("locked", locked);
        return super.writeToNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        if (tagCompound.hasKey("vertical")) {
            verticalRange = tagCompound.getInteger("vertical");
        }
        if (tagCompound.hasKey("horizontal")) {
            horizontalRange = tagCompound.getInteger("horizontal");
        }
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("vertical", verticalRange);
        tagCompound.setInteger("horizontal", horizontalRange);
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
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        if (isLocked()) {
            probeInfo.text(TextStyleClass.WARNING + "Locked!");
        }
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
