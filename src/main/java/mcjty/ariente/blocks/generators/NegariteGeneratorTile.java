package mcjty.ariente.blocks.generators;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.entities.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.*;
import mcjty.ariente.items.ModItems;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.RedstoneMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class NegariteGeneratorTile extends GenericTileEntity implements ITickable, DefaultSidedInventory, IGuiTile {

    public static final String CMD_RSMODE = "negarite_gen.setRsMode";

    public static final PropertyBool WORKING = PropertyBool.create("working");

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(Ariente.MODID, "gui/negarite_generator.gui"));
    public static final int SLOT_NEGARITE_INPUT = 0;
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);

    private boolean working = false;

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    public void update() {
        long time = System.currentTimeMillis();
        if ((time / 2000) %2 == 0) {
            setWorking(true);
        } else {
            setWorking(false);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean working = isWorking();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            boolean newWorking = isWorking();
            if (newWorking != working) {
                world.markBlockRangeForRenderUpdate(getPos(), getPos());
                BlockPos p = pos.up();
                IBlockState state = world.getBlockState(p);
                while (state.getBlock() == ModBlocks.negariteTankBlock) {
                    world.markBlockRangeForRenderUpdate(p, p);
                    p = p.up();
                    state = world.getBlockState(p);
                }


            }
        }
    }


    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(WORKING, isWorking());
    }

    private void setWorking(boolean working) {
        if (this.working == working) {
            return;
        }
        this.working = working;
        markDirtyClient();
    }

    public boolean isWorking() {
        return working;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { SLOT_NEGARITE_INPUT };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_NEGARITE_INPUT) {
            return stack.getItem() == ModItems.negariteDust;
        }
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        working = tagCompound.getBoolean("working");

        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setBoolean("working", working);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            setRSMode(RedstoneMode.values()[params.get(ImageChoiceLabel.PARAM_CHOICE_IDX)]);
            return true;
        }

        return false;
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


    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (isWorking()) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    @Override
    public IGuiComponent createGui(HoloGuiEntity entity) {
        return new HoloPanel(0, 0, 8, 8)
                .add(new HoloText(0, 0, 8, 1,"Negarite", 0xaaccff))
                .add(new HoloItemStack(0, 3, 1, 1, new ItemStack(ModItems.negariteDust)))

                .add(new HoloIcon(1, 3, 1, 1).image(128+64, 128))
                .add(new HoloNumber(2, 3, 1, 1, 0xffffff,
                        this::countNegarite))

                .add(new HoloButton(2, 4, 1, 1).image(128+32, 128+16).hover(128+32+16, 128+16)
                    .hitEvent((component, player, entity1, x, y) -> toPlayer(player, 64)))
                .add(new HoloButton(3, 4, 1, 1).image(128+32, 128).hover(128+32+16, 128)
                    .hitEvent((component, player, entity1, x, y) -> toPlayer(player, 1)))
                .add(new HoloButton(5, 4, 1, 1).image(128, 128).hover(128+16, 128)
                    .hitEvent((component, player, entity1, x, y) -> toMachine(player, 1)))
                .add(new HoloButton(6, 4, 1, 1).image(128, 128+16).hover(128+16, 128+16)
                    .hitEvent((component, player, entity1, x, y) -> toMachine(player, 64)))

                .add(new HoloItemStack(5, 3, 1, 1, new ItemStack(ModBlocks.negariteGeneratorBlock)))
                .add(new HoloNumber(6, 3, 1, 1,0xffffff, this::countNegariteGenerator))

                .add(new HoloMode(7, 6, 1, 1, this::getRSModeInt)
                    .choice(128, 128+32)
                    .choice(128+16, 128+32)
                    .choice(128+32, 128+32)
                    .hitEvent((component, player, entity1, x, y) -> changeMode()))
                ;
    }

    private void changeMode() {
        int current = rsMode.ordinal() + 1;
        if (current >= RedstoneMode.values().length) {
            current = 0;
        }
        setRSMode(RedstoneMode.values()[current]);
        markDirtyClient();
    }

    private void toPlayer(EntityPlayer player, int amount) {
        ItemStack stack = inventoryHelper.decrStackSize(SLOT_NEGARITE_INPUT, amount);
        if ((!stack.isEmpty()) && player.inventory.addItemStackToInventory(stack)) {
            markDirtyClient();
        } else {
            ItemStack stillThere = inventoryHelper.getStackInSlot(SLOT_NEGARITE_INPUT);
            if (stillThere.isEmpty()) {
                stillThere = stack;
            } else {
                stillThere.grow(stack.getCount());
            }
            inventoryHelper.setStackInSlot(SLOT_NEGARITE_INPUT, stillThere);
        }
    }

    private void toMachine(EntityPlayer player, int amount) {
        ItemStack toTransfer = ItemStack.EMPTY;
        ItemStack stackInSlot = inventoryHelper.getStackInSlot(SLOT_NEGARITE_INPUT);
        if (!stackInSlot.isEmpty()) {
            amount = Math.min(amount, 64 - stackInSlot.getCount());    // @todo item specific max stacksize
        }

        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.getItem() == ModItems.negariteDust) {
                ItemStack splitted = stack.splitStack(amount);
                if ((!splitted.isEmpty())) {
                    if (toTransfer.isEmpty()) {
                        toTransfer = splitted;
                    } else {
                        toTransfer.grow(amount);
                    }
                    amount -= splitted.getCount();
                    if (amount <= 0) {
                        break;
                    }
                }
            }
        }

        if (!toTransfer.isEmpty()) {
            if (!stackInSlot.isEmpty()) {
                toTransfer.grow(stackInSlot.getCount());
            }
            inventoryHelper.setStackInSlot(SLOT_NEGARITE_INPUT, toTransfer);
            markDirtyClient();
        }

    }

    @Override
    public void syncToServer() {
        // @todo more efficient
        markDirtyClient();
    }

    public Integer countNegarite() {
        InventoryPlayer inventory = Ariente.proxy.getClientPlayer().inventory;
        int size = inventory.getSizeInventory();
        int cnt = 0;
        for (int i = 0 ; i < size ; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.negariteDust) {
                cnt += stack.getCount();
            }
        }
        return cnt;
    }

    public Integer countNegariteGenerator() {
        int size = inventoryHelper.getCount();
        int cnt = 0;
        for (int i = 0 ; i < size ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.negariteDust) {
                cnt += stack.getCount();
            }
        }
        return cnt;
    }
}
