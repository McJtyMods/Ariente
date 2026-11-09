package mcjty.ariente.items;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.nbt.ListTag;

import mcjty.lib.varia.FluidStack;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
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
        return get(stack).getCompoundOrEmpty(key);
    }

    public static void setSection(ItemStack stack, String key, CompoundTag value) {
        update(stack, tag -> tag.put(key, value));
    }

    public static Tag encode(HolderLookup.Provider lookup, ItemStack stack) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }

    public static ItemStack decode(HolderLookup.Provider lookup, Tag tag) {
        return ItemStack.OPTIONAL_CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
    }

    public static Tag encodeFluid(HolderLookup.Provider lookup, FluidStack stack) {
        return FluidStack.OPTIONAL_CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }

    public static FluidStack decodeFluid(HolderLookup.Provider lookup, Tag tag) {
        return FluidStack.OPTIONAL_CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(FluidStack.EMPTY);
    }

    public static void loadItems(CompoundTag tag, ItemStackList items, HolderLookup.Provider lookup) {
        for (Tag entry : tag.getListOrEmpty("Items")) {
            if (entry instanceof CompoundTag item) {
                int slot = item.getByteOr("Slot", (byte) 0) & 255;
                if (slot < items.size()) items.set(slot, decode(lookup, item));
            }
        }
    }

    public static CompoundTag saveItems(ItemStackList items, HolderLookup.Provider lookup) {
        CompoundTag result = new CompoundTag();
        ListTag list = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty()) {
                CompoundTag item = (CompoundTag) encode(lookup, items.get(slot));
                item.putByte("Slot", (byte) slot);
                list.add(item);
            }
        }
        result.put("Items", list);
        return result;
    }
}
