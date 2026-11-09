package mcjty.ariente.blocks.utility.autofield;

import net.minecraft.world.item.crafting.RecipePropertySet;

import java.util.Locale;
import mcjty.ariente.recipes.PurifierRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Recipe-aware destinations; the optional purifier path precedes direct smelting. */
public enum ItemFilterCategory {
    ALL("All items"),
    SMELTABLE("Smeltable"),
    WOOD("Wood (non-smeltable)"),
    INGOTS("Ingots (non-smeltable)"),
    OTHER("Other (non-smeltable)"),
    PURIFIABLE("Purifiable ores"),
    DIRECT_SMELTABLE("Smeltable (skip purifiable ores)");

    private final String title;

    ItemFilterCategory(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ItemFilterCategory fromId(String id) {
        for (ItemFilterCategory category : values()) {
            if (category.id().equals(id)) {
                return category;
            }
        }
        return ALL;
    }

    public boolean matches(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (this == ALL) {
            return true;
        }
        if (this == PURIFIABLE) {
            return PurifierRecipes.find(level, stack) != null;
        }
        boolean smeltable = level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT).test(stack);
        if (this == DIRECT_SMELTABLE) {
            return smeltable && PurifierRecipes.find(level, stack) == null;
        }
        if (this == SMELTABLE || smeltable) {
            return this == SMELTABLE && smeltable;
        }
        boolean wood = stack.typeHolder().tags().anyMatch(tag -> {
            String namespace = tag.location().getNamespace();
            String path = tag.location().getPath();
            return namespace.equals("minecraft") && (path.equals("logs") || path.equals("planks") || path.startsWith("wooden_"))
                    || namespace.equals("c") && (path.equals("rods/wooden") || path.equals("chests/wooden") || path.equals("fences/wooden"));
        });
        boolean ingot = stack.typeHolder().tags().anyMatch(tag -> tag.location().getNamespace().equals("c")
                && (tag.location().getPath().equals("ingots") || tag.location().getPath().startsWith("ingots/")));
        return switch (this) {
            case WOOD -> wood;
            case INGOTS -> !wood && ingot;
            case OTHER -> !wood && !ingot;
            default -> false;
        };
    }
}
