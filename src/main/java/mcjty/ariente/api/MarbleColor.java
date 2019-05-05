package mcjty.ariente.api;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public enum MarbleColor implements IStringSerializable {
    GRAY("gray"),
    WHITE("white"),
    BLUE("blue"),
    BLACK("black"),
    LIME("lime"),
    RED("red");

    public static final MarbleColor[] VALUES = new MarbleColor[MarbleColor.values().length];

    public static final PropertyEnum<MarbleColor> COLOR = PropertyEnum.create("type", MarbleColor.class);

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
