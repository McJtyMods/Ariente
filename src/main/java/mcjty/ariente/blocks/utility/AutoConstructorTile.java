package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.*;
import mcjty.hologui.api.components.IPlayerSlots;
import mcjty.hologui.api.components.ISlots;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.AutomationFilterItemHander;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.OrientationTools;
import mcjty.lib.varia.RedstoneMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mcjty.ariente.blocks.BlockProperties.WORKING;
import static mcjty.ariente.blocks.utility.BlueprintStorageTile.BLUEPRINTS;
import static mcjty.ariente.blocks.utility.BlueprintStorageTile.SLOT_BLUEPRINT;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class AutoConstructorTile extends GenericTileEntity implements IGuiTile, ITickableTileEntity, IPowerReceiver, ICityEquipment, IPowerUser {

    public static final int INGREDIENTS = 6*3;
    public static final int OUTPUT = 6;
    public static final int SLOT_INGREDIENTS = 0;
    public static final int SLOT_OUTPUT = SLOT_INGREDIENTS + INGREDIENTS;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(INGREDIENTS + OUTPUT)); // @todo 1.14 new ResourceLocation(Ariente.MODID, "gui/constructor.gui"));
//    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, INGREDIENTS + OUTPUT);

    private final GenericItemHandler items = createItemHandler();
    private final LazyOptional<GenericItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<AutomationFilterItemHander> automationItemHandler = LazyOptional.of(() -> new AutomationFilterItemHander(items));

    public static String TAG_INGREDIENTS = "ingredients";

    private long usingPower = 0;
    private int craftIndex = 0;
    private int busyCounter = 0;

    public AutoConstructorTile() {
        super(Registration.AUTO_CONSTRUCTOR_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(AutoConstructorTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(WORKING);
            }
        };
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return ActionResultType.SUCCESS;
    }

    @Override
    public long getUsingPower() {
        return usingPower;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    private boolean hasIngredient(ItemStack ingredient) {
        if (ingredient.isEmpty()) {
            return true;
        }
        int needed = ingredient.getCount();
        for (int i = SLOT_INGREDIENTS; i < SLOT_INGREDIENTS + INGREDIENTS; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (ItemStack.isSame(ingredient, stack)) {
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
            ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
            if (recipe != null) {
                // Check if we have enough
                for (ItemStack ingredient : recipe.getIngredientList()) {
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
            ItemStack stack = items.getStackInSlot(i);
            if (ItemStack.isSame(ingredient, stack)) {
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
        if (canCraft(blueprintStack)) {
            ItemStack destination = BlueprintItem.getDestination(blueprintStack);
            ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
            if (recipe != null) {
                // Check if we have room for the destination
                boolean ok = false;
                for (int i = SLOT_OUTPUT; i < SLOT_OUTPUT + OUTPUT; i++) {
                    ItemStack outputSlot = items.getStackInSlot(i);
                    if (outputSlot.isEmpty()) {
                        if (PowerReceiverSupport.consumePower(level, worldPosition, 100, true)) {
                            usingPower += 100;
                            items.setStackInSlot(i, recipe.getDestination().copy());
                            ok = true;
                        } else {
                            return; // Do nothing. Not enough power
                        }
                        break;
                    } else {
                        if (ItemHandlerHelper.canItemStacksStack(recipe.getDestination(), outputSlot)) {
                            if (outputSlot.getCount() < outputSlot.getMaxStackSize()) {
                                if (PowerReceiverSupport.consumePower(level, worldPosition, 100, true)) {
                                    usingPower += 100;
                                    outputSlot.grow(1);
                                    markDirtyQuick();
                                    ok = true;
                                } else {
                                    return; // Do nothing. Not enough power
                                }
                                break;
                            }
                        }
                    }
                }

                if (ok) {
                    // We have enough. Consume and craft
                    for (ItemStack ingredient : recipe.getIngredientList()) {
                        consumeIngredient(ingredient);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {

            boolean wasUsing = usingPower > 0;
            usingPower = 0;
            if (isMachineEnabled()) {
                if (PowerReceiverSupport.consumePower(level, worldPosition, 10, true)) {
                    usingPower = 10;
                    markDirtyQuick();

                    busyCounter--;
                    if (busyCounter > 0) {
                        return;
                    }
                    busyCounter = 10;

                    craftIndex++;

                    int ci = craftIndex;
                    // @todo optimize
                    List<BlueprintStorageTile> storageTiles = new ArrayList<>();
                    for (Direction value : OrientationTools.DIRECTION_VALUES) {
                        TileEntity te = level.getBlockEntity(worldPosition.relative(value));
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
                    GenericItemHandler helper = blueprints.getItems();
                    ItemStack blueprintStack = helper.getStackInSlot(SLOT_BLUEPRINT + index);
                    if (blueprintStack.isEmpty()) {
                        // Nothing to do. Decrease busyCounter so we skip to the next blueprint faster
                        busyCounter = 3;
                        return;
                    }

                    attemptCraft(blueprintStack);
                }
            }

            boolean isUsing = usingPower > 0;
            if (isUsing != wasUsing) {
                markDirtyClient();
            }

        }
    }

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.with(WORKING, isWorking());
//    }

    public boolean isWorking() {
        return usingPower > 0 && isMachineEnabled();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        boolean working = isWorking();

        super.onDataPacket(net, packet);

        if (level.isClientSide) {
            // If needed send a render update.
            boolean newWorking = isWorking();
            if (newWorking != working) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        }
    }

    private boolean isOutputSlot(int index) {
        return index >= SLOT_OUTPUT;
    }


    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readRestorableFromNBT(tagCompound);
        craftIndex = tagCompound.getInt("craftIndex");
        busyCounter = tagCompound.getInt("busy");
    }

    @Override
    public CompoundNBT save(CompoundNBT tagCompound) {
        tagCompound.putInt("craftIndex", craftIndex);
        tagCompound.putInt("busy", busyCounter);
        return super.save(tagCompound);
    }

    public void readRestorableFromNBT(CompoundNBT tagCompound) {
//        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    public void writeRestorableToNBT(CompoundNBT tagCompound) {
//        writeBufferToNBT(tagCompound, inventoryHelper);
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

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_INGREDIENTS.equals(tag)) {
            return createIngredientsGui(registry);
        } else if (TAG_HELP.equals(tag)) {
            return HoloGuiTools.createHelpGui(registry,
                    HelpBuilder.create()
                            .line("The auto constructor can be")
                            .line("used to automatically craft items")
                            .line("with blueprints from adjacent")
                            .line("blueprint storages.")
                            .nl()
                            .line("Use the 'ingredients' page to")
                            .line("transfer ingredients between the")
                            .line("player and this machine.")
                            .line("Automation is also possible")
                            .nl()
                            .line("Output appears in the slots below")
                            .line("the main page"));
        } else {
            return createMainMenuGui(registry);
        }
    }

    private IGuiComponent<?> createMainMenuGui(IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry)
                .add(registry.text(0, 0.5, 1, 1).text("Main menu").color(registry.color(StyledColor.LABEL)))
                .add(registry.stackIcon(0.5, 2.5, 1, 1).itemStack(new ItemStack(Items.IRON_INGOT)))
                .add(registry.button(2, 2, 5, 1)
                        .text("Ingredients")
                        .hitEvent((component, p, entity, x1, y1) -> entity.switchTag(TAG_INGREDIENTS)))


                .add(registry.iconChoice(7, 0, 1, 1)
                        .getter((player) -> getRSModeInt())
                        .addImage(registry.image(REDSTONE_DUST))
                        .addImage(registry.image(REDSTONE_OFF))
                        .addImage(registry.image(REDSTONE_ON))
                        .hitEvent((component, player, entity1, x, y) -> changeMode()))

                .add(registry.text(0, 5, 1, 1).text("Output").color(registry.color(StyledColor.LABEL)))

                .add(registry.stackIcon(0, 6.5, 1, 1).itemStack(new ItemStack(Registration.CONSTRUCTOR.get())))
                .add(registry.slots(1.5, 6.5, 6, 1)
                        .name("outputslots")
                        .withAmount()
                        .filter((stack, index) -> isOutputSlot(index))
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, entity, "outputslots"))
                        .itemHandler(items))
                ;
    }

    private IGuiComponent<?> createIngredientsGui(IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)

                .add(registry.text(0, 0, 8, 1).text("Ingredients").color(registry.color(StyledColor.LABEL)))

                .add(registry.icon(0, 1.5, 1, 1).icon(registry.image(WHITE_PLAYER)))
                .add(registry.playerSlots(1.5, 1, 6, 3)
                        .name("playerslots")
                        .withAmount()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToMachine(player, entity))
                        .filter((stack, index) -> isIngredient(stack)))

                .add(registry.iconButton(2, 4.2, 1, 1).icon(registry.image(GRAY_ARROW_DOWN)).hover(registry.image(WHITE_ARROW_DOWN))
                        .hitEvent((component, player, entity, x, y) -> transferToMachine(player, entity)))
                .add(registry.iconButton(3, 4.2, 1, 1).icon(registry.image(GRAY_ARROW_UP)).hover(registry.image(WHITE_ARROW_UP))
                        .hitEvent((component, player, entity, x, y) -> transferToPlayer(player, entity, "slots")))

                .add(registry.stackIcon(0, 5.5, 1, 1).itemStack(new ItemStack(Registration.CONSTRUCTOR.get())))
                .add(registry.slots(1.5, 5.5, 6, 3)
                        .name("slots")
                        .withAmount()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, entity, "slots"))
                        .filter((stack, index) -> isIngredientSlot(index))
                        .itemHandler(items))
                ;
    }

    private boolean isIngredientSlot(Integer index) {
        return index >= SLOT_INGREDIENTS && index < SLOT_INGREDIENTS + INGREDIENTS;
    }

    private boolean isIngredient(ItemStack stack) {
        // @todo optimize!
        for (Direction value : OrientationTools.DIRECTION_VALUES) {
            TileEntity te = level.getBlockEntity(worldPosition.relative(value));
            if (te instanceof BlueprintStorageTile) {
                BlueprintStorageTile blueprints = (BlueprintStorageTile) te;
                GenericItemHandler helper = blueprints.getItems();
                for (int i = SLOT_BLUEPRINT; i < SLOT_BLUEPRINT + BLUEPRINTS; i++) {
                    ItemStack blueprintStack = helper.getStackInSlot(i);
                    if (!blueprintStack.isEmpty()) {
                        ItemStack destination = BlueprintItem.getDestination(blueprintStack);
                        ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
                        if (recipe != null) {
                            for (ItemStack ingredient : recipe.getIngredientList()) {
                                if (ItemStack.isSame(ingredient, stack)) {
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


    private void transferToPlayer(PlayerEntity player, IHoloGuiEntity entity, String compName) {
        entity.findComponent(compName).ifPresent(component -> {
            if (component instanceof ISlots) {
                int selected = ((ISlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = items.extractItem(selected, 64, false);
                    if (!extracted.isEmpty()) {
                        if (!player.inventory.add(extracted)) {
                            items.insertItem(selected, extracted, false);
                        } else {
                            ((ISlots) component).setSelection(-1);
                        }
                        markDirtyClient();
                    }
                }
            }
        });
    }

    private void transferToMachine(PlayerEntity player, IHoloGuiEntity entity) {
        // @todo don't put in output slots!
        entity.findComponent("playerslots").ifPresent(component -> {
            if (component instanceof IPlayerSlots) {
                int selected = ((IPlayerSlots) component).getSelected();
                if (selected != -1) {
                    ItemStack extracted = player.inventory.getItem(selected);
                    if (!extracted.isEmpty()) {
                        ItemStack notInserted = ItemHandlerHelper.insertItem(items, extracted, false);
                        player.inventory.setItem(selected, notInserted);
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
        markDirtyClient();
    }

    private GenericItemHandler createItemHandler() {
        return new GenericItemHandler(AutoConstructorTile.this, CONTAINER_FACTORY.get()) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() != Registration.BLUEPRINT.get();
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return automationItemHandler.cast();
        }
        return super.getCapability(cap);
    }
}
