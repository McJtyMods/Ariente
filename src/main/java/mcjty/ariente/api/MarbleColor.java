package mcjty.ariente.api;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public enum MarbleColor implements IStringSerializable {
    GRAY("gray"),
    WHITE("white"),
    BLUE("blue"),
    BLACK("black"),
    LIME("lime"),
    RED("red"),
    DARKBLUE("darkblue")
    ;

    public static final MarbleColor[] VALUES = new MarbleColor[MarbleColor.values().length];

    public static final EnumProperty<MarbleColor> COLOR = EnumProperty.create("color", MarbleColor.class);

    static {
        int i = 0;
        for (MarbleColor color : MarbleColor.values()) {
            VALUES[i++] = color;
        }
    }

    private final String name;

    MarbleColor(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUnlocalizedName()
    {
        return name;
    }

}
