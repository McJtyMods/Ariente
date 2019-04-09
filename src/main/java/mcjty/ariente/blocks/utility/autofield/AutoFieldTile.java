package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.*;

import static mcjty.hologui.api.Icons.*;

public class AutoFieldTile extends GenericTileEntity implements IGuiTile, ITickable, IPowerReceiver {

    private int height = 1;
    private AxisAlignedBB fieldBox = null;
    private AxisAlignedBB renderBox = null;
    private Set<BlockPos> markers = null;
    private Set<PartPos> itemNodes = null;

    private ConsumerInfo consumerInfo = null;
    private ProducerInfo producerInfo = null;

    // Server side renderInfo
    private final AutoFieldRenderInfo renderInfo = new AutoFieldRenderInfo();

    // Client side renderInfo
    private AutoFieldRenderInfo clientRenderInfo = null;
    private long clientRenderInfoAge = -1;
    private TransferRender[] transferRenders = new TransferRender[] { null, null, null };

    @Override
    public void update() {
        findConsumers();
        findProducers();

        for (Map.Entry<PartPos, ProducerInfo.Producer> entry : producerInfo.getProducers().entrySet()) {
            PartPos sourcePos = entry.getKey();
            ItemNodeTile producingItemNode = getItemNodeAt(sourcePos);
            if (producingItemNode != null) {
                IItemHandler producingItemHandler = producingItemNode.getConnectedItemHandler(sourcePos);
                if (producingItemHandler != null) {
                    ProducerInfo.Producer producer = entry.getValue();
                    for (int i = 0 ; i < producingItemHandler.getSlots() ; i++) {
                        ItemStack stack = producingItemHandler.extractItem(i, 1, true);
                        if (!stack.isEmpty()) {
                            ProducerInfo.ProvidedItem providedItem = producer.getProvidedItem(stack);
                            if (providedItem != null) {
                                int finalI = i;
                                consumerInfo.getWantedStream(stack)
                                        .filter(destPos -> canInsert(destPos, stack))
                                        .findFirst()
                                        .ifPresent(destPos -> {
                                            ItemStack extracted = producingItemHandler.extractItem(finalI, 1, false);
                                            doInsert(sourcePos, destPos, extracted);
                                        });
                            }
                        }
                    }
                }
            }
        }
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
        return transferRenders;
    }

    private boolean canInsert(PartPos partPos, ItemStack stack) {
        ItemNodeTile itemNode = getItemNodeAt(partPos);
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
        ItemNodeTile itemNode = getItemNodeAt(destPos);
        if (itemNode != null) {
            IItemHandler connectedItemHandler = itemNode.getConnectedItemHandler(destPos);
            if (connectedItemHandler != null) {
                ItemHandlerHelper.insertItem(connectedItemHandler, stack, false);
                renderInfo.registerTransfer(sourcePos, destPos, stack);
            }
        }
    }

    private ItemNodeTile getItemNodeAt(PartPos partPos) {
        TileEntity te = MultipartHelper.getTileEntity(world, partPos);
        if (te instanceof ItemNodeTile) {
            return (ItemNodeTile) te;
        } else {
            return null;
        }
    }

    private void findConsumers() {
        if (consumerInfo == null) {
            consumerInfo = new ConsumerInfo(world, getItemNodes());
        }
    }

    private void findProducers() {
        if (producerInfo == null) {
            producerInfo = new ProducerInfo(world, getItemNodes());
        }
    }

    private Set<PartPos> getItemNodes() {
        if (itemNodes == null) {
            itemNodes = new HashSet<>();
            for (BlockPos mpos : getMarkers()) {
                for (int y = 0 ; y <= height ; y++) {
                    BlockPos p = mpos.up(y);
                    for (PartSlot slot : PartSlot.VALUES) {
                        TileEntity te = MultipartHelper.getTileEntity(world, p, slot);
                        if (te instanceof ItemNodeTile) {
                            itemNodes.add(PartPos.create(p, slot));
                        }
                    }
                }
            }
        }
        return itemNodes;
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
        itemNodes = null;
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
        itemNodes = null;
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
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        height = tagCompound.getInteger("height");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("height", height);
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

        if (getWorld().isRemote) {
            // If needed send a render update.
            if (oldheight != height) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
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
                ;
    }

    @Override
    public void syncToClient() {

    }
}
