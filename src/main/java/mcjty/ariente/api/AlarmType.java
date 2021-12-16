package mcjty.ariente.api;

import net.minecraft.util.StringRepresentable;

public enum AlarmType implements StringRepresentable {
    DEAD("dead"),
    SAFE("safe"),
    ALERT("alert");

    private final String name;

    AlarmType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
