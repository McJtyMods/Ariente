package mcjty.ariente.api;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public enum ArmorUpgradeType {
    ENERGY("energy", "Energy Efficiency Upgrade", -1),
    ARMOR("armor", "Armor Improvement Upgrade", 20),
    FEATHERFALLING("featherfalling", "Feather Falling Upgrade", 20),
    FLIGHT("flight", "Flight upgrade", 40),
    FORCEFIELD("forcefield", "Forcefield upgrade", 80),
    INVISIBILITY("invisibility", "Invisibility upgrade (Not Implemented Yet)", 20),
    NIGHTVISION("nightvision", "Nightvision upgrade", 20),
    REGENERATION("regeneration", "Regeneration upgrade", 40),
    SPEED("speed", "Speed upgrade", 20),
    SCRAMBLE("scramble", "Scramble upgrade", 50),
    AUTOFEED("autofeed", "Autofeed power upgrade", 0),
    STEPASSIST("stepassist", "Step Assist power upgrade", 10),
    INHIBIT("inhibit", "Inhibit sabre upgrade", 30),
    POWER("power", "Power sabre upgrade", 30),
    LOOTING("looting", "Looting sabre upgrade", 20),
    FIRE("fire", "Fire damage sabre upgrade", 30),
    ;

    private final String name;
    private final String description;
    private final int powerUsage;

    private static Map<String, ArmorUpgradeType> MAP = new HashMap<>();

    public static final ArmorUpgradeType[] VALUES = new ArmorUpgradeType[ArmorUpgradeType.values().length];

    static {
        int i = 0;
        for (ArmorUpgradeType type : ArmorUpgradeType.values()) {
            VALUES[i++] = type;
        }
    }


    static {
        for (ArmorUpgradeType type : ArmorUpgradeType.values()) {
            MAP.put(type.getName(), type);
        }
    }

    ArmorUpgradeType(String name, String description, int powerUsage) {
        this.name = name;
        this.description = description;
        this.powerUsage = powerUsage;
    }

    public String getModuleKey() {
        return "module_" + getName();
    }

    public String getWorkingKey() {
        return "working_" + getName();
    }

    public String getHotkeyKey() {
        return "key_" + getName();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPowerUsage() {
        return powerUsage;
    }

    @Nullable
    public static ArmorUpgradeType getByName(String name) {
        return MAP.get(name);
    }
}
