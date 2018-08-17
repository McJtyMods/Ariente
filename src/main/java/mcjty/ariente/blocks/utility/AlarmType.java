package mcjty.ariente.blocks.utility;

import net.minecraft.util.IStringSerializable;

public enum AlarmType implements IStringSerializable {
    DEAD("dead"),
    SAFE("safe"),
    ALERT("alert");

    private final String name;

    AlarmType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
