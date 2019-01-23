package mcjty.ariente.blocks.utility.autofield;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum NodeOrientation implements IStringSerializable {
    NORTH_00("north_00", EnumFacing.NORTH, 0, 0),
    NORTH_01("north_01", EnumFacing.NORTH, 0, 1),
    NORTH_10("north_10", EnumFacing.NORTH, 1, 0),
    NORTH_11("north_11", EnumFacing.NORTH, 1, 1),
    SOUTH_00("south_00", EnumFacing.SOUTH, 0, 0),
    SOUTH_01("south_01", EnumFacing.SOUTH, 0, 1),
    SOUTH_10("south_10", EnumFacing.SOUTH, 1, 0),
    SOUTH_11("south_11", EnumFacing.SOUTH, 1, 1),
    WEST_00("west_00", EnumFacing.WEST, 0, 0),
    WEST_01("west_01", EnumFacing.WEST, 0, 1),
    WEST_10("west_10", EnumFacing.WEST, 1, 0),
    WEST_11("west_11", EnumFacing.WEST, 1, 1),
    EAST_00("east_00", EnumFacing.EAST, 0, 0),
    EAST_01("east_01", EnumFacing.EAST, 0, 1),
    EAST_10("east_10", EnumFacing.EAST, 1, 0),
    EAST_11("east_11", EnumFacing.EAST, 1, 1),
    UP_00("up_00", EnumFacing.UP, 0, 0),
    UP_01("up_01", EnumFacing.UP, 0, 1),
    UP_10("up_10", EnumFacing.UP, 1, 0),
    UP_11("up_11", EnumFacing.UP, 1, 1),
    DOWN_00("down_00", EnumFacing.DOWN, 0, 0),
    DOWN_01("down_01", EnumFacing.DOWN, 0, 1),
    DOWN_10("down_10", EnumFacing.DOWN, 1, 0),
    DOWN_11("down_11", EnumFacing.DOWN, 1, 1);

    public static final NodeOrientation[] VALUES = NodeOrientation.values();

    private final String name;
    private final EnumFacing mainDirection;
    private final int dx;
    private final int dy;

    NodeOrientation(String name, EnumFacing mainDirection, int dx, int dy) {
        this.name = name;
        this.mainDirection = mainDirection;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public String getName() {
        return name;
    }

    public EnumFacing getMainDirection() {
        return mainDirection;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
