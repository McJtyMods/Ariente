package mcjty.ariente.blocks.utility;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.HoloGuiEntity;
import mcjty.ariente.gui.HoloGuiHandler;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.network.PacketClickStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ItemStackList;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import java.util.List;

public class StorageTile extends GenericTileEntity implements IGuiTile, IInventory {

    public static final int STACKS_PER_TYPE = 64;
    public static final int STACKS = 4;

    private ItemStackList stacks = ItemStackList.create(4);
    private int[] counts = new int[STACKS * STACKS_PER_TYPE];
    private int[] totals = new int[STACKS];

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < STACKS; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks.set(i, new ItemStack(nbtTagCompound));
        }
        int[] cc = tagCompound.getIntArray("Counts");
        System.arraycopy(cc, 0, counts, 0, cc.length);
        int[] ct = tagCompound.getIntArray("Totals");
        System.arraycopy(ct, 0, totals, 0, ct.length);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0; i < STACKS; i++) {
            ItemStack stack = stacks.get(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (!stack.isEmpty()) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
        tagCompound.setIntArray("Counts", counts);
        tagCompound.setIntArray("Totals", totals);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        Boolean working = isWorking();
//        if (working) {
//            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
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

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (isWorking()) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    public static int getSlot(RayTraceResult mouseOver, World world) {
        return getSlot(world, mouseOver.getBlockPos(), mouseOver.sideHit, mouseOver.hitVec);
    }

    public static int getSlot(World world, BlockPos pos, EnumFacing sideHit, Vec3d hitVec) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        EnumFacing k = ModBlocks.storageBlock.getFrontDirection(world.getBlockState(pos));
        if (sideHit == k) {
            float sx = (float) (hitVec.x - x);
            float sy = (float) (hitVec.y - y);
            float sz = (float) (hitVec.z - z);
            return calculateHitIndex(sx, sy, sz, k);
        } else {
            return -1;
        }
    }

    public void giveToPlayer(int type, EntityPlayer player) {
        if (totals[type] == 0) {
            return;
        }
        ItemStack stack = getStackFromType(type, player.isSneaking() ? 10000 : 1);
        if (!stack.isEmpty()) {
            if (player.getHeldItemMainhand().isEmpty()) {
                player.setHeldItem(EnumHand.MAIN_HAND, stack);
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
            for (int i = type * STACKS_PER_TYPE ; i < (type+1) * STACKS_PER_TYPE ; i++) {
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

    public static void onClick(World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            // On client. We find out what part of the block was hit and send that to the server.
            RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
            int index = getSlot(mouseOver, world);
            if (index >= 0) {
                ArienteMessages.INSTANCE.sendToServer(new PacketClickStorage(pos, index));
            }
        }
    }

    private static long doubleClickTime = -1;

    public static boolean onActivate(World world, BlockPos pos, EntityPlayer player, EnumFacing side, float sx, float sy, float sz) {
        EnumFacing k = ModBlocks.storageBlock.getFrontDirection(world.getBlockState(pos));
        if (side == k) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (!world.isRemote) {
                if (tileEntity instanceof StorageTile) {
                    StorageTile te = (StorageTile) tileEntity;
                    int type = calculateHitIndex(sx, sy, sz, k);

                    if (type == -1) {
                        HoloGuiHandler.openHoloGui(world, pos, player);
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

    private static void placeAll(EntityPlayer player, StorageTile te, int type, ItemStack heldItem) {
        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
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

    private static void placeHeld(EntityPlayer player, StorageTile te, int type, ItemStack heldItem) {
        ItemStack remaining = insertItem(te.getInvHandler(), heldItem, type);
        player.setHeldItem(EnumHand.MAIN_HAND, remaining);
    }

    @Nonnull
    private static ItemStack insertItem(IItemHandler dest, @Nonnull ItemStack stack, int type) {
        if (dest == null || stack.isEmpty()) {
            return stack;
        }

        for (int i = type * STACKS_PER_TYPE; i < (type+1) * STACKS_PER_TYPE; i++) {
            stack = dest.insertItem(i, stack, false);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }


    private static int calculateHitIndex(float sx, float sy, float sz, EnumFacing k) {
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
    public IGuiComponent createGui(HoloGuiEntity entity, String tag) {
        return new HoloPanel(0, 0, 8, 8)
                .add(new HoloText(0, 0, 1, 1, "0", 0xffffff))
                .add(new HoloText(1, 0, 1, 1, "1", 0xffffff))
                .add(new HoloText(2, 0, 1, 1, "2", 0xffffff))
                .add(new HoloText(3, 0, 1, 1, "3", 0xffffff))
                .add(new HoloText(4, 0, 1, 1, "4", 0xffffff))
                .add(new HoloText(5, 0, 1, 1, "5", 0xffffff))
                .add(new HoloText(6, 0, 1, 1, "6", 0xffffff))
                .add(new HoloText(7, 0, 1, 1, "7", 0xffffff))
                .add(new HoloText(0, 1, 1, 1, "1", 0x00ff00))
                .add(new HoloText(0, 2, 1, 1, "2", 0x00ff00))
                .add(new HoloText(0, 3, 1, 1, "3", 0x00ff00))
                .add(new HoloText(0, 4, 1, 1, "4", 0x00ff00))
                .add(new HoloText(0, 5, 1, 1, "5", 0x00ff00))
                .add(new HoloText(0, 6, 1, 1, "6", 0x00ff00))
                .add(new HoloText(0, 7, 1, 1, "7", 0x00ff00))
                .add(new HoloText(7, 7, 1, 1, "X", 0xff0000));
    }

    @Override
    public void syncToServer() {

    }

    @Override
    public int getSizeInventory() {
        return STACKS * STACKS_PER_TYPE;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

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

    @Override
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
                markDirtyQuick();
            }
            return stack;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (counts[index] == 0) {
            return ItemStack.EMPTY;
        } else {
            int type = index / STACKS_PER_TYPE;
            ItemStack stack = stacks.get(type).copy();
            if (!stack.isEmpty()) {
                totals[type] -= counts[index];
                stack.setCount(counts[index]);
                counts[index] = 0;
                updateTotals(type);
                markDirtyQuick();
            }
            return stack;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
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
        markDirtyQuick();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        int type = index / STACKS_PER_TYPE;
        if (stacks.get(type).isEmpty()) {
            return true;
        } else {
            return ItemHandlerHelper.canItemStacksStack(stacks.get(type), stack);
        }
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return "storage";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    private IItemHandler invHandler;

    private IItemHandler getInvHandler() {
        if (invHandler == null) {
            invHandler = new InvWrapper(this);
        }
        return invHandler;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getInvHandler());
        }
        return super.getCapability(capability, facing);
    }
}
