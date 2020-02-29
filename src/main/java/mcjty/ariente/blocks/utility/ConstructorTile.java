package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.hologui.api.*;
import mcjty.lib.McJtyLib;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.WHITE_PLAYER;

public class ConstructorTile extends GenericTileEntity implements IGuiTile, ICityEquipment {

    private BlueprintItemHandler blueprintItemHandler = null;

    public static final ResourceLocation CROSS = new ResourceLocation("hologui", "textures/gui/cross.png");

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    public ConstructorTile() {
        super(Registration.CONSTRUCTOR_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.constructor")
                .topDriver(DRIVER)
                .tileEntitySupplier(ConstructorTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        };
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGui(world, pos, player);
        return ActionResultType.SUCCESS;
    }


    @Override
    public void remove() {
        super.remove();
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
    public void setup(ICityAI cityAI, World world, boolean firstTime) {

    }

    private boolean hasIngredient(PlayerEntity player, ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return true;
        }
        int needed = ingredient.getCount();
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
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

    private void consumeIngredient(PlayerEntity player, ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return;
        }

        int needed = ingredient.getCount();
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
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

    private boolean canCraft(PlayerEntity player, ItemStack blueprintStack) {
        if (!blueprintStack.isEmpty()) {
            ItemStack destination = BlueprintItem.getDestination(blueprintStack);
            ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
            if (recipe != null) {
                // Check if we have enough
                for (ItemStack ingredient : recipe.getIngredientList()) {
                    if (!hasIngredient(player, ingredient)) {
                        return false; // Can't craft
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void attemptCraft(PlayerEntity player, ItemStack blueprintStack) {
        if (!blueprintStack.isEmpty()) {
            if (canCraft(player, blueprintStack)) {
                ItemStack destination = BlueprintItem.getDestination(blueprintStack);
                ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
                if (recipe != null) {
                    // We have enough. Consume and craft
                    for (ItemStack ingredient : recipe.getIngredientList()) {
                        consumeIngredient(player, ingredient);
                    }

                    markDirtyClient();

                    if (!player.inventory.addItemStackToInventory(destination)) {
                        player.entityDropItem(destination, 1.05f);
                    }

                    if (player.openContainer != null) {
                        player.openContainer.detectAndSendChanges();
                    }
                }
            }
        }
    }


    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_HELP.equals(tag)) {
            return HoloGuiTools.createHelpGui(registry,
                    HelpBuilder.create()
                            .line("With this block you can craft")
                            .line("items from blueprints that are in")
                            .line("adjacent blueprint storages")
                            .nl()
                            .line("Top grid: player inventory")
                            .line("Bottom grid: available blueprints")
                            .nl()
                            .line("Double click on blueprint to craft)", 0xffffff00)
            );
        } else {
            return createMainGui(registry);
        }
    }

    private IGuiComponent<?> createMainGui(IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry)
                .add(registry.text(0, -.2, 8, 1).text("Ingredients").color(registry.color(StyledColor.LABEL)))

                .add(registry.icon(0, 1.5, 1, 1).icon(registry.image(WHITE_PLAYER)))
                .add(registry.playerSlots(1.5, 1.0, 6, 3)
                        .name("playerslots")
                        .withAmount()
                        .fullBright()
                        .filter((stack, index) -> isIngredient(stack)))

                .add(registry.text(0, 4.5, 8, 1).text("Craft").color(registry.color(StyledColor.LABEL)))

                .add(registry.stackIcon(0, 5.5, 1, 1).itemStack(new ItemStack(Registration.CONSTRUCTOR.get())))
                .add(registry.slots(1.5, 5.5, 6, 3)
                        .name("outputslots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> attemptCraft(player, stack))
                        .overlay((stack, integer) -> getCraftableOverlay(registry, stack))
                        .tooltipHandler(this::tooltipHandler)
                        .itemHandler(getItemHandler()))
                ;
    }

    private void tooltipHandler(ItemStack blueprintStack, List<String> tooltip) {
        if (!blueprintStack.isEmpty()) {
            if (!blueprintStack.isEmpty()) {
                ItemStack destination = BlueprintItem.getDestination(blueprintStack);
                ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
                if (recipe != null) {
                    boolean ok = true;
                    // Check if we have enough
                    for (ItemStack ingredient : recipe.getIngredientList()) {
                        if (!hasIngredient(McJtyLib.proxy.getClientPlayer(), ingredient)) {
                            tooltip.add(TextFormatting.RED + "Missing: " + TextFormatting.WHITE + ingredient.getDisplayName());
                            ok = false;
                        }
                    }
                    if (ok) {
                        tooltip.add(0, TextFormatting.GOLD + "Doubleclick to craft!");
                    }
                }
            }
        }
    }

    private IImage getCraftableOverlay(IGuiComponentRegistry registry, ItemStack stack) {
        if (canCraft(McJtyLib.proxy.getClientPlayer(), stack)) {
            return null;
        } else {
            return registry.image(Icons.RED_CROSS);
        }
    }

    private boolean isIngredient(ItemStack stack) {
        // @todo optimize!
        IItemHandler handler = getItemHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack blueprintStack = handler.getStackInSlot(i);
            if (!blueprintStack.isEmpty()) {
                ItemStack destination = BlueprintItem.getDestination(blueprintStack);
                ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
                if (recipe != null) {
                    for (ItemStack ingredient : recipe.getIngredientList()) {
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


    @Override
    public void syncToClient() {
    }
}
