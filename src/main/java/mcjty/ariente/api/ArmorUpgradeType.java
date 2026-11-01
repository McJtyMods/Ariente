package mcjty.ariente.api;

import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.minecraft.world.entity.EquipmentSlot.*;

public enum ArmorUpgradeType {
    ENERGY("energy", "Energy Efficiency Upgrade", -1, HEAD, CHEST, LEGS, FEET, MAINHAND),
    ARMOR("armor", "Armor Improvement Upgrade", 20, HEAD, CHEST, LEGS, FEET),
    FEATHERFALLING("featherfalling", "Feather Falling Upgrade", 20, FEET),
    FLIGHT("flight", "Flight upgrade", 30, CHEST),
    FORCEFIELD("forcefield", "Forcefield upgrade", 80, CHEST),
    INVISIBILITY("invisibility", "Invisibility upgrade (Currently Unavailable)", 20, HEAD),
    NIGHTVISION("nightvision", "Nightvision upgrade", 20, HEAD),
    REGENERATION("regeneration", "Regeneration upgrade", 40, CHEST),
    SPEED("speed", "Speed upgrade", 20, LEGS),
    SCRAMBLE("scramble", "Scramble upgrade", 50, HEAD),
    AUTOFEED("autofeed", "Autofeed power upgrade", 0, HEAD, CHEST, LEGS, FEET, MAINHAND),
    STEPASSIST("stepassist", "Step Assist power upgrade", 10, FEET),
    INHIBIT("inhibit", "Inhibit weapon upgrade", 30, MAINHAND),
    POWER("power", "Power weapon upgrade", 30, MAINHAND),
    LOOTING("looting", "Looting weapon upgrade", 20, MAINHAND),
    FIRE("fire", "Fire damage weapon upgrade", 30, MAINHAND),
    HOVER("hover", "Hover upgrade", 10, CHEST),
    ;

    private final String name;
    private final String description;
    private final int powerUsage;
    private final Set<EquipmentSlot> equipmentSlots;

    private static final Map<String, ArmorUpgradeType> MAP = new HashMap<>();

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

    ArmorUpgradeType(String name, String description, int powerUsage, EquipmentSlot... equipmentSlots) {
        this.name = name;
        this.description = description;
        this.powerUsage = powerUsage;
        EnumSet<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);
        Collections.addAll(slots, equipmentSlots);
        this.equipmentSlots = Collections.unmodifiableSet(slots);
    }

    public boolean supportsSlot(EquipmentSlot slot) {
        return equipmentSlots.contains(slot);
    }

    public Set<EquipmentSlot> getEquipmentSlots() {
        return equipmentSlots;
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
