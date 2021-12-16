package mcjty.ariente.api;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;

public enum MarbleType implements StringRepresentable {
    VARIATION1("variation1", "marbletech1"),
    VARIATION2("variation2", "marbletech2"),
    VARIATION3("variation3", "marbletech3"),
    ;

    public static final MarbleType[] VALUES = new MarbleType[MarbleType.values().length];

    public static final EnumProperty<MarbleType> TYPE = EnumProperty.create("type", MarbleType.class);

    static {
        int i = 0;
        for (MarbleType type : MarbleType.values()) {
            VALUES[i++] = type;
        }
    }

    private final String name;
    private final String texture;

    MarbleType(String name, String texture) {
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
