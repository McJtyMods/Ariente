package mcjty.ariente.blocks;

import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Shared, item-preserving upgrade state for powered Ariente machines. */
public abstract class ExtremeMachineTile extends ArienteBlockEntity {

    private static final String EXTREME_CIRCUIT = "extremeCircuit";
    private boolean extreme;

    protected ExtremeMachineTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean acceptsExtremeCircuit() {
        return true;
    }

    public boolean hasExtremeCircuit() {
        return extreme;
    }

    public boolean installExtremeCircuit() {
        if (extreme || !acceptsExtremeCircuit()) {
            return false;
        }
        extreme = true;
        markDirtyClient();
        return true;
    }

    public int operationSpeed() {
        return extreme ? 2 : 1;
    }

    public long powerCost(long amount) {
        // Round up so positive costs never become free, including odd config values.
        return extreme ? amount / 2 + amount % 2 : amount;
    }

    public long powerBudget(long available) {
        return extreme ? Math.min(available, Long.MAX_VALUE / 2) * 2 : available;
    }

    public static boolean hasExtremeCircuit(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(Registration.MACHINE_DATA.get(), CustomData.EMPTY).copyTag();
        return data != null && data.getCompound("Info").getBoolean(EXTREME_CIRCUIT);
    }

    @Override
    protected void saveInfo(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveInfo(tag, provider);
        getOrCreateInfo(tag).putBoolean(EXTREME_CIRCUIT, extreme);
    }

    @Override
    protected void loadInfo(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadInfo(tag, provider);
        extreme = tag.getCompound("Info").getBoolean(EXTREME_CIRCUIT);
    }
}
