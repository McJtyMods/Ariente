package mcjty.ariente.display;

import java.util.ArrayList;
import java.util.List;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class TextDisplayModule implements DisplayModuleType {

    @Override
    public String title() {
        return "TEXT MODULE";
    }

    public static int scale(ItemStack stack) {
        CompoundTag tag = DisplayModuleItem.config(stack);
        return tag.contains("Scale") ? Mth.clamp(tag.getIntOr("Scale", 0), 1, 4) : 1;
    }

    private static int glyphWidth(int point) {
        if (point == ' ') {
            return 4;
        }
        if ("!.,:;'|".indexOf(point) >= 0) {
            return 2;
        }
        if ("`il".indexOf(point) >= 0) {
            return 3;
        }
        if ("I[]t".indexOf(point) >= 0) {
            return 4;
        }
        if ("(){}f<>".indexOf(point) >= 0) {
            return 5;
        }
        return point > 126 ? 10 : point == '@' || point == '~' ? 7 : 6;
    }

    public static int lineWidth(String line) {
        return line.codePoints().map(TextDisplayModule::glyphWidth).sum();
    }

    public static List<String> lines(ItemStack stack) {
        String text = DisplayModuleItem.config(stack).getStringOr("Text", "");
        List<String> lines = new ArrayList<>();
        int maxWidth = 488 / scale(stack);
        for (String paragraph : text.split("\\n", -1)) {
            StringBuilder line = new StringBuilder();
            int width = 0;
            for (int point : paragraph.codePoints().toArray()) {
                int glyph = glyphWidth(point);
                if (width + glyph > maxWidth && !line.isEmpty()) {
                    lines.add(line.toString());
                    line.setLength(0);
                    width = 0;
                }
                line.appendCodePoint(point);
                width += glyph;
            }
            lines.add(line.toString());
        }
        return List.copyOf(lines.subList(0, Math.min(lines.size(), 60 / scale(stack))));
    }

    @Override
    public Size size(ItemStack stack) {
        List<String> lines = lines(stack);
        int scale = scale(stack);
        int width = lines.stream().mapToInt(TextDisplayModule::lineWidth).max().orElse(1);
        return new Size(Math.max(32, width * scale + 12), Math.max(24, lines.size() * 10 * scale + 12));
    }

    @Override
    public void configure(IGuiComponentRegistry r, IPanel panel, ModuleSettings settings) {
        panel.add(r.textEntry(.3, 1, 7.2, 3.3).name("display_text").maxLength(1024)
                .text(() -> settings.config().getStringOr("Text", ""))
                .changed((player, entity, text) -> settings.text(player, entity, "Text", text)));
        settings.addColorButton(r, panel, 5.8);
        settings.addNumber(r, panel, 4.8, "Scale", "Text size", 1, 1, 4);
        panel.add(r.text(.3, 6.9, 8, .5).text("Esc / click outside finishes typing.").scale(.38f));
    }
}
