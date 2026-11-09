package mcjty.ariente.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import net.minecraft.server.level.ServerLevel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.config.PowerConfiguration;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Discovers ore/raw pairs from shared tags. Explicit datapack recipes take precedence. */
public class PurifierRecipes {

    private static volatile Snapshot snapshot;
    private static volatile List<PurifyingRecipe> clientRecipes = List.of();

    public static void receivedRecipes(List<PurifyingRecipe> recipes) {
        clientRecipes = List.copyOf(recipes);
    }

    public static synchronized void tagsUpdated() {
        snapshot = null;
    }

    @Nullable
    public static PurifyingRecipe find(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        PurifyingRecipe explicit = ((ServerLevel) level).recipeAccess().getRecipeFor(BlueprintRecipeRegistry.PURIFYING.get(),
                new SingleRecipeInput(stack), level, (ResourceKey<Recipe<?>>) null).map(RecipeHolder::value).orElse(null);
        return explicit != null ? explicit : getSnapshot().byItem().get(stack.getItem());
    }

    /** Includes the same automatic conversions as the machine, excluding inputs overridden by JSON. */

    public static List<PurifyingRecipe> getRecipes(Level level) {
        List<PurifyingRecipe> explicit = level instanceof ServerLevel server
                ? server.recipeAccess().getRecipes().stream().map(RecipeHolder::value).filter(recipe -> recipe instanceof PurifyingRecipe).map(recipe -> (PurifyingRecipe) recipe).toList()
                : clientRecipes;
        List<PurifyingRecipe> recipes = new ArrayList<>(explicit);
        for (PurifyingRecipe automatic : getSnapshot().recipes()) {
            List<ItemStack> inputs = automatic.ingredient().items().map(holder -> new ItemStack(holder.value()))
                    .filter(stack -> explicit.stream().noneMatch(recipe -> recipe.ingredient().test(stack))).toList();
            if (!inputs.isEmpty()) {
                recipes.add(new PurifyingRecipe(automatic.id(), Ingredient.of(inputs.stream().map(ItemStack::getItem)), automatic.result().copy()));
            }
        }
        return List.copyOf(recipes);
    }

    private static Snapshot getSnapshot() {
        List<? extends String> preferred = PowerConfiguration.PURIFIER_PREFERRED_OUTPUT_MODS.get();
        Snapshot current = snapshot;
        if (current != null && current.preferred().equals(preferred)) {
            return current;
        }

        synchronized (PurifierRecipes.class) {
            current = snapshot;
            if (current == null || !current.preferred().equals(preferred)) {
                current = discover(List.copyOf(preferred));
                snapshot = current;
            }
        }
        return current;
    }

    private static Snapshot discover(List<String> preferred) {
        Set<Identifier> oreTags = new TreeSet<>();
        BuiltInRegistries.ITEM.listTagIds().map(TagKey::location).filter(PurifierRecipes::isOreTag).forEach(oreTags::add);
        BuiltInRegistries.BLOCK.listTagIds().map(TagKey::location).filter(PurifierRecipes::isOreTag).forEach(oreTags::add);
        Comparator<Item> outputOrder = Comparator.<Item>comparingInt(item -> {
            int index = preferred.indexOf(BuiltInRegistries.ITEM.getKey(item).getNamespace());
            return index < 0 ? preferred.size() : index;
        }).thenComparing(item -> BuiltInRegistries.ITEM.getKey(item).toString());
        Map<Item, Item> outputs = new HashMap<>();
        Set<Item> ambiguous = new HashSet<>();
        List<PurifyingRecipe> candidates = new ArrayList<>();
        for (Identifier oreTag : oreTags) {
            String material = oreTag.getPath().substring("ores/".length());
            TagKey<Item> rawTag = TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath("c", "raw_materials/" + material));
            Item raw = BuiltInRegistries.ITEM.get(rawTag).stream().flatMap(set -> set.stream()).map(Holder::value).min(outputOrder).orElse(null);
            if (raw == null) {
                continue;
            }
            Set<Item> ores = new TreeSet<>(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
            TagKey<Item> itemTag = TagKey.create(BuiltInRegistries.ITEM.key(), oreTag);
            BuiltInRegistries.ITEM.get(itemTag).stream().flatMap(set -> set.stream()).map(Holder::value).filter(item -> item instanceof BlockItem).forEach(ores::add);
            TagKey<Block> blockTag = TagKey.create(BuiltInRegistries.BLOCK.key(), oreTag);
            BuiltInRegistries.BLOCK.get(blockTag).stream().flatMap(set -> set.stream()).map(Holder::value).map(Block::asItem)
                    .filter(item -> item instanceof BlockItem).forEach(ores::add);
            for (Item ore : ores) {
                Item previous = outputs.putIfAbsent(ore, raw);
                if (previous != null && previous != raw) {
                    ambiguous.add(ore);
                }
            }
            if (!ores.isEmpty()) {
                candidates.add(new PurifyingRecipe(Identifier.fromNamespaceAndPath(Ariente.MODID, "automatic_purifying/" + material),
                        Ingredient.of(ores.stream()), new ItemStack(raw)));
            }
        }
        Map<Item, PurifyingRecipe> byItem = new HashMap<>();
        List<PurifyingRecipe> recipes = new ArrayList<>();
        for (PurifyingRecipe candidate : candidates) {
            List<ItemStack> inputs = candidate.ingredient().items().map(holder -> new ItemStack(holder.value()))
                    .filter(stack -> !ambiguous.contains(stack.getItem()) && !byItem.containsKey(stack.getItem())).toList();
            if (!inputs.isEmpty()) {
                PurifyingRecipe recipe = new PurifyingRecipe(candidate.id(), Ingredient.of(inputs.stream().map(ItemStack::getItem)), candidate.result());
                inputs.forEach(stack -> byItem.put(stack.getItem(), recipe));
                recipes.add(recipe);
            }
        }
        return new Snapshot(preferred, Map.copyOf(byItem), List.copyOf(recipes));
    }

    private static boolean isOreTag(Identifier tag) {
        return tag.getNamespace().equals("c") && tag.getPath().startsWith("ores/");
    }

    private record Snapshot(List<String> preferred, Map<Item, PurifyingRecipe> byItem, List<PurifyingRecipe> recipes) {
    }
}
