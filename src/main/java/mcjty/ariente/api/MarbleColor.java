package mcjty.ariente.api;

import net.minecraft.item.DyeColor;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public enum MarbleColor implements IStringSerializable {
    GRAY("gray", "graymarble", DyeColor.GRAY),
    WHITE("white", "whitemarble", DyeColor.WHITE),
    BLUE("blue", "bluemarble", DyeColor.LIGHT_BLUE),
    BLACK("black", "blackmarble", DyeColor.BLACK),
    LIME("lime", "limemarble", DyeColor.LIME),
    RED("red", "redmarble", DyeColor.RED),
    DARKBLUE("darkblue", "darkbluemarble", DyeColor.BLUE)
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
    private final DyeColor color;

    MarbleColor(String name, String texture, DyeColor color) {
        this.name = name;
        this.texture = texture;
        this.color = color;
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

    public DyeColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        return name;
    }
}
