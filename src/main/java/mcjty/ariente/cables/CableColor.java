package mcjty.ariente.cables;

import net.minecraft.util.IStringSerializable;

public enum CableColor implements IStringSerializable {
    NEGARITE("negarite", "dyeBlue"),
    POSIRITE("posirite", "dyeRed"),
    COMBINED("combined", "dyeYellow"),
    DATA("data", "dyeGreen");

    public static final CableColor[] VALUES = CableColor.values();

    private final String name;
    private final String dye;

    CableColor(String name, String dye) {
        this.name = name;
        this.dye = dye;
    }

    @Override
    public String getString() {
        return name;
    }

    public String getDye() {
        return dye;
    }
}
