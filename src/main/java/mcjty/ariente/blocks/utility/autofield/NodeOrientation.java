package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.PartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum NodeOrientation implements IStringSerializable {
    DOWN_NE("down_ne", EnumFacing.DOWN, PartSlot.DOWN_NE, PartSlot.DOWN_NE_BACK),
    DOWN_NW("down_nw", EnumFacing.DOWN, PartSlot.DOWN_NW, PartSlot.DOWN_NW_BACK),
    DOWN_SE("down_se", EnumFacing.DOWN, PartSlot.DOWN_SE, PartSlot.DOWN_SE_BACK),
    DOWN_SW("down_sw", EnumFacing.DOWN, PartSlot.DOWN_SW, PartSlot.DOWN_SW_BACK),
    UP_NE("up_ne", EnumFacing.UP, PartSlot.UP_NE, PartSlot.UP_NE_BACK),
    UP_NW("up_nw", EnumFacing.UP, PartSlot.UP_NW, PartSlot.UP_NW_BACK),
    UP_SE("up_se", EnumFacing.UP, PartSlot.UP_SE, PartSlot.UP_SE_BACK),
    UP_SW("up_sw", EnumFacing.UP, PartSlot.UP_SW, PartSlot.UP_SW_BACK),
    NORTH_DE("north_de", EnumFacing.NORTH, PartSlot.NORTH_DE, PartSlot.NORTH_DE_BACK),
    NORTH_DW("north_dw", EnumFacing.NORTH, PartSlot.NORTH_DW, PartSlot.NORTH_DW_BACK),
    NORTH_UE("north_ue", EnumFacing.NORTH, PartSlot.NORTH_UE, PartSlot.NORTH_UE_BACK),
    NORTH_UW("north_uw", EnumFacing.NORTH, PartSlot.NORTH_UW, PartSlot.NORTH_UW_BACK),
    SOUTH_DE("south_de", EnumFacing.SOUTH, PartSlot.SOUTH_DE, PartSlot.SOUTH_DE_BACK),
    SOUTH_DW("south_dw", EnumFacing.SOUTH, PartSlot.SOUTH_DW, PartSlot.SOUTH_DW_BACK),
    SOUTH_UE("south_ue", EnumFacing.SOUTH, PartSlot.SOUTH_UE, PartSlot.SOUTH_UE_BACK),
    SOUTH_UW("south_uw", EnumFacing.SOUTH, PartSlot.SOUTH_UW, PartSlot.SOUTH_UW_BACK),
    WEST_DN("west_dn", EnumFacing.WEST, PartSlot.WEST_DN, PartSlot.WEST_DN_BACK),
    WEST_DS("west_ds", EnumFacing.WEST, PartSlot.WEST_DS, PartSlot.WEST_DS_BACK),
    WEST_UN("west_un", EnumFacing.WEST, PartSlot.WEST_UN, PartSlot.WEST_UN_BACK),
    WEST_US("west_us", EnumFacing.WEST, PartSlot.WEST_US, PartSlot.WEST_US_BACK),
    EAST_DN("east_dn", EnumFacing.EAST, PartSlot.EAST_DN, PartSlot.EAST_DN_BACK),
    EAST_DS("east_ds", EnumFacing.EAST, PartSlot.EAST_DS, PartSlot.EAST_DS_BACK),
    EAST_UN("east_un", EnumFacing.EAST, PartSlot.EAST_UN, PartSlot.EAST_UN_BACK),
    EAST_US("east_us", EnumFacing.EAST, PartSlot.EAST_US, PartSlot.EAST_US_BACK);

    public static final NodeOrientation[] VALUES = NodeOrientation.values();

    private final String name;
    private final EnumFacing mainDirection;
    private final PartSlot slot;
    private final PartSlot backSlot;

    NodeOrientation(String name, EnumFacing mainDirection, PartSlot slot, PartSlot backSlot) {
        this.name = name;
        this.mainDirection = mainDirection;
        this.slot = slot;
        this.backSlot = backSlot;
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

    public PartSlot getBackSlot() {
        return backSlot;
    }
}
