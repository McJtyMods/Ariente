package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiHandler;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.*;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class ElevatorTile extends GenericTileEntity implements IGuiTile, ITickable {

    public static final String TAG_ELEVATOR = "elevator";

    private AxisAlignedBB cachedBox = null;

    private int height = 9;

    private Map<UUID, Integer> playerToHoloGui = new HashMap<>();

    // Client side only
    private int moveToFloor = -1;

    @Override
    public void update() {
        if (!world.isRemote) {
            List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, getBeamBox());
            for (EntityPlayer player : players) {
                if (openOrMoveHoloGui(player)) {
                    player.setPositionAndUpdate(pos.getX() + .5, player.posY, pos.getZ() + .5);
                }
                player.isAirBorne = true;
                player.fallDistance = 0;
            }
            removeStaleHoloEntries();
        } else {
            List<Integer> floors = findFloors();
            EntityPlayer clientPlayer = Ariente.proxy.getClientPlayer();
            if (clientPlayer.getEntityBoundingBox().intersects(getBeamBox())) {
                clientPlayer.isAirBorne = true;
                clientPlayer.fallDistance = 0;
                if (floors.size() < 2) {
                    if (clientPlayer.isSneaking()) {
                        clientPlayer.motionY = -0.7;
                    } else if (Ariente.proxy.isJumpKeyDown()) {
                        clientPlayer.motionY = 0.5;
                    } else {
                        clientPlayer.motionY = 0.0;
                    }
                } else {
                    if (moveToFloor == -1) {
                        if (clientPlayer.isSneaking()) {
                            moveToFloor = findLowerFloor(floors, (int) clientPlayer.posY);
                            System.out.println("DOWN: moveToFloor = " + moveToFloor);
                            clientPlayer.setPosition(pos.getX() + .5, clientPlayer.posY, pos.getZ() + .5);
                        } else if (Ariente.proxy.isJumpKeyDown()) {
                            moveToFloor = findUpperFloor(floors, (int) clientPlayer.posY);
                            System.out.println("UP: moveToFloor = " + moveToFloor);
                            clientPlayer.setPosition(pos.getX() + .5, clientPlayer.posY, pos.getZ() + .5);
                        } else {
                            moveToFloor = -1;
                        }
                    }
                    if (moveToFloor >= floors.size()) {
                        moveToFloor = -1;
                    }
                    if (moveToFloor != -1) {
                        if (clientPlayer.posY > floors.get(moveToFloor)) {
                            clientPlayer.motionY = -Math.min(1.4, clientPlayer.posY - floors.get(moveToFloor));
                        } else if (clientPlayer.posY < floors.get(moveToFloor)) {
                            clientPlayer.motionY = Math.min(1.0, floors.get(moveToFloor) - clientPlayer.posY);
                        } else {
                            clientPlayer.motionY = 0.0;
                            moveToFloor = -1;
                        }
                    } else {
                        clientPlayer.motionY = 0.0;
                    }
                }
            } else {
                moveToFloor = -1;
            }
        }
    }

    private int findLowerFloor(List<Integer> floors, int y) {
        for (int i = floors.size()-1 ; i >= 0 ; i--) {
            if (floors.get(i) < y) {
                return i;
            }
        }
        return -1;
    }

    private int findUpperFloor(List<Integer> floors, int y) {
        for (int i = 0 ; i < floors.size() ; i++) {
            if (floors.get(i) > y) {
                return i;
            }
        }
        return -1;
    }

    // Return true if the gui was opened now
    private boolean openOrMoveHoloGui(EntityPlayer player) {
        Integer holoID = playerToHoloGui.get(player.getUniqueID());
        if (holoID == null) {
//            List<Integer> floors = findFloors();
//            if (!floors.isEmpty()) {
                HoloGuiEntity holoEntity = HoloGuiHandler.openHoloGuiEntity(world, pos, player, TAG_ELEVATOR, 2.0);
                if (holoEntity != null) {
                    playerToHoloGui.put(player.getUniqueID(), holoEntity.getEntityId());
                    holoEntity.setTimeout(5);
                    holoEntity.setMaxTimeout(5);
                }
//            }
            return true;
        } else {
            Entity entity = world.getEntityByID(holoID);
            if (entity instanceof HoloGuiEntity) {
                HoloGuiEntity holoEntity = (HoloGuiEntity) entity;
                holoEntity.setTimeout(5);
                double oldPosY = holoEntity.posY;
                double newPosY = player.posY+player.eyeHeight - .5;
                double y = (newPosY + oldPosY) / 2;
                holoEntity.setPositionAndUpdate(holoEntity.posX, y, holoEntity.posZ);
            }
            return false;
        }
    }

    private void removeStaleHoloEntries() {
        Set<UUID> toRemove = new HashSet<>();
        for (Map.Entry<UUID, Integer> entry : playerToHoloGui.entrySet()) {
            Entity entity = world.getEntityByID(entry.getValue());
            if (!(entity instanceof HoloGuiEntity)) {
                toRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : toRemove) {
            playerToHoloGui.remove(uuid);
        }
    }

    private List<Integer> findFloors() {
        List<Integer> result = new ArrayList<>();
        for (int y = pos.getY() ; y < pos.getY() + height ; y++) {
            for (int dx = -2 ; dx <= 2 ; dx++) {
                for (int dz = -2 ; dz <= 2 ; dz++) {
                    BlockPos p = new BlockPos(pos.getX() + dx, y, pos.getZ() + dz);
                    TileEntity te = world.getTileEntity(p);
                    if (te instanceof LevelMarkerTile) {
                        result.add(y);
                    }
                }
            }
        }
        return result;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private void changeHeight(int dy) {
        height += dy;
        if (height < 1) {
            height = 1;
        } else if (height > 256) {
            height = 256;
        }
        cachedBox = null;
        markDirtyClient();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        int oldheight = height;

        super.onDataPacket(net, packet);

        if (getWorld().isRemote) {
            // If needed send a render update.
            if (oldheight != height) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
                cachedBox = null;
            }
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        height = tagCompound.getInteger("height");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("height", height);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        Boolean working = isWorking();
//        if (working) {
//            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (isWorking()) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }


    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getBeamBox();
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    private AxisAlignedBB getBeamBox() {
        if (cachedBox == null) {
            cachedBox = new AxisAlignedBB(getPos()).union(new AxisAlignedBB(new BlockPos(pos.getX(), pos.getY() + height + 2, pos.getZ())));
        }
        return cachedBox;
    }


    @Override
    public IGuiComponent createGui(HoloGuiEntity entity, String tag) {
        if (TAG_ELEVATOR.equals(tag)) {
            HoloPanel panel = new HoloPanel(0, 0, 8, 8);
            List<Integer> floors = findFloors();

            if (!floors.isEmpty()) {
                panel.add(new HoloText(0, 0, 1, 1, "Floor", 0xaaccff));
                int x = 0;
                int y = 1;
                int idx = 1;
                for (Integer floor : floors) {
                    int finalIdx = idx;
                    panel.add(new HoloTextButton(x, y, 1, 1, "" + idx)
                            .hitClientEvent((component, player, entity1, x1, y1) -> {
                                moveToFloor = finalIdx - 1;
                                player.setPosition(pos.getX() + .5, player.posY, pos.getZ() + .5);
                            }));
                    y++;
                    if (y > 8) {
                        y = 1;
                        x++;
                    }
                    idx++;
                }
            } else {
                panel.add(new HoloText(0, 0, 1, 1, "Jump: go up", 0xaaccff))
                        .add(new HoloText(0, 1, 1, 1, "Crouch: go down", 0xaaccff));
            }

            return panel;
        } else {
            return new HoloPanel(0, 0, 8, 8)
                    .add(new HoloText(0, 2, 1, 1, "Height", 0xaaccff))
                    .add(new HoloNumber(3, 4, 1, 1, 0xffffff, this::getHeight))

                    .add(new HoloButton(1, 4, 1, 1).image(128 + 32, 128 + 16).hover(128 + 32 + 16, 128 + 16)
                            .hitEvent((component, player, entity1, x, y) -> changeHeight(-8)))
                    .add(new HoloButton(2, 4, 1, 1).image(128 + 32, 128).hover(128 + 32 + 16, 128)
                            .hitEvent((component, player, entity1, x, y) -> changeHeight(-1)))
                    .add(new HoloButton(5, 4, 1, 1).image(128, 128).hover(128 + 16, 128)
                            .hitEvent((component, player, entity1, x, y) -> changeHeight(1)))
                    .add(new HoloButton(6, 4, 1, 1).image(128, 128 + 16).hover(128 + 16, 128 + 16)
                            .hitEvent((component, player, entity1, x, y) -> changeHeight(8)))
                    ;
        }
    }

    @Override
    public void syncToServer() {

    }
}
