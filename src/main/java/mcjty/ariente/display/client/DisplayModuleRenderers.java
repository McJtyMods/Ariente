package mcjty.ariente.display.client;

import mcjty.ariente.items.ItemData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import mcjty.ariente.display.AlienTextDisplayModule;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.DisplayModuleType;
import mcjty.ariente.display.InstalledModule;
import mcjty.ariente.display.PowerDisplayModule;
import mcjty.ariente.display.StorageDisplayModule;
import mcjty.ariente.display.TextDisplayModule;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/** Client extension point. New modules register a server type and a matching renderer. */
public class DisplayModuleRenderers {

    private static final Map<DisplayModuleType, ModuleRenderer> RENDERERS = new HashMap<>();
    private static final Map<InstalledModule, List<String>> TEXT_LINES = new WeakHashMap<>();
    private static final Map<InstalledModule, ItemIcons> ITEM_ICONS = new WeakHashMap<>();
    private static final int CYAN = 0x83eaff;
    private static final int WHITE = 0xe8faff;

    public static void register(DisplayModuleType type, ModuleRenderer renderer) {
        RENDERERS.put(type, renderer);
    }

    public static void init() {
        register(Registration.DISPLAY_POWER.get().type(), DisplayModuleRenderers::power);
        register(Registration.DISPLAY_STORAGE.get().type(), DisplayModuleRenderers::storage);
        register(Registration.DISPLAY_TEXT.get().type(), DisplayModuleRenderers::text);
        register(Registration.DISPLAY_ALIEN_TEXT.get().type(), DisplayModuleRenderers::alienText);
        register(Registration.DISPLAY_CAMERA.get().type(), CameraDisplayRenderer::render);
        ResourceDisplayRenderers.init();
    }

    public static void render(DisplayCanvas canvas, InstalledModule module) {
        ModuleRenderer renderer = RENDERERS.get(module.type());
        if (renderer != null) {
            renderer.render(canvas, module);
        }
    }

    static void panel(DisplayCanvas c, InstalledModule module, String title) {
        int w = module.size().width();
        int h = module.size().height();
        c.poses().pushPose();
        c.poses().translate(0, 0, -.04);
        c.fill(1, 1, w - 2, h - 2, 0xdd081925);
        c.poses().popPose();
        c.border(1, 1, w - 2, h - 2, 0x8057c9e9);
        c.fill(1, 1, 2, 8, 0xff77efff);
        c.text(title, 7, 5, CYAN, .6f, w - 12);
        c.fill(6, 14, w - 12, .5f, 0x4057c9e9);
    }

    public static String amount(long value) {
        if (value >= 1_000_000_000) {
            return String.format(Locale.ROOT, "%.1fG", value / 1_000_000_000d);
        }
        if (value >= 1_000_000) {
            return String.format(Locale.ROOT, "%.1fM", value / 1_000_000d);
        }
        if (value >= 10_000) {
            return String.format(Locale.ROOT, "%.1fk", value / 1_000d);
        }
        return Long.toString(value);
    }

    private static void power(DisplayCanvas c, InstalledModule module) {
        CompoundTag data = module.snapshot();
        ItemStack stack = module.stack();
        int width = module.size().width();
        panel(c, module, "POWER / " + data.getStringOr("Name", ""));
        if (data.contains("Status")) {
            c.text(data.getStringOr("Status", ""), 6, 20, 0xffbc7a, .6f, width - 12);
            return;
        }
        int y = 20;
        String[] keys = {"Available", "Usage", "Output", "Fuel"};
        String[] labels = {"Flux ", "Use ", "Gen ", "Fuel "};
        for (int index = 0; index < keys.length; index++) {
            if (PowerDisplayModule.flag(stack, keys[index], index < 2)) {
                String value = data.contains(keys[index]) ? index == 3 ? amount(data.getIntOr("Fuel", 0) / 20) + "s"
                        : amount(data.getLongOr(keys[index], 0)) + (index == 0 ? "" : "/t") : "n/a";
                c.text(labels[index] + value, 6, y, WHITE, .65f, width - 12);
                y += 10;
            }
        }
        if (PowerDisplayModule.mode(stack) == 1) {
            c.fill(6, y + 2, width - 12, 5, 0xff183748);
            long available = data.getLongOr("Available", 0);
            long usage = data.getLongOr("Usage", 0);
            float fraction = (float) (usage == 0 ? available > 0 ? 1 : 0 : Math.min(1, available / (double) Math.max(1, usage)));
            c.fill(6, y + 2, (width - 12) * fraction, 5, 0xff63e8cb);
        } else if (PowerDisplayModule.mode(stack) == 2) {
            long[] history = data.getLongArray("History").orElseGet(() -> new long[0]);
            long max = 1;
            for (long value : history) {
                max = Math.max(max, value);
            }
            c.poses().pushPose();
            c.poses().translate(0, 0, -.01);
            for (int line = 0; line <= 3; line++) {
                c.fill(6, y + line * 10, width - 12, .4f, 0x304daac2);
            }
            c.poses().popPose();
            for (int index = 0; index < history.length; index++) {
                float h = 30f * history[index] / max;
                c.fill(6 + (32 - history.length + index) * (width - 12) / 32f, y + 30 - h,
                        (width - 12) / 32f - .7f, Math.max(.5f, h), 0xd075eede);
            }
            c.text("32s", width - 22, y + 32, CYAN, .45f, 18);
        }
    }

    private static void storage(DisplayCanvas c, InstalledModule module) {
        CompoundTag data = module.snapshot();
        ItemStack stack = module.stack();
        boolean grid = StorageDisplayModule.grid(stack);
        int width = module.size().width();
        panel(c, module, grid ? "STORAGE " + data.getIntOr("Page", 0) + "/" + data.getIntOr("Pages", 0) : "MOST STORED");
        if (data.contains("Status")) {
            c.text(data.getStringOr("Status", ""), 6, 20, 0xffbc7a, .65f, width - 12);
            return;
        }
        ListTag items = data.getListOrEmpty("Items");
        if (items.isEmpty()) {
            c.text("Empty inventory", 6, 20, CYAN, .6f, width - 12);
        }
        int cols = Math.min(StorageDisplayModule.columns(stack), StorageDisplayModule.count(stack));
        ItemIcons saved = ITEM_ICONS.get(module);
        if (saved == null || saved.version() != module.snapshotVersion()) {
            List<ItemStack> result = new ArrayList<>();
            for (int index = 0; index < items.size(); index++) {
                result.add(ItemData.decode(Minecraft.getInstance().level.registryAccess(), items.getCompoundOrEmpty(index).getCompoundOrEmpty("Item")));
            }
            saved = new ItemIcons(module.snapshotVersion(), result);
            ITEM_ICONS.put(module, saved);
        }
        List<ItemStack> icons = saved.icons();
        for (int index = 0; index < items.size(); index++) {
            CompoundTag entry = items.getCompoundOrEmpty(index);
            ItemStack item = icons.get(index);
            int x = grid ? 6 + index % cols * 24 : 6;
            int y = grid ? 20 + index / cols * 24 : 19 + index * 20;
            c.item(item, x, y);
            String count = amount(entry.getLongOr("Count", 0));
            if (grid) {
                c.text(count, x, y + 17, WHITE, .5f, 22);
            } else {
                c.text(item.getHoverName().getString(), 28, y + 1, WHITE, .6f, width - 34);
                c.text(count, 28, y + 10, CYAN, .6f, width - 34);
            }
        }
    }

    private static void text(DisplayCanvas c, InstalledModule module) {
        CompoundTag config = DisplayModuleItem.config(module.stack());
        int color = config.contains("Color") ? config.getIntOr("Color", 0) : 0xb8f4ff;
        int scale = TextDisplayModule.scale(module.stack());
        List<String> lines = TEXT_LINES.computeIfAbsent(module, key -> TextDisplayModule.lines(key.stack()));
        for (int line = 0; line < lines.size(); line++) {
            c.text(lines.get(line), 6, 6 + line * 10 * scale, color, scale, module.size().width() - 12);
        }
    }

    private static void alienText(DisplayCanvas c, InstalledModule module) {
        CompoundTag config = DisplayModuleItem.config(module.stack());
        int color = config.contains("Color") ? config.getIntOr("Color", 0) : 0xb8f4ff;
        int scale = TextDisplayModule.scale(module.stack());
        List<String> lines = TEXT_LINES.computeIfAbsent(module, key -> AlienTextDisplayModule.lines(key.stack()));
        for (int line = 0; line < lines.size(); line++) {
            String symbols = lines.get(line);
            for (int column = 0; column < symbols.length(); column++) {
                char symbol = symbols.charAt(column);
                if (symbol != ' ') {
                    c.alienGlyph(AlienTextDisplayModule.glyphIndex(symbol),
                            6 + column * AlienTextDisplayModule.GLYPH_ADVANCE * scale,
                            6 + line * AlienTextDisplayModule.LINE_HEIGHT * scale,
                            AlienTextDisplayModule.GLYPH_SIZE * scale, color);
                }
            }
        }
    }

    private record ItemIcons(int version, List<ItemStack> icons) {
    }

    @FunctionalInterface
    public interface ModuleRenderer {

        void render(DisplayCanvas canvas, InstalledModule module);
    }
}
