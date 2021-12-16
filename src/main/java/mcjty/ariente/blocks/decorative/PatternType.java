package mcjty.ariente.blocks.decorative;

import net.minecraft.util.StringRepresentable;

public enum PatternType implements StringRepresentable {
    DOTS("dots", "pattern_dots"),
    LINES("lines", "pattern_lines")
    ;

    public static final PatternType[] VALUES = new PatternType[PatternType.values().length];

    static {
        int i = 0;
        for (PatternType type : PatternType.values()) {
            VALUES[i++] = type;
        }
    }

    private final String name;
    private final String texture;

    PatternType(String name, String texture) {
        this.name = name;
        this.texture = texture;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }

    @Override
    public String toString() {
        return name;
    }
}
