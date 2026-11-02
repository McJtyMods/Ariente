package mcjty.ariente.blocks.storagecube;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.TickingTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mcjty.lib.container.SlotDefinition.generic;

public class StorageCubeTile extends TickingTileEntity implements IGuiTile {

    public static final int MAX_SLOTS = 120;
    public static final int PREVIEW_SIZE = 16;

    private final GenericItemHandler items;
    private LazyOptional<IItemHandler> itemCapability;
    private final Map<ViewerKey, Viewer> viewers = new HashMap<>();
    private List<Preview> preview = List.of();
    private boolean previewDirty = true;
    private int paletteSize;

    public StorageCubeTile(BlockPos pos, BlockState state) {
        super(Registration.HOLO_STORAGE_TILE.get(), pos, state);
        int size = state.is(Registration.HOLO_STORAGE_3.get()) ? 120 : state.is(Registration.HOLO_STORAGE_2.get()) ? 60 : 30;
        items = new GenericItemHandler(this, new ContainerFactory(size).box(generic().in().out(), 0, 0, 0, size, 1)) {

            @Override
            protected void onUpdate(int index, ItemStack stack) {
                setChanged();
                previewDirty = true;
            }
        };
        itemCapability = LazyOptional.of(() -> items);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of().mapColor(MapColor.METAL).strength(5, 30).noOcclusion())
                .tileEntitySupplier(StorageCubeTile::new)) {

            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

    public GenericItemHandler getItems() {
        return items;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        return capability == ForgeCapabilities.ITEM_HANDLER ? itemCapability.cast() : super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.of(() -> items);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveClientDataToNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        loadClientDataFromNBT(tag);
    }

    public List<Preview> getPreview() {
        return preview;
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.HOLO_STORAGE.createHelpGui(registry);
        }
        return GuiHelp.HOLO_STORAGE.createPanel(registry).add(new StorageCubePanel(this));
    }

    @Override
    public void syncToClient() {
        // Inventory updates go only to subscribed viewers, never to chunk watchers.
    }

    public boolean canUse(ServerPlayer player, int entityId) {
        return !isRemoved() && player.isAlive() && !player.isSpectator() && player.level() == level
                && player.containerMenu == player.inventoryMenu
                && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64
                && level.getEntity(entityId) instanceof HoloGuiEntity holo && holo.isAlive()
                && worldPosition.equals(holo.getGuiTile()) && holo.getGuiId().isEmpty() && !TAG_HELP.equals(holo.getTag())
                && player.distanceToSqr(holo) <= 64;
    }

    public void subscribe(ServerPlayer player, int entityId) {
        if (!canUse(player, entityId)) {
            return;
        }
        ViewerKey key = new ViewerKey(player.getUUID(), entityId);
        Viewer viewer = viewers.get(key);
        if (viewer == null) {
            viewer = new Viewer(entityId);
            viewers.put(key, viewer);
        }
        viewer.deadline = level.getGameTime() + 60;
        sendChanges(player, viewer, false);
    }

    public void refresh(ServerPlayer player, int entityId) {
        Viewer viewer = viewers.get(new ViewerKey(player.getUUID(), entityId));
        if (viewer != null && viewer.entityId == entityId) {
            sendChanges(player, viewer, true);
        }
    }

    public void transfer(ServerPlayer player, int entityId, long revision, int source, int destination, int amount, boolean quick) {
        Viewer viewer = viewers.get(new ViewerKey(player.getUUID(), entityId));
        if (viewer == null || viewer.entityId != entityId || viewer.revision != revision
                || source < 0 || source >= viewer.snapshot.length
                || !quick && (destination < 0 || destination >= viewer.snapshot.length)) {
            return;
        }
        // Compare against the authoritative stacks last sent to this viewer. Only a revision
        // travels back from the client, so custom item share tags/capabilities remain supported.
        StorageCubeTransfer.apply(this, player, source, destination, amount, quick,
                viewer.snapshot[source], quick ? ItemStack.EMPTY : viewer.snapshot[destination]);
        viewer.deadline = level.getGameTime() + 60;
    }

    public void unsubscribe(ServerPlayer player, int entityId) {
        viewers.remove(new ViewerKey(player.getUUID(), entityId));
    }

    public void sort(ServerPlayer player, int entityId, long revision, boolean byCount) {
        Viewer viewer = viewers.get(new ViewerKey(player.getUUID(), entityId));
        if (!canUse(player, entityId) || viewer == null || viewer.entityId != entityId || viewer.revision != revision) {
            return;
        }
        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (viewer.snapshot[slot] == null || !ItemStack.matches(items.getStackInSlot(slot), viewer.snapshot[slot])) {
                // Automation or another viewer changed the inventory since this sort was requested.
                return;
            }
        }
        StorageCubeSort.apply(items, byCount);
        viewer.deadline = level.getGameTime() + 60;
    }

    private void sendChanges(ServerPlayer player, Viewer viewer, boolean force) {
        List<PacketStorageCubeData.Entry> changes = new ArrayList<>();
        for (int i = 0; i < items.getSlots() + 36; i++) {
            ItemStack stack = i < items.getSlots() ? items.getStackInSlot(i) : player.getInventory().getItem(i - items.getSlots());
            if (viewer.snapshot[i] == null || !ItemStack.matches(stack, viewer.snapshot[i])) {
                viewer.snapshot[i] = stack.copy();
                changes.add(new PacketStorageCubeData.Entry(i, stack.copy()));
            }
        }
        if (force || !changes.isEmpty()) {
            if (!changes.isEmpty()) {
                viewer.revision++;
            }
            ArienteMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new PacketStorageCubeData(worldPosition, viewer.entityId, items.getSlots(), viewer.revision, force, changes));
        }
    }

    @Override
    public void tickServer() {
        long now = level.getGameTime();
        if (now % 5 == 0 && !viewers.isEmpty()) {
            viewers.entrySet().removeIf(entry -> {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey().player());
                Viewer viewer = entry.getValue();
                if (player == null || now > viewer.deadline || !canUse(player, viewer.entityId)) {
                    return true;
                }
                sendChanges(player, viewer, false);
                return false;
            });
        }
        if (now % 20 == 0 && (previewDirty || paletteSize > PREVIEW_SIZE && now % 100 == 0)) {
            updatePreview();
        }
    }

    private void updatePreview() {
        List<Preview> palette = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            int index = -1;
            for (int j = 0; j < palette.size(); j++) {
                if (ItemStack.isSameItemSameTags(stack, palette.get(j).stack())) {
                    index = j;
                    break;
                }
            }
            if (index >= 0) {
                Preview previous = palette.get(index);
                palette.set(index, new Preview(previous.stack(), previous.weight() + stack.getCount()));
            } else {
                palette.add(new Preview(stack.copyWithCount(1), stack.getCount()));
            }
        }
        paletteSize = palette.size();
        List<Preview> sample = new ArrayList<>();
        if (palette.size() <= PREVIEW_SIZE) {
            sample.addAll(palette);
            palette.clear();
        }
        while (!palette.isEmpty() && sample.size() < PREVIEW_SIZE) {
            int roll = level.random.nextInt(palette.stream().mapToInt(Preview::weight).sum());
            for (int i = 0; i < palette.size(); i++) {
                roll -= palette.get(i).weight();
                if (roll < 0) {
                    sample.add(palette.remove(i));
                    break;
                }
            }
        }
        // Stable ordering avoids updates caused only by the sampling order.
        sample.sort(Comparator.comparingInt((Preview entry) -> Item.getId(entry.stack().getItem()))
                .thenComparing(entry -> entry.stack().hasTag() ? entry.stack().getTag().toString() : ""));
        boolean changed = sample.size() != preview.size();
        for (int i = 0; !changed && i < sample.size(); i++) {
            changed = sample.get(i).weight() != preview.get(i).weight()
                    || !ItemStack.matches(sample.get(i).stack(), preview.get(i).stack());
        }
        preview = List.copyOf(sample);
        previewDirty = false;
        if (changed) {
            markDirtyClient();
        }
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag) {
        ListTag entries = new ListTag();
        for (Preview entry : preview) {
            CompoundTag data = entry.stack().save(new CompoundTag());
            data.putInt("Weight", entry.weight());
            entries.add(data);
        }
        tag.put("Preview", entries);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag) {
        ListTag entries = tag.getList("Preview", Tag.TAG_COMPOUND);
        List<Preview> sample = new ArrayList<>();
        for (int i = 0; i < Math.min(PREVIEW_SIZE, entries.size()); i++) {
            CompoundTag data = entries.getCompound(i);
            ItemStack stack = ItemStack.of(data);
            if (!stack.isEmpty()) {
                sample.add(new Preview(stack, Math.max(1, data.getInt("Weight"))));
            }
        }
        preview = List.copyOf(sample);
    }

    public record Preview(ItemStack stack, int weight) {
    }

    private record ViewerKey(UUID player, int entityId) {
    }

    private class Viewer {

        private final int entityId;
        private final ItemStack[] snapshot = new ItemStack[items.getSlots() + 36];
        private long deadline;
        private long revision;

        private Viewer(int entityId) {
            this.entityId = entityId;
        }
    }
}
