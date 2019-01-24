package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum NodeOrientation implements IStringSerializable {
    DOWN_NE("down_ne", EnumFacing.DOWN, 0, 0, PartSlot.DOWN_NE),
    DOWN_NW("down_nw", EnumFacing.DOWN, 0, 1, PartSlot.DOWN_NW),
    DOWN_SE("down_se", EnumFacing.DOWN, 1, 0, PartSlot.DOWN_SE),
    DOWN_SW("down_sw", EnumFacing.DOWN, 1, 1, PartSlot.DOWN_SW),
    UP_NE("up_ne", EnumFacing.UP, 0, 0, PartSlot.UP_NE),
    UP_NW("up_nw", EnumFacing.UP, 0, 1, PartSlot.UP_NW),
    UP_SE("up_se", EnumFacing.UP, 1, 0, PartSlot.UP_SE),
    UP_SW("up_sw", EnumFacing.UP, 1, 1, PartSlot.UP_SW),
    NORTH_DE("north_de", EnumFacing.NORTH, 0, 0, PartSlot.NORTH_DE),
    NORTH_DW("north_dw", EnumFacing.NORTH, 0, 1, PartSlot.NORTH_DW),
    NORTH_UE("north_ue", EnumFacing.NORTH, 1, 0, PartSlot.NORTH_UE),
    NORTH_UW("north_uw", EnumFacing.NORTH, 1, 1, PartSlot.NORTH_UW),
    SOUTH_DE("south_de", EnumFacing.SOUTH, 0, 0, PartSlot.SOUTH_DE),
    SOUTH_DW("south_dw", EnumFacing.SOUTH, 0, 1, PartSlot.SOUTH_DW),
    SOUTH_UE("south_ue", EnumFacing.SOUTH, 1, 0, PartSlot.SOUTH_UE),
    SOUTH_UW("south_uw", EnumFacing.SOUTH, 1, 1, PartSlot.SOUTH_UW),
    WEST_DN("west_dn", EnumFacing.WEST, 0, 0, PartSlot.WEST_DN),
    WEST_DS("west_ds", EnumFacing.WEST, 0, 1, PartSlot.WEST_DS),
    WEST_UN("west_un", EnumFacing.WEST, 1, 0, PartSlot.WEST_UN),
    WEST_US("west_us", EnumFacing.WEST, 1, 1, PartSlot.WEST_US),
    EAST_DN("east_dn", EnumFacing.EAST, 0, 0, PartSlot.EAST_DN),
    EAST_DS("east_ds", EnumFacing.EAST, 0, 1, PartSlot.EAST_DS),
    EAST_UN("east_un", EnumFacing.EAST, 1, 0, PartSlot.EAST_UN),
    EAST_US("east_us", EnumFacing.EAST, 1, 1, PartSlot.EAST_US);

    public static final NodeOrientation[] VALUES = NodeOrientation.values();

    private final String name;
    private final EnumFacing mainDirection;
    private final int dx;
    private final int dy;
    private final PartSlot slot;

    NodeOrientation(String name, EnumFacing mainDirection, int dx, int dy, PartSlot slot) {
        this.name = name;
        this.mainDirection = mainDirection;
        this.dx = dx;
        this.dy = dy;
        this.slot = slot;
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

    public PartSlot getSlot() {
        return slot;
    }
}
