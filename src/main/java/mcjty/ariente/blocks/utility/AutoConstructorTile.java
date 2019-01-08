package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.ai.CityAI;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.ICityEquipment;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.RecipeRegistry;
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
import mcjty.lib.varia.RedstoneMode;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
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

import static mcjty.ariente.blocks.utility.BlueprintStorageTile.BLUEPRINTS;
import static mcjty.ariente.blocks.utility.BlueprintStorageTile.SLOT_BLUEPRINT;
import static mcjty.hologui.api.Icons.*;

public class AutoConstructorTile extends GenericTileEntity implements DefaultSidedInventory, IGuiTile, ITickable, IPowerReceiver, ICityEquipment {

    public static final PropertyBool WORKING = PropertyBool.create("working");
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(Ariente.MODID, "gui/constructor.gui"));
    public static final int INGREDIENTS = 6*2;
    public static final int OUTPUT = 6;
    public static final int SLOT_INGREDIENTS = 0;
    public static final int SLOT_OUTPUT = SLOT_INGREDIENTS + INGREDIENTS;
    public static int[] slots = null;
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, INGREDIENTS + OUTPUT);

    public static String TAG_INGREDIENTS = "ingredients";
    public static String TAG_CRAFTING = "crafting";

    private ItemStack focus = ItemStack.EMPTY;
    private long usingPower = 0;
    private int craftIndex = 0;

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    private void attemptCraft(ItemStack blueprintStack) {
        if (PowerReceiverSupport.consumePower(world, pos, 100, true)) {
            boolean wasUsing = usingPower > 0;
            usingPower = 100;
            boolean isUsing = usingPower > 0;
            if (isUsing != wasUsing) {
                markDirtyClient();
            }
        }
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            usingPower = 0;
            if (isMachineEnabled()) {
//                for (int i = 0; i < BLUEPRINTS; i++) {
//                    int index = SLOT_BLUEPRINT + ((i + craftIndex) % BLUEPRINTS);
//                    ItemStack blueprintStack = inventoryHelper.getStackInSlot(index);
//                    if (!blueprintStack.isEmpty()) {
//                        if (!focus.isEmpty()) {
//                            if (ItemHandlerHelper.canItemStacksStack(blueprintStack, focus)) {
//                                attemptCraft(blueprintStack);
//                                break;
//                            }
//                        } else {
//                            attemptCraft(blueprintStack);
//                            break;
//                        }
//                    }
//                }
//
//                craftIndex++;
//                if (craftIndex >= BLUEPRINTS) {
//                    craftIndex = 0;
//                }
//                markDirtyQuick();

            }
        }
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
        return usingPower > 0 && isMachineEnabled();
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
        if (slots == null) {
            slots = new int[inventoryHelper.getCount()];
            for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
                slots[i] = i;
            }
        }
        return slots;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        if (isOutputSlot(index)) {
            return false;
        }
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (isIngredientSlot(index)) {
            return stack.getItem() != ModItems.blueprintItem;
        }
        return true;
    }

    private boolean isOutputSlot(int index) {
        return index >= SLOT_OUTPUT;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return isOutputSlot(index);
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
        craftIndex = tagCompound.getInteger("craftIndex");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("craftIndex", craftIndex);
        return super.writeToNBT(tagCompound);
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
        probeInfo.text(TextStyleClass.LABEL + "Using: " + TextStyleClass.INFO + usingPower + " flux");
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
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_INGREDIENTS.equals(tag)) {
            return createIngredientsGui(registry);
        } else if (TAG_CRAFTING.equals(tag)) {
            return createCraftingGui(registry);
        } else {
            return createMainMenuGui(registry);
        }
    }

    private IGuiComponent<?> createMainMenuGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 1, 1, 1).text("Main menu").color(0xaaccff))
                .add(registry.stackIcon(0.5, 2.5, 1, 1).itemStack(new ItemStack(Items.IRON_INGOT)))
                .add(registry.button(2, 2.5, 5, 1)
                        .text("Ingredients")
                        .hitEvent((component, p, entity, x1, y1) -> entity.switchTag(TAG_INGREDIENTS)))
                .add(registry.stackIcon(0.5, 3.5, 1, 1).itemStack(new ItemStack(Blocks.CRAFTING_TABLE)))
                .add(registry.button(2, 3.5, 5, 1)
                        .text("Crafting")
                        .hitEvent((component, p, entity, x1, y1) -> entity.switchTag(TAG_CRAFTING)))
                ;
    }

    private IGuiComponent<?> createIngredientsGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)

                .add(registry.text(0, 0, 8, 1).text("Ingredients").color(0xaaccff))

                .add(registry.icon(0, 2, 1, 1).icon(WHITE_PLAYER))
                .add(registry.playerSlots(1.5, 1.5, 6, 2)
                        .name("playerslots")
                        .withAmount()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToMachine(player, entity))
                        .filter((stack, index) -> isIngredient(stack)))

                .add(registry.iconButton(2, 3.5, 1, 1).icon(GRAY_ARROW_DOWN).hover(WHITE_ARROW_DOWN)
                        .hitEvent((component, player, entity, x, y) -> transferToMachine(player, entity)))
                .add(registry.iconButton(3, 3.5, 1, 1).icon(GRAY_ARROW_UP).hover(WHITE_ARROW_UP)
                        .hitEvent((component, player, entity, x, y) -> transferToPlayer(player, entity)))

                .add(registry.stackIcon(0, 4.5, 1, 1).itemStack(new ItemStack(ModBlocks.constructorBlock)))
                .add(registry.slots(1.5, 4.5, 6, 2)
                        .name("slots")
                        .withAmount()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, entity))
                        .filter((stack, index) -> isIngredientSlot(index))
                        .itemHandler(getItemHandler()))

                .add(registry.stackIcon(0, 6.5, 1, 1).itemStack(new ItemStack(ModBlocks.constructorBlock)))
                .add(registry.slots(1.5, 6.5, 6, 1)
                        .name("outputslots")
                        .filter((stack, index) -> isOutputSlot(index))
                        .itemHandler(getItemHandler()))
                ;
    }

    private boolean isIngredientSlot(Integer index) {
        return index >= SLOT_INGREDIENTS && index < SLOT_INGREDIENTS + INGREDIENTS;
    }

    private IGuiComponent<?> createCraftingGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)

                .add(registry.text(0, 0, 8, 1).text("Crafting").color(0xaaccff))

                .add(registry.iconChoice(7, 7, 1, 1)
                        .getter((player) -> getRSModeInt())
                        .icon(REDSTONE_DUST)
                        .icon(REDSTONE_OFF)
                        .icon(REDSTONE_ON)
                        .hitEvent((component, player, entity1, x, y) -> changeMode()))
                ;
    }

    private boolean isIngredient(ItemStack stack) {
        // @todo optimize!
        for (EnumFacing value : EnumFacing.VALUES) {
            TileEntity te = world.getTileEntity(pos.offset(value));
            if (te instanceof BlueprintStorageTile) {
                BlueprintStorageTile blueprints = (BlueprintStorageTile) te;
                InventoryHelper helper = blueprints.getInventoryHelper();
                for (int i = SLOT_BLUEPRINT; i < SLOT_BLUEPRINT + BLUEPRINTS; i++) {
                    ItemStack blueprintStack = helper.getStackInSlot(i);
                    if (!blueprintStack.isEmpty()) {
                        ItemStack destination = BlueprintItem.getDestination(blueprintStack);
                        ConstructorRecipe recipe = RecipeRegistry.findRecipe(destination);
                        if (recipe != null) {
                            for (ItemStack ingredient : recipe.getIngredients()) {
                                if (ItemStack.areItemsEqual(ingredient, stack)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

//                .add(registry.text(0, 6.5, 2, 1)
//                        .text("Focus")
//                        .color(0xaaccff))
//                .add(registry.stackIcon(4, 6.5, 1.5, 1.5)
//                        .scale(1.5)
//                        .itemStack(this::getFocus))
//                ;

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
