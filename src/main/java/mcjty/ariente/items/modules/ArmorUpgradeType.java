package mcjty.ariente.items.modules;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum ArmorUpgradeType {
    ENERGY("energy", "Energy Efficiency Upgrade"),
    ARMOR("armor", "Armor Improvement Upgrade"),
    FEATHERFALLING("featherfalling", "Feather Falling Upgrade"),
    FLIGHT("flight", "Flight upgrade"),
    FORCEFIELD("forcefield", "Forcefield upgrade"),
    INVISIBILITY("invisibility", "Invisibility upgrade"),
    NIGHTVISION("nightvision", "Nightvision upgrade"),
    REGENERATION("regeneration", "Regeneration upgrade"),
    SPEED("speed", "Speed upgrade"),
    SCRAMBLE("scramble", "Scramble upgrade");

    private final String name;
    private final String description;

    private static Map<String, ArmorUpgradeType> MAP = new HashMap<>();

    static {
        for (ArmorUpgradeType type : ArmorUpgradeType.values()) {
            MAP.put(type.getName(), type);
        }
    }

    ArmorUpgradeType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Nullable
    public static ArmorUpgradeType getByName(String name) {
        return MAP.get(name);
    }
}
