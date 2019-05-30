package mcjty.ariente.api;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

public enum MarbleType implements IStringSerializable {
    VARIATION1("variation1"),
    VARIATION2("variation2"),
    VARIATION3("variation3"),
    ;

    public static final MarbleType[] VALUES = new MarbleType[MarbleType.values().length];

    public static final PropertyEnum<MarbleType> TYPE = PropertyEnum.create("type", MarbleType.class);

    static {
        int i = 0;
        for (MarbleType type : MarbleType.values()) {
            VALUES[i++] = type;
        }
    }

    private final String name;

    MarbleType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
