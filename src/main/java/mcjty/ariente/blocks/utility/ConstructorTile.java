package mcjty.ariente.blocks.utility;

import mcjty.lib.varia.SafeClientTools;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.PlayerInventoryPanel;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.ConstructorCrafting;
import net.minecraftforge.items.wrapper.InvWrapper;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.*;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class ConstructorTile extends GenericTileEntity implements IGuiTile, ICityEquipment {

    private BlueprintItemHandler blueprintItemHandler = null;

    public static final ResourceLocation CROSS = new ResourceLocation("hologui", "textures/gui/cross.png");

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    public ConstructorTile(BlockPos pos, BlockState state) {
        super(Registration.CONSTRUCTOR_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
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
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
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
    public void setup(ICityAI cityAI, Level world, boolean firstTime) {
    }

    private boolean hasIngredient(Player player, ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return true;
        }
        int needed = ingredient.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItem(ingredient, stack)) {
                needed -= stack.getCount();
                if (needed <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canCraft(Player player, ItemStack blueprintStack) {
        if (player == null || !blueprintStack.is(Registration.BLUEPRINT.get())) {
            return false;
        }
        ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(blueprintStack));
        return recipe != null && ConstructorCrafting.planIngredients(recipe, new InvWrapper(player.getInventory()),
                0, player.getInventory().getContainerSize()) != null;
    }

    private void attemptCraft(Player player, ItemStack blueprintStack) {
        if (!canCraft(player, blueprintStack)) {
            return;
        }
        ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(blueprintStack));
        var inventory = new InvWrapper(player.getInventory());
        int[] reserved = ConstructorCrafting.planIngredients(recipe, inventory, 0, inventory.getSlots());
        if (reserved == null) {
            return;
        }
        for (int slot = 0; slot < reserved.length; slot++) {
            if (reserved[slot] > 0) {
                inventory.extractItem(slot, reserved[slot], false);
            }
        }
        ItemStack result = recipe.getDestination().copy();
        if (!player.getInventory().add(result)) {
            player.spawnAtLocation(result, 1.05f);
        }
        markDirtyClient();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.CONSTRUCTOR.createHelpGui(registry);
        } else {
            return createMainGui(registry);
        }
    }

    private IGuiComponent<?> createMainGui(IGuiComponentRegistry registry) {
        return GuiHelp.CONSTRUCTOR.createPanel(registry)
                .add(registry.text(0, -.2, 8, 1).text("Craft").color(registry.color(StyledColor.LABEL)))

                .add(registry.stackIcon(0, 2, 1, 1).itemStack(new ItemStack(Registration.CONSTRUCTOR.get())))
                .add(registry.slots(1.5, 1, 6, 3)
                        .name("outputslots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> attemptCraft(player, stack))
                        .overlay((stack, integer) -> getCraftableOverlay(registry, stack))
                        .tooltipHandler(this::tooltipHandler)
                        .itemHandler(getItemHandler()))

                .add(new PlayerInventoryPanel("Relevant ingredients")
                        .name("playerslots")
                        .filter((stack, index) -> isIngredient(stack)))
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
                        if (!hasIngredient(SafeClientTools.getClientPlayer(), ingredient)) {
                            tooltip.add(ChatFormatting.RED + "Missing: " + ChatFormatting.WHITE + ingredient.getHoverName());
                            ok = false;
                        }
                    }
                    if (ok) {
                        tooltip.add(0, ChatFormatting.GOLD + "Doubleclick to craft!");
                    }
                }
            }
        }
    }

    private IImage getCraftableOverlay(IGuiComponentRegistry registry, ItemStack stack) {
        if (canCraft(SafeClientTools.getClientPlayer(), stack)) {
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
                        if (ItemStack.isSameItem(ingredient, stack)) {
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
            blueprintItemHandler = new BlueprintItemHandler(level, worldPosition);
        }
        return blueprintItemHandler;
    }

    @Override
    public void syncToClient() {
    }
}
