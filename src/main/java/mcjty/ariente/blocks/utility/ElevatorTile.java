package mcjty.ariente.blocks.utility;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import mcjty.hologui.api.*;
import java.util.*;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.api.IElevator;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.display.TextDisplayModule;
import mcjty.ariente.gui.GuiHelp;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.SafeClientTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
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

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ElevatorTile extends ArienteBlockEntity implements IGuiTile, ICityEquipment, IElevator {

    public static final String TAG_ELEVATOR = "elevator";

    private static final double FLOOR_TOLERANCE = 0.125;
    private static final int VISIBLE_FLOORS = 5;
    private static final int CURRENT_FLOOR_BORDER = 0xffa0a0a0;

    private AABB cachedBox = null;

    private int height = 9;

    private Map<UUID, Integer> playerToHoloGui = new HashMap<>();

    // Client side only
    private Integer moveToFloor;

    public ElevatorTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.ELEVATOR_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
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
    public void tickServer() {
        List<Player> players = level.getEntitiesOfClass(Player.class, getBeamBox());
        for (Player player : players) {
            openOrMoveHoloGui(player);
            player.fallDistance = 0;
        }
        removeStaleHoloEntries();
    }

    @Override
    public void tickClient() {
        Player clientPlayer = SafeClientTools.getClientPlayer();
        if (clientPlayer == null) {
            moveToFloor = null;
            return;
        }
        List<Integer> floors = findFloors();
        if (clientPlayer.getBoundingBox().intersects(getBeamBox())) {
            clientPlayer.fallDistance = 0;
            if (moveToFloor != null && !floors.contains(moveToFloor)) {
                moveToFloor = null;
            }
            if (floors.size() < 2 && moveToFloor == null) {
                double motionY;
                if (clientPlayer.isShiftKeyDown()) {
                    motionY = -0.7;
                } else if (isJumpKeyDown()) {
                    motionY = 0.5;
                } else {
                    motionY = 0.0;
                    alignWithFloor(clientPlayer, floors);
                }
                Vec3 motion = clientPlayer.getDeltaMovement();
                clientPlayer.setDeltaMovement(motion.x, motionY, motion.z);
            } else {
                if (moveToFloor == null) {
                    int index = -1;
                    if (clientPlayer.isShiftKeyDown()) {
                        index = findLowerFloor(floors, clientPlayer.getY());
                    } else if (isJumpKeyDown()) {
                        index = findUpperFloor(floors, clientPlayer.getY());
                    }
                    if (index != -1) {
                        selectFloor(clientPlayer, floors.get(index));
                    }
                }
                if (moveToFloor != null) {
                    double motionY;
                    if (Math.abs(clientPlayer.getY() - moveToFloor) <= FLOOR_TOLERANCE
                            && alignWithFloor(clientPlayer, List.of(moveToFloor))) {
                        motionY = 0.0;
                        moveToFloor = null;
                    } else if (clientPlayer.getY() > moveToFloor) {
                        motionY = -Math.min(1.4, clientPlayer.getY() - moveToFloor);
                    } else if (clientPlayer.getY() < moveToFloor) {
                        motionY = Math.min(1.0, moveToFloor - clientPlayer.getY());
                    } else {
                        motionY = 0.0;
                        moveToFloor = null;
                    }
                    Vec3 motion = clientPlayer.getDeltaMovement();
                    clientPlayer.setDeltaMovement(motion.x, motionY, motion.z);
                } else {
                    alignWithFloor(clientPlayer, floors);
                    Vec3 motion = clientPlayer.getDeltaMovement();
                    clientPlayer.setDeltaMovement(motion.x, 0, motion.z);
                }
            }
        } else {
            moveToFloor = null;
        }
    }

    private void selectFloor(Player player, int floor) {
        moveToFloor = floor;
        player.setPos(worldPosition.getX() + .5, player.getY(), worldPosition.getZ() + .5);
    }

    private boolean alignWithFloor(Player player, List<Integer> floors) {
        double y = player.getY();
        double floorY = floors.stream().mapToDouble(Integer::doubleValue)
                .filter(floor -> Math.abs(floor - y) <= FLOOR_TOLERANCE)
                .findFirst().orElse(floors.isEmpty() ? Math.rint(y) : Double.NaN);
        if (!Double.isFinite(floorY) || Math.abs(floorY - y) > FLOOR_TOLERANCE) {
            return false;
        }
        // Keep feet level with the landing instead of hovering just below its edge.
        if (level.noCollision(player, player.getBoundingBox().move(0, floorY - y, 0))) {
            player.setPos(player.getX(), floorY, player.getZ());
            return true;
        }
        return false;
    }

    private boolean isJumpKeyDown() {
        // Was McJtyLib.proxy.isJumpKeyDown()
        return SafeClientTools.isJumpKeyDown();
    }

    private int findLowerFloor(List<Integer> floors, double y) {
        for (int i = floors.size()-1 ; i >= 0 ; i--) {
            if (floors.get(i) < y - FLOOR_TOLERANCE) {
                return i;
            }
        }
        return -1;
    }

    private int findUpperFloor(List<Integer> floors, double y) {
        for (int i = 0 ; i < floors.size() ; i++) {
            if (floors.get(i) > y + FLOOR_TOLERANCE) {
                return i;
            }
        }
        return -1;
    }

    private void openOrMoveHoloGui(Player player) {
        Integer holoID = playerToHoloGui.get(player.getUUID());
        if (holoID == null) {
            IHoloGuiEntity holoEntity = Ariente.guiHandler.openHoloGuiEntity(level, worldPosition, player, TAG_ELEVATOR, 2.0);
            if (holoEntity != null) {
                playerToHoloGui.put(player.getUUID(), holoEntity.getEntity().getId());
                holoEntity.setTimeout(5);
                holoEntity.setMaxTimeout(5);
            }
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
    public void onDataPacket(Connection net, ValueInput input) {
        int oldheight = height;

        super.onDataPacket(net, input);

        if (getLevel().isClientSide()) {
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
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        CompoundTag info = tagCompound.getCompoundOrEmpty("Info");
        if (info.contains("height")) {
            height = info.getIntOr("height", 0);
        }
        cachedBox = null;
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        getOrCreateInfo(tagCompound).putInt("height", height);
        super.saveAdditional(tagCompound, provider);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        saveAdditional(tag, provider);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
    }

    public AABB getRenderBoundingBox() {
        // Animated beams start below the block and extend above the transport column.
        return getBeamBox().inflate(0.05, 1.05, 0.05);
    }

    private AABB getBeamBox() {
        if (cachedBox == null) {
            cachedBox = new AABB(getBlockPos()).minmax(new AABB(new BlockPos(worldPosition.getX(), worldPosition.getY() + height + 2, worldPosition.getZ())));
        }
        return cachedBox;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.ELEVATOR.createHelpGui(registry);
        } else if ((TAG_ELEVATOR + "_help").equals(tag)) {
            return GuiHelp.ELEVATOR.createHelpGui(registry, TAG_ELEVATOR);
        } else if (TAG_ELEVATOR.equals(tag)) {
            IPanel panel = GuiHelp.ELEVATOR.createPanel(registry, entity -> entity.switchTag(TAG_ELEVATOR + "_help"));
            FloorSelection selection = new FloorSelection();
            IColor labelColor = registry.color(StyledColor.LABEL);
            IColor currentColor = registry.color(StyledColor.INFORMATION);
            panel.add(registry.text(.3, .2, 7.2, .8).text("Select floor").scale(.65f));
            for (int row = 0; row < VISIBLE_FLOORS; row++) {
                int index = row;
                double y = 1.2 + row;
                panel.add(registry.panel(.3, y, 6.2, .85).visible(() -> selection.hasFloor(index))
                        .add(registry.button(.3, y, 6.2, .85).name("floor_" + row).text("")
                                .borderColor(() -> selection.isCurrent(index) ? CURRENT_FLOOR_BORDER : -1)
                                .hitClientEvent((c, player, entity, x, cy) -> selection.select(player, index)))
                        .add(registry.text(.5, y + .2, 5.8, .6).name("floor_label_" + row)
                                .text(() -> selection.label(index)).scale(.45f)
                                .color(() -> selection.isCurrent(index) ? currentColor.getColor() : labelColor.getColor())));
            }
            panel.add(registry.panel(6.8, 1.2, .9, 4.9).visible(() -> selection.floors().size() > VISIBLE_FLOORS)
                    .add(registry.iconButton(6.8, 1.2, .9, .9).name("floors_up")
                            .icon(registry.image(GRAY_ARROW_UP)).hover(registry.image(WHITE_ARROW_UP))
                            .hitClientEvent((c, player, entity, x, y) -> selection.scroll(-1)))
                    .add(registry.iconButton(6.8, 5.2, .9, .9).name("floors_down")
                            .icon(registry.image(GRAY_ARROW_DOWN)).hover(registry.image(WHITE_ARROW_DOWN))
                            .hitClientEvent((c, player, entity, x, y) -> selection.scroll(1))));
            panel.add(registry.text(.3, 6.4, 7.2, .6).name("floor_range").text(selection::range).scale(.4f));
            panel.add(registry.panel(.3, 1.2, 7.2, 2).visible(() -> selection.floors().isEmpty())
                    .add(registry.text(.3, 1.2, 7.2, 1).text("Jump: go up").scale(.6f))
                    .add(registry.text(.3, 2.2, 7.2, 1).text("Crouch: go down").scale(.6f)));
            return panel;
        } else {
            return GuiHelp.ELEVATOR.createPanel(registry)
                    .add(registry.integerSlider(.3, 3, 7.2, 1).name("elevator_height").label("Height").range(1, 256)
                            .getter((p, e) -> getHeight()).changed((p, e, value) -> changeHeight(value - getHeight())))
                    ;
        }
    }

    // Each hologram has its own scroll position; refresh its floor heights once per tick.
    private class FloorSelection {

        private List<Integer> floors = Collections.emptyList();
        private long scannedAt = Long.MIN_VALUE;
        private int first;
        private boolean initialized;

        private List<Integer> floors() {
            if (scannedAt != level.getGameTime()) {
                scannedAt = level.getGameTime();
                floors = findFloors();
                if (!initialized && !floors.isEmpty()) {
                    if (level.isClientSide()) {
                        Player player = SafeClientTools.getClientPlayer();
                        if (player != null) {
                            first = nearestFloor(player.getY()) - VISIBLE_FLOORS / 2;
                        }
                    }
                    initialized = true;
                }
                first = Math.max(0, Math.min(first, floors.size() - VISIBLE_FLOORS));
            }
            return floors;
        }

        private boolean hasFloor(int row) {
            floors();
            return first + row < floors.size();
        }

        private int nearestFloor(double y) {
            int nearest = -1;
            for (int i = 0; i < floors.size(); i++) {
                if (nearest == -1 || Math.abs(floors.get(i) - y) < Math.abs(floors.get(nearest) - y)) {
                    nearest = i;
                }
            }
            return nearest;
        }

        private int currentFloor() {
            floors();
            Player player = level.isClientSide() ? SafeClientTools.getClientPlayer() : null;
            return player == null ? -1 : nearestFloor(player.getY());
        }

        private boolean isCurrent(int row) {
            return hasFloor(row) && first + row == currentFloor();
        }

        private void scroll(int direction) {
            floors();
            first = Math.max(0, Math.min(first + direction, floors.size() - VISIBLE_FLOORS));
        }

        private void select(Player player, int row) {
            if (hasFloor(row)) {
                selectFloor(player, floors.get(first + row));
            }
        }

        private String range() {
            int count = floors().size();
            return count == 0 ? "No marked floors" : "Floors " + (first + 1) + "-" + Math.min(first + VISIBLE_FLOORS, count) + " / " + count;
        }

        private String label(int row) {
            if (!hasFloor(row)) {
                return "";
            }
            int index = first + row;
            int y = floors.get(index);
            String name = "";
            for (int dx = -2; dx <= 2 && name.isEmpty(); dx++) {
                for (int dz = -2; dz <= 2 && name.isEmpty(); dz++) {
                    BlockEntity tile = level.getBlockEntity(new BlockPos(worldPosition.getX() + dx, y, worldPosition.getZ() + dz));
                    if (tile instanceof LevelMarkerTile marker) {
                        name = marker.getFloorName();
                    }
                }
            }
            String label = Integer.toString(index + 1) + (name.isEmpty() ? "" : ": " + name);
            String shortened = label;
            while (TextDisplayModule.lineWidth(shortened) > 116) {
                shortened = shortened.substring(0, shortened.offsetByCodePoints(shortened.length(), -1));
            }
            return shortened.equals(label) ? label : shortened + "...";
        }
    }

    @Override
    public void syncToClient() {
    }
}
