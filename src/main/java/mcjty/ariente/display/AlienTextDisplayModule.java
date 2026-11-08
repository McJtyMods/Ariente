package mcjty.ariente.display;

import java.util.ArrayList;
import java.util.List;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.world.item.ItemStack;

/** Decorative, salvage-only text using the sixteen symbols of the alien alphabet. */
public class AlienTextDisplayModule implements DisplayModuleType {

    public static final int GLYPH_SIZE = 8;
    public static final int GLYPH_ADVANCE = 10;
    public static final int LINE_HEIGHT = 10;
    private static final String ALPHABET = "0123456789ABCDEF";
    private static final String DEFAULT_TEXT = "0123\n4567\n89AB\nCDEF";

    @Override
    public String title() {
        return "ALIEN TEXT MODULE";
    }

    public static int glyphIndex(int point) {
        int digit = Character.digit(point, 16);
        return digit >= 0 ? digit : Math.floorMod(point, 16);
    }

    public static List<String> lines(ItemStack stack) {
        var config = DisplayModuleItem.config(stack);
        String text = config.contains("Text") ? config.getString("Text") : DEFAULT_TEXT;
        int scale = TextDisplayModule.scale(stack);
        int maxColumns = 488 / (GLYPH_ADVANCE * scale);
        int maxLines = 60 / scale;
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (int point : paragraph.codePoints().toArray()) {
                if (line.length() == maxColumns) {
                    lines.add(line.toString());
                    if (lines.size() == maxLines) {
                        return List.copyOf(lines);
                    }
                    line.setLength(0);
                }
                line.append(Character.isWhitespace(point) ? ' ' : ALPHABET.charAt(glyphIndex(point)));
            }
            lines.add(line.toString());
            if (lines.size() == maxLines) {
                break;
            }
        }
        return List.copyOf(lines);
    }

    @Override
    public Size size(ItemStack stack) {
        List<String> lines = lines(stack);
        int scale = TextDisplayModule.scale(stack);
        int columns = lines.stream().mapToInt(String::length).max().orElse(0);
        return new Size(Math.max(32, columns * GLYPH_ADVANCE * scale + 12),
                Math.max(24, lines.size() * LINE_HEIGHT * scale + 12));
    }

    @Override
    public void configure(IGuiComponentRegistry r, IPanel panel, ModuleSettings settings) {
        panel.add(r.textEntry(.3, 1, 7.2, 3.3).name("display_text").maxLength(1024)
                .text(() -> settings.config().contains("Text") ? settings.config().getString("Text") : DEFAULT_TEXT)
                .changed((player, entity, text) -> settings.text(player, entity, "Text", text)));
        settings.addNumber(r, panel, 4.8, "Scale", "Text size", 1, 1, 4);
        settings.addColorButton(r, panel, 5.8);
        panel.add(r.text(.3, 6.9, 8, .5).text("0-9 / A-F select symbols. Other text is scrambled.").scale(.3f));
    }
}
