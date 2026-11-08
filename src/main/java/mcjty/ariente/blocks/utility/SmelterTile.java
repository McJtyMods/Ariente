package mcjty.ariente.blocks.utility;

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
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.items.IItemHandler;

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

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == SLOT_INPUT && findRecipe(stack) != null;
        }

        @Override
        protected void onUpdate(int index, ItemStack stack) {
            markDirtyClient();
        }
    };

    private final Map<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);
    private final RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe> recipeCheck = RecipeManager.createCheck(RecipeType.SMELTING);
    private ResourceLocation recipeId;
    private int progress;
    private long usingPower;

    public SmelterTile(BlockPos pos, BlockState state) {
        super(Registration.SMELTER_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
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
                if (!level.isClientSide && level.getBlockEntity(pos) instanceof SmelterTile smelter
                        && player.pick(player.blockInteractionRange(), 0, false) instanceof BlockHitResult hit
                        && hit.getBlockPos().equals(pos)) {
                    smelter.giveToPlayer(smelter.getSlot(hit), player);
                }
            }
        };
    }

    public ItemStack getStack(int slot) {
        return items.getStackInSlot(slot);
    }

    public int getSlot(BlockHitResult hit) {
        Direction front = Registration.SMELTER.get().getFrontDirection(getBlockState());
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
        if (!level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            if (held.isEmpty()) {
                giveToPlayer(slot, player);
            } else if (slot == SLOT_INPUT) {
                player.setItemInHand(hand, items.insertItem(SLOT_INPUT, held, false));
                player.containerMenu.broadcastChanges();
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public void giveToPlayer(int slot, Player player) {
        if (slot != SLOT_INPUT && slot != SLOT_OUTPUT) {
            return;
        }
        ItemStack taken = items.extractItem(slot, player.isShiftKeyDown() ? 64 : 1, false);
        if (!taken.isEmpty()) {
            if (!player.getInventory().add(taken)) {
                player.drop(taken, false);
            }
            player.containerMenu.broadcastChanges();
        }
    }

    @Nullable
    private RecipeHolder<SmeltingRecipe> findRecipe(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        return recipeCheck.getRecipeFor(new SingleRecipeInput(stack), level).orElse(null);
    }

    @Override
    public void tickServer() {
        long previousPower = usingPower;
        usingPower = 0;
        var recipe = findRecipe(items.getStackInSlot(SLOT_INPUT));
        ResourceLocation nextId = recipe == null ? null : recipe.id();
        if (!Objects.equals(nextId, recipeId)) {
            progress = 0;
            recipeId = nextId;
            markDirtyQuick();
        }
        if (recipe != null) {
            ItemStack result = recipe.value().assemble(new SingleRecipeInput(items.getStackInSlot(SLOT_INPUT)), level.registryAccess());
            ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
            int limit = Math.min(items.getSlotLimit(SLOT_OUTPUT), result.getMaxStackSize());
            if (!result.isEmpty() && (output.isEmpty() || ItemStack.isSameItemSameComponents(output, result))
                    && output.getCount() + result.getCount() <= limit
                    && PowerReceiverSupport.consumePower(level, worldPosition, powerCost(PowerConfiguration.SMELTER_POWER_PER_TICK.get()), true)) {
                usingPower = powerCost(PowerConfiguration.SMELTER_POWER_PER_TICK.get());
                progress += PowerConfiguration.SMELTER_SPEED.get() * operationSpeed();
                while (progress >= recipe.value().getCookingTime() && !items.getStackInSlot(SLOT_INPUT).isEmpty()
                        && output.getCount() + result.getCount() <= limit) {
                    progress -= Math.max(1, recipe.value().getCookingTime());
                    items.extractItem(SLOT_INPUT, 1, false);
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
        CompoundTag info = tag.getCompound("Info");
        progress = Math.max(0, info.getInt("smeltProgress"));
        recipeId = ResourceLocation.tryParse(info.getString("smeltRecipe"));
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("Items", items.serializeNBT(provider));
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tag, HolderLookup.Provider provider) {
        items.deserializeNBT(provider, tag.getCompound("Items"));
    }

    @Override
    public IItemHandler getAutomationHandler(@Nullable Direction side) {
        return side == null ? items : sidedHandlers.computeIfAbsent(side, SidedHandler::new);
    }

    private class SidedHandler implements IItemHandler {

        private final Direction side;

        private SidedHandler(Direction side) {
            this.side = side;
        }

        private boolean isOutput() {
            return side == Direction.DOWN || side == Registration.SMELTER.get().getFrontDirection(getBlockState()).getOpposite();
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(isOutput() ? SLOT_OUTPUT : SLOT_INPUT) : ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? items.insertItem(SLOT_INPUT, stack, simulate) : stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 && isOutput() ? items.extractItem(SLOT_OUTPUT, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 64 : 0;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0 && !isOutput() && items.isItemValid(SLOT_INPUT, stack);
        }
    }
}
