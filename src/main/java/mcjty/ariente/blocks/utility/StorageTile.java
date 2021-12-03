package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.api.IStorageTile;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.security.IKeyCardSlot;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class StorageTile extends GenericTileEntity implements IGuiTile, ICityEquipment, IKeyCardSlot, ILockable, IStorageTile {

//    public static final PropertyBool LOCKED = PropertyBool.create("locked");

    private boolean locked = false;
    private String keyId;

    public static final int STACKS_PER_TYPE = 64;
    public static final int STACKS = 4;

    private final ItemStackList stacks = ItemStackList.create(4);
    private int[] counts = new int[STACKS * STACKS_PER_TYPE];
    private int[] totals = new int[STACKS];

    public StorageTile() {
        super(Registration.STORAGE_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
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
            protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
                super.fillStateContainer(builder);
//                builder.add(LOCKED);
            }

            @Override
            public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
                onClick(worldIn, pos, player);
            }
        };
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Vector3d hit = result.getHitVec();
        BlockPos pos = result.getPos();
        StorageTile.onActivate(world, this.pos, player, result.getFace(), hit.x - pos.getX(), hit.y - pos.getY(), hit.z - pos.getZ());
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        boolean locked = isLocked();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            boolean newLocked = isLocked();
            if (newLocked != locked) {
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
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
    public void setup(ICityAI cityAI, World world, boolean firstTime) {
        if (firstTime) {
            cityAI.fillLoot(this);
            setLocked(true);
            setKeyId(cityAI.getStorageKeyId());
        }
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            locked = info.getBoolean("locked");
            if (info.contains("keyId")) {
                keyId = info.getString("keyId");
            }

            ListNBT bufferTagList = info.getList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < STACKS; i++) {
                CompoundNBT CompoundNBT = bufferTagList.getCompound(i);
                stacks.set(i, ItemStack.read(CompoundNBT));
            }
            int[] cc = info.getIntArray("Counts");
            System.arraycopy(cc, 0, counts, 0, cc.length);
            int[] ct = info.getIntArray("Totals");
            System.arraycopy(ct, 0, totals, 0, ct.length);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putBoolean("locked", locked);
        if (keyId != null) {
            info.putString("keyId", keyId);
        }
        ListNBT bufferTagList = new ListNBT();
        for (int i = 0; i < STACKS; i++) {
            ItemStack stack = stacks.get(i);
            CompoundNBT CompoundNBT = new CompoundNBT();
            if (!stack.isEmpty()) {
                stack.write(CompoundNBT);
            }
            bufferTagList.add(CompoundNBT);
        }
        info.put("Items", bufferTagList);
        info.putIntArray("Counts", counts);
        info.putIntArray("Totals", totals);
        return super.write(tagCompound);
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (locked == this.locked) {
            return;
        }
        this.locked = locked;
        markDirtyClient();
    }

    public void toggleLock() {
        if (!world.isRemote) {
            setLocked(!locked);
        }
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
        markDirty();
    }

//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.with(LOCKED, isLocked());
//    }
//

    @Override
    public void acceptKeyCard(ItemStack stack) {
        Set<String> tags = KeyCardItem.getSecurityTags(stack);
        if (tags.contains(keyId)) {
            world.playSound(null, pos, ModSounds.buzzOk, SoundCategory.BLOCKS, 1.0f, 1.0f);
            toggleLock();
        } else {
            world.playSound(null, pos, ModSounds.buzzError, SoundCategory.BLOCKS, 1.0f, 1.0f);
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

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
////        if (isWorking()) {
////            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
////        }
//    }

    public static int getSlot(BlockRayTraceResult mouseOver, World world) {
        return getSlot(world, mouseOver.getPos(), mouseOver.getFace(), mouseOver.getHitVec());
    }

    public static int getSlot(World world, BlockPos pos, Direction sideHit, Vector3d hitVec) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Direction k = Registration.STORAGE.get().getFrontDirection(world.getBlockState(pos));
        if (sideHit == k) {
            float sx = (float) (hitVec.x - x);
            float sy = (float) (hitVec.y - y);
            float sz = (float) (hitVec.z - z);
            return calculateHitIndex(sx, sy, sz, k);
        } else {
            return -1;
        }
    }

    public void giveToPlayer(int type, PlayerEntity player) {
        if (locked) {
            Ariente.guiHandler.openHoloGui(world, pos, player);
            return;
        }
        if (totals[type] == 0) {
            return;
        }
        ItemStack stack = getStackFromType(type, player.isSneaking() ? 10000 : 1);
        if (!stack.isEmpty()) {
            if (player.getHeldItemMainhand().isEmpty()) {
                player.setHeldItem(Hand.MAIN_HAND, stack);
            } else if (ItemHandlerHelper.canItemStacksStack(player.getHeldItemMainhand(), stack)) {
                boolean added = player.inventory.add(player.inventory.currentItem, stack);
                if (!added) {
                    if (!player.inventory.addItemStackToInventory(stack)) {
                        player.entityDropItem(stack, 1.05f);
                    }
                }
            } else {
                if (!player.inventory.addItemStackToInventory(stack)) {
                    player.entityDropItem(stack, 1.05f);
                }
            }
            player.openContainer.detectAndSendChanges();
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

//        totals[type] = 0;
//        for (int i = type * STACKS_PER_TYPE; i < (type+1) * STACKS_PER_TYPE; i++) {
//            totals[type] += counts[i];
//        }

        if (totals[type] == 0) {
            // @todo lock
            stacks.set(type, ItemStack.EMPTY);
            markDirtyClient();
        }
    }

    public static void onClick(World world, BlockPos pos, PlayerEntity player) {
        if (world.isRemote) {
            // On client. We find out what part of the block was hit and send that to the server.
            RayTraceResult mouseOver = Minecraft.getInstance().objectMouseOver;
            if (mouseOver instanceof BlockRayTraceResult) {
                int index = getSlot((BlockRayTraceResult) mouseOver, world);
                if (index >= 0) {
                    ArienteMessages.INSTANCE.sendToServer(new PacketClickStorage(pos, index));
                }
            }
        }
    }

    private static long doubleClickTime = -1;

    public static boolean onActivate(World world, BlockPos pos, PlayerEntity player, Direction side, double sx, double sy, double sz) {
        Direction k = Registration.STORAGE.get().getFrontDirection(world.getBlockState(pos));
        if (side == k) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!world.isRemote) {
                if (tileEntity instanceof StorageTile) {
                    StorageTile te = (StorageTile) tileEntity;
                    if (te.isLocked()) {
                        Ariente.guiHandler.openHoloGui(world, pos, player);
                        return true;
                    }

                    int type = calculateHitIndex(sx, sy, sz, k);

                    if (type == -1) {
                        Ariente.guiHandler.openHoloGui(world, pos, player);
                        return true;
                    }

                    ItemStack heldItem = player.getHeldItemMainhand();
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
                    player.openContainer.detectAndSendChanges();
                }
            }
        }
        return true;
    }

    private static void placeAll(PlayerEntity player, StorageTile te, int type, ItemStack heldItem) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (ItemHandlerHelper.canItemStacksStack(stack, heldItem)) {
                ItemStack remaining = insertItem(te.getInvHandler(), stack, type);
                player.inventory.setInventorySlotContents(i, remaining);
                if (!remaining.isEmpty()) {
                    break;  // Storage is full
                }
            }
        }
    }

    private static void placeHeld(PlayerEntity player, StorageTile te, int type, ItemStack heldItem) {
        ItemStack remaining = insertItem(te.getInvHandler(), heldItem, type);
        player.setHeldItem(Hand.MAIN_HAND, remaining);
    }

    @Nonnull
    private static ItemStack insertItem(IItemHandler dest, @Nonnull ItemStack stack, int type) {
        if (dest == null || stack.isEmpty()) {
            return stack;
        }

        for (int i = type * STACKS_PER_TYPE; i < (type + 1) * STACKS_PER_TYPE; i++) {
            stack = dest.insertItem(i, stack, false);
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

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        return Ariente.guiHandler.createNoAccessPanel();
    }

    @Override
    public void syncToClient() {

    }

    @Override
    public void setLoot(ResourceLocation id, int i, boolean doBlueprint, int amount, int meta) {
        if (id == null) {
            // Random blueprint
            ConstructorRecipe recipe = BlueprintRecipeRegistry.getRandomRecipes().getRandom();
            ItemStack blueprint = BlueprintItem.makeBluePrint(recipe.getDestination());
            initTotalStack(i, blueprint);
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(id);
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

    private class StorageItemHandler implements IItemHandlerModifiable {
        @Override
        public int getSlots() {
            return STACKS * STACKS_PER_TYPE;
        }

        @Nonnull
        @Override
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
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack stackInSlot = getStackInSlot(slot);

            int m;
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot)))
                    return stack;

                if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
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
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0)
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

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int index, @Nonnull ItemStack stack) {
            int type = index / STACKS_PER_TYPE;
            if (stacks.get(type).isEmpty()) {
                return true;
            } else {
                return ItemHandlerHelper.canItemStacksStack(stacks.get(type), stack);
            }
        }

        @Override
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

    private IItemHandler invHandler;
    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(this::getInvHandler);

    private IItemHandler getInvHandler() {
        if (invHandler == null) {
            invHandler = new StorageItemHandler();
        }
        return invHandler;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        return super.getCapability(cap);
    }
}
