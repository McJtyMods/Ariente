package mcjty.ariente.loot;

import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintRarity;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Standalone exploration needs a usable library without relying on city-specific reward tables. */
public final class StandaloneBlueprintLoot {

    private StandaloneBlueprintLoot() {
    }

    public static void addBlueprints(List<ItemStack> loot, RandomSource random, int count) {
        List<ConstructorRecipe> recipes = BlueprintRecipeRegistry.getRecipes();
        List<ConstructorRecipe> starters = recipes.stream().filter(StandaloneBlueprintLoot::isStarter).toList();
        Map<BlueprintRarity, List<ConstructorRecipe>> tiers = new EnumMap<>(BlueprintRarity.class);
        for (ConstructorRecipe recipe : recipes) {
            BlueprintRarity rarity = BlueprintRarity.of(recipe);
            if (rarity != null) {
                tiers.computeIfAbsent(rarity, key -> new ArrayList<>()).add(recipe);
            }
        }
        for (int index = 0; index < count; index++) {
            ConstructorRecipe recipe;
            if (index == 0 && !starters.isEmpty()) {
                recipe = starters.get(random.nextInt(starters.size()));
            } else {
                int roll = random.nextInt(100);
                // Select a tier first, then a uniform recipe: 65% Common, 20% Uncommon, 13% Rare, 2% Extreme.
                BlueprintRarity rarity = roll < 65 ? BlueprintRarity.COMMON : roll < 85 ? BlueprintRarity.UNCOMMON
                        : roll < 98 ? BlueprintRarity.RARE : BlueprintRarity.EXTREME;
                List<ConstructorRecipe> choices = tiers.get(rarity);
                recipe = choices == null ? BlueprintRecipeRegistry.getRandomRecipe(random)
                        : choices.get(random.nextInt(choices.size()));
            }
            loot.add(BlueprintItem.makeBluePrint(recipe.getDestination()));
        }
    }

    private static boolean isStarter(ConstructorRecipe recipe) {
        ItemStack result = recipe.getDestination();
        return result.is(Registration.CIRCUIT.get()) || result.is(Registration.ENERGY_HOLDER.get())
                || result.is(Registration.NEGARITE_GENERATOR.get().asItem()) || result.is(Registration.POSIRITE_GENERATOR.get().asItem())
                || result.is(Registration.SMELTER.get().asItem()) || result.is(Registration.AUTO_CONSTRUCTOR.get().asItem())
                || result.is(Registration.RECYCLER.get().asItem());
    }
}
