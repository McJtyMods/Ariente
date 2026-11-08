package mcjty.ariente.blocks.utility;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.PlayerInventoryPanel;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.Recycling;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;
import static mcjty.lib.container.SlotDefinition.generic;

public class RecyclerTile extends ExtremeMachineTile implements IGuiTile, IPowerReceiver, IPowerUser, ICityEquipment {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int OUTPUTS = 10;
    public static final int RECYCLE_TICKS = 40;
    public static final int POWER_PER_TICK = 10;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(1 + OUTPUTS)
            .slot(generic().in(), SLOT_INPUT, 0, 0)
            .box(generic().out(), SLOT_OUTPUT, 0, 0, 5, 2));

    private final GenericItemHandler items = new GenericItemHandler(this, CONTAINER_FACTORY.get()) {

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == SLOT_INPUT && Recycling.findRecipe(stack) != null;
        }

        @Override
        protected void onUpdate(int index, ItemStack stack) {
            markDirtyClient();
        }
    };

    private final Map<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private ResourceLocation recipeId;
    private int progress;
    private long usingPower;

    public RecyclerTile(BlockPos pos, BlockState state) {
        super(Registration.RECYCLER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(RecyclerTile::new)) {

            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult hit) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        long previousPower = usingPower;
        usingPower = 0;
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        ConstructorRecipe recipe = Recycling.findRecipe(input);
        ResourceLocation nextId = recipe == null ? null : recipe.getId();
        if (!Objects.equals(nextId, recipeId)) {
            recipeId = nextId;
            progress = 0;
            markDirtyClient();
        }
        if (recipe != null && input.getCount() >= recipe.getDestination().getCount()) {
            ItemStack[] outputs = planOutputs(recipe, input);
            if (outputs != null && PowerReceiverSupport.consumePower(level, worldPosition, powerCost(POWER_PER_TICK), true)) {
                usingPower = powerCost(POWER_PER_TICK);
                progress += operationSpeed();
                if (progress >= RECYCLE_TICKS) {
                    progress = 0;
                    items.extractItem(SLOT_INPUT, recipe.getDestination().getCount(), false);
                    for (int index = 0; index < OUTPUTS; index++) {
                        items.setStackInSlot(SLOT_OUTPUT + index, outputs[index]);
                    }
                }
                markDirtyQuick();
                if (progress % 5 == 0) {
                    markDirtyClient();
                }
            }
        }
        if (previousPower != usingPower) {
            markDirtyClient();
        }
    }

    @Nullable
    private ItemStack[] planOutputs(ConstructorRecipe recipe, ItemStack input) {
        ItemStack[] outputs = new ItemStack[OUTPUTS];
        for (int index = 0; index < OUTPUTS; index++) {
            outputs[index] = items.getStackInSlot(SLOT_OUTPUT + index).copy();
        }
        for (ItemStack recovered : Recycling.recover(recipe, input)) {
            int remaining = recovered.getCount();
            // Fill compatible stacks before reserving an empty slot.
            for (int pass = 0; pass < 2; pass++) {
                for (int index = 0; index < OUTPUTS && remaining > 0; index++) {
                    ItemStack output = outputs[index];
                    if ((pass == 0 && !output.isEmpty() && ItemStack.isSameItemSameComponents(output, recovered))
                            || (pass == 1 && output.isEmpty())) {
                        int limit = Math.min(items.getSlotLimit(SLOT_OUTPUT + index), recovered.getMaxStackSize());
                        int added = Math.min(remaining, limit - output.getCount());
                        if (added > 0) {
                            ItemStack merged = recovered.copy();
                            merged.setCount(output.getCount() + added);
                            outputs[index] = merged;
                            remaining -= added;
                        }
                    }
                }
            }
            if (remaining > 0) {
                return null;
            }
        }
        return outputs;
    }

    @Override
    public long getUsingPower() {
        return usingPower;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        if (TAG_HELP.equals(tag)) {
            return GuiHelp.RECYCLER.createHelpGui(registry);
        }
        return GuiHelp.RECYCLER.createPanel(registry)
                .add(registry.text(0, 0, 8, .6).scale(.7f).text(Component.translatable("block.ariente.recycler").getString())
                        .color(registry.color(StyledColor.LABEL)))
                .add(registry.text(.4, .7, 2, .5).scale(.4f).text(Component.translatable("message.ariente.recycler.input").getString()))
                .add(new RecyclerSlots(.4, 1.3, items, SLOT_INPUT, 1, 1).name("inputslots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, index)))
                .add(registry.text(2, .7, 5, .5).scale(.4f).text(Component.translatable("message.ariente.recycler.output").getString()))
                .add(new RecyclerSlots(2, 1.3, items, SLOT_OUTPUT, 5, 2).name("outputslots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToPlayer(player, index)))
                .add(registry.text(.4, 3.6, 7, .6).scale(.4f)
                        .text(() -> Component.translatable("message.ariente.recycler.progress", progress * 100 / RECYCLE_TICKS).getString()))
                .add(new PlayerInventoryPanel(Component.translatable("message.ariente.recycler.insert_hint").getString())
                        .name("playerslots").filter((stack, index) -> Recycling.findRecipe(stack) != null)
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> transferToMachine(player, index)));
    }

    public void transferToMachine(Player player, int slot) {
        if (!canInteract(player) || slot < 0 || slot >= 36) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, items.insertItem(SLOT_INPUT, stack, false));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    public void transferToPlayer(Player player, int slot) {
        if (!canInteract(player) || slot < SLOT_INPUT || slot >= SLOT_OUTPUT + OUTPUTS) {
            return;
        }
        ItemStack remaining = items.getStackInSlot(slot).copy();
        if (!remaining.isEmpty()) {
            player.getInventory().add(remaining);
            items.setStackInSlot(slot, remaining);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }

    private boolean canInteract(Player player) {
        return level != null && !level.isClientSide && player.level() == level && !isRemoved()
                && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64;
    }

    @Override
    protected void saveInfo(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveInfo(tag, provider);
        CompoundTag info = getOrCreateInfo(tag);
        info.putInt("recycleProgress", progress);
        if (recipeId != null) {
            info.putString("recycleRecipe", recipeId.toString());
        }
    }

    @Override
    protected void loadInfo(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadInfo(tag, provider);
        CompoundTag info = tag.getCompound("Info");
        progress = Math.max(0, Math.min(RECYCLE_TICKS - 1, info.getInt("recycleProgress")));
        recipeId = ResourceLocation.tryParse(info.getString("recycleRecipe"));
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("Items", items.serializeNBT(provider));
        tag.putInt("recycleProgress", progress);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        items.deserializeNBT(provider, tag.getCompound("Items"));
        progress = tag.getInt("recycleProgress");
    }

    @Override
    public void syncToClient() {
        markDirtyClient();
    }

    @Override
    public IItemHandler getAutomationHandler(@Nullable Direction side) {
        return side == null ? items : sidedHandlers.computeIfAbsent(side, SidedHandler::new);
    }

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

    private class SidedHandler implements IItemHandler {

        private final Direction side;

        private SidedHandler(Direction side) {
            this.side = side;
        }

        private boolean isOutput() {
            return side == Direction.DOWN || side == Registration.RECYCLER.get().getFrontDirection(getBlockState()).getOpposite();
        }

        @Override
        public int getSlots() {
            return isOutput() ? OUTPUTS : 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots() ? items.getStackInSlot(isOutput() ? SLOT_OUTPUT + slot : SLOT_INPUT) : ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? items.insertItem(SLOT_INPUT, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return isOutput() && slot >= 0 && slot < OUTPUTS ? items.extractItem(SLOT_OUTPUT + slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < getSlots() ? 64 : 0;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return !isOutput() && slot == 0 && items.isItemValid(SLOT_INPUT, stack);
        }
    }
}
