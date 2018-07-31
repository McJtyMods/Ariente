package mcjty.ariente.blocks.decorative;

import net.minecraft.util.IStringSerializable;

public enum PatternType implements IStringSerializable {
    DOTS("dots"),
    LINES("lines")
    ;

    public static final PatternType[] VALUES = new PatternType[PatternType.values().length];

    static {
        int i = 0;
        for (PatternType type : PatternType.values()) {
            VALUES[i++] = type;
        }
    }

    private final String name;

    PatternType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
