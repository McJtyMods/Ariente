package mcjty.ariente.display;

import mcjty.ariente.items.ItemData;
import mcjty.lib.varia.CapabilityTools;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidDisplayModule extends ResourceDisplayModule {

    @Override
    public String title() {
        return "FLUID MODULE";
    }

    public static int rows(ItemStack stack) {
        return number(stack, "Rows", 2, 1, 8);
    }

    public static int rowHeight(ItemStack stack) {
        return switch (mode(stack)) {
            case 1 -> 32;
            case 2 -> 64;
            default -> 24;
        };
    }

    @Override
    public Size size(ItemStack stack) {
        return new Size(128, 24 + rows(stack) * rowHeight(stack));
    }

    public static ResourceHandler<FluidResource> fluids(BlockEntity tile, Direction side) {
        ResourceHandler<FluidResource> handler = CapabilityTools.fluidHandler(tile.getLevel().getCapability(Capabilities.Fluid.BLOCK, tile.getBlockPos(), side));
        return handler != null ? handler : CapabilityTools.fluidHandler(tile.getLevel().getCapability(Capabilities.Fluid.BLOCK, tile.getBlockPos(), null));
    }

    @Override
    public boolean canLink(Level level, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        return tile != null && fluids(tile, side) != null;
    }

    @Override
    public CompoundTag sample(Level level, ItemStack stack, CompoundTag previous) {
        CompoundTag data = new CompoundTag();
        ModuleLink link = ModuleLink.resolve(level, stack);
        if (link.tile() == null) {
            data.putString("Status", link.status());
            return data;
        }
        ResourceHandler<FluidResource> handler = fluids(link.tile(), link.side());
        if (handler == null) {
            data.putString("Status", "Fluid storage unavailable");
            return data;
        }
        int first = number(stack, "Tank", 1, 1, 9999) - 1;
        int tanks = handler.size();
        if (first >= tanks) {
            data.putString("Status", tanks == 0 ? "No fluid tanks" : "Selected tank unavailable");
            return data;
        }
        data.putString("Name", link.tile().getBlockState().getBlock().getName().getString());
        data.putInt("TankCount", tanks);
        ListTag readings = new ListTag();
        ListTag old = previous.getListOrEmpty("Tanks");
        for (int tank = first; tank < Math.min(tanks, first + rows(stack)); tank++) {
            FluidStack fluid = handler.getResource(tank).toStack((int) handler.getAmountAsLong(tank));
            CompoundTag entry = new CompoundTag();
            entry.putInt("Tank", tank + 1);
            entry.put("Fluid", ItemData.encodeFluid(level.registryAccess(), fluid));
            entry.putLong("Amount", fluid.isEmpty() ? 0 : Math.max(0, fluid.getAmount()));
            entry.putLong("Capacity", Math.max(0, handler.getCapacityAsLong(tank, handler.getResource(tank))));
            if (mode(stack) == 2) {
                CompoundTag last = new CompoundTag();
                for (int index = 0; index < old.size(); index++) {
                    CompoundTag candidate = old.getCompoundOrEmpty(index);
                    FluidStack oldFluid = ItemData.decodeFluid(level.registryAccess(), candidate.getCompoundOrEmpty("HistoryFluid"));
                    // Do not join histories of different tanks or different fluids.
                    if (candidate.getIntOr("Tank", 0) == tank + 1 && (fluid.isEmpty() || oldFluid.isEmpty() || FluidStack.isSameFluidSameComponents(fluid, oldFluid))) {
                        last = candidate;
                        break;
                    }
                }
                entry.put("HistoryFluid", fluid.isEmpty() ? last.getCompoundOrEmpty("HistoryFluid").copy()
                        : ItemData.encodeFluid(level.registryAccess(), fluid));
                appendHistory(entry, last, entry.getLongOr("Amount", 0));
            }
            readings.add(entry);
        }
        data.put("Tanks", readings);
        return data;
    }

    @Override
    public void configure(IGuiComponentRegistry r, IPanel panel, ModuleSettings settings) {
        addStyle(r, panel, settings);
        settings.addNumber(r, panel, 2, "Tank", "First tank (1-based)", 1, 1, 9999);
        settings.addNumber(r, panel, 3, "Rows", "Tanks shown", 2, 1, 8);
        panel.add(r.textChoice(.3, 4, 7.2, .85).name("setting_Buckets").scale(.48f)
                .addText("Units: Millibuckets (mB)").addText("Units: Buckets (B)")
                .getter(p -> settings.flag("Buckets", false) ? 1 : 0)
                .hitEvent((c, p, e, x, y) -> settings.toggle(p, e, "Buckets", false)));
        settings.addToggle(r, panel, 5, "Capacity", "Show capacity", true);
        settings.addColorButton(r, panel, 6);
    }
}
