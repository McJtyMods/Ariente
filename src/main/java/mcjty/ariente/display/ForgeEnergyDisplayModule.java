package mcjty.ariente.display;

import team.reborn.energy.api.EnergyStorage;

import mcjty.lib.varia.CapabilityTools;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import mcjty.lib.varia.IEnergyStorage;

public class ForgeEnergyDisplayModule extends ResourceDisplayModule {

    @Override
    public String title() {
        return "FORGE ENERGY MODULE";
    }

    @Override
    public Size size(ItemStack stack) {
        int rows = 1 + (flag(stack, "Capacity", true) ? 1 : 0) + (flag(stack, "Percent", true) ? 1 : 0)
                + (flag(stack, "Rate", false) ? 1 : 0);
        return new Size(128, 24 + rows * 10 + (mode(stack) == 1 ? 12 : mode(stack) == 2 ? 40 : 0));
    }

    public static IEnergyStorage energy(BlockEntity tile, Direction side) {
        IEnergyStorage handler = CapabilityTools.energyHandler(EnergyStorage.SIDED.find(tile.getLevel(), tile.getBlockPos(), side));
        return handler != null ? handler : CapabilityTools.energyHandler(EnergyStorage.SIDED.find(tile.getLevel(), tile.getBlockPos(), null));
    }

    @Override
    public boolean canLink(Level level, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        return tile != null && energy(tile, side) != null;
    }

    @Override
    public CompoundTag sample(Level level, ItemStack stack, CompoundTag previous) {
        CompoundTag data = new CompoundTag();
        ModuleLink link = ModuleLink.resolve(level, stack);
        if (link.tile() == null) {
            data.putString("Status", link.status());
            return data;
        }
        IEnergyStorage handler = energy(link.tile(), link.side());
        if (handler == null) {
            data.putString("Status", "FE storage unavailable");
            return data;
        }
        data.putString("Name", link.tile().getBlockState().getBlock().getName().getString());
        long amount = Math.max(0, handler.getEnergyStored());
        data.putLong("Amount", amount);
        data.putLong("Capacity", Math.max(0, handler.getMaxEnergyStored()));
        long now = link.level().getGameTime();
        data.putLong("Tick", now);
        // This is net buffer change, not separately measured input or output.
        long elapsed = now - previous.getLongOr("Tick", 0);
        if (previous.contains("Tick") && elapsed > 0 && elapsed <= 40) {
            data.putDouble("Rate", (amount - previous.getLongOr("Amount", 0)) / (double) elapsed);
        }
        if (mode(stack) == 2) {
            appendHistory(data, previous, amount);
        }
        return data;
    }

    @Override
    public void configure(IGuiComponentRegistry r, IPanel panel, ModuleSettings settings) {
        addStyle(r, panel, settings);
        settings.addToggle(r, panel, 2, "Capacity", "Show capacity", true);
        settings.addToggle(r, panel, 3, "Percent", "Show percentage", true);
        settings.addToggle(r, panel, 4, "Rate", "Net buffer change / tick", false);
        settings.addToggle(r, panel, 5, "Compact", "Abbreviate large numbers", true);
        settings.addColorButton(r, panel, 6);
    }
}
