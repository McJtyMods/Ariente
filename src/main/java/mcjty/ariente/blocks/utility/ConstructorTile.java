package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.ai.CityAI;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.ICityEquipment;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.RedstoneMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ConstructorTile extends GenericTileEntity implements DefaultSidedInventory, IGuiTile, ITickable, IPowerReceiver, ICityEquipment {

    public static final PropertyBool WORKING = PropertyBool.create("working");
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(Ariente.MODID, "gui/constructor.gui"));
    public static final int SLOT_BLUEPRINT = 0;
    public static final int BLUEPRINTS = 6;
    public static final int[] SLOTS = {SLOT_BLUEPRINT, SLOT_BLUEPRINT + 1, SLOT_BLUEPRINT + 2, SLOT_BLUEPRINT + 3, SLOT_BLUEPRINT + 4, SLOT_BLUEPRINT + 5};
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, BLUEPRINTS);

    public static String TAG_BLUEPRINTS = "blueprints";
    public static String TAG_CRAFTING = "crafting";

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    public void update() {
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(WORKING, isWorking());
    }

    public boolean isWorking() {
        return isMachineEnabled();
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
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index >= SLOT_BLUEPRINT && index < SLOT_BLUEPRINT + BLUEPRINTS) {
            return stack.getItem() == ModItems.blueprintItem;
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
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        probeInfo.text(TextStyleClass.LABEL + "Using: " + TextStyleClass.INFO + POWER_USAGE + " flux");
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
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
    public void setup(CityAI cityAI, World world, boolean firstTime) {

    }

    @Override
    public IGuiComponent createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_BLUEPRINTS.equals(tag)) {
            return createBlueprintGui(registry);
        } else if (TAG_CRAFTING.equals(tag)) {
            return createCrartingGui(registry);
        } else {
            return createMainMenuGui(registry);
        }
    }

    private IGuiComponent createMainMenuGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 1, 1, 1).text("Main menu").color(0xaaccff))
                .add(registry.stackIcon(0.5, 2.5, 1, 1).itemStack(new ItemStack(ModItems.blueprintItem)))
                .add(registry.button(2, 2.5, 5, 1)
                        .text("Blueprints")
                        .hitEvent((component, p, entity, x1, y1) -> switchBlueprintGui(p, entity)))
                .add(registry.stackIcon(0.5, 3.5, 1, 1).itemStack(new ItemStack(Blocks.CRAFTING_TABLE)))
                .add(registry.button(2, 3.5, 5, 1)
                        .text("Crafting")
                        .hitEvent((component, p, entity, x1, y1) -> switchCraftingGui(p, entity)))
                ;
    }

    private void switchBlueprintGui(EntityPlayer player, IHoloGuiEntity entity) {
        entity.switchTag(TAG_BLUEPRINTS);
    }

    private void switchCraftingGui(EntityPlayer player, IHoloGuiEntity entity) {
        entity.switchTag(TAG_CRAFTING);
    }

    private IGuiComponent createCrartingGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.stackIcon(1, 1, 1, 1).itemStack(new ItemStack(ModItems.platinumIngot)))
                .add(registry.stackIcon(2, 1, 1, 1).itemStack(new ItemStack(ModItems.silverIngot)))
                .add(registry.stackIcon(3, 1, 1, 1).itemStack(new ItemStack(ModItems.platinumIngot)))
                .add(registry.stackIcon(1, 2, 1, 1).itemStack(new ItemStack(ModItems.silverIngot)))
                .add(registry.stackIcon(2, 2, 1, 1).itemStack(new ItemStack(ModItems.negariteDust)))
                .add(registry.stackIcon(3, 2, 1, 1).itemStack(new ItemStack(ModItems.silverIngot)))
                .add(registry.stackIcon(1, 3, 1, 1).itemStack(new ItemStack(ModItems.platinumIngot)))
                .add(registry.stackIcon(2, 3, 1, 1).itemStack(new ItemStack(ModItems.silverIngot)))
                .add(registry.stackIcon(3, 3, 1, 1).itemStack(new ItemStack(ModItems.platinumIngot)))
                ;
    }


    private IGuiComponent createBlueprintGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 0, 8, 1).text("Blueprints").color(0xaaccff))
//                .add(registry.stackIcon(0, 3, 1, 1).itemStack(new ItemStack(ModItems.negariteDust)))

//                .add(registry.iconButton(2, 4, 1, 1).icon(128+32, 128+16).hover(128+32+16, 128+16)
//                        .hitEvent((component, player, entity1, x, y) -> toPlayer(player, 64)))
//                .add(registry.iconButton(3, 4, 1, 1).icon(128+32, 128).hover(128+32+16, 128)
//                        .hitEvent((component, player, entity1, x, y) -> toPlayer(player, 1)))
//                .add(registry.iconButton(5, 4, 1, 1).icon(128, 128).hover(128+16, 128)
//                        .hitEvent((component, player, entity1, x, y) -> toMachine(player, 1)))
//                .add(registry.iconButton(6, 4, 1, 1).icon(128, 128+16).hover(128+16, 128+16)
//                        .hitEvent((component, player, entity1, x, y) -> toMachine(player, 64)))

//                .add(registry.stackIcon(5, 3, 1, 1).itemStack(new ItemStack(ModBlocks.negariteGeneratorBlock)))
//                .add(registry.number(6, 3, 1, 1).color(0xffffff).getter(this::countNegariteGenerator))

                .add(registry.stackIcon(0, 2, 1, 1).itemStack(new ItemStack(ModBlocks.constructorBlock)))
                .add(registry.slots(1.5, 2, 6, 1)
                        .filter(stack -> stack.getItem() instanceof BlueprintItem)
                        .hitEvent((component, player, entity, x, y, stack, i) -> transferToPlayer(player, stack, i))
                        .itemHandler(getItemHandler()))
                .add(registry.icon(0, 5, 1, 1).icon(128+64, 128))
                .add(registry.playerSlots(1.5, 5, 6, 2)
                        .hitEvent((component, player, entity, x, y, stack, i) -> transferToMachine(player, stack, i))
                        .filter(stack -> stack.getItem() instanceof BlueprintItem))

                .add(registry.iconChoice(7.5, 7.5, 1, 1)
                        .getter((player) -> getRSModeInt())
                        .icon(128, 128+32)
                        .icon(128+16, 128+32)
                        .icon(128+32, 128+32)
                        .hitEvent((component, player, entity1, x, y) -> changeMode()))
                ;
    }

    private IItemHandler getItemHandler() {
        return getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    private void transferToPlayer(EntityPlayer player, ItemStack stack, int index) {
        if (!stack.isEmpty()) {
            IItemHandler handler = getItemHandler();
            ItemStack extracted = handler.extractItem(index, stack.getCount(), false);
            player.inventory.addItemStackToInventory(extracted);
            markDirtyClient();
        }
    }

    private void transferToMachine(EntityPlayer player, ItemStack stack, int index) {
        if (!stack.isEmpty()) {
            IItemHandler handler = getItemHandler();
            ItemStack notInserted = ItemHandlerHelper.insertItem(handler, stack, false);
            player.inventory.setInventorySlotContents(index, notInserted);
            markDirtyClient();
        }
    }

    private void changeMode() {
        int current = rsMode.ordinal() + 1;
        if (current >= RedstoneMode.values().length) {
            current = 0;
        }
        setRSMode(RedstoneMode.values()[current]);
        markDirtyClient();
    }

    @Override
    public void syncToClient() {
    }
}
