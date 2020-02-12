package mcjty.ariente.api;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public enum MarbleColor implements IStringSerializable {
    GRAY("gray", "graymarble"),
    WHITE("white", "whitemarble"),
    BLUE("blue", "bluemarble"),
    BLACK("black", "blackmarble"),
    LIME("lime", "limemarble"),
    RED("red", "redmarble"),
    DARKBLUE("darkblue", "darkbluemarble")
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
    private final String texture;

    MarbleColor(String name, String texture) {
        this.name = name;
        this.texture = texture;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getTexture() {
        return texture;
    }

    public String getUnlocalizedName()
    {
        return name;
    }


    @Override
    public String toString() {
        return name;
    }
}
