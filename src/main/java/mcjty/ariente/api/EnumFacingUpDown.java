package mcjty.ariente.api;

import com.google.common.collect.Maps;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

public enum EnumFacingUpDown implements IStringSerializable {
    NORTH("north"),
    SOUTH("south"),
    WEST("west"),
    EAST("east"),
    NORTH_UP("north_up"),
    SOUTH_UP("south_up"),
    WEST_UP("west_up"),
    EAST_UP("east_up");

    public static final PropertyEnum<EnumFacingUpDown> FACING = PropertyEnum.create("facing", EnumFacingUpDown.class);

    private final String name;
    public static final EnumFacingUpDown[] VALUES = new EnumFacingUpDown[8];
    private static final Map<String, EnumFacingUpDown> NAME_LOOKUP = Maps.<String, EnumFacingUpDown>newHashMap();

    private EnumFacingUpDown(String nameIn) {
        this.name = nameIn;
    }

    public EnumFacingUpDown rotateY() {
        switch (this) {
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    public String getName2() {
        return this.name;
    }

    @Nullable
    public static EnumFacingUpDown byName(String name) {
        return name == null ? null : NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    static {
        for (EnumFacingUpDown enumfacing : values()) {
            VALUES[enumfacing.ordinal()] = enumfacing;
            NAME_LOOKUP.put(enumfacing.getName2().toLowerCase(Locale.ROOT), enumfacing);
        }
    }
}