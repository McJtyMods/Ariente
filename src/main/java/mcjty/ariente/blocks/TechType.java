package mcjty.ariente.blocks;

import net.minecraft.util.IStringSerializable;

public enum TechType implements IStringSerializable {
    DOTS("dots"),
    LINES("lines");

    public static final TechType[] VALUES = new TechType[TechType.values().length];

    static {
        int i = 0;
        for (TechType type : TechType.values()) {
            VALUES[i++] = type;
        }
    }

    private final String name;

    TechType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
