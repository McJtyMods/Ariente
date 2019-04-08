package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum NodeOrientation implements IStringSerializable {
    DOWN_NE("down_ne", EnumFacing.DOWN, PartSlot.DOWN_NE),
    DOWN_NW("down_nw", EnumFacing.DOWN, PartSlot.DOWN_NW),
    DOWN_SE("down_se", EnumFacing.DOWN, PartSlot.DOWN_SE),
    DOWN_SW("down_sw", EnumFacing.DOWN, PartSlot.DOWN_SW),
    UP_NE("up_ne", EnumFacing.UP, PartSlot.UP_NE),
    UP_NW("up_nw", EnumFacing.UP, PartSlot.UP_NW),
    UP_SE("up_se", EnumFacing.UP, PartSlot.UP_SE),
    UP_SW("up_sw", EnumFacing.UP, PartSlot.UP_SW),
    NORTH_DE("north_de", EnumFacing.NORTH, PartSlot.NORTH_DE),
    NORTH_DW("north_dw", EnumFacing.NORTH, PartSlot.NORTH_DW),
    NORTH_UE("north_ue", EnumFacing.NORTH, PartSlot.NORTH_UE),
    NORTH_UW("north_uw", EnumFacing.NORTH, PartSlot.NORTH_UW),
    SOUTH_DE("south_de", EnumFacing.SOUTH, PartSlot.SOUTH_DE),
    SOUTH_DW("south_dw", EnumFacing.SOUTH, PartSlot.SOUTH_DW),
    SOUTH_UE("south_ue", EnumFacing.SOUTH, PartSlot.SOUTH_UE),
    SOUTH_UW("south_uw", EnumFacing.SOUTH, PartSlot.SOUTH_UW),
    WEST_DN("west_dn", EnumFacing.WEST, PartSlot.WEST_DN),
    WEST_DS("west_ds", EnumFacing.WEST, PartSlot.WEST_DS),
    WEST_UN("west_un", EnumFacing.WEST, PartSlot.WEST_UN),
    WEST_US("west_us", EnumFacing.WEST, PartSlot.WEST_US),
    EAST_DN("east_dn", EnumFacing.EAST, PartSlot.EAST_DN),
    EAST_DS("east_ds", EnumFacing.EAST, PartSlot.EAST_DS),
    EAST_UN("east_un", EnumFacing.EAST, PartSlot.EAST_UN),
    EAST_US("east_us", EnumFacing.EAST, PartSlot.EAST_US);

    public static final NodeOrientation[] VALUES = NodeOrientation.values();

    private final String name;
    private final EnumFacing mainDirection;
    private final PartSlot slot;

    NodeOrientation(String name, EnumFacing mainDirection, PartSlot slot) {
        this.name = name;
        this.mainDirection = mainDirection;
        this.slot = slot;
    }

    @Override
    public String getName() {
        return name;
    }

    public EnumFacing getMainDirection() {
        return mainDirection;
    }

    public PartSlot getSlot() {
        return slot;
    }
}
