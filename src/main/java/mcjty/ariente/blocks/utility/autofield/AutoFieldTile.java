package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.Ariente;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
// @todo 1.18 import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class AutoFieldTile extends GenericTileEntity implements IGuiTile, /* @todo 1.18 ITickableTileEntity, */ IPowerReceiver, IPowerUser {

    private int height = 1;
    private AABB fieldBox = null;
    private AABB renderBox = null;
    private Set<BlockPos> markers = null;
    private Set<PartPos> inputItemNodes = null;
    private Set<PartPos> outputItemNodes = null;
    private Set<PartPos> outputmodifierNodes = null;
    private Map<DyeColor, List<PartPos>> sensorNodes = null;
    private final Map<DyeColor, Boolean> sensorMeasurements = new HashMap<>();

    private ConsumerInfo consumerInfo = null;
    private ProducerInfo producerInfo = null;
    private long accumulatedPower = 0;

    private boolean renderOutline = true;
    private boolean renderItems = true;

    // Server side renderInfo
    private final AutoFieldRenderInfo renderInfo = new AutoFieldRenderInfo();

    // Client side renderInfo
    private AutoFieldRenderInfo clientRenderInfo = null;
    private long clientRenderInfoAge = -1;
    private TransferRender[] transferRenders = null;

    // Transient
    private int ticker = 20;
    private long usingPower = 0;

    public AutoFieldTile(BlockPos pos, BlockState state) {
        super(Registration.AUTOFIELD_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(AutoFieldTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
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
        findConsumers();
        findProducers();

        if (renderItems) {
            ticker--;
            if (ticker < 0) {
                ticker = 20;
                renderInfo.cleanOldTransfers();
            }
        }

        markDirtyQuick();

        usingPower = 0;
        if (!handleAccumulatedPower()) {
            return;
        }

        boolean hasWorkToDo = true;
        // As long as we have work to do and we have at least enough power to transfer a single item we
        // continue looping
        while (hasWorkToDo && accumulatedPower >= UtilityConfiguration.AUTOFIELD_POWER_PER_OPERATION.get()) {
            sensorMeasurements.clear();
            hasWorkToDo = false;
            for (Map.Entry<PartPos, ProducerInfo.Producer> entry : producerInfo.getProducers().entrySet()) {
                PartPos sourcePos = entry.getKey();
                OutputItemNodeTile producingItemNode = getOutputItemNodeAt(sourcePos);
                if (producingItemNode != null && canNodeWork(producingItemNode)) {
                    ProducerInfo.Producer producer = entry.getValue();
                    boolean didSomeWork = tryProducer(sourcePos, producingItemNode, producer);
                    if (didSomeWork) {
                        // As long as we managed to process an item we try again as we may have some accumulated power left
                        hasWorkToDo = true;
                    }
                }
            }
        }
    }

    private boolean canNodeWork(AbstractNodeTile tile) {
        for (int i = 0; i < tile.getFilters().length; i++) {
            DyeColor filter = tile.getFilters()[i];
            if (filter != null) {
                boolean result = getSensorOutput(filter);
                if (result == i >= 2) {      // i < 2 are the positive filters
                    return false;
                }
            }
        }
        return true;
    }

    private boolean tryProducer(PartPos sourcePos, OutputItemNodeTile producingItemNode, ProducerInfo.Producer producer) {
        AtomicBoolean didSomeWork = new AtomicBoolean(false);
        producingItemNode.getConnectedItemHandler(sourcePos).ifPresent(h -> {
            int minStackSize = producer.getMinStackSize();
            for (int i = 0; i < h.getSlots(); i++) {
                ItemStack stack = h.extractItem(i, minStackSize, true);
                if (!stack.isEmpty()) {
                    if (stack.getCount() >= minStackSize) {
                        long neededPower = computeNeededPower(stack);
                        if (neededPower <= accumulatedPower) {
                            ProducerInfo.ProvidedItem providedItem = producer.getProvidedItem(stack);
                            if (providedItem != null) {
                                int finalI = i;
                                List<PartPos> destinations = consumerInfo.getWantedStream(stack)
                                        .filter(destPos -> canInsert(destPos, stack)).collect(Collectors.toList());
                                if (!destinations.isEmpty()) {
                                    int index = 0;
                                    if (destinations.size() > 1) {
                                        // We need to round robin
                                        PartSlot roundRobinSlot = producer.getRoundRobinSlot();
                                        if (roundRobinSlot != null) {
                                            // @todo more efficient way to get to the round robin index?
                                            BlockEntity roundRobinModifier = MultipartHelper.getTileEntity(level, sourcePos.getPos(), roundRobinSlot);
                                            if (roundRobinModifier instanceof RoundRobinNodeTile) {
                                                index = ((RoundRobinNodeTile) roundRobinModifier).fetchIndex();
                                                index = index % destinations.size();
                                            }
                                        }
                                    }
                                    PartPos destPos = destinations.get(index);
                                    accumulatedPower -= neededPower;
                                    ItemStack extracted = h.extractItem(finalI, minStackSize, false);
                                    doInsert(sourcePos, destPos, extracted);
                                    didSomeWork.set(true);
                                }
                            }
                        }
                    }
                }
            }
        });
        return didSomeWork.get();
    }

    private boolean handleAccumulatedPower() {
        long availablePower = PowerReceiverSupport.getPowerAvailable(level, worldPosition, true);
        if (availablePower == 0) {
            // If there is no power available all accumulated power is lost and nothing can happen
            accumulatedPower = 0;
            return false;
        }
        long toAccumulate = Math.min(availablePower, UtilityConfiguration.AUTOFIELD_ACCUMULATE_PER_TICK.get());
        long maxAccumulatedPower = UtilityConfiguration.AUTOFIELD_MAX_ACCUMULATED_POWER.get();

        if (accumulatedPower + toAccumulate > maxAccumulatedPower) {
            toAccumulate = maxAccumulatedPower - accumulatedPower;
        }

        if (toAccumulate > 0) {
            PowerReceiverSupport.consumerPowerNoCheck(level, worldPosition, toAccumulate, true);
            usingPower = toAccumulate;
            accumulatedPower += toAccumulate;
        }
        return true;
    }

    private long computeNeededPower(ItemStack stack) {
        return (long) (UtilityConfiguration.AUTOFIELD_POWER_PER_OPERATION.get() + (stack.getCount() - 1) * UtilityConfiguration.AUTOFIELD_FACTOR_PER_COMBINED_STACK.get());
    }

    // Call client side to request render info
    public void clientRequestRenderInfo() {
        long time = System.currentTimeMillis();
        if (clientRenderInfo == null || time > clientRenderInfoAge + 1000) {
            ArienteMessages.INSTANCE.sendToServer(new PacketAutoFieldRequestRenderInfo(worldPosition));
        }
    }

    // Called server side when a client has requested render info
    public void renderInfoRequested() {
        double x = worldPosition.getX();
        double y = worldPosition.getY();
        double z = worldPosition.getZ();
        double radius = 50;
        PacketAutoFieldReturnRenderInfo info = new PacketAutoFieldReturnRenderInfo(worldPosition, renderInfo);
        for (Player player : level.getServer().getPlayerList().getPlayers()) {
            double dx = x - player.getX();
            double dy = y - player.getY();
            double dz = z - player.getZ();

            if (dx * dx + dy * dy + dz * dz < radius * radius) {
                ArienteMessages.INSTANCE.sendTo(info, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }

    // Called client side when a client has received render info
    public void clientRenderInfoReceived(AutoFieldRenderInfo info) {
        clientRenderInfo = info;
        clientRenderInfoAge = System.currentTimeMillis();
    }

    public AutoFieldRenderInfo getClientRenderInfo() {
        return clientRenderInfo;
    }

    public TransferRender[] getTransferRenders() {
        if (transferRenders == null) {
            int size = getInputItemNodes().size();
            if (size <= 2) {
                transferRenders = new TransferRender[2];
            } else if (size <= 4) {
                transferRenders = new TransferRender[4];
            } else if (size <= 8) {
                transferRenders = new TransferRender[6];
            } else if (size <= 12) {
                transferRenders = new TransferRender[8];
            } else {
                transferRenders = new TransferRender[12];
            }
        }
        return transferRenders;
    }


    public boolean isRenderOutline() {
        return renderOutline;
    }

    public boolean isRenderItems() {
        return renderItems;
    }

    private boolean canInsert(PartPos partPos, ItemStack stack) {
        InputItemNodeTile itemNode = getInputItemNodeAt(partPos);
        if (itemNode != null && canNodeWork(itemNode)) {
            return itemNode.getConnectedItemHandler(partPos).map(h -> {
                ItemStack remainer = ItemHandlerHelper.insertItem(h, stack, true);
                return remainer.isEmpty();
            }).orElse(false);
        }
        return false;
    }

    private void doInsert(PartPos sourcePos, PartPos destPos, ItemStack stack) {
        InputItemNodeTile itemNode = getInputItemNodeAt(destPos);
        if (itemNode != null) {
            itemNode.getConnectedItemHandler(destPos).ifPresent(connectedItemHandler -> {
                ItemHandlerHelper.insertItem(connectedItemHandler, stack, false);
                if (renderItems) {
                    renderInfo.registerTransfer(sourcePos, destPos, stack);
                }
            });
        }
    }

    private InputItemNodeTile getInputItemNodeAt(PartPos partPos) {
        BlockEntity te = MultipartHelper.getTileEntity(level, partPos);
        if (te instanceof InputItemNodeTile) {
            return (InputItemNodeTile) te;
        } else {
            return null;
        }
    }

    private OutputItemNodeTile getOutputItemNodeAt(PartPos partPos) {
        BlockEntity te = MultipartHelper.getTileEntity(level, partPos);
        if (te instanceof OutputItemNodeTile) {
            return (OutputItemNodeTile) te;
        } else {
            return null;
        }
    }

    private void findConsumers() {
        if (consumerInfo == null) {
            consumerInfo = new ConsumerInfo(level, getInputItemNodes());
        }
    }

    private void findProducers() {
        if (producerInfo == null) {
            producerInfo = new ProducerInfo(level, getOutputItemNodes(), getOutputModifierNodes());
        }
    }

    private Set<PartPos> getInputItemNodes() {
        if (inputItemNodes == null) {
            inputItemNodes = new HashSet<>();
            for (BlockPos mpos : getMarkers()) {
                for (int y = 0; y <= height; y++) {
                    BlockPos p = mpos.above(y);
                    for (PartSlot slot : PartSlot.VALUES) {
                        BlockEntity te = MultipartHelper.getTileEntity(level, p, slot);
                        if (te instanceof InputItemNodeTile) {
                            inputItemNodes.add(PartPos.create(p, slot));
                        }
                    }
                }
            }
        }
        return inputItemNodes;
    }

    private boolean getSensorOutput(DyeColor color) {
        if (!sensorMeasurements.containsKey(color)) {
            sensorMeasurements.put(color, false);
            for (PartPos sensorPos : getSensorNodes().getOrDefault(color, Collections.emptyList())) {
                BlockEntity te = MultipartHelper.getTileEntity(level, sensorPos);
                if (te instanceof SensorItemNodeTile) {
                    SensorItemNodeTile sensor = (SensorItemNodeTile) te;
                    // This recursion will work even if we need the same color because
                    // this routine pre-inits the sensor color with false
                    if (canNodeWork(sensor) && sensor.sense(sensorPos)) {
                        sensorMeasurements.put(color, true);
                        break;
                    }
                }
            }
        }
        return sensorMeasurements.get(color);
    }

    private Map<DyeColor, List<PartPos>> getSensorNodes() {
        if (sensorNodes == null) {
            findSensorNodes();
        }
        return sensorNodes;
    }

    private Set<PartPos> getOutputItemNodes() {
        if (outputItemNodes == null) {
            findOutputNodes();
        }
        return outputItemNodes;
    }

    private Set<PartPos> getOutputModifierNodes() {
        if (outputmodifierNodes == null) {
            findOutputNodes();
        }
        return outputmodifierNodes;
    }

    private void findOutputNodes() {
        outputItemNodes = new HashSet<>();
        outputmodifierNodes = new HashSet<>();
        for (BlockPos mpos : getMarkers()) {
            for (int y = 0; y <= height; y++) {
                BlockPos p = mpos.above(y);
                for (NodeOrientation orientation : NodeOrientation.VALUES) {
                    PartSlot slot = orientation.getSlot();
                    BlockEntity te = MultipartHelper.getTileEntity(level, p, slot);
                    if (te instanceof OutputItemNodeTile) {
                        outputItemNodes.add(PartPos.create(p, slot));
                    }
                    PartSlot backSlot = orientation.getBackSlot();
                    te = MultipartHelper.getTileEntity(level, p, backSlot);
                    if (te instanceof RoundRobinNodeTile) {
                        outputmodifierNodes.add(PartPos.create(p, slot));  // Use 'slot' even if the round robin modifier is at backSlot
                    }
                }
            }
        }
    }

    private void findSensorNodes() {
        sensorNodes = new HashMap<>();
        for (BlockPos mpos : getMarkers()) {
            for (int y = 0; y <= height; y++) {
                BlockPos p = mpos.above(y);
                for (NodeOrientation orientation : NodeOrientation.VALUES) {
                    PartSlot slot = orientation.getSlot();
                    BlockEntity te = MultipartHelper.getTileEntity(level, p, slot);
                    if (te instanceof SensorItemNodeTile) {
                        DyeColor outputColor = ((SensorItemNodeTile) te).getOutputColor();
                        sensorNodes.computeIfAbsent(outputColor, color -> new ArrayList<>());
                        sensorNodes.get(outputColor).add(PartPos.create(p, slot));
                    }
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (markers != null) {
            for (BlockPos marker : markers) {
                BlockEntity tileEntity = level.getBlockEntity(marker);
                if (tileEntity instanceof FieldMarkerTile) {
                    ((FieldMarkerTile) tileEntity).setAutoFieldTile(null);
                }
            }
        }
    }

    // Call this if a node in the field is updated/changed/added/removed
    public void notifyNode(BlockPos originalPos) {
        consumerInfo = null;
        producerInfo = null;
        inputItemNodes = null;
        outputItemNodes = null;
        transferRenders = null;
        outputmodifierNodes = null;
        sensorNodes = null;
    }

    // Call this to check if there is a field marker below us and if that
    // field marker is connected to an auto field tile that tile will be notified
    public static void notifyField(Level world, BlockPos pos) {
        BlockPos originalPos = pos;
        while (pos.getY() > 0) {
            BlockEntity te = MultipartHelper.getTileEntity(world, pos, PartSlot.DOWN);
            if (te instanceof FieldMarkerTile) {
                BlockPos fieldPos = ((FieldMarkerTile) te).getAutoFieldTile();
                if (fieldPos != null) {
                    BlockEntity tileEntity = world.getBlockEntity(fieldPos);
                    if (tileEntity instanceof AutoFieldTile) {
                        ((AutoFieldTile) tileEntity).notifyNode(originalPos);
                    }
                }
                return;
            }
            pos = pos.below();
        }
    }

    public void addFieldMarker(BlockPos pos) {
        markers.add(pos);
        invalidateBox();
        markDirtyClient();
    }

    public void removeFieldMarker(BlockPos pos) {
        markers.remove(pos);
        invalidateBox();
        markDirtyClient();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        invalidateBox();
        markDirtyClient();
    }

    private void invalidateBox() {
        fieldBox = null;
        renderBox = null;
        markers = null;
        consumerInfo = null;
        producerInfo = null;
        inputItemNodes = null;
        outputItemNodes = null;
        outputmodifierNodes = null;
        sensorNodes = null;
    }

    private void changeHeight(int dy) {
        height += dy;
        if (height < 1) {
            height = 1;
        } else if (height > 256) {
            height = 256;
        }
        invalidateBox();
        markDirtyClient();
    }

    public AABB getFieldBox() {
        if (fieldBox == null) {
            calculateMarkers();
        }
        return fieldBox;
    }

    public AABB getRenderBox() {
        if (renderBox == null) {
            calculateMarkers();
        }
        return renderBox;
    }

    private Set<BlockPos> getMarkers() {
        if (markers == null) {
            calculateMarkers();
        }
        return markers;
    }

    private void calculateMarkers() {
        fieldBox = null;
        markers = new HashSet<>();
        Queue<BlockPos> todo = new ArrayDeque<>();
        todo.add(getBlockPos());
        while (!todo.isEmpty()) {
            BlockPos pos = todo.poll();
            for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
                BlockPos p = pos.relative(facing);
                if (!markers.contains(p)) {
                    BlockEntity te = level.getBlockEntity(p);
                    if (te instanceof MultipartTE) {
                        BlockState state = MultipartHelper.getBlockState(level, p, PartSlot.DOWN);
                        if (state != null && state.getBlock() == Registration.FIELD_MARKER.get()) {
                            if (fieldBox == null) {
                                fieldBox = new AABB(p, p.offset(0, height, 0));
                            } else {
                                fieldBox = fieldBox.minmax(new AABB(p));
                            }
                            markers.add(p);
                            BlockEntity markerTE = MultipartHelper.getTileEntity(level, p, PartSlot.DOWN);
                            if (markerTE instanceof FieldMarkerTile) {
                                ((FieldMarkerTile) markerTE).setAutoFieldTile(this.worldPosition);
                            }
                            todo.add(p);
                        }
                    }
                }
            }
        }

        renderBox = new AABB(getBlockPos());
        if (fieldBox != null) {
            renderBox = renderBox.minmax(fieldBox);
        }
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            height = info.getInt("height");
            renderItems = info.getBoolean("renderItems");
            renderOutline = info.getBoolean("renderOutline");
        }
        accumulatedPower = tagCompound.getLong("accPower");
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        tagCompound.putLong("accPower", accumulatedPower);
        getOrCreateInfo(tagCompound).putInt("height", height);
        getOrCreateInfo(tagCompound).putBoolean("renderItems", renderItems);
        getOrCreateInfo(tagCompound).putBoolean("renderOutline", renderOutline);
        super.saveAdditional(tagCompound);
    }

    @Override
    public long getUsingPower() {
        return usingPower;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getRenderBox();
    }

    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }


    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        int oldheight = height;

        super.onDataPacket(net, packet);

        if (level.isClientSide) {
            // If needed send a render update.
            if (oldheight != height) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                invalidateBox();
            }
        }
    }


    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_HELP.equals(tag)) {
            return HoloGuiTools.createHelpGui(registry,
                    HelpBuilder.create()
                            .line("This block sets up an automation")
                            .line("field. Setup adjacent field")
                            .line("markers in a rectangle to mark where")
                            .line("the field is active. Use nodes in")
                            .line("the field to transfer items,")
                            .line("energy, ...")
            );
        } else {
            return createMainGui(registry);
        }
    }

    private IGuiComponent<?> createMainGui(IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry)
                .add(registry.text(0, 2, 1, 1).text("Height").color(registry.color(StyledColor.LABEL)))
                .add(registry.number(3, 4, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> getHeight()))

                .add(registry.iconButton(1, 4, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeHeight(-8)))
                .add(registry.iconButton(2, 4, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeHeight(-1)))
                .add(registry.iconButton(5, 4, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeHeight(1)))
                .add(registry.iconButton(6, 4, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeHeight(8)))

                .add(registry.iconToggle(1, 6, 1, 1)
                        .icon(registry.image(ITEMFLOW_OFF))
                        .selected(registry.image(ITEMFLOW_ON))
                        .getter(player -> isRenderItems())
                        .hitEvent((component, player, entity, x, y) -> toggleRenderItems()))

                .add(registry.iconToggle(3, 6, 1, 1)
                        .icon(registry.image(BOX_OFF))
                        .selected(registry.image(BOX_ON))
                        .getter(player -> isRenderOutline())
                        .hitEvent((component, player, entity, x, y) -> toggleRenderOutline()))
                ;
    }

    private void toggleRenderItems() {
        renderItems = !renderItems;
        markDirtyClient();
    }

    private void toggleRenderOutline() {
        renderOutline = !renderOutline;
        markDirtyClient();
    }

    @Override
    public void syncToClient() {

    }
}
