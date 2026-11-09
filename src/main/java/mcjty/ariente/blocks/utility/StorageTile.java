package mcjty.ariente.blocks.utility;

import net.neoforged.neoforge.transfer.IndexModifier;
import mcjty.lib.varia.InventoryTools;
import java.util.ArrayList;
import java.util.List;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import mcjty.ariente.items.ItemData;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.api.IStorageTile;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.security.AreaLocks;
import mcjty.ariente.security.IKeyCardSlot;
import mcjty.ariente.sounds.ModSounds;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class StorageTile extends ArienteBlockEntity implements IGuiTile, ICityEquipment, IKeyCardSlot, ILockTarget, IStorageTile {

    private boolean locked = false;
    private String keyId;

    public static final int STACKS_PER_TYPE = 64;
    public static final int STACKS = 4;

    private final int gridSize;
    private final ItemStackList stacks;
    private final int[] counts;
    private final int[] totals;

    private final ResourceHandler<ItemResource> invHandler = new StorageItemHandler();

    public StorageTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.STORAGE_TILE.get(), pos, state);
        gridSize = state.is(mcjty.ariente.setup.Registration.STORAGE_3X3.get()) ? 3 : 2;
        stacks = ItemStackList.create(getTypeCount());
        counts = new int[getTypeCount() * STACKS_PER_TYPE];
        totals = new int[getTypeCount()];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        AreaLocks.loaded(this);
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getTypeCount() {
        return gridSize * gridSize;
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(StorageTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.ROTATION;
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
            }

            @Override
            public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
                onClick(worldIn, pos, player);
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Vec3 hit = result.getLocation();
        BlockPos pos = result.getBlockPos();
        StorageTile.onActivate(level, this.worldPosition, player, result.getDirection(), hit.x - pos.getX(), hit.y - pos.getY(), hit.z - pos.getZ());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        boolean locked = isLocked();

        super.onDataPacket(net, input);

        if (level.isClientSide()) {
            // If needed send a render update.
            boolean newLocked = isLocked();
            if (newLocked != locked) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Nullable
    @Override
    public Map<String, Object> save() {
        return null;
    }

    @Override
    public void load(Map<String, Object> data) {
    }

    @Override
    public void setup(ICityAI cityAI, Level world, boolean firstTime) {
        if (firstTime) {
            cityAI.fillLoot(this);
            setLocked(true);
            setKeyId(cityAI.getStorageKeyId());
        }
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        AreaLocks.loadTarget(this, tagCompound);
        CompoundTag info = tagCompound.getCompoundOrEmpty("Info");
        if (!info.isEmpty()) {
            readDisplayData(info, provider);
            if (info.contains("keyId")) {
                keyId = info.getStringOr("keyId", "");
            }
            int[] cc = info.getIntArray("Counts").orElseGet(() -> new int[0]);
            System.arraycopy(cc, 0, counts, 0, Math.min(cc.length, counts.length));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        AreaLocks.saveTarget(this, tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        writeDisplayData(info, provider);
        if (keyId != null) {
            info.putString("keyId", keyId);
        }
        info.putIntArray("Counts", counts);
        super.saveAdditional(tagCompound, provider);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        writeDisplayData(getOrCreateInfo(tagCompound), provider);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        readDisplayData(tagCompound.getCompoundOrEmpty("Info"), provider);
    }

    private void writeDisplayData(CompoundTag info, HolderLookup.Provider provider) {
        info.putBoolean("locked", locked);
        ListTag items = new ListTag();
        for (ItemStack stack : stacks) {
            items.add(ItemData.encode(provider, stack));
        }
        info.put("Items", items);
        info.putIntArray("Totals", totals.clone());
    }

    private void readDisplayData(CompoundTag info, HolderLookup.Provider provider) {
        locked = info.getBooleanOr("locked", false);
        ListTag items = info.getListOrEmpty("Items");
        int[] syncedTotals = info.getIntArray("Totals").orElseGet(() -> new int[0]);
        for (int i = 0; i < getTypeCount(); i++) {
            stacks.set(i, ItemData.decode(provider, items.getCompoundOrEmpty(i)));
            totals[i] = i < syncedTotals.length ? syncedTotals[i] : 0;
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(boolean locked) {
        if (locked == this.locked) {
            return;
        }
        this.locked = locked;
        markDirtyClient();
    }

    public void toggleLock() {
        if (!level.isClientSide()) {
            setLocked(!locked);
        }
    }

    public String getKeyId() {
        return keyId;
    }

    @Override
    public boolean canBeControlledBy(@Nullable String keyId) {
        return this.keyId == null || this.keyId.isBlank() || this.keyId.equals(keyId);
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
        setChanged();
    }

    @Override
    public void acceptKeyCard(ItemStack stack) {
        Set<String> tags = KeyCardItem.getSecurityTags(stack);
        if (tags.contains(keyId)) {
            level.playSound(null, worldPosition, ModSounds.buzzOk.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            toggleLock();
        } else {
            level.playSound(null, worldPosition, ModSounds.buzzError.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public ItemStack getTotalStack(int type) {
        if (totals[type] == 0) {
            return ItemStack.EMPTY;
        } else {
            ItemStack result = stacks.get(type).copy();
            result.setCount(totals[type]);
            return result;
        }
    }

    // Only use this for initial loot placement
    public void initTotalStack(int type, ItemStack stack) {
        stacks.set(type, stack);
        counts[type * STACKS_PER_TYPE] = stack.getCount();
        totals[type] = stack.getCount();
    }

    public static int getSlot(BlockHitResult mouseOver, Level world) {
        return getSlot(world, mouseOver.getBlockPos(), mouseOver.getDirection(), mouseOver.getLocation());
    }

    public static int getSlot(Level world, BlockPos pos, Direction sideHit, Vec3 hitVec) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (!(world.getBlockEntity(pos) instanceof StorageTile storage)) {
            return -1;
        }
        BaseBlock block = (BaseBlock) storage.getBlockState().getBlock();
        Direction k = block.getFrontDirection(storage.getBlockState());
        if (sideHit == k) {
            float sx = (float) (hitVec.x - x);
            float sy = (float) (hitVec.y - y);
            float sz = (float) (hitVec.z - z);
            return storage.calculateSlot(sx, sy, sz, k);
        } else {
            return -1;
        }
    }

    public void giveToPlayer(int type, Player player) {
        if (type < 0 || type >= getTypeCount()) {
            return;
        }
        if (locked) {
            Ariente.guiHandler.openHoloGui(level, worldPosition, player);
            return;
        }
        if (totals[type] == 0) {
            return;
        }
        ItemStack stack = getStackFromType(type, player.isShiftKeyDown() ? 10000 : 1);
        if (!stack.isEmpty()) {
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            } else if (ItemStack.isSameItemSameComponents(player.getMainHandItem(), stack)) {
                boolean added = player.getInventory().add(player.getInventory().getSelectedSlot(), stack);
                if (!added) {
                    if (!player.getInventory().add(stack)) {
                        player.spawnAtLocation((ServerLevel) level, stack, 1.05f);
                    }
                }
            } else {
                if (!player.getInventory().add(stack)) {
                    player.spawnAtLocation((ServerLevel) level, stack, 1.05f);
                }
            }
            player.containerMenu.broadcastChanges();
        }
    }

    private ItemStack getStackFromType(int type, int maxamount) {
        ItemStack stack = stacks.get(type);
        if (stack.isEmpty() || totals[type] <= 0) {
            return ItemStack.EMPTY;
        } else {
            maxamount = Math.min(maxamount, stack.getMaxStackSize());
            int collected = 0;
            for (int i = type * STACKS_PER_TYPE; i < (type + 1) * STACKS_PER_TYPE; i++) {
                if (counts[i] > 0) {
                    if (maxamount >= counts[i]) {
                        maxamount -= counts[i];
                        totals[type] -= counts[i];
                        collected += counts[i];
                        counts[i] = 0;
                    } else {
                        counts[i] -= maxamount;
                        totals[type] -= maxamount;
                        collected += maxamount;
                        maxamount = 0;
                    }
                    if (maxamount <= 0) {
                        break;
                    }
                }
            }
            stack = stack.copy();
            stack.setCount(collected);
            updateTotals(type);
            markDirtyClient();
            return stack;
        }
    }

    private void updateTotals(int type) {
        if (totals[type] == 0) {
            // @todo lock
            stacks.set(type, ItemStack.EMPTY);
            markDirtyClient();
        }
    }

    public static void onClick(Level world, BlockPos pos, Player player) {
        if (world.isClientSide()) {
            // On client. We find out what part of the block was hit and send that to the server.
            HitResult mouseOver = Minecraft.getInstance().hitResult;
            if (mouseOver instanceof BlockHitResult) {
                int index = getSlot((BlockHitResult) mouseOver, world);
                if (index >= 0) {
                    ClientPacketDistributor.sendToServer(new PacketClickStorage(pos, index));
                }
            }
        }
    }

    private static long doubleClickTime = -1;

    public static boolean onActivate(Level world, BlockPos pos, Player player, Direction side, double sx, double sy, double sz) {
        if (!(world.getBlockEntity(pos) instanceof StorageTile te)) {
            return true;
        }
        BaseBlock block = (BaseBlock) te.getBlockState().getBlock();
        Direction k = block.getFrontDirection(te.getBlockState());
        if (side == k) {
            if (!world.isClientSide()) {
                if (te.isLocked()) {
                    Ariente.guiHandler.openHoloGui(world, pos, player);
                    return true;
                }

                int type = te.calculateSlot(sx, sy, sz, k);

                if (type == -1) {
                    Ariente.guiHandler.openHoloGui(world, pos, player);
                    return true;
                }

                ItemStack heldItem = player.getMainHandItem();
                long time = System.currentTimeMillis();
                if (doubleClickTime != -1 && (time < (doubleClickTime + 250)) && !te.stacks.get(type).isEmpty()) {
                    // Doubleclick
                    placeAll(player, te, type, te.stacks.get(type));
                    doubleClickTime = -1;
                } else {
                    doubleClickTime = time;
                    if (!heldItem.isEmpty()) {
                        placeHeld(player, te, type, heldItem);
                    }
                }
                te.markDirtyClient();
                player.containerMenu.broadcastChanges();
            }
        }
        return true;
    }

    private static void placeAll(Player player, StorageTile te, int type, ItemStack heldItem) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, heldItem)) {
                ItemStack remaining = insertItem(te.invHandler, stack, type);
                player.getInventory().setItem(i, remaining);
                if (!remaining.isEmpty()) {
                    break;  // Storage is full
                }
            }
        }
    }

    private static void placeHeld(Player player, StorageTile te, int type, ItemStack heldItem) {
        ItemStack remaining = insertItem(te.invHandler, heldItem, type);
        player.setItemInHand(InteractionHand.MAIN_HAND, remaining);
    }

    @Nonnull
    private static ItemStack insertItem(ResourceHandler<ItemResource> dest, @Nonnull ItemStack stack, int type) {
        if (dest == null || stack.isEmpty()) {
            return stack;
        }

        for (int i = type * STACKS_PER_TYPE; i < (type + 1) * STACKS_PER_TYPE; i++) {
            stack = InventoryTools.insertItem(dest, i, stack, false);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    private static int calculateHitIndex(double sx, double sy, double sz, Direction k) {
        int index = -1;
        switch (k) {
            case DOWN:
                if (sz < .13) {
                    return -1;
                }
                index = (sx > .5 ? 1 : 0) + (sz < .54 ? 2 : 0);
                break;
            case UP:
                if (sz > 1 - .13) {
                    return -1;
                }
                index = (sx > .5 ? 1 : 0) + (sz > .54 ? 2 : 0);
                break;
            case NORTH:
                if (sy < .13) {
                    return -1;
                }
                index = (sx < .5 ? 1 : 0) + (sy < .54 ? 2 : 0);
                break;
            case SOUTH:
                if (sy < .13) {
                    return -1;
                }
                index = (sx > .5 ? 1 : 0) + (sy < .54 ? 2 : 0);
                break;
            case WEST:
                if (sy < .13) {
                    return -1;
                }
                index = (sz > .5 ? 1 : 0) + (sy < .54 ? 2 : 0);
                break;
            case EAST:
                if (sy < .13) {
                    return -1;
                }
                index = (sz < .5 ? 1 : 0) + (sy < .54 ? 2 : 0);
                break;
        }
        return index;
    }

    private int calculateSlot(double sx, double sy, double sz, Direction front) {
        if (gridSize == 2) {
            return calculateHitIndex(sx, sy, sz, front);
        }
        double horizontal = switch (front) {
            case NORTH -> 1 - sx;
            case EAST -> 1 - sz;
            case WEST -> sz;
            default -> sx;
        };
        double vertical = switch (front) {
            case DOWN -> sz;
            case UP -> 1 - sz;
            default -> sy;
        };
        if (vertical < .13) {
            return -1;
        }
        // The nine rendered cells span x=.11..87 and y=.16..92, at .25 intervals.
        int column = Math.max(0, Math.min(2, (int) Math.floor((horizontal - .11) / .25)));
        int row = Math.max(0, Math.min(2, (int) Math.floor((.92 - vertical) / .25)));
        return row * gridSize + column;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        return Ariente.guiHandler.createNoAccessPanel();
    }

    @Override
    public void syncToClient() {
    }

    @Override
    public void setLoot(Identifier id, int i, boolean doBlueprint, int amount, int meta) {
        if (id == null) {
            // Random blueprint
            ConstructorRecipe recipe = BlueprintRecipeRegistry.getRandomRecipes().getRandom();
            ItemStack blueprint = BlueprintItem.makeBluePrint(recipe.getDestination());
            initTotalStack(i, blueprint);
        } else {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != null) {
                if (doBlueprint) {
                    // @todo 1.14 meta
                    ItemStack blueprint = BlueprintItem.makeBluePrint(new ItemStack(item, 1));
                    initTotalStack(i, blueprint);
                } else {
                    // @todo 1.14 meta
                    initTotalStack(i, new ItemStack(item, amount));
                }
            }
        }
    }

    private boolean storageTransaction;

    @Override
    public void markDirtyClient() {
        if (!storageTransaction) super.markDirtyClient();
    }

    private record InventorySnapshot(List<ItemStack> stacks, int[] counts, int[] totals) {
    }

    private class StorageItemHandler implements ResourceHandler<ItemResource>, IndexModifier<ItemResource> {

        @Override
        public void set(int index, ItemResource resource, int amount) {
            setStackInSlot(index, resource.toStack(amount));
        }

        private final SnapshotJournal<InventorySnapshot> journal = new SnapshotJournal<>() {

            @Override
            protected InventorySnapshot createSnapshot() {
                List<ItemStack> copies = new ArrayList<>();
                for (int i = 0; i < stacks.size(); i++) copies.add(stacks.get(i).copy());
                return new InventorySnapshot(copies, counts.clone(), totals.clone());
            }

            @Override
            protected void revertToSnapshot(InventorySnapshot snapshot) {
                for (int i = 0; i < stacks.size(); i++) stacks.set(i, snapshot.stacks().get(i));
                System.arraycopy(snapshot.counts(), 0, counts, 0, counts.length);
                System.arraycopy(snapshot.totals(), 0, totals, 0, totals.length);
            }

            @Override
            protected void onRootCommit(InventorySnapshot before) {
                markDirtyClient();
            }
        };

        @Override
        public int size() {
            return getSlots();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(getStackInSlot(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return getStackInSlot(index).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return getSlotLimit(index);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return isItemValid(index, resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            ItemStack incoming = resource.toStack(amount);
            int inserted = amount - insertItem(index, incoming, true).getCount();
            if (inserted == 0) return 0;
            journal.updateSnapshots(transaction);
            storageTransaction = true;
            try {
                return amount - insertItem(index, incoming, false).getCount();
            } finally {
                storageTransaction = false;
            }
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (!resource.matches(getStackInSlot(index)) || amount == 0 || locked) return 0;
            journal.updateSnapshots(transaction);
            storageTransaction = true;
            try {
                return extractItem(index, amount, false).getCount();
            } finally {
                storageTransaction = false;
            }
        }

                public int getSlots() {
            return getTypeCount() * STACKS_PER_TYPE;
        }

        @Nonnull
                public ItemStack getStackInSlot(int index) {
            if (counts[index] == 0) {
                return ItemStack.EMPTY;
            } else {
                int type = index / STACKS_PER_TYPE;
                ItemStack stack = stacks.get(type).copy();
                if (!stack.isEmpty()) {
                    stack.setCount(counts[index]);
                }
                return stack;
            }
        }

        @Nonnull
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (locked) {
                return stack;
            }
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack stackInSlot = getStackInSlot(slot);

            int m;
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot)))
                    return stack;

                if (!ItemStack.isSameItemSameComponents(stack, stackInSlot))
                    return stack;

                if (!isItemValid(slot, stack))
                    return stack;

                m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();

                if (stack.getCount() <= m) {
                    if (!simulate) {
                        ItemStack copy = stack.copy();
                        copy.grow(stackInSlot.getCount());
                        setStackInSlot(slot, copy);
                    }

                    return ItemStack.EMPTY;
                } else {
                    // copy the stack to not modify the original one
                    stack = stack.copy();
                    if (!simulate) {
                        ItemStack copy = stack.split(m);
                        copy.grow(stackInSlot.getCount());
                        setStackInSlot(slot, copy);
                        return stack;
                    } else {
                        stack.shrink(m);
                        return stack;
                    }
                }
            } else {
                if (!isItemValid(slot, stack))
                    return stack;

                m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
                if (m < stack.getCount()) {
                    // copy the stack to not modify the original one
                    stack = stack.copy();
                    if (!simulate) {
                        setStackInSlot(slot, stack.split(m));
                        return stack;
                    } else {
                        stack.shrink(m);
                        return stack;
                    }
                } else {
                    if (!simulate) {
                        setStackInSlot(slot, stack);
                    }
                    return ItemStack.EMPTY;
                }
            }
        }

        public ItemStack decrStackSize(int index, int count) {
            if (counts[index] == 0) {
                return ItemStack.EMPTY;
            } else {
                int type = index / STACKS_PER_TYPE;
                ItemStack stack = stacks.get(type).copy();
                if (!stack.isEmpty()) {
                    if (count <= counts[index]) {
                        counts[index] -= count;
                        totals[type] -= count;
                        stack.setCount(count);
                    } else {
                        totals[type] -= counts[index];
                        stack.setCount(counts[index]);
                        counts[index] = 0;
                    }
                    updateTotals(type);
                    markDirtyClient();  // @todo is this really needed?
                }
                return stack;
            }
        }

        @Nonnull
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (locked || amount <= 0)
                return ItemStack.EMPTY;

            ItemStack stackInSlot = getStackInSlot(slot);

            if (stackInSlot.isEmpty())
                return ItemStack.EMPTY;

            if (simulate) {
                if (stackInSlot.getCount() < amount) {
                    return stackInSlot.copy();
                } else {
                    ItemStack copy = stackInSlot.copy();
                    copy.setCount(amount);
                    return copy;
                }
            } else {
                int m = Math.min(stackInSlot.getCount(), amount);
                return decrStackSize(slot, m);
            }
        }

                public int getSlotLimit(int slot) {
            return 64;
        }

                public boolean isItemValid(int index, @Nonnull ItemStack stack) {
            if (locked) {
                return false;
            }
            int type = index / STACKS_PER_TYPE;
            if (stacks.get(type).isEmpty()) {
                return true;
            } else {
                return ItemStack.isSameItemSameComponents(stacks.get(type), stack);
            }
        }

                public void setStackInSlot(int index, @Nonnull ItemStack stack) {
            // We assume here that the actual stack is valid for this slot
            int type = index / STACKS_PER_TYPE;
            totals[type] -= counts[index];
            counts[index] = stack.getCount();
            totals[type] += counts[index];
            if (totals[type] == 0) {
                // @todo lock
                stacks.set(type, ItemStack.EMPTY);
            } else {
                stacks.set(type, stack);
            }
            updateTotals(type);
            markDirtyClient();  // @todo is this really required?
        }
    }

    @Override
    public ResourceHandler<ItemResource> getAutomationHandler(Direction side) {
        return invHandler;
    }

}
