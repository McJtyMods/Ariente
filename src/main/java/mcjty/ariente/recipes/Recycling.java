package mcjty.ariente.recipes;

import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.ariente.blocks.ExtremeMachineTile;
import mcjty.ariente.setup.Registration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Recycling {

    private static final Map<Item, ConstructorRecipe> MODULE_RECIPES = new ConcurrentHashMap<>();

    @Nullable
    public static ConstructorRecipe findRecipe(ItemStack stack) {
        if (stack.is(Registration.EXTREME_CIRCUIT.get())) {
            return null;
        }
        if (!stack.isEmpty()) {
            for (ConstructorRecipe recipe : BlueprintRecipeRegistry.getRecipes()) {
                // Charge, damage and installed modules do not require a separate blueprint.
                if (ItemStack.isSameItem(recipe.getDestination(), stack)) {
                    return recipe;
                }
            }
            if (stack.getItem() instanceof ArmorModuleItem) {
                return MODULE_RECIPES.computeIfAbsent(stack.getItem(), Recycling::moduleRecipe);
            }
        }
        return null;
    }

    private static ConstructorRecipe moduleRecipe(Item module) {
        // Recycling-only estimate: never register these in the Constructor or blueprint catalogs.
        return new ConstructorRecipe(new ItemStack(module), 0,
                new ItemStack(Registration.CIRCUIT.get()),
                new ItemStack(Registration.INGOT_SILVER.get(), 2),
                new ItemStack(Registration.DUST_SILICON.get()));
    }

    public static List<ItemStack> recover(ConstructorRecipe recipe, ItemStack input) {
        List<ItemStack> result = recover(recipe);
        if (ExtremeMachineTile.hasExtremeCircuit(input)) {
            result.add(new ItemStack(Registration.EXTREME_CIRCUIT.get()));
        }
        return result;
    }

    /** Recover floor(75% of a recipe's ingredients), with a guaranteed material deficit. */
    public static List<ItemStack> recover(ConstructorRecipe recipe) {
        List<ItemStack> ingredients = new ArrayList<>();
        for (ItemStack ingredient : recipe.getIngredientList()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            ItemStack existing = ingredients.stream()
                    .filter(stack -> ItemHandlerHelper.canItemStacksStack(stack, ingredient)).findFirst().orElse(null);
            if (existing == null) {
                ingredients.add(ingredient.copy());
            } else {
                existing.grow(ingredient.getCount());
            }
        }
        int total = ingredients.stream().mapToInt(ItemStack::getCount).sum();
        int[] recovered = new int[ingredients.size()];
        List<Integer> order = new ArrayList<>();
        int assigned = 0;
        for (int index = 0; index < ingredients.size(); index++) {
            recovered[index] = ingredients.get(index).getCount() * 3 / 4;
            assigned += recovered[index];
            order.add(index);
        }
        // Largest remainders keep small components useful without rounding the total up.
        order.sort(Comparator.comparingInt((Integer index) -> ingredients.get(index).getCount() * 3 % 4).reversed());
        int extras = total * 3 / 4 - assigned;
        for (int index = 0; index < extras; index++) {
            recovered[order.get(index)]++;
        }
        List<ItemStack> result = new ArrayList<>();
        for (int index = 0; index < ingredients.size(); index++) {
            int remaining = recovered[index];
            while (remaining > 0) {
                ItemStack stack = ingredients.get(index).copy();
                stack.setCount(Math.min(remaining, stack.getMaxStackSize()));
                result.add(stack);
                remaining -= stack.getCount();
            }
        }
        return result;
    }
}
