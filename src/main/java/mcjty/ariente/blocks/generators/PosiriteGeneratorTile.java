package mcjty.ariente.blocks.generators;

import mcjty.ariente.Ariente;
import mcjty.ariente.ai.IAlarmMode;
import mcjty.ariente.api.hologui.IGuiComponent;
import mcjty.ariente.api.hologui.IGuiComponentRegistry;
import mcjty.ariente.api.hologui.IGuiTile;
import mcjty.ariente.api.hologui.IHoloGuiEntity;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cables.CableColor;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.ariente.power.PowerSystem;
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
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

public class PosiriteGeneratorTile extends GenericTileEntity implements ITickable, DefaultSidedInventory, IGuiTile, IPowerBlob, IAlarmMode {

    public static final String CMD_RSMODE = "posirite_gen.setRsMode";

    public static final PropertyBool WORKING = PropertyBool.create("working");

    public static final int POWERGEN = 1000;        // @todo configurable and based on tanks!

    public static final int SLOT_POSIRITE_INPUT = 0;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(Ariente.MODID, "gui/posirite_generator.gui"));
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);
    private PowerSenderSupport powerBlobSupport = new PowerSenderSupport();

    // @todo, temporary: base on tanks later!
    private int dustCounter;        // Number of ticks before the current dust depletes

    @Override
    public boolean isHighAlert() {
        return false;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (!isMachineEnabled()) {
                return;
            }
            if (dustCounter > 0) {
                dustCounter--;
                if (dustCounter == 0 && !canProceed()) {
                    markDirtyClient();
                } else {
                    markDirtyQuick();
                }
                sendPower();
            } else {
                if (canProceed()) {
                    inventoryHelper.decrStackSize(SLOT_POSIRITE_INPUT, 1);
                    dustCounter = 600;
                    markDirtyQuick();
                    sendPower();
                }
            }
        }
    }

    private boolean canProceed() {
        ItemStack stack = inventoryHelper.getStackInSlot(SLOT_POSIRITE_INPUT);
        return !stack.isEmpty() && stack.getItem() == ModItems.posiriteDust;
    }

    private void sendPower() {
        int cnt = 0;
        BlockPos p = pos.up();
        while (world.getTileEntity(p) instanceof PosiriteTankTile) {
            cnt++;
            p = p.up();
        }
        if (cnt > 0) {
            PowerSystem powerSystem = PowerSystem.getPowerSystem(world);
            powerSystem.addPower(powerBlobSupport.getCableId(), POWERGEN * cnt);
        }
    }

    @Override
    public boolean canSendPower() {
        return true;
    }


    @Override
    public void setPowerInput(int powered) {
        boolean changed = powerLevel != powered;
        super.setPowerInput(powered);
        if (changed) {
            markDirtyClient();
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
            }
        }
    }


    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(WORKING, isWorking());
    }

    public boolean isWorking() {
        return dustCounter > 0 && isMachineEnabled();
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] {SLOT_POSIRITE_INPUT};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_POSIRITE_INPUT) {
            return stack.getItem() == ModItems.posiriteDust;
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
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerBlobSupport.setCableId(tagCompound.getInteger("cableId"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("cableId", powerBlobSupport.getCableId());
        return super.writeToNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        dustCounter = tagCompound.getInteger("dust");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("dust", dustCounter);
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
        probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + powerBlobSupport.getCableId());
        if (isWorking()) {
            probeInfo.text(TextStyleClass.LABEL + "Generating: " + TextStyleClass.INFO + POWERGEN + " flux");
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

    @Override
    public int getCableId() {
        return powerBlobSupport.getCableId();
    }

    @Override
    public void fillCableId(int id) {
        powerBlobSupport.fillCableId(world, pos, id, getCableColor());
    }

    @Override
    public CableColor getCableColor() {
        return CableColor.POSIRITE;
    }


    @Override
    public IGuiComponent createGui(String tag, IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 0, 8, 1).text("Posirite").color(0xaaccff))
                .add(registry.stackIcon(0, 3, 1, 1).itemStack(new ItemStack(ModItems.posiriteDust)))

                .add(registry.icon(1, 3, 1, 1).image(128+64, 128))
                .add(registry.number(2, 3, 1, 1).color(0xffffff).getter((p,h) -> HoloGuiTools.countItem(p, ModItems.posiriteDust)))

                .add(registry.iconButton(2, 4, 1, 1).image(128+32, 128+16).hover(128+32+16, 128+16)
                    .hitEvent((component, player, entity1, x, y) -> toPlayer(player, 64)))
                .add(registry.iconButton(3, 4, 1, 1).image(128+32, 128).hover(128+32+16, 128)
                    .hitEvent((component, player, entity1, x, y) -> toPlayer(player, 1)))
                .add(registry.iconButton(5, 4, 1, 1).image(128, 128).hover(128+16, 128)
                    .hitEvent((component, player, entity1, x, y) -> toMachine(player, 1)))
                .add(registry.iconButton(6, 4, 1, 1).image(128, 128+16).hover(128+16, 128+16)
                    .hitEvent((component, player, entity1, x, y) -> toMachine(player, 64)))

                .add(registry.stackIcon(5, 3, 1, 1).itemStack(new ItemStack(ModBlocks.posiriteGeneratorBlock)))
                .add(registry.number(6, 3, 1, 1).color(0xffffff).getter(this::countPosiriteGenerator))

                .add(registry.modeToggle(7, 6, 1, 1)
                        .getter(this::getRSModeInt)
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
        ItemStack stack = inventoryHelper.decrStackSize(SLOT_POSIRITE_INPUT, amount);
        if ((!stack.isEmpty()) && player.inventory.addItemStackToInventory(stack)) {
            markDirtyClient();
        } else {
            ItemStack stillThere = inventoryHelper.getStackInSlot(SLOT_POSIRITE_INPUT);
            if (stillThere.isEmpty()) {
                stillThere = stack;
            } else {
                stillThere.grow(stack.getCount());
            }
            inventoryHelper.setStackInSlot(SLOT_POSIRITE_INPUT, stillThere);
        }
    }

    private void toMachine(EntityPlayer player, int amount) {
        ItemStack toTransfer = ItemStack.EMPTY;
        ItemStack stackInSlot = inventoryHelper.getStackInSlot(SLOT_POSIRITE_INPUT);
        if (!stackInSlot.isEmpty()) {
            amount = Math.min(amount, 64 - stackInSlot.getCount());    // @todo item specific max stacksize
        }

        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.getItem() == ModItems.posiriteDust) {
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
            inventoryHelper.setStackInSlot(SLOT_POSIRITE_INPUT, toTransfer);
            markDirtyClient();
        }

    }

    @Override
    public void syncToClient() {
        // @todo more efficient
        markDirtyClient();
    }

    public Integer countPosiriteGenerator(EntityPlayer player, IHoloGuiEntity holo) {
        int size = inventoryHelper.getCount();
        int cnt = 0;
        for (int i = 0 ; i < size ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.posiriteDust) {
                cnt += stack.getCount();
            }
        }
        return cnt;
    }


    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            PowerSenderSupport.fixNetworks(world, pos);
        }
    }

    @Override
    public void onBlockBreak(World workd, BlockPos pos, IBlockState state) {
        super.onBlockBreak(workd, pos, state);
        if (!world.isRemote) {
            PowerSenderSupport.fixNetworks(world, pos);
        }
    }
}
