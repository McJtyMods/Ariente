package mcjty.ariente.display.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import mcjty.ariente.display.FluidDisplayModule;
import mcjty.ariente.display.InstalledModule;
import mcjty.ariente.display.ResourceDisplayModule;
import mcjty.ariente.setup.Registration;
import mcjty.lib.varia.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ResourceDisplayRenderers {

    private static final int WHITE = 0xe8faff;
    private static final int CYAN = 0x83eaff;
    private static final Map<InstalledModule, FluidNames> FLUID_NAMES = new WeakHashMap<>();

    public static void init() {
        DisplayModuleRenderers.register(Registration.DISPLAY_FLUIDS.get().type(), ResourceDisplayRenderers::fluids);
        DisplayModuleRenderers.register(Registration.DISPLAY_FE.get().type(), ResourceDisplayRenderers::energy);
    }

    private static boolean status(DisplayCanvas c, CompoundTag data, int width) {
        if (data.contains("Status")) {
            c.text(data.getString("Status"), 6, 20, 0xffbc7a, .6f, width - 12);
            return true;
        }
        return false;
    }

    private static int color(ItemStack stack) {
        return ResourceDisplayModule.number(stack, "Color", 0xb8f4ff, 0, 0xffffff);
    }

    private static String percentage(long amount, long capacity) {
        return capacity <= 0 ? "n/a" : String.format(Locale.ROOT, "%.1f%%", 100d * amount / capacity);
    }

    private static String energyAmount(long amount, ItemStack stack) {
        return (ResourceDisplayModule.flag(stack, "Compact", true) ? DisplayModuleRenderers.amount(amount) : Long.toString(amount)) + " FE";
    }

    private static String fluidAmount(long amount, ItemStack stack) {
        return ResourceDisplayModule.flag(stack, "Buckets", false)
                ? String.format(Locale.ROOT, "%.3f B", amount / 1000d) : amount + " mB";
    }

    private static void energy(DisplayCanvas c, InstalledModule module) {
        CompoundTag data = module.snapshot();
        ItemStack stack = module.stack();
        int width = module.size().width();
        DisplayModuleRenderers.panel(c, module, "FE / " + data.getString("Name"));
        if (status(c, data, width)) {
            return;
        }
        int y = 20;
        c.text("Stored " + energyAmount(data.getLong("Amount"), stack), 6, y, WHITE, .65f, width - 12);
        y += 10;
        if (ResourceDisplayModule.flag(stack, "Capacity", true)) {
            c.text("Capacity " + energyAmount(data.getLong("Capacity"), stack), 6, y, WHITE, .65f, width - 12);
            y += 10;
        }
        if (ResourceDisplayModule.flag(stack, "Percent", true)) {
            c.text("Filled " + percentage(data.getLong("Amount"), data.getLong("Capacity")), 6, y, CYAN, .65f, width - 12);
            y += 10;
        }
        if (ResourceDisplayModule.flag(stack, "Rate", false)) {
            c.text("Net " + (data.contains("Rate") ? String.format(Locale.ROOT, "%+.1f FE/t", data.getDouble("Rate")) : "n/a"),
                    6, y, WHITE, .65f, width - 12);
            y += 10;
        }
        chart(c, data, stack, 6, y + 2, width - 12);
    }

    private static void fluids(DisplayCanvas c, InstalledModule module) {
        CompoundTag data = module.snapshot();
        ItemStack stack = module.stack();
        int width = module.size().width();
        DisplayModuleRenderers.panel(c, module, "FLUID / " + data.getString("Name"));
        if (status(c, data, width)) {
            return;
        }
        ListTag tanks = data.getList("Tanks", Tag.TAG_COMPOUND);
        FluidNames saved = FLUID_NAMES.get(module);
        if (saved == null || saved.version() != module.snapshotVersion()) {
            List<String> names = new ArrayList<>();
            for (int index = 0; index < tanks.size(); index++) {
                FluidStack fluid = FluidStack.parseOptional(Minecraft.getInstance().level.registryAccess(), tanks.getCompound(index).getCompound("Fluid"));
                names.add(fluid.isEmpty() ? "Empty" : fluid.getDisplayName().getString());
            }
            saved = new FluidNames(module.snapshotVersion(), names);
            FLUID_NAMES.put(module, saved);
        }
        for (int index = 0; index < Math.min(tanks.size(), FluidDisplayModule.rows(stack)); index++) {
            CompoundTag tank = tanks.getCompound(index);
            int y = 20 + index * FluidDisplayModule.rowHeight(stack);
            c.text("Tank " + tank.getInt("Tank") + "/" + data.getInt("TankCount") + ": " + saved.names().get(index),
                    6, y, CYAN, .6f, width - 12);
            String amount = fluidAmount(tank.getLong("Amount"), stack);
            if (ResourceDisplayModule.flag(stack, "Capacity", true)) {
                amount += " / " + fluidAmount(tank.getLong("Capacity"), stack);
            }
            amount += " (" + percentage(tank.getLong("Amount"), tank.getLong("Capacity")) + ")";
            c.text(amount, 6, y + 10, WHITE, .55f, width - 12);
            chart(c, tank, stack, 6, y + 21, width - 12);
        }
    }

    private static void chart(DisplayCanvas c, CompoundTag data, ItemStack stack, int x, int y, int width) {
        int mode = ResourceDisplayModule.mode(stack);
        int color = 0xff000000 | color(stack);
        if (mode == 1) {
            long capacity = data.getLong("Capacity");
            float fraction = capacity <= 0 ? 0 : (float) Math.min(1, data.getLong("Amount") / (double) capacity);
            c.fill(x, y, width, 5, 0xff183748);
            c.fill(x, y, width * fraction, 5, color);
        } else if (mode == 2) {
            long[] history = data.getLongArray("History");
            long max = Math.max(1, data.getLong("Capacity"));
            for (long value : history) {
                max = Math.max(max, value);
            }
            c.poses().pushPose();
            c.poses().translate(0, 0, -.01);
            for (int line = 0; line <= 3; line++) {
                c.fill(x, y + line * 10, width, .4f, 0x304daac2);
            }
            c.poses().popPose();
            for (int index = 0; index < history.length; index++) {
                float height = 30f * history[index] / max;
                c.fill(x + (32 - history.length + index) * width / 32f, y + 30 - height,
                        width / 32f - .7f, Math.max(.5f, height), color);
            }
            c.text("32 readings", x + width - 52, y + 32, CYAN, .45f, 52);
        }
    }

    private record FluidNames(int version, List<String> names) {
    }
}
