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
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.api.components.ISlots;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.ItemStackTools;
import mcjty.lib.varia.NBTTools;
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

    private ItemStack focus = ItemStack.EMPTY;

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

    public ItemStack getFocus() {
        return focus;
    }

    public void setFocus(ItemStack focus) {
        this.focus = focus;
        markDirtyClient();
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
        if (tagCompound.hasKey("focus")) {
            focus = new ItemStack(tagCompound.getCompoundTag("focus"));
        } else {
            focus = ItemStack.EMPTY;
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        NBTTagCompound focusNBT = new NBTTagCompound();
        focus.writeToNBT(focusNBT);
        tagCompound.setTag("focus", focusNBT);
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

                .add(registry.iconChoice(7.5, 7.5, 1, 1)
                        .getter((player) -> getRSModeInt())
                        .icon(128, 128+32)
                        .icon(128+16, 128+32)
                        .icon(128+32, 128+32)
                        .hitEvent((component, player, entity1, x, y) -> changeMode()))
                ;
    }


    private IGuiComponent createBlueprintGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 0, 8, 1).text("Blueprints").color(0xaaccff))

                .add(registry.icon(0, 2, 1, 1).icon(128+64, 128))
                .add(registry.playerSlots(1.5, 1.5, 6, 2)
                        .name("playerslots")
                        .filter(stack -> stack.getItem() instanceof BlueprintItem))

                .add(registry.iconButton(2, 3.5, 1, 1).icon(128, 128-16).hover(128+16, 128-16)
                        .hitEvent((component, player, entity, x, y) -> transferToMachine(player, entity)))
                .add(registry.iconButton(3, 3.5, 1, 1).icon(128+32, 128-16).hover(128+32+16, 128-16)
                        .hitEvent((component, player, entity, x, y) -> transferToPlayer(player, entity)))

                .add(registry.stackIcon(0, 4.5, 1, 1).itemStack(new ItemStack(ModBlocks.constructorBlock)))
                .add(registry.slots(1.5, 4.5, 6, 1)
                        .name("slots")
                        .filter(stack -> stack.getItem() instanceof BlueprintItem)
                        .itemHandler(getItemHandler()))
                .add(registry.button(7.8, 4.5, 1, 1)
                        .hitEvent((component, player, entity, x, y) -> setFocus(entity))
                        .text("F"))

                .add(registry.text(0, 6.5, 2, 1)
                        .text("Focus")
                        .color(0xaaccff))
                .add(registry.stackIcon(4, 6.5, 1.5, 1.5)
                        .scale(1.5)
                        .itemStack(this::getFocus))
                ;
    }

    private IItemHandler getItemHandler() {
        return getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    private void setFocus(IHoloGuiEntity entity) {
        entity.findComponent("slots").ifPresent(component -> {
            if (component instanceof ISlots) {
                int selected = ((ISlots) component).getSelected();
                ItemStack stack = ItemStack.EMPTY;
                if (selected != -1) {
                    stack = getItemHandler().getStackInSlot(selected);
                }
                setFocus(stack);
            }
        });
    }

    private void transferToPlayer(EntityPlayer player, IHoloGuiEntity entity) {
        entity.findComponent("slots").ifPresent(component -> {
            if (component instanceof ISlots) {
                int selected = ((ISlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = getItemHandler().extractItem(selected, 64, false);
                    if (!extracted.isEmpty()) {
                        if (!player.inventory.addItemStackToInventory(extracted)) {
                            getItemHandler().insertItem(selected, extracted, false);
                        } else {
                            ((ISlots) component).setSelection(-1);
                        }
                        markDirtyClient();
                    }
                }
            }
        });
    }

    private void transferToMachine(EntityPlayer player, IHoloGuiEntity entity) {
        entity.findComponent("playerslots").ifPresent(component -> {
            if (component instanceof IPlayerSlots) {
                int selected = ((IPlayerSlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = player.inventory.getStackInSlot(selected);
                    if (!extracted.isEmpty()) {
                        ItemStack notInserted = ItemHandlerHelper.insertItem(getItemHandler(), extracted, false);
                        player.inventory.setInventorySlotContents(selected, notInserted);
                        if (notInserted.isEmpty()) {
                            ((IPlayerSlots) component).setSelection(-1);
                        }
                        markDirtyClient();
                    }
                }
            }
        });
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
