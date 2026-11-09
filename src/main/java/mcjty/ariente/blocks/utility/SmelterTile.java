package mcjty.ariente.blocks.utility;

import net.minecraft.util.Prediction;
import mcjty.lib.varia.InventoryTools;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.config.PowerConfiguration;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.IPowerUser;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;

import static mcjty.ariente.blocks.BlockProperties.WORKING;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;
import static mcjty.lib.container.SlotDefinition.generic;

public class SmelterTile extends ExtremeMachineTile implements IPowerReceiver, IPowerUser {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(2)
            .slot(generic().in(), SLOT_INPUT, 0, 0)
            .slot(generic().out(), SLOT_OUTPUT, 0, 0));

    private final GenericItemHandler items = new GenericItemHandler(this, CONTAINER_FACTORY.get()) {

                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == SLOT_INPUT && findRecipe(stack) != null;
        }

        @Override
        protected void onUpdate(int index, ItemStack stack) {
            markDirtyClient();
        }
    };

    private final Map<Direction, ResourceHandler<ItemResource>> sidedHandlers = new EnumMap<>(Direction.class);
    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> recipeCheck = RecipeManager.createCheck(RecipeType.SMELTING);
    private Identifier recipeId;
    private int progress;
    private long usingPower;

    public SmelterTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.SMELTER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(SmelterTile::new)) {

            {
                registerDefaultState(defaultBlockState().setValue(WORKING, false));
            }

            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(WORKING);
            }

            @Override
            public void attack(BlockState state, Level level, BlockPos pos, Player player) {
                if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SmelterTile smelter
                        && player.pick(player.blockInteractionRange(), 0, false) instanceof BlockHitResult hit
                        && hit.getBlockPos().equals(pos)) {
                    smelter.giveToPlayer(smelter.getSlot(hit), player);
                }
            }
        };
    }

    public ItemStack getStack(int slot) {
        return ItemUtil.getStack(items, slot);
    }

    public int getSlot(BlockHitResult hit) {
        Direction front = mcjty.ariente.setup.Registration.SMELTER.get().getFrontDirection(getBlockState());
        if (hit.getDirection() != front || !hit.getBlockPos().equals(worldPosition)) {
            return -1;
        }
        Vec3 local = hit.getLocation().subtract(Vec3.atLowerCornerOf(worldPosition));
        double x = switch (front) {
            case NORTH -> 1 - local.x;
            case SOUTH -> local.x;
            case WEST -> local.z;
            case EAST -> 1 - local.z;
            default -> -1;
        };
        if (local.y < 5.75 / 16 || local.y > 10.25 / 16) {
            return -1;
        }
        if (x >= 1.5 / 16 && x <= 6.0 / 16) {
            return SLOT_INPUT;
        }
        return x >= 10.0 / 16 && x <= 14.5 / 16 ? SLOT_OUTPUT : -1;
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult hit) {
        int slot = getSlot(hit);
        if (slot < 0) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.isEmpty()) {
                giveToPlayer(slot, player);
            } else if (slot == SLOT_INPUT) {
                player.setItemInHand(hand, InventoryTools.insertItem(items, SLOT_INPUT, held, false));
                player.containerMenu.broadcastChanges();
            }
        }
        return InteractionResult.SUCCESS;
    }

    public void giveToPlayer(int slot, Player player) {
        if (slot != SLOT_INPUT && slot != SLOT_OUTPUT) {
            return;
        }
        ItemStack taken = InventoryTools.extractItem(items, slot, player.isShiftKeyDown() ? 64 : 1, false);
        if (!taken.isEmpty()) {
            if (!player.getInventory().add(taken)) {
                player.drop(taken, false, Prediction.SERVER_ONLY);
            }
            player.containerMenu.broadcastChanges();
        }
    }

    @Nullable
    private RecipeHolder<SmeltingRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        return recipeCheck.getRecipeFor(new SingleRecipeInput(stack), (ServerLevel) level).orElse(null);
    }

    @Override
    public void tickServer() {
        long previousPower = usingPower;
        usingPower = 0;
        var recipe = findRecipe(ItemUtil.getStack(items, SLOT_INPUT));
        Identifier nextId = recipe == null ? null : recipe.id().identifier();
        if (!Objects.equals(nextId, recipeId)) {
            progress = 0;
            recipeId = nextId;
            markDirtyQuick();
        }
        if (recipe != null) {
            ItemStack result = recipe.value().assemble(new SingleRecipeInput(ItemUtil.getStack(items, SLOT_INPUT)));
            ItemStack output = ItemUtil.getStack(items, SLOT_OUTPUT);
            int limit = Math.min(items.getSlotLimit(SLOT_OUTPUT), result.getMaxStackSize());
            if (!result.isEmpty() && (output.isEmpty() || ItemStack.isSameItemSameComponents(output, result))
                    && output.getCount() + result.getCount() <= limit
                    && PowerReceiverSupport.consumePower(level, worldPosition, powerCost(PowerConfiguration.SMELTER_POWER_PER_TICK.get()), true)) {
                usingPower = powerCost(PowerConfiguration.SMELTER_POWER_PER_TICK.get());
                progress += PowerConfiguration.SMELTER_SPEED.get() * operationSpeed();
                while (progress >= recipe.value().cookingTime() && !ItemUtil.getStack(items, SLOT_INPUT).isEmpty()
                        && output.getCount() + result.getCount() <= limit) {
                    progress -= Math.max(1, recipe.value().cookingTime());
                    InventoryTools.extractItem(items, SLOT_INPUT, 1, false);
                    output = result.copyWithCount(output.getCount() + result.getCount());
                    items.setStackInSlot(SLOT_OUTPUT, output);
                    markDirtyClient();
                }
                markDirtyQuick();
            }
        }
        boolean working = usingPower > 0;
        if (getBlockState().getValue(WORKING) != working) {
            level.setBlock(worldPosition, getBlockState().setValue(WORKING, working), Block.UPDATE_CLIENTS);
        }
        if (previousPower != usingPower) {
            markDirtyClient();
        }
    }

    @Override
    public long getUsingPower() {
        return usingPower;
    }

    @Override
    protected void saveInfo(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveInfo(tag, provider);
        CompoundTag info = getOrCreateInfo(tag);
        info.putInt("smeltProgress", progress);
        if (recipeId != null) {
            info.putString("smeltRecipe", recipeId.toString());
        }
    }

    @Override
    protected void loadInfo(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadInfo(tag, provider);
        CompoundTag info = tag.getCompoundOrEmpty("Info");
        progress = Math.max(0, info.getIntOr("smeltProgress", 0));
        recipeId = Identifier.tryParse(info.getStringOr("smeltRecipe", ""));
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("Items", items.serializeNBT(provider));
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        items.deserializeNBT(provider, tag.getCompoundOrEmpty("Items"));
    }

    @Override
    public ResourceHandler<ItemResource> getAutomationHandler(@Nullable Direction side) {
        return side == null ? items : sidedHandlers.computeIfAbsent(side, SidedHandler::new);
    }

    private class SidedHandler implements ResourceHandler<ItemResource> {

        private final Direction side;

        private SidedHandler(Direction side) {
            this.side = side;
        }

        private boolean isOutput() {
            return side == Direction.DOWN || side == mcjty.ariente.setup.Registration.SMELTER.get().getFrontDirection(getBlockState()).getOpposite();
        }

        private int mapped(int index) {
            if (index < 0 || index >= size()) throw new IndexOutOfBoundsException(index);
            return isOutput() ? SLOT_OUTPUT : SLOT_INPUT;
        }

        @Override
        public int size() {
            return getSlots();
        }

        @Override
        public ItemResource getResource(int index) {
            return items.getResource(mapped(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return items.getAmountAsLong(mapped(index));
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return items.getCapacityAsLong(mapped(index), resource);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return !isOutput() && items.isValid(mapped(index), resource);
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return isValid(index, resource) ? items.insert(mapped(index), resource, amount, transaction) : 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return isOutput() ? items.extract(mapped(index), resource, amount, transaction) : 0;
        }

                public int getSlots() {
            return 1;
        }

        @Nonnull
                public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(isOutput() ? SLOT_OUTPUT : SLOT_INPUT) : ItemStack.EMPTY;
        }

        @Nonnull
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? InventoryTools.insertItem(items, SLOT_INPUT, stack, simulate) : stack;
        }

        @Nonnull
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 && isOutput() ? InventoryTools.extractItem(items, SLOT_OUTPUT, amount, simulate) : ItemStack.EMPTY;
        }

                public int getSlotLimit(int slot) {
            return slot == 0 ? 64 : 0;
        }

                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 && !isOutput() && items.isItemValid(SLOT_INPUT, stack);
        }
    }
}
