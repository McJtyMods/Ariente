package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static mcjty.hologui.api.Icons.*;

public class AutoFieldTile extends GenericTileEntity implements IGuiTile, ITickable, IPowerReceiver {

    private int height = 1;
    private AxisAlignedBB fieldBox = null;
    private AxisAlignedBB renderBox = null;
    private Set<BlockPos> markers = null;
    private Set<PartPos> inputItemNodes = null;
    private Set<PartPos> outputItemNodes = null;

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

    @Override
    public void update() {
        if (world.isRemote) {
            return;
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
        while (hasWorkToDo && accumulatedPower >= UtilityConfiguration.AUTOFIELD_POWER_PER_OPERATION.get()) {
            hasWorkToDo = false;
            for (Map.Entry<PartPos, ProducerInfo.Producer> entry : producerInfo.getProducers().entrySet()) {
                PartPos sourcePos = entry.getKey();
                OutputItemNodeTile producingItemNode = getOutputItemNodeAt(sourcePos);
                if (producingItemNode != null) {
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

    private boolean tryProducer(PartPos sourcePos, OutputItemNodeTile producingItemNode, ProducerInfo.Producer producer) {
        AtomicBoolean didSomeWork = new AtomicBoolean(false);
        IItemHandler producingItemHandler = producingItemNode.getConnectedItemHandler(sourcePos);
        if (producingItemHandler != null) {
            int minStackSize = producer.getMinStackSize();
            for (int i = 0 ; i < producingItemHandler.getSlots() ; i++) {
                ItemStack stack = producingItemHandler.extractItem(i, minStackSize, true);
                if (!stack.isEmpty()) {
                    if (stack.getCount() >= minStackSize) {
                        long neededPower = computeNeededPower(stack);
                        if (neededPower <= accumulatedPower) {
                            ProducerInfo.ProvidedItem providedItem = producer.getProvidedItem(stack);
                            if (providedItem != null) {
                                int finalI = i;
                                consumerInfo.getWantedStream(stack)
                                        .filter(destPos -> canInsert(destPos, stack))
                                        .findFirst()
                                        .ifPresent(destPos -> {
                                            accumulatedPower -= neededPower;
                                            ItemStack extracted = producingItemHandler.extractItem(finalI, minStackSize, false);
                                            doInsert(sourcePos, destPos, extracted);
                                            didSomeWork.set(true);
                                        });
                            }
                        }
                    }
                }
            }
        }
        return didSomeWork.get();
    }

    private boolean handleAccumulatedPower() {
        long availablePower = PowerReceiverSupport.getPowerAvailable(world, pos, true);
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
            PowerReceiverSupport.consumerPowerNoCheck(world, pos, toAccumulate, true);
            usingPower = toAccumulate;
            accumulatedPower += toAccumulate;
        }
        return true;
    }

    private long computeNeededPower(ItemStack stack) {
        return (long) (UtilityConfiguration.AUTOFIELD_POWER_PER_OPERATION.get() + (stack.getCount()-1) * UtilityConfiguration.AUTOFIELD_FACTOR_PER_COMBINED_STACK.get());
    }

    // Call client side to request render info
    public void clientRequestRenderInfo() {
        long time = System.currentTimeMillis();
        if (clientRenderInfo == null || time > clientRenderInfoAge + 1000) {
            ArienteMessages.INSTANCE.sendToServer(new PacketAutoFieldRequestRenderInfo(pos));
        }
    }

    // Called server side when a client has requested render info
    public void renderInfoRequested() {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        double radius = 50;
        PacketAutoFieldReturnRenderInfo info = new PacketAutoFieldReturnRenderInfo(pos, renderInfo);
        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
            double dx = x - player.posX;
            double dy = y - player.posY;
            double dz = z - player.posZ;

            if (dx * dx + dy * dy + dz * dz < radius * radius) {
                ArienteMessages.INSTANCE.sendTo(info, player);
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
        if (itemNode != null) {
            IItemHandler connectedItemHandler = itemNode.getConnectedItemHandler(partPos);
            if (connectedItemHandler != null) {
                ItemStack remainer = ItemHandlerHelper.insertItem(connectedItemHandler, stack, true);
                return remainer.isEmpty();
            }
        }
        return false;
    }

    private void doInsert(PartPos sourcePos, PartPos destPos, ItemStack stack) {
        InputItemNodeTile itemNode = getInputItemNodeAt(destPos);
        if (itemNode != null) {
            IItemHandler connectedItemHandler = itemNode.getConnectedItemHandler(destPos);
            if (connectedItemHandler != null) {
                ItemHandlerHelper.insertItem(connectedItemHandler, stack, false);
                if (renderItems) {
                    renderInfo.registerTransfer(sourcePos, destPos, stack);
                }
            }
        }
    }

    private InputItemNodeTile getInputItemNodeAt(PartPos partPos) {
        TileEntity te = MultipartHelper.getTileEntity(world, partPos);
        if (te instanceof InputItemNodeTile) {
            return (InputItemNodeTile) te;
        } else {
            return null;
        }
    }

    private OutputItemNodeTile getOutputItemNodeAt(PartPos partPos) {
        TileEntity te = MultipartHelper.getTileEntity(world, partPos);
        if (te instanceof OutputItemNodeTile) {
            return (OutputItemNodeTile) te;
        } else {
            return null;
        }
    }

    private void findConsumers() {
        if (consumerInfo == null) {
            consumerInfo = new ConsumerInfo(world, getInputItemNodes());
        }
    }

    private void findProducers() {
        if (producerInfo == null) {
            producerInfo = new ProducerInfo(world, getOutputItemNodes());
        }
    }

    private Set<PartPos> getInputItemNodes() {
        if (inputItemNodes == null) {
            inputItemNodes = new HashSet<>();
            for (BlockPos mpos : getMarkers()) {
                for (int y = 0 ; y <= height ; y++) {
                    BlockPos p = mpos.up(y);
                    for (PartSlot slot : PartSlot.VALUES) {
                        TileEntity te = MultipartHelper.getTileEntity(world, p, slot);
                        if (te instanceof InputItemNodeTile) {
                            inputItemNodes.add(PartPos.create(p, slot));
                        }
                    }
                }
            }
        }
        return inputItemNodes;
    }

    private Set<PartPos> getOutputItemNodes() {
        if (outputItemNodes == null) {
            outputItemNodes = new HashSet<>();
            for (BlockPos mpos : getMarkers()) {
                for (int y = 0 ; y <= height ; y++) {
                    BlockPos p = mpos.up(y);
                    for (PartSlot slot : PartSlot.VALUES) {
                        TileEntity te = MultipartHelper.getTileEntity(world, p, slot);
                        if (te instanceof OutputItemNodeTile) {
                            outputItemNodes.add(PartPos.create(p, slot));
                        }
                    }
                }
            }
        }
        return outputItemNodes;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        for (BlockPos marker : markers) {
            TileEntity tileEntity = world.getTileEntity(marker);
            if (tileEntity instanceof FieldMarkerTile) {
                ((FieldMarkerTile) tileEntity).setAutoFieldTile(null);
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
    }

    // Call this to check if there is a field marker below us and if that
    // field marker is connected to an auto field tile that tile will be notified
    public static void notifyField(World world, BlockPos pos) {
        BlockPos originalPos = pos;
        while (pos.getY() > 0) {
            TileEntity te = MultipartHelper.getTileEntity(world, pos, PartSlot.DOWN);
            if (te instanceof FieldMarkerTile) {
                BlockPos fieldPos = ((FieldMarkerTile) te).getAutoFieldTile();
                if (fieldPos != null) {
                    TileEntity tileEntity = world.getTileEntity(fieldPos);
                    if (tileEntity instanceof AutoFieldTile) {
                        ((AutoFieldTile) tileEntity).notifyNode(originalPos);
                    }
                }
                return;
            }
            pos = pos.down();
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

    public AxisAlignedBB getFieldBox() {
        if (fieldBox == null) {
            calculateMarkers();
        }
        return fieldBox;
    }

    public AxisAlignedBB getRenderBox() {
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
        todo.add(getPos());
        while (!todo.isEmpty()) {
            BlockPos pos = todo.poll();
            for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                BlockPos p = pos.offset(facing);
                if (!markers.contains(p)) {
                    TileEntity te = world.getTileEntity(p);
                    if (te instanceof MultipartTE) {
                        IBlockState state = MultipartHelper.getBlockState(world, p, PartSlot.DOWN);
                        if (state != null && state.getBlock() == ModBlocks.fieldMarker) {
                            if (fieldBox == null) {
                                fieldBox = new AxisAlignedBB(p, p.add(0, height, 0));
                            } else {
                                fieldBox = fieldBox.union(new AxisAlignedBB(p));
                            }
                            markers.add(p);
                            TileEntity markerTE = MultipartHelper.getTileEntity(world, p, PartSlot.DOWN);
                            if (markerTE instanceof FieldMarkerTile) {
                                ((FieldMarkerTile) markerTE).setAutoFieldTile(pos);
                            }
                            todo.add(p);
                        }
                    }
                }
            }
        }

        renderBox = new AxisAlignedBB(getPos());
        if (fieldBox != null) {
            renderBox = renderBox.union(fieldBox);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        accumulatedPower = tagCompound.getLong("accPower");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setLong("accPower", accumulatedPower);
        return super.writeToNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        height = tagCompound.getInteger("height");
        renderItems = tagCompound.getBoolean("renderItems");
        renderOutline = tagCompound.getBoolean("renderOutline");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("height", height);
        tagCompound.setBoolean("renderItems", renderItems);
        tagCompound.setBoolean("renderOutline", renderOutline);
    }


    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        probeInfo.text(TextStyleClass.LABEL + "Using: " + TextStyleClass.INFO + usingPower + " flux");
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
        return getRenderBox();
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }


    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        int oldheight = height;

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            if (oldheight != height) {
                world.markBlockRangeForRenderUpdate(pos, pos);
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
                .add(registry.text(0, 2, 1, 1).text("Height").color(0xaaccff))
                .add(registry.number(3, 4, 1, 1).color(0xffffff).getter((p,h) -> getHeight()))

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
