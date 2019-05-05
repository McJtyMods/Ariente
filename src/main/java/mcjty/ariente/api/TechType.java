package mcjty.ariente.api;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public enum TechType implements IStringSerializable {
    DOTS("dots"),
    LINES("lines"),
    VAR1("var1"),
    RED_VAR1("red_var1"),
    LINES_GLOW("lines_glow"),
    RED_LINES_GLOW("red_lines_glow"),
    PANEL("panel")
    ;

    public static final TechType[] VALUES = new TechType[TechType.values().length];

    public static final PropertyEnum<TechType> TYPE = PropertyEnum.create("type", TechType.class);

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
