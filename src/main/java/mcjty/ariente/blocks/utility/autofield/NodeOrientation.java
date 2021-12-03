package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.PartSlot;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

public enum NodeOrientation implements IStringSerializable {
    DOWN_NE("down_ne", Direction.DOWN, PartSlot.DOWN_NE, PartSlot.DOWN_NE_BACK, "br"),
    DOWN_NW("down_nw", Direction.DOWN, PartSlot.DOWN_NW, PartSlot.DOWN_NW_BACK, "bl"),
    DOWN_SE("down_se", Direction.DOWN, PartSlot.DOWN_SE, PartSlot.DOWN_SE_BACK, "tr"),
    DOWN_SW("down_sw", Direction.DOWN, PartSlot.DOWN_SW, PartSlot.DOWN_SW_BACK, "tl"),
    UP_NE("up_ne", Direction.UP, PartSlot.UP_NE, PartSlot.UP_NE_BACK, "tr"),
    UP_NW("up_nw", Direction.UP, PartSlot.UP_NW, PartSlot.UP_NW_BACK, "tl"),
    UP_SE("up_se", Direction.UP, PartSlot.UP_SE, PartSlot.UP_SE_BACK, "br"),
    UP_SW("up_sw", Direction.UP, PartSlot.UP_SW, PartSlot.UP_SW_BACK, "bl"),
    NORTH_DE("north_de", Direction.NORTH, PartSlot.NORTH_DE, PartSlot.NORTH_DE_BACK, "bl"),
    NORTH_DW("north_dw", Direction.NORTH, PartSlot.NORTH_DW, PartSlot.NORTH_DW_BACK, "br"),
    NORTH_UE("north_ue", Direction.NORTH, PartSlot.NORTH_UE, PartSlot.NORTH_UE_BACK, "tl"),
    NORTH_UW("north_uw", Direction.NORTH, PartSlot.NORTH_UW, PartSlot.NORTH_UW_BACK, "tr"),
    SOUTH_DE("south_de", Direction.SOUTH, PartSlot.SOUTH_DE, PartSlot.SOUTH_DE_BACK, "br"),
    SOUTH_DW("south_dw", Direction.SOUTH, PartSlot.SOUTH_DW, PartSlot.SOUTH_DW_BACK, "bl"),
    SOUTH_UE("south_ue", Direction.SOUTH, PartSlot.SOUTH_UE, PartSlot.SOUTH_UE_BACK, "tr"),
    SOUTH_UW("south_uw", Direction.SOUTH, PartSlot.SOUTH_UW, PartSlot.SOUTH_UW_BACK, "tl"),
    WEST_DN("west_dn", Direction.WEST, PartSlot.WEST_DN, PartSlot.WEST_DN_BACK, "bl"),
    WEST_DS("west_ds", Direction.WEST, PartSlot.WEST_DS, PartSlot.WEST_DS_BACK, "br"),
    WEST_UN("west_un", Direction.WEST, PartSlot.WEST_UN, PartSlot.WEST_UN_BACK, "tl"),
    WEST_US("west_us", Direction.WEST, PartSlot.WEST_US, PartSlot.WEST_US_BACK, "tr"),
    EAST_DN("east_dn", Direction.EAST, PartSlot.EAST_DN, PartSlot.EAST_DN_BACK, "br"),
    EAST_DS("east_ds", Direction.EAST, PartSlot.EAST_DS, PartSlot.EAST_DS_BACK, "bl"),
    EAST_UN("east_un", Direction.EAST, PartSlot.EAST_UN, PartSlot.EAST_UN_BACK, "tr"),
    EAST_US("east_us", Direction.EAST, PartSlot.EAST_US, PartSlot.EAST_US_BACK, "tl");

    public static final NodeOrientation[] VALUES = NodeOrientation.values();

    private final String name;
    private final Direction mainDirection;
    private final PartSlot slot;
    private final PartSlot backSlot;
    private final String modelSuffix;

    NodeOrientation(String name, Direction mainDirection, PartSlot slot, PartSlot backSlot, String modelSuffix) {
        this.name = name;
        this.mainDirection = mainDirection;
        this.slot = slot;
        this.backSlot = backSlot;
        this.modelSuffix = modelSuffix;
    }

    @Override
    public String getString() {
        return name;
    }

    public Direction getMainDirection() {
        return mainDirection;
    }

    public PartSlot getSlot() {
        return slot;
    }

    public PartSlot getBackSlot() {
        return backSlot;
    }

    public String getModelSuffix() {
        return modelSuffix;
    }

    @Override
    public String toString() {
        return name;
    }
}
