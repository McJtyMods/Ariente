package mcjty.ariente.cables;

import net.minecraft.util.StringRepresentable;

public enum CableColor implements StringRepresentable {
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
    public String getSerializedName() {
        return name;
    }

    public String getDye() {
        return dye;
    }
}
