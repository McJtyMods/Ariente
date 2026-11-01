package mcjty.ariente.display;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;

/** Immutable surface settings shared by every base in a joined display. */
public record DisplayAppearance(DisplayBackground type, int color, int transparency, boolean border) {

    public static final int DEFAULT_COLOR = 0xdcf3fa;
    public static final int DEFAULT_TRANSPARENCY = 87;
    public static final DisplayAppearance DEFAULT = new DisplayAppearance(DisplayBackground.TRANSPARENT,
            DEFAULT_COLOR, DEFAULT_TRANSPARENCY, true);

    public DisplayAppearance {
        color &= 0xffffff;
        transparency = Mth.clamp(transparency, 0, 100);
    }

    public DisplayAppearance withType(DisplayBackground type) {
        return new DisplayAppearance(type, color, transparency, border);
    }

    public DisplayAppearance withColor(int color) {
        return new DisplayAppearance(type, color, transparency, border);
    }

    public DisplayAppearance withTransparency(int transparency) {
        return new DisplayAppearance(type, color, transparency, border);
    }

    public DisplayAppearance withBorder(boolean border) {
        return new DisplayAppearance(type, color, transparency, border);
    }

    public int alpha() {
        return switch (type) {
            case INVISIBLE -> 0;
            case TRANSPARENT -> Math.round((100 - transparency) * 255f / 100);
            case SOLID -> 255;
        };
    }

    public int surfaceColor() {
        return alpha() << 24 | color;
    }

    public int scanLineColor() {
        // Fade the decorative texture along with the surface, including a fully clear 100% setting.
        return Math.round(alpha() * 22f / 33) << 24 | color;
    }

    public void write(CompoundTag tag) {
        CompoundTag appearance = new CompoundTag();
        appearance.putInt("Type", type.ordinal());
        appearance.putInt("Color", color);
        appearance.putInt("Transparency", transparency);
        appearance.putBoolean("Border", border);
        tag.put("Appearance", appearance);
    }

    public static DisplayAppearance read(CompoundTag tag) {
        if (tag.contains("Appearance", Tag.TAG_COMPOUND)) {
            CompoundTag appearance = tag.getCompound("Appearance");
            return new DisplayAppearance(appearance.contains("Type") ? DisplayBackground.from(appearance.getInt("Type")) : DEFAULT.type,
                    appearance.contains("Color") ? appearance.getInt("Color") : DEFAULT_COLOR,
                    appearance.contains("Transparency") ? appearance.getInt("Transparency") : DEFAULT_TRANSPARENCY,
                    !appearance.contains("Border") || appearance.getBoolean("Border"));
        }
        // The former Background field combined six presets. Preserve their appearance when upgrading saves.
        if (tag.contains("Background")) {
            return switch (Math.floorMod(tag.getInt("Background"), 6)) {
                case 0 -> DEFAULT.withType(DisplayBackground.INVISIBLE).withBorder(false);
                case 2 -> DEFAULT.withType(DisplayBackground.SOLID).withColor(0x000000);
                case 3 -> DEFAULT.withType(DisplayBackground.SOLID).withColor(0xffffff);
                case 4 -> DEFAULT.withType(DisplayBackground.SOLID).withColor(0x679ec4);
                case 5 -> DEFAULT.withType(DisplayBackground.SOLID).withColor(0x81c374);
                default -> DEFAULT;
            };
        }
        return DEFAULT;
    }
}
