package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.api.IElevator;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.hologui.api.*;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.McJtyLib;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;

import static mcjty.hologui.api.Icons.*;

public class ElevatorTile extends GenericTileEntity implements IGuiTile, ITickableTileEntity, IPowerReceiver, ICityEquipment, IElevator {

    public static final String TAG_ELEVATOR = "elevator";

    public static final int POWER_USAGE = 10;

    private AxisAlignedBB cachedBox = null;

    private int height = 9;

    private Map<UUID, Integer> playerToHoloGui = new HashMap<>();

    // Client side only
    private int moveToFloor = -1;

    public ElevatorTile(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            PowerReceiverSupport.consumePower(world, pos, POWER_USAGE, true);

            List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, getBeamBox());
            for (PlayerEntity player : players) {
                if (openOrMoveHoloGui(player)) {
                    player.setPositionAndUpdate(pos.getX() + .5, player.getPosY(), pos.getZ() + .5);
                }
                player.isAirBorne = true;
                player.fallDistance = 0;
            }
            removeStaleHoloEntries();
        } else {
            List<Integer> floors = findFloors();
            PlayerEntity clientPlayer = McJtyLib.proxy.getClientPlayer();
            if (clientPlayer.getBoundingBox().intersects(getBeamBox())) {
                clientPlayer.isAirBorne = true;
                clientPlayer.fallDistance = 0;
                if (floors.size() < 2) {
                    double motionY;
                    if (clientPlayer.isShiftKeyDown()) {
                        motionY = -0.7;
                    } else if (McJtyLib.proxy.isJumpKeyDown()) {
                        motionY = 0.5;
                    } else {
                        motionY = 0.0;
                    }
                    Vec3d motion = clientPlayer.getMotion();
                    clientPlayer.setMotion(motion.x, motionY, motion.z);
                } else {
                    if (moveToFloor == -1) {
                        if (clientPlayer.isShiftKeyDown()) {
                            moveToFloor = findLowerFloor(floors, (int) clientPlayer.getPosY());
                            System.out.println("DOWN: moveToFloor = " + moveToFloor);
                            clientPlayer.setPosition(pos.getX() + .5, clientPlayer.getPosY(), pos.getZ() + .5);
                        } else if (McJtyLib.proxy.isJumpKeyDown()) {
                            moveToFloor = findUpperFloor(floors, (int) clientPlayer.getPosY());
                            System.out.println("UP: moveToFloor = " + moveToFloor);
                            clientPlayer.setPosition(pos.getX() + .5, clientPlayer.getPosY(), pos.getZ() + .5);
                        } else {
                            moveToFloor = -1;
                        }
                    }
                    if (moveToFloor >= floors.size()) {
                        moveToFloor = -1;
                    }
                    if (moveToFloor != -1) {
                        double motionY;
                        if (clientPlayer.getPosY() > floors.get(moveToFloor)) {
                            motionY = -Math.min(1.4, clientPlayer.getPosY() - floors.get(moveToFloor));
                        } else if (clientPlayer.getPosY() < floors.get(moveToFloor)) {
                            motionY = Math.min(1.0, floors.get(moveToFloor) - clientPlayer.getPosY());
                        } else {
                            motionY = 0.0;
                            moveToFloor = -1;
                        }
                        Vec3d motion = clientPlayer.getMotion();
                        clientPlayer.setMotion(motion.x, motionY, motion.z);
                    } else {
                        Vec3d motion = clientPlayer.getMotion();
                        clientPlayer.setMotion(motion.x, 0, motion.z);
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
    private boolean openOrMoveHoloGui(PlayerEntity player) {
        Integer holoID = playerToHoloGui.get(player.getUniqueID());
        if (holoID == null) {
//            List<Integer> floors = findFloors();
//            if (!floors.isEmpty()) {
                IHoloGuiEntity holoEntity = Ariente.guiHandler.openHoloGuiEntity(world, pos, player, TAG_ELEVATOR, 2.0);
                if (holoEntity != null) {
                    playerToHoloGui.put(player.getUniqueID(), holoEntity.getEntity().getEntityId());
                    holoEntity.setTimeout(5);
                    holoEntity.setMaxTimeout(5);
                }
//            }
            return true;
        } else {
            Entity entity = world.getEntityByID(holoID);
            if (entity instanceof IHoloGuiEntity) {
                IHoloGuiEntity holoEntity = (IHoloGuiEntity) entity;
                holoEntity.setTimeout(5);
                Entity h = holoEntity.getEntity();
                double oldPosY = h.getPosY();
                double newPosY = player.getPosY()+player.getEyeHeight() - .5;
                double y = (newPosY + oldPosY) / 2;
                h.setPositionAndUpdate(h.getPosX(), y, h.getPosZ());
            }
            return false;
        }
    }

    private void removeStaleHoloEntries() {
        Set<UUID> toRemove = new HashSet<>();
        for (Map.Entry<UUID, Integer> entry : playerToHoloGui.entrySet()) {
            Entity entity = world.getEntityByID(entry.getValue());
            if (!(entity instanceof IHoloGuiEntity)) {
                toRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : toRemove) {
            playerToHoloGui.remove(uuid);
        }
    }

    private List<Integer> findFloors() {
        Set<Integer> result = new HashSet<>();
        for (int y = pos.getY() ; y < pos.getY() + height ; y++) {
            for (int dx = -2 ; dx <= 2 ; dx++) {
                for (int dz = -2 ; dz <= 2 ; dz++) {
                    BlockPos p = new BlockPos(pos.getX() + dx, y, pos.getZ() + dz);
                    TileEntity te = world.getTileEntity(p);
                    if (te instanceof LevelMarkerTile) {
                        result.add(y);
                        break;
                    }
                }
            }
        }
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> floors = new ArrayList<>(result);
        floors.sort(Comparator.naturalOrder());
        return floors;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
        cachedBox = null;
        markDirtyClient();
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
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        int oldheight = height;

        super.onDataPacket(net, packet);

        if (getWorld().isRemote) {
            // If needed send a render update.
            if (oldheight != height) {
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
                cachedBox = null;
            }
        }
    }

    @Nullable
    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();
        data.put("height", getHeight());
        return data;
    }

    @Override
    public void load(Map<String, Object> data) {
        if (data.get("height") instanceof Integer) {
            setHeight((Integer) data.get("height"));
        }
    }

    @Override
    public void setup(ICityAI cityAI, World world, boolean firstTime) {
    }

    // @todo 1.14 loot
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        height = tagCompound.getInt("height");
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("height", height);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        writeRestorableToNBT(tagCompound);
        return super.write(tagCompound);
    }

    // @todo 1.14
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        probeInfo.text(TextStyleClass.LABEL + "Using: " + TextStyleClass.INFO + POWER_USAGE + " flux");
//
////        Boolean working = isWorking();
////        if (working) {
////            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
////        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
////        if (isWorking()) {
////            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
////        }
//    }
//

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    public AxisAlignedBB getRenderBoundingBox() {
//        return getBeamBox();
//    }


    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }

    private AxisAlignedBB getBeamBox() {
        if (cachedBox == null) {
            cachedBox = new AxisAlignedBB(getPos()).union(new AxisAlignedBB(new BlockPos(pos.getX(), pos.getY() + height + 2, pos.getZ())));
        }
        return cachedBox;
    }


    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_ELEVATOR.equals(tag)) {
            IPanel panel = registry.panel(0, 0, 8, 8);
            List<Integer> floors = findFloors();

            if (!floors.isEmpty()) {
                panel.add(registry.text(0, 0, 1, 1).text("Floor").color(registry.color(StyledColor.LABEL)));
                int x = 0;
                int y = 1;
                int idx = 1;
                for (Integer floor : floors) {
                    int finalIdx = idx;
                    panel.add(registry.button(x, y, 1, 1)
                            .text("" + idx)
                            .hitClientEvent((component, player, entity1, x1, y1) -> {
                                moveToFloor = finalIdx - 1;
                                player.setPosition(pos.getX() + .5, player.getPosY(), pos.getZ() + .5);
                            }));
                    y++;
                    if (y > 8) {
                        y = 1;
                        x++;
                    }
                    idx++;
                }
            } else {
                panel.add(registry.text(0, 0, 1, 1).text("Jump: go up").color(registry.color(StyledColor.LABEL)))
                        .add(registry.text(0, 1, 1, 1).text("Crouch: go down").color(registry.color(StyledColor.LABEL)));
            }

            return panel;
        } else {
            return registry.panel(0, 0, 8, 8)
                    .add(registry.text(0, 2, 1, 1).text("Height").color(registry.color(StyledColor.LABEL)))
                    .add(registry.number(3, 4, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p,h) -> getHeight()))

                    .add(registry.iconButton(1, 4, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                            .hitEvent((component, player, entity1, x, y) -> changeHeight(-8)))
                    .add(registry.iconButton(2, 4, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                            .hitEvent((component, player, entity1, x, y) -> changeHeight(-1)))
                    .add(registry.iconButton(5, 4, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                            .hitEvent((component, player, entity1, x, y) -> changeHeight(1)))
                    .add(registry.iconButton(6, 4, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                            .hitEvent((component, player, entity1, x, y) -> changeHeight(8)))
                    ;
        }
    }

    @Override
    public void syncToClient() {

    }
}
