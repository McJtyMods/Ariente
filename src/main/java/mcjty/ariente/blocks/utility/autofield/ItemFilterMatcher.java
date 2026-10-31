package mcjty.ariente.blocks.utility.autofield;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemFilterMatcher {

    private static final Set<String> VANILLA_MATERIALS = Set.of("planks", "logs", "logs_that_burn", "wool", "sand", "coals", "leaves", "saplings", "stone_tool_materials");
    private static final Set<String> FORGE_MATERIALS = Set.of("ingots", "nuggets", "dusts", "gems", "ores", "raw_materials", "storage_blocks", "plates", "rods", "wires");

    public static boolean matches(ItemStack filter, ItemStack stack, boolean tags, boolean damage, boolean nbt) {
        if (filter.isEmpty() || stack.isEmpty()) {
            return false;
        }
        if (!ItemStack.isSameItem(filter, stack) && (!tags || materialTags(filter).stream().noneMatch(materialTags(stack)::contains))) {
            return false;
        }
        if (damage && filter.getDamageValue() != stack.getDamageValue()) {
            return false;
        }
        return !nbt || Objects.equals(withoutDamage(filter), withoutDamage(stack));
    }

    private static CompoundTag withoutDamage(ItemStack stack) {
        CompoundTag tag = stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        tag.remove("Damage");
        return tag;
    }

    private static Set<ResourceLocation> materialTags(ItemStack stack) {
        // Broad groups such as forge:ingots and ores_in_ground/stone are not material equivalence.
        return stack.getTags().map(tag -> tag.location()).filter(id -> switch (id.getNamespace()) {
            case "minecraft" -> VANILLA_MATERIALS.contains(id.getPath());
            case "forge" -> id.getPath().contains("/") && FORGE_MATERIALS.contains(id.getPath().substring(0, id.getPath().indexOf('/')));
            default -> true;
        }).collect(Collectors.toSet());
    }
}
