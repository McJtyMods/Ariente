package mcjty.ariente;

import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.DisplayModuleType;
import mcjty.ariente.display.FluidDisplayModule;
import mcjty.ariente.display.ForgeEnergyDisplayModule;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class ResourceDisplayTests {

    private static ProbeChest machine(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockState state = Blocks.CHEST.defaultBlockState();
        helper.getLevel().setBlockAndUpdate(pos, state);
        ProbeChest tile = new ProbeChest(pos, state);
        helper.getLevel().setBlockEntity(tile);
        return tile;
    }

    private static ItemStack linked(GameTestHelper helper, ProbeChest machine, DisplayModuleItem item) {
        ItemStack stack = new ItemStack(item);
        CompoundTag link = new CompoundTag();
        link.putString("Dimension", helper.getLevel().dimension().location().toString());
        link.putLong("Pos", machine.getBlockPos().asLong());
        link.putInt("Side", Direction.NORTH.get3DDataValue());
        link.putString("Block", "minecraft:chest");
        DisplayModuleItem.config(stack).put("Link", link);
        return stack;
    }

    @GameTest(template = "empty")
    public static void readsMachineReservoirsWithoutTransferringFluid(GameTestHelper helper) {
        ProbeChest machine = machine(helper);
        ItemStack stack = linked(helper, machine, Registration.DISPLAY_FLUIDS.get());
        DisplayModuleType module = Registration.DISPLAY_FLUIDS.get().type();
        helper.assertTrue(module.canLink(helper.getLevel(), machine.getBlockPos(), Direction.NORTH), "An internal machine reservoir can be linked");
        machine.fluid.contents[0].getOrCreateTag().putInt("Quality", 1);
        CompoundTag data = module.sample(helper.getLevel(), stack, new CompoundTag());
        ListTag tanks = data.getList("Tanks", Tag.TAG_COMPOUND);
        helper.assertTrue(tanks.size() == 2 && tanks.getCompound(0).getLong("Amount") == 1000
                && tanks.getCompound(1).getLong("Amount") == 2000, "Both exposed tanks are read independently");
        helper.assertTrue(machine.fluid.contents[0].getAmount() == 1000 && machine.fluid.contents[1].getAmount() == 2000,
                "Sampling leaves the handler's stacks unchanged");
        machine.fluid.contents[0].getOrCreateTag().putInt("Quality", 2);
        helper.assertTrue(FluidStack.loadFluidStackFromNBT(tanks.getCompound(0).getCompound("Fluid")).getTag().getInt("Quality") == 1,
                "Snapshots must not retain the handler's live mutable fluid tags");
        DisplayModuleItem.config(stack).putInt("Tank", 3);
        data = module.sample(helper.getLevel(), stack, data);
        CompoundTag empty = data.getList("Tanks", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(empty.getLong("Amount") == 0 && empty.getLong("Capacity") == 16000, "An empty selected tank retains its capacity");
        DisplayModuleItem.config(stack).putInt("Tank", 4);
        helper.assertTrue(module.sample(helper.getLevel(), stack, data).getString("Status").equals("Selected tank unavailable"),
                "A removed or invalid tank is reported instead of silently reading another tank");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void respectsClickedSideAndSupportsUnsidedStorage(GameTestHelper helper) {
        ProbeChest machine = machine(helper);
        for (DisplayModuleType module : new DisplayModuleType[]{Registration.DISPLAY_FLUIDS.get().type(), Registration.DISPLAY_FE.get().type()}) {
            helper.assertTrue(module.canLink(helper.getLevel(), machine.getBlockPos(), Direction.NORTH), "The exposed face is linkable");
            helper.assertTrue(!module.canLink(helper.getLevel(), machine.getBlockPos(), Direction.SOUTH), "Other sided handlers are not silently substituted");
        }
        machine.unsided = true;
        helper.assertTrue(FluidDisplayModule.fluids(machine, Direction.SOUTH) == machine.fluid
                && ForgeEnergyDisplayModule.energy(machine, Direction.SOUTH) == machine.energy, "Unsided storage is a fallback for both capabilities");
        machine.available = false;
        for (DisplayModuleItem item : new DisplayModuleItem[]{Registration.DISPLAY_FLUIDS.get(), Registration.DISPLAY_FE.get()}) {
            helper.assertTrue(item.type().sample(helper.getLevel(), linked(helper, machine, item), new CompoundTag()).contains("Status"),
                    "Lost capabilities produce an offline status");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fluidHistoryFollowsTankAndFluidIdentity(GameTestHelper helper) {
        ProbeChest machine = machine(helper);
        ItemStack stack = linked(helper, machine, Registration.DISPLAY_FLUIDS.get());
        CompoundTag settings = DisplayModuleItem.config(stack);
        settings.putInt("Mode", 2);
        DisplayModuleType module = Registration.DISPLAY_FLUIDS.get().type();
        CompoundTag data = new CompoundTag();
        for (int reading = 0; reading < 40; reading++) {
            data = module.sample(helper.getLevel(), stack, data);
        }
        helper.assertTrue(data.getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getLongArray("History").length == 32, "History is bounded");
        settings.putInt("Tank", 2);
        data = module.sample(helper.getLevel(), stack, data);
        helper.assertTrue(data.getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getLongArray("History")[0] == 2000,
                "Changing the selected range does not use another tank's history");
        machine.fluid.contents[1] = FluidStack.EMPTY;
        data = module.sample(helper.getLevel(), stack, data);
        helper.assertTrue(data.getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getLongArray("History").length == 32,
                "Draining a tank to empty preserves its history");
        machine.fluid.contents[1] = new FluidStack(Fluids.WATER, 500);
        data = module.sample(helper.getLevel(), stack, data);
        long[] changed = data.getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getLongArray("History");
        helper.assertTrue(changed.length == 1 && changed[0] == 500, "Replacing lava with water starts a fresh history even after emptying");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void readsForgeEnergyAndMeasuresNetChange(GameTestHelper helper) {
        ProbeChest machine = machine(helper);
        ItemStack stack = linked(helper, machine, Registration.DISPLAY_FE.get());
        DisplayModuleItem.config(stack).putInt("Mode", 2);
        DisplayModuleType module = Registration.DISPLAY_FE.get().type();
        CompoundTag data = module.sample(helper.getLevel(), stack, new CompoundTag());
        helper.assertTrue(data.getLong("Amount") == 1000 && data.getLong("Capacity") == 10000 && !data.contains("Rate"),
                "FE sampling reads stored energy and capacity without inventing an initial rate");
        data.putLong("Tick", helper.getLevel().getGameTime() - 20);
        machine.energy.amount = 1400;
        data = module.sample(helper.getLevel(), stack, data);
        helper.assertTrue(data.getDouble("Rate") == 20, "Net change uses the elapsed game ticks");
        data.putLong("Tick", helper.getLevel().getGameTime() - 20);
        machine.energy.amount = 1000;
        data = module.sample(helper.getLevel(), stack, data);
        helper.assertTrue(data.getDouble("Rate") == -20, "Discharging gives a negative net rate");
        data.putLong("Tick", helper.getLevel().getGameTime() - 100);
        helper.assertTrue(!module.sample(helper.getLevel(), stack, data).contains("Rate"), "Unobserved intervals do not masquerade as current throughput");
        for (int reading = 0; reading < 40; reading++) {
            data = module.sample(helper.getLevel(), stack, data);
        }
        helper.assertTrue(data.getLongArray("History").length == 32 && machine.energy.amount == 1000, "Energy histories are bounded and read only");
        helper.getLevel().setBlockAndUpdate(machine.getBlockPos(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(module.sample(helper.getLevel(), stack, data).getString("Status").equals("Target missing"), "Breaking the linked machine is reported");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blueprintsAndFootprintsCoverBothModules(GameTestHelper helper) {
        for (DisplayModuleItem item : new DisplayModuleItem[]{Registration.DISPLAY_FLUIDS.get(), Registration.DISPLAY_FE.get()}) {
            ItemStack stack = new ItemStack(item);
            helper.assertTrue(BlueprintRecipeRegistry.findRecipe(stack) != null && BlueprintItem.getDestination(BlueprintItem.makeBluePrint(stack)).is(item),
                    "Each module has a working Constructor blueprint");
            for (int mode : new int[]{Integer.MIN_VALUE, 0, 1, 2, Integer.MAX_VALUE}) {
                for (int rows : new int[]{Integer.MIN_VALUE, 1, 8, Integer.MAX_VALUE}) {
                    DisplayModuleItem.config(stack).putInt("Mode", mode);
                    DisplayModuleItem.config(stack).putInt("Rows", rows);
                    DisplayModuleType.Size size = item.type().size(stack);
                    helper.assertTrue(size.width() <= 512 && size.height() <= 636, "All modes have a bounded footprint even with invalid saved settings");
                }
            }
        }
        helper.succeed();
    }

    private static class ProbeChest extends ChestBlockEntity {

        private final FluidProbe fluid = new FluidProbe();
        private final EnergyProbe energy = new EnergyProbe();
        private final LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> fluid);
        private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
        private boolean unsided;
        private boolean available = true;

        private ProbeChest(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
            boolean exposed = available && (side == Direction.NORTH || side == null && unsided);
            if (capability == ForgeCapabilities.FLUID_HANDLER) {
                return exposed ? fluidCapability.cast() : LazyOptional.empty();
            }
            if (capability == ForgeCapabilities.ENERGY) {
                return exposed ? energyCapability.cast() : LazyOptional.empty();
            }
            return super.getCapability(capability, side);
        }
    }

    private static class FluidProbe implements IFluidHandler {

        private final FluidStack[] contents = {new FluidStack(Fluids.WATER, 1000), new FluidStack(Fluids.LAVA, 2000), FluidStack.EMPTY};
        private final int[] capacities = {4000, 8000, 16000};

        @Override
        public int getTanks() {
            return contents.length;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return contents[tank];
        }

        @Override
        public int getTankCapacity(int tank) {
            return capacities[tank];
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            throw new AssertionError("Display must not attempt to fill a tank");
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            throw new AssertionError("Display must not attempt to drain a tank");
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            throw new AssertionError("Display must not attempt to drain a tank");
        }
    }

    private static class EnergyProbe implements IEnergyStorage {

        private int amount = 1000;

        @Override
        public int getEnergyStored() {
            return amount;
        }

        @Override
        public int getMaxEnergyStored() {
            return 10000;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            throw new AssertionError("Display must not attempt to receive energy");
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            throw new AssertionError("Display must not attempt to extract energy");
        }
    }
}
