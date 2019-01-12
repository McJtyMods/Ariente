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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mcjty.ariente.blocks.utility.BlueprintStorageTile.BLUEPRINTS;
import static mcjty.ariente.blocks.utility.BlueprintStorageTile.SLOT_BLUEPRINT;
import static mcjty.hologui.api.Icons.*;

public class AutoConstructorTile extends GenericTileEntity implements DefaultSidedInventory, IGuiTile, ITickable, IPowerReceiver, ICityEquipment {

    public static final PropertyBool WORKING = PropertyBool.create("working");
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(Ariente.MODID, "gui/constructor.gui"));
    public static final int INGREDIENTS = 6*3;
    public static final int OUTPUT = 6;
    public static final int SLOT_INGREDIENTS = 0;
    public static final int SLOT_OUTPUT = SLOT_INGREDIENTS + INGREDIENTS;
    public static int[] slots = null;
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, INGREDIENTS + OUTPUT);

    public static String TAG_INGREDIENTS = "ingredients";
    public static String TAG_CRAFTING = "crafting";

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

    private boolean hasIngredient(ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return true;
        }
        int needed = ingredient.getCount();
        for (int i = SLOT_INGREDIENTS; i < SLOT_INGREDIENTS + INGREDIENTS; i++) {
            ItemStack stack = getStackInSlot(i);
            if (ItemStack.areItemsEqual(ingredient, stack)) {
                needed -= stack.getCount();
                if (needed <= 0) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean canCraft(ItemStack blueprintStack) {
        if (!blueprintStack.isEmpty()) {
            ItemStack destination = BlueprintItem.getDestination(blueprintStack);
            ConstructorRecipe recipe = RecipeRegistry.findRecipe(destination);
            if (recipe != null) {
                // Check if we have enough
                for (ItemStack ingredient : recipe.getIngredients()) {
                    if (!hasIngredient(ingredient)) {
                        return false; // Can't craft
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void consumeIngredient(ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return;
        }

        int needed = ingredient.getCount();
        for (int i = SLOT_INGREDIENTS; i < SLOT_INGREDIENTS + INGREDIENTS; i++) {
            ItemStack stack = getStackInSlot(i);
            if (ItemStack.areItemsEqual(ingredient, stack)) {
                markDirtyQuick();
                if (needed <= stack.getCount()) {
                    stack.shrink(needed);
                    return;
                }
                needed -= stack.getCount();
                stack.shrink(stack.getCount());
                if (needed <= 0) {
                    return;
                }
            }
        }
    }


    private void attemptCraft(ItemStack blueprintStack) {
        if (PowerReceiverSupport.consumePower(world, pos, 100, true)) {
            boolean wasUsing = usingPower > 0;
            usingPower = 100;
            boolean isUsing = usingPower > 0;
            if (isUsing != wasUsing) {
                markDirtyClient();
            }

            if (canCraft(blueprintStack)) {
                ItemStack destination = BlueprintItem.getDestination(blueprintStack);
                ConstructorRecipe recipe = RecipeRegistry.findRecipe(destination);
                if (recipe != null) {
                    // Check if we have room for the destination
                    boolean ok = false;
                    for (int i = SLOT_OUTPUT ; i < SLOT_OUTPUT + OUTPUT ; i++) {
                        ItemStack outputSlot = getStackInSlot(i);
                        if (outputSlot.isEmpty()) {
                            setInventorySlotContents(i, recipe.getDestination().copy());
                            ok = true;
                            break;
                        } else {
                            if (ItemHandlerHelper.canItemStacksStack(recipe.getDestination(), outputSlot)) {
                                if (outputSlot.getCount() < outputSlot.getMaxStackSize()) {
                                    outputSlot.grow(1);
                                    markDirtyQuick();
                                    ok = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (ok) {
                        // We have enough. Consume and craft
                        for (ItemStack ingredient : recipe.getIngredients()) {
                            consumeIngredient(ingredient);
                        }
                    }
                }
            }

        }
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            usingPower = 0;
            if (isMachineEnabled()) {
                craftIndex++;
                markDirtyQuick();

                if (craftIndex % 10 != 0) {
                    // Only craft every 10 ticks
                    return;
                }

                int ci = craftIndex / 10;
                // @todo optimize
                List<BlueprintStorageTile> storageTiles = new ArrayList<>();
                for (EnumFacing value : EnumFacing.VALUES) {
                    TileEntity te = world.getTileEntity(pos.offset(value));
                    if (te instanceof BlueprintStorageTile) {
                        BlueprintStorageTile blueprints = (BlueprintStorageTile) te;
                        storageTiles.add(blueprints);
                    }
                }
                if (storageTiles.isEmpty()) {
                    // Nothing to do
                    return;
                }
                BlueprintStorageTile blueprints = storageTiles.get((ci / BLUEPRINTS) % storageTiles.size());
                int index = ci % BLUEPRINTS;
                InventoryHelper helper = blueprints.getInventoryHelper();
                ItemStack blueprintStack = helper.getStackInSlot(SLOT_BLUEPRINT + index);
                if (blueprintStack.isEmpty()) {
                    // Nothing to do
                    return;
                }

                attemptCraft(blueprintStack);
            }
        }
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

                .add(registry.icon(0, 2, 1, 1).icon(registry.image(WHITE_PLAYER)))
                .add(registry.playerSlots(1.5, 1.5, 6, 2)
                        .name("playerslots")
                        .withAmount()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToMachine(player, entity))
                        .filter((stack, index) -> isIngredient(stack)))

                .add(registry.iconButton(2, 3.5, 1, 1).icon(registry.image(GRAY_ARROW_DOWN)).hover(registry.image(WHITE_ARROW_DOWN))
                        .hitEvent((component, player, entity, x, y) -> transferToMachine(player, entity)))
                .add(registry.iconButton(3, 3.5, 1, 1).icon(registry.image(GRAY_ARROW_UP)).hover(registry.image(WHITE_ARROW_UP))
                        .hitEvent((component, player, entity, x, y) -> transferToPlayer(player, entity, "slots")))

                .add(registry.stackIcon(0, 4.5, 1, 1).itemStack(new ItemStack(ModBlocks.constructorBlock)))
                .add(registry.slots(1.5, 4.5, 6, 3)
                        .name("slots")
                        .withAmount()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, entity, "slots"))
                        .filter((stack, index) -> isIngredientSlot(index))
                        .itemHandler(getItemHandler()))

                .add(registry.stackIcon(0, 7.5, 1, 1).itemStack(new ItemStack(ModBlocks.constructorBlock)))
                .add(registry.slots(1.5, 7.5, 6, 1)
                        .name("outputslots")
                        .withAmount()
                        .filter((stack, index) -> isOutputSlot(index))
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, entity, "outputslots"))
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
                        .addImage(registry.image(REDSTONE_DUST))
                        .addImage(registry.image(REDSTONE_OFF))
                        .addImage(registry.image(REDSTONE_ON))
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


    private IItemHandler getItemHandler() {
        return getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }


    private void transferToPlayer(EntityPlayer player, IHoloGuiEntity entity, String compName) {
        entity.findComponent(compName).ifPresent(component -> {
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
        // @todo don't put in output slots!
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
