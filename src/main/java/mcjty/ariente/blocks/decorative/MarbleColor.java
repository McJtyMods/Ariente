package mcjty.ariente.blocks.decorative;

import net.minecraft.util.IStringSerializable;

public enum MarbleColor implements IStringSerializable {
    GRAY("gray"),
    WHITE("white"),
    BLUE("blue"),
    BLACK("black");

    public static final MarbleColor[] VALUES = new MarbleColor[MarbleColor.values().length];

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

}
