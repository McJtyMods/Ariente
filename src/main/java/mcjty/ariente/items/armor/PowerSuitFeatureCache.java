package mcjty.ariente.items.armor;

import mcjty.ariente.items.modules.ArmorUpgradeType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

public class PowerSuitFeatureCache {

    private static Map<Integer, PowerSuitFeatures> cachedFeatures = new HashMap<>();

    public static void cleanCache(int id) {
        cachedFeatures.remove(id);
    }

    // @todo is it worth keeping this cache?
    public static void checkCacheClean(int id, EntityEquipmentSlot slot, ItemStack from, ItemStack to) {
        if (!cachedFeatures.containsKey(id)) {
            return; // Nothing to do
        }
        if (to.isEmpty() || !(to.getItem() instanceof PowerSuit)) {
            // Removing is always ok in this case
            cachedFeatures.remove(id);
            return;
        }
        if (from.isEmpty() || !(from.getItem() instanceof PowerSuit)) {
            // Removing is ok since there is a change anyway. This case can probably not happen
            cachedFeatures.remove(id);
            return;
        }
        // Both items are powersuit items. Check a few essentials
        if (from.hasTagCompound() && !to.hasTagCompound()) {
            cachedFeatures.remove(id);
            return;
        }
        if (!from.hasTagCompound()) {
            cachedFeatures.remove(id);
            return;
        }
        NBTTagCompound compoundFrom = from.getTagCompound();
        NBTTagCompound compoundTo = to.getTagCompound();
        if (compoundFrom.getSize() != compoundTo.getSize()) {
            cachedFeatures.remove(id);
            return;
        }
        for (ArmorUpgradeType type : ArmorUpgradeType.values()) {
            if (compoundFrom.getBoolean(type.getModuleKey()) != compoundTo.getBoolean(type.getModuleKey())) {
                cachedFeatures.remove(id);
                return;
            }
        }
        // All ok
    }
}
