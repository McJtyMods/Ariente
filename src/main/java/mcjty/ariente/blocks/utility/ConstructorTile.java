package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.ai.CityAI;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.cities.ICityEquipment;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.RecipeRegistry;
import mcjty.hologui.api.*;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.api.components.ISlots;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Map;

import static mcjty.hologui.api.Icons.WHITE_PLAYER;

public class ConstructorTile extends GenericTileEntity implements IGuiTile, ICityEquipment {

    private BlueprintItemHandler blueprintItemHandler = null;

    public static final ResourceLocation CROSS = new ResourceLocation("hologui", "textures/gui/cross.png");

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        blueprintItemHandler = null;
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

    private boolean hasIngredient(EntityPlayer player, ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return true;
        }
        int needed = ingredient.getCount();
        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (ItemStack.areItemsEqual(ingredient, stack)) {
                needed -= stack.getCount();
                if (needed <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void consumeIngredient(EntityPlayer player, ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return;
        }

        int needed = ingredient.getCount();
        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (ItemStack.areItemsEqual(ingredient, stack)) {
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

    private boolean canCraft(EntityPlayer player, ItemStack blueprintStack) {
        if (!blueprintStack.isEmpty()) {
            if (!blueprintStack.isEmpty()) {
                ItemStack destination = BlueprintItem.getDestination(blueprintStack);
                ConstructorRecipe recipe = RecipeRegistry.findRecipe(destination);
                if (recipe != null) {
                    // Check if we have enough
                    for (ItemStack ingredient : recipe.getIngredients()) {
                        if (!hasIngredient(player, ingredient)) {
                            return false; // Can't craft
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void attemptCraft(EntityPlayer player, ItemStack blueprintStack) {
        if (!blueprintStack.isEmpty()) {
            if (canCraft(player, blueprintStack)) {
                ItemStack destination = BlueprintItem.getDestination(blueprintStack);
                ConstructorRecipe recipe = RecipeRegistry.findRecipe(destination);
                if (recipe != null) {
                    // We have enough. Consume and craft
                    for (ItemStack ingredient : recipe.getIngredients()) {
                        consumeIngredient(player, ingredient);
                    }

                    if (!player.inventory.addItemStackToInventory(destination)) {
                        player.entityDropItem(destination, 1.05f);
                    }

                    markDirtyClient();

                    if (player.openContainer != null) {
                        player.openContainer.detectAndSendChanges();
                    }
                }
            }
        }
    }


    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)

                .add(registry.text(0, 0, 8, 1).text("Ingredients").color(0xaaccff))

                .add(registry.icon(0, 1.7, 1, 1).icon(registry.image(WHITE_PLAYER)))
                .add(registry.playerSlots(1.5, 1.2, 6, 3)
                        .name("playerslots")
                        .withAmount()
                        .filter((stack, index) -> isIngredient(stack)))

                .add(registry.text(0, 4.5, 8, 1).text("Craft").color(0xaaccff))

                .add(registry.stackIcon(0, 5.5, 1, 1).itemStack(new ItemStack(ModBlocks.constructorBlock)))
                .add(registry.slots(1.5, 5.5, 6, 3)
                        .name("outputslots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> attemptCraft(player, stack))
                        .filter((stack, index) -> isOutputSlot(index))
                        .overlay((stack, integer) -> getCraftableOverlay(registry, stack))
                        .itemHandler(getItemHandler()))
                ;
    }

    private IImage getCraftableOverlay(IGuiComponentRegistry registry, ItemStack stack) {
        if (canCraft(Ariente.proxy.getClientPlayer(), stack)) {
            return null;
        } else {
            return registry.image(Icons.RED_CROSS);
        }
    }

    private boolean isOutputSlot(int index) {
        return true;
    }

    private boolean isIngredient(ItemStack stack) {
        // @todo optimize!
        IItemHandler handler = getItemHandler();
        for (int i = 0 ; i < handler.getSlots() ; i++) {
            ItemStack blueprintStack = handler.getStackInSlot(i);
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
        return false;
    }

    private IItemHandler getItemHandler() {
        // Get a virtual item handler with all collected slots from blueprint storages around this
        if (blueprintItemHandler == null) {
            blueprintItemHandler = new BlueprintItemHandler(world, pos);
        }
        return blueprintItemHandler;
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

    @Override
    public void syncToClient() {
    }
}
