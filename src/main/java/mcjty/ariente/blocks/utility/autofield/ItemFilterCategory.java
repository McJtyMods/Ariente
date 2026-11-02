package mcjty.ariente.blocks.utility.autofield;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Locale;

/** Disjoint processing destinations: furnace recipes take precedence over material groups. */
public enum ItemFilterCategory {
    ALL("All items"),
    SMELTABLE("Smeltable"),
    WOOD("Wood (non-smeltable)"),
    INGOTS("Ingots (non-smeltable)"),
    OTHER("Other (non-smeltable)");

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
        boolean smeltable = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), level).isPresent();
        if (this == SMELTABLE || smeltable) {
            return this == SMELTABLE && smeltable;
        }
        boolean wood = stack.getTags().anyMatch(tag -> {
            String namespace = tag.location().getNamespace();
            String path = tag.location().getPath();
            return namespace.equals("minecraft") && (path.equals("logs") || path.equals("planks") || path.startsWith("wooden_"))
                    || namespace.equals("forge") && (path.equals("rods/wooden") || path.equals("chests/wooden") || path.equals("fences/wooden"));
        });
        boolean ingot = stack.getTags().anyMatch(tag -> tag.location().getNamespace().equals("forge")
                && (tag.location().getPath().equals("ingots") || tag.location().getPath().startsWith("ingots/")));
        return switch (this) {
            case WOOD -> wood;
            case INGOTS -> !wood && ingot;
            case OTHER -> !wood && !ingot;
            default -> false;
        };
    }
}
