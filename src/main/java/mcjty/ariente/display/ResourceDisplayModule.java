package mcjty.ariente.display;

import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** Common settings and bounded histories for stored fluid and Forge Energy readings. */
public abstract class ResourceDisplayModule implements DisplayModuleType {

    public static int number(ItemStack stack, String key, int fallback, int min, int max) {
        CompoundTag config = DisplayModuleItem.config(stack);
        return Mth.clamp(config.contains(key) ? config.getInt(key) : fallback, min, max);
    }

    public static boolean flag(ItemStack stack, String key, boolean fallback) {
        CompoundTag config = DisplayModuleItem.config(stack);
        return config.contains(key) ? config.getBoolean(key) : fallback;
    }

    public static int mode(ItemStack stack) {
        return number(stack, "Mode", 1, 0, 2);
    }

    protected static void addStyle(IGuiComponentRegistry r, IPanel panel, ModuleSettings settings) {
        panel.add(r.textChoice(.3, 1, 7.2, .85).name("setting_Mode").scale(.48f)
                .addText("Style: Numbers").addText("Style: Meter").addText("Style: History")
                .getter(p -> mode(settings.stack()))
                .hitEvent((c, p, e, x, y) -> settings.setNumber(p, e, "Mode", (mode(settings.stack()) + 1) % 3)));
    }

    protected static void appendHistory(CompoundTag data, CompoundTag previous, long amount) {
        long[] old = previous.getLongArray("History");
        int length = Math.min(32, old.length + 1);
        long[] history = new long[length];
        int keep = length - 1;
        System.arraycopy(old, old.length - keep, history, 0, keep);
        history[length - 1] = amount;
        data.putLongArray("History", history);
    }
}
