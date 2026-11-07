package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.Ariente;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.gui.GuiHelp;
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
import mcjty.ariente.multipart.MultipartHelper;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Containers;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class AutoFieldTile extends ExtremeMachineTile implements IGuiTile, IPowerReceiver, IPowerUser {

    private static final Set<AutoFieldTile> LOADED_FIELDS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private int height = 1;
    private AABB fieldBox = null;
    private AABB renderBox = null;
    private Set<BlockPos> markers = null;
    private Set<BlockPos> fieldColumns = null;
    private final Set<BlockPos> linkedMarkers = new HashSet<>();
    private volatile Set<Long> watchedChunks = Set.of();
    private final AtomicBoolean refreshField = new AtomicBoolean();
    private boolean listeningForChunks;
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

    @Override
    public void tickServer() {
        if (refreshField.getAndSet(false)) {
            invalidateBox();
            markDirtyClient();
        }
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
        while (hasWorkToDo && accumulatedPower >= powerCost(UtilityConfiguration.AUTOFIELD_POWER_PER_OPERATION.get())) {
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
                            if (producer.provides(stack)) {
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
                                            BlockEntity roundRobinModifier = MultipartHelper.getBlockEntity(level, sourcePos.pos(), roundRobinSlot);
                                            if (roundRobinModifier instanceof RoundRobinNodeTile) {
                                                index = ((RoundRobinNodeTile) roundRobinModifier).fetchIndex();
                                                index = Math.floorMod(index, destinations.size());
                                            }
                                        }
                                    }
                                    PartPos destPos = destinations.get(index);
                                    ItemStack extracted = h.extractItem(finalI, minStackSize, false);
                                    if (extracted.isEmpty()) {
                                        continue;
                                    }
                                    if (extracted.getCount() < minStackSize) {
                                        returnToSource(h, finalI, extracted, sourcePos);
                                        continue;
                                    }
                                    ItemStack remainder = doInsert(sourcePos, destPos, extracted.copy());
                                    int inserted = extracted.getCount() - remainder.getCount();
                                    if (inserted > 0) {
                                        ItemStack transferred = extracted.copy();
                                        transferred.setCount(inserted);
                                        accumulatedPower -= computeNeededPower(transferred);
                                        if (renderItems) {
                                            renderInfo.registerTransfer(sourcePos, destPos, transferred);
                                        }
                                        didSomeWork.set(true);
                                    }
                                    returnToSource(h, finalI, remainder, sourcePos);
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
        long toAccumulate = Math.min(powerBudget(availablePower), UtilityConfiguration.AUTOFIELD_ACCUMULATE_PER_TICK.get());
        long maxAccumulatedPower = UtilityConfiguration.AUTOFIELD_MAX_ACCUMULATED_POWER.get();

        if (accumulatedPower + toAccumulate > maxAccumulatedPower) {
            toAccumulate = maxAccumulatedPower - accumulatedPower;
        }

        if (toAccumulate > 0) {
            PowerReceiverSupport.consumerPowerNoCheck(level, worldPosition, powerCost(toAccumulate), true);
            usingPower = powerCost(toAccumulate);
            accumulatedPower += toAccumulate;
        }
        return true;
    }

    private long computeNeededPower(ItemStack stack) {
        return powerCost((long) (UtilityConfiguration.AUTOFIELD_POWER_PER_OPERATION.get() + (stack.getCount() - 1) * UtilityConfiguration.AUTOFIELD_FACTOR_PER_COMBINED_STACK.get()));
    }

    // Call client side to request render info
    public void clientRequestRenderInfo() {
        long time = System.currentTimeMillis();
        if (time > clientRenderInfoAge + 1000) {
            clientRenderInfoAge = time;
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

            if (player.level() == level && dx * dx + dy * dy + dz * dz < radius * radius) {
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

    private ItemStack doInsert(PartPos sourcePos, PartPos destPos, ItemStack stack) {
        InputItemNodeTile node = getInputItemNodeAt(destPos);
        return node == null ? stack : node.getConnectedItemHandler(destPos)
                .map(handler -> ItemHandlerHelper.insertItem(handler, stack, false)).orElse(stack);
    }

    private void returnToSource(IItemHandler source, int slot, ItemStack stack, PartPos pos) {
        if (!stack.isEmpty()) {
            ItemStack remaining = source.insertItem(slot, stack, false);
            remaining = ItemHandlerHelper.insertItem(source, remaining, false);
            if (!remaining.isEmpty()) {
                Containers.dropItemStack(level, pos.pos().getX() + .5, pos.pos().getY() + .5, pos.pos().getZ() + .5, remaining);
            }
        }
    }

    private InputItemNodeTile getInputItemNodeAt(PartPos partPos) {
        BlockEntity te = MultipartHelper.getBlockEntity(level, partPos);
        if (te instanceof InputItemNodeTile) {
            return (InputItemNodeTile) te;
        } else {
            return null;
        }
    }

    private OutputItemNodeTile getOutputItemNodeAt(PartPos partPos) {
        BlockEntity te = MultipartHelper.getBlockEntity(level, partPos);
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
            for (BlockPos mpos : getFieldColumns()) {
                for (int y = 0; y <= height; y++) {
                    BlockPos p = mpos.above(y);
                    for (PartSlot slot : PartSlot.VALUES) {
                        BlockEntity te = MultipartHelper.getBlockEntity(level, p, slot);
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
                BlockEntity te = MultipartHelper.getBlockEntity(level, sensorPos);
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
        for (BlockPos mpos : getFieldColumns()) {
            for (int y = 0; y <= height; y++) {
                BlockPos p = mpos.above(y);
                for (NodeOrientation orientation : NodeOrientation.VALUES) {
                    PartSlot slot = orientation.getSlot();
                    BlockEntity te = MultipartHelper.getBlockEntity(level, p, slot);
                    if (te instanceof OutputItemNodeTile) {
                        outputItemNodes.add(PartPos.create(p, slot));
                    }
                    PartSlot backSlot = orientation.getBackSlot();
                    te = MultipartHelper.getBlockEntity(level, p, backSlot);
                    if (te instanceof RoundRobinNodeTile) {
                        outputmodifierNodes.add(PartPos.create(p, slot));  // Use 'slot' even if the round robin modifier is at backSlot
                    }
                }
            }
        }
    }

    private void findSensorNodes() {
        sensorNodes = new HashMap<>();
        for (BlockPos mpos : getFieldColumns()) {
            for (int y = 0; y <= height; y++) {
                BlockPos p = mpos.above(y);
                for (NodeOrientation orientation : NodeOrientation.VALUES) {
                    PartSlot slot = orientation.getSlot();
                    BlockEntity te = MultipartHelper.getBlockEntity(level, p, slot);
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
    public void onLoad() {
        super.onLoad();
        invalidateBox();
        LOADED_FIELDS.add(this);
        if (!level.isClientSide && !listeningForChunks) {
            MinecraftForge.EVENT_BUS.register(this);
            listeningForChunks = true;
        }
    }

    @SubscribeEvent
    public void onFieldChunkChanged(ChunkEvent event) {
        if ((event instanceof ChunkEvent.Load || event instanceof ChunkEvent.Unload)
                && event.getLevel() == level && watchedChunks.contains(event.getChunk().getPos().toLong())) {
            // Chunk load events can arrive before FULL status. Rebuild on the next controller tick.
            refreshField.set(true);
        }
    }

    private void stopListeningForChunks() {
        LOADED_FIELDS.remove(this);
        if (listeningForChunks) {
            MinecraftForge.EVENT_BUS.unregister(this);
            listeningForChunks = false;
        }
    }

    @Override
    public void onChunkUnloaded() {
        stopListeningForChunks();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        stopListeningForChunks();
        super.setRemoved();
    }

    @Override
    public void onReplaced(Level world, BlockPos pos, BlockState state, BlockState newstate) {
        if (state.is(newstate.getBlock())) {
            return;
        }
        getMarkers();
        linkedMarkers.forEach(this::clearMarkerLink);
        linkedMarkers.clear();
    }

    private boolean clearMarkerLink(BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        if (MultipartHelper.getBlockEntity(level, pos, PartSlot.DOWN) instanceof FieldMarkerTile marker
                && worldPosition.equals(marker.getAutoFieldTile())) {
            marker.setAutoFieldTile(null);
        }
        return true;
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

    // Interior nodes need no marker below them, so notify loaded controllers covering this position.
    public static void notifyField(Level world, BlockPos pos) {
        List<AutoFieldTile> fields;
        synchronized (LOADED_FIELDS) {
            fields = List.copyOf(LOADED_FIELDS);
        }
        for (AutoFieldTile field : fields) {
            if (field.level == world && !field.isRemoved() && pos.getY() >= field.worldPosition.getY()
                    && pos.getY() <= field.worldPosition.getY() + field.height
                    && field.getFieldColumns().contains(new BlockPos(pos.getX(), field.worldPosition.getY(), pos.getZ()))) {
                field.notifyNode(pos);
            }
        }
    }

    public void addFieldMarker(BlockPos pos) {
        invalidateBox();
        markDirtyClient();
    }

    public void removeFieldMarker(BlockPos pos) {
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
        fieldColumns = null;
        consumerInfo = null;
        producerInfo = null;
        inputItemNodes = null;
        outputItemNodes = null;
        outputmodifierNodes = null;
        sensorNodes = null;
        transferRenders = null;
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

    private Set<BlockPos> getFieldColumns() {
        if (fieldColumns == null) {
            calculateMarkers();
        }
        return fieldColumns;
    }

    private void calculateMarkers() {
        LOADED_FIELDS.add(this);
        fieldBox = null;
        markers = new HashSet<>();
        Set<Long> chunks = new HashSet<>();
        Queue<BlockPos> todo = new ArrayDeque<>();
        todo.add(getBlockPos());
        while (!todo.isEmpty()) {
            BlockPos pos = todo.poll();
            for (Direction facing : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
                BlockPos p = pos.relative(facing);
                chunks.add(ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4));
                if (!markers.contains(p)) {
                    BlockEntity te = level.hasChunkAt(p) ? level.getBlockEntity(p) : null;
                    if (te instanceof MultipartTE) {
                        BlockState state = MultipartHelper.getBlockState(level, p, PartSlot.DOWN);
                        if (state != null && state.getBlock() == Registration.FIELD_MARKER.get()) {
                            if (fieldBox == null) {
                                fieldBox = new AABB(p).expandTowards(0, height, 0);
                            } else {
                                fieldBox = fieldBox.minmax(new AABB(p).expandTowards(0, height, 0));
                            }
                            markers.add(p);
                            BlockEntity markerTE = MultipartHelper.getBlockEntity(level, p, PartSlot.DOWN);
                            if (markerTE instanceof FieldMarkerTile) {
                                ((FieldMarkerTile) markerTE).setAutoFieldTile(this.worldPosition);
                            }
                            todo.add(p);
                        }
                    }
                }
            }
        }

        linkedMarkers.removeIf(pos -> !markers.contains(pos) && clearMarkerLink(pos));
        linkedMarkers.addAll(markers);
        linkedMarkers.forEach(pos -> chunks.add(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4)));
        calculateFieldColumns();
        fieldColumns.forEach(pos -> chunks.add(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4)));
        watchedChunks = Set.copyOf(chunks);

        renderBox = new AABB(getBlockPos());
        if (fieldBox != null) {
            renderBox = renderBox.minmax(fieldBox);
        }
    }

    private void calculateFieldColumns() {
        fieldColumns = new HashSet<>(markers);
        if (fieldBox == null) {
            return;
        }
        int minX = (int) fieldBox.minX - 1;
        int maxX = (int) fieldBox.maxX;
        int minZ = (int) fieldBox.minZ - 1;
        int maxZ = (int) fieldBox.maxZ;
        int y = worldPosition.getY();

        // Flood the outside of the marker outline. Only markers and the controller form the border;
        // machines, nodes and other blocks inside the field must not affect its shape.
        Set<BlockPos> outside = new HashSet<>();
        Queue<BlockPos> todo = new ArrayDeque<>();
        BlockPos start = new BlockPos(minX, y, minZ);
        outside.add(start);
        todo.add(start);
        while (!todo.isEmpty()) {
            BlockPos pos = todo.poll();
            for (Direction direction : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
                BlockPos next = pos.relative(direction);
                if (next.getX() >= minX && next.getX() <= maxX && next.getZ() >= minZ && next.getZ() <= maxZ
                        && !next.equals(worldPosition) && !markers.contains(next) && outside.add(next)) {
                    todo.add(next);
                }
            }
        }
        for (int x = minX + 1; x < maxX; x++) {
            for (int z = minZ + 1; z < maxZ; z++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!outside.contains(pos) && !pos.equals(worldPosition)) {
                    fieldColumns.add(pos);
                }
            }
        }
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        invalidateBox();
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
            return GuiHelp.FIELD.createHelpGui(registry);
        } else {
            return createMainGui(registry);
        }
    }

    private IGuiComponent<?> createMainGui(IGuiComponentRegistry registry) {
        return GuiHelp.FIELD.createPanel(registry)
                .add(registry.integerSlider(.3, 3, 7.2, 1).name("field_height").label("Height").range(1, 256)
                        .getter((p, e) -> getHeight()).changed((p, e, value) -> changeHeight(value - getHeight())))

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
