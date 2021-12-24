package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.api.IElevator;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.*;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.varia.SafeClientTools;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ElevatorTile extends TickingTileEntity implements IGuiTile, IPowerReceiver, ICityEquipment, IElevator, IPowerUser {

    public static final String TAG_ELEVATOR = "elevator";

    public static final int POWER_USAGE = 10;

    private AABB cachedBox = null;

    private int height = 9;

    private Map<UUID, Integer> playerToHoloGui = new HashMap<>();

    // Client side only
    private int moveToFloor = -1;

    public ElevatorTile(BlockPos pos, BlockState state) {
        super(Registration.ELEVATOR_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(ElevatorTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public long getUsingPower() {
        return POWER_USAGE;
    }

    @Override
    public void tickServer() {
        PowerReceiverSupport.consumePower(level, worldPosition, POWER_USAGE, true);

        List<Player> players = level.getEntitiesOfClass(Player.class, getBeamBox());
        for (Player player : players) {
            if (openOrMoveHoloGui(player)) {
                player.teleportTo(worldPosition.getX() + .5, player.getY(), worldPosition.getZ() + .5);
            }
            player.hasImpulse = true;
            player.fallDistance = 0;
        }
        removeStaleHoloEntries();
    }

    @Override
    public void tickClient() {
        List<Integer> floors = findFloors();
        Player clientPlayer = SafeClientTools.getClientPlayer();
        if (clientPlayer.getBoundingBox().intersects(getBeamBox())) {
            clientPlayer.hasImpulse = true;
            clientPlayer.fallDistance = 0;
            if (floors.size() < 2) {
                double motionY;
                if (clientPlayer.isShiftKeyDown()) {
                    motionY = -0.7;
                } else if (isJumpKeyDown()) {
                    motionY = 0.5;
                } else {
                    motionY = 0.0;
                }
                Vec3 motion = clientPlayer.getDeltaMovement();
                clientPlayer.setDeltaMovement(motion.x, motionY, motion.z);
            } else {
                if (moveToFloor == -1) {
                    if (clientPlayer.isShiftKeyDown()) {
                        moveToFloor = findLowerFloor(floors, (int) clientPlayer.getY());
                        System.out.println("DOWN: moveToFloor = " + moveToFloor);
                        clientPlayer.setPos(worldPosition.getX() + .5, clientPlayer.getY(), worldPosition.getZ() + .5);
                    } else if (isJumpKeyDown()) {
                        moveToFloor = findUpperFloor(floors, (int) clientPlayer.getY());
                        System.out.println("UP: moveToFloor = " + moveToFloor);
                        clientPlayer.setPos(worldPosition.getX() + .5, clientPlayer.getY(), worldPosition.getZ() + .5);
                    } else {
                        moveToFloor = -1;
                    }
                }
                if (moveToFloor >= floors.size()) {
                    moveToFloor = -1;
                }
                if (moveToFloor != -1) {
                    double motionY;
                    if (clientPlayer.getY() > floors.get(moveToFloor)) {
                        motionY = -Math.min(1.4, clientPlayer.getY() - floors.get(moveToFloor));
                    } else if (clientPlayer.getY() < floors.get(moveToFloor)) {
                        motionY = Math.min(1.0, floors.get(moveToFloor) - clientPlayer.getY());
                    } else {
                        motionY = 0.0;
                        moveToFloor = -1;
                    }
                    Vec3 motion = clientPlayer.getDeltaMovement();
                    clientPlayer.setDeltaMovement(motion.x, motionY, motion.z);
                } else {
                    Vec3 motion = clientPlayer.getDeltaMovement();
                    clientPlayer.setDeltaMovement(motion.x, 0, motion.z);
                }
            }
        } else {
            moveToFloor = -1;
        }
    }

    private boolean isJumpKeyDown() {
        // Was SafeClientTools.isJumpKeyDown()
        return Minecraft.getInstance().options.keyJump.isDown();
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
    private boolean openOrMoveHoloGui(Player player) {
        Integer holoID = playerToHoloGui.get(player.getUUID());
        if (holoID == null) {
//            List<Integer> floors = findFloors();
//            if (!floors.isEmpty()) {
                IHoloGuiEntity holoEntity = Ariente.guiHandler.openHoloGuiEntity(level, worldPosition, player, TAG_ELEVATOR, 2.0);
                if (holoEntity != null) {
                    playerToHoloGui.put(player.getUUID(), holoEntity.getEntity().getId());
                    holoEntity.setTimeout(5);
                    holoEntity.setMaxTimeout(5);
                }
//            }
            return true;
        } else {
            Entity entity = level.getEntity(holoID);
            if (entity instanceof IHoloGuiEntity) {
                IHoloGuiEntity holoEntity = (IHoloGuiEntity) entity;
                holoEntity.setTimeout(5);
                Entity h = holoEntity.getEntity();
                double oldPosY = h.getY();
                double newPosY = player.getY()+player.getEyeHeight() - .5;
                double y = (newPosY + oldPosY) / 2;
                h.teleportTo(h.getX(), y, h.getZ());
            }
            return false;
        }
    }

    private void removeStaleHoloEntries() {
        Set<UUID> toRemove = new HashSet<>();
        for (Map.Entry<UUID, Integer> entry : playerToHoloGui.entrySet()) {
            Entity entity = level.getEntity(entry.getValue());
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
        for (int y = worldPosition.getY() ; y < worldPosition.getY() + height ; y++) {
            for (int dx = -2 ; dx <= 2 ; dx++) {
                for (int dz = -2 ; dz <= 2 ; dz++) {
                    BlockPos p = new BlockPos(worldPosition.getX() + dx, y, worldPosition.getZ() + dz);
                    BlockEntity te = level.getBlockEntity(p);
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        int oldheight = height;

        super.onDataPacket(net, packet);

        if (getLevel().isClientSide) {
            // If needed send a render update.
            if (oldheight != height) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
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
    public void setup(ICityAI cityAI, Level world, boolean firstTime) {
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        if (info.contains("height")) {
            height = info.getInt("height");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        getOrCreateInfo(tagCompound).putInt("height", height);
        super.saveAdditional(tagCompound);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getBeamBox();
    }


    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }

    private AABB getBeamBox() {
        if (cachedBox == null) {
            cachedBox = new AABB(getBlockPos()).minmax(new AABB(new BlockPos(worldPosition.getX(), worldPosition.getY() + height + 2, worldPosition.getZ())));
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
                                player.setPos(worldPosition.getX() + .5, player.getY(), worldPosition.getZ() + .5);
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
