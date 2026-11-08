package mcjty.ariente.items;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Copy-on-write access to the mod's custom item component. */
public final class ItemData {

    private ItemData() {
    }

    public static CompoundTag get(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static boolean has(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    public static void set(ItemStack stack, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> change) {
        CompoundTag tag = get(stack);
        change.accept(tag);
        set(stack, tag);
    }

    public static CompoundTag section(ItemStack stack, String key) {
        return get(stack).getCompound(key);
    }

    public static void setSection(ItemStack stack, String key, CompoundTag value) {
        update(stack, tag -> tag.put(key, value));
    }
}
