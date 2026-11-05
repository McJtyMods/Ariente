package mcjty.ariente.recipes;

import mcjty.ariente.Ariente;
import mcjty.ariente.config.PowerConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Discovers ore/raw pairs from shared tags. Explicit datapack recipes take precedence. */
@Mod.EventBusSubscriber(modid = Ariente.MODID)
public class PurifierRecipes {

    private static volatile Snapshot snapshot;

    @SubscribeEvent
    public static synchronized void tagsUpdated(TagsUpdatedEvent event) {
        snapshot = null;
    }

    @Nullable
    public static PurifyingRecipe find(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        PurifyingRecipe explicit = level.getRecipeManager().getRecipeFor(BlueprintRecipeRegistry.PURIFYING.get(),
                new SimpleContainer(stack), level).orElse(null);
        return explicit != null ? explicit : getSnapshot().byItem().get(stack.getItem());
    }

    /** Includes the same automatic conversions as the machine, excluding inputs overridden by JSON. */
    public static List<PurifyingRecipe> getRecipes(Level level) {
        List<PurifyingRecipe> explicit = level.getRecipeManager().getAllRecipesFor(BlueprintRecipeRegistry.PURIFYING.get());
        List<PurifyingRecipe> recipes = new ArrayList<>(explicit);
        for (PurifyingRecipe automatic : getSnapshot().recipes()) {
            List<ItemStack> inputs = Arrays.stream(automatic.ingredient().getItems())
                    .filter(stack -> explicit.stream().noneMatch(recipe -> recipe.ingredient().test(stack))).toList();
            if (!inputs.isEmpty()) {
                recipes.add(new PurifyingRecipe(automatic.id(), Ingredient.of(inputs.stream()), automatic.result().copy()));
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
        Set<ResourceLocation> oreTags = new TreeSet<>();
        ForgeRegistries.ITEMS.tags().getTagNames().map(TagKey::location).filter(PurifierRecipes::isOreTag).forEach(oreTags::add);
        ForgeRegistries.BLOCKS.tags().getTagNames().map(TagKey::location).filter(PurifierRecipes::isOreTag).forEach(oreTags::add);
        Comparator<Item> outputOrder = Comparator.<Item>comparingInt(item -> {
            int index = preferred.indexOf(ForgeRegistries.ITEMS.getKey(item).getNamespace());
            return index < 0 ? preferred.size() : index;
        }).thenComparing(item -> ForgeRegistries.ITEMS.getKey(item).toString());
        Map<Item, Item> outputs = new HashMap<>();
        Set<Item> ambiguous = new HashSet<>();
        List<PurifyingRecipe> candidates = new ArrayList<>();
        for (ResourceLocation oreTag : oreTags) {
            String material = oreTag.getPath().substring("ores/".length());
            TagKey<Item> rawTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation("forge", "raw_materials/" + material));
            Item raw = ForgeRegistries.ITEMS.tags().getTag(rawTag).stream().min(outputOrder).orElse(null);
            if (raw == null) {
                continue;
            }
            Set<Item> ores = new TreeSet<>(Comparator.comparing(item -> ForgeRegistries.ITEMS.getKey(item).toString()));
            TagKey<Item> itemTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), oreTag);
            ForgeRegistries.ITEMS.tags().getTag(itemTag).stream().filter(item -> item instanceof BlockItem).forEach(ores::add);
            TagKey<Block> blockTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), oreTag);
            ForgeRegistries.BLOCKS.tags().getTag(blockTag).stream().map(Block::asItem)
                    .filter(item -> item instanceof BlockItem).forEach(ores::add);
            for (Item ore : ores) {
                Item previous = outputs.putIfAbsent(ore, raw);
                if (previous != null && previous != raw) {
                    ambiguous.add(ore);
                }
            }
            if (!ores.isEmpty()) {
                candidates.add(new PurifyingRecipe(new ResourceLocation(Ariente.MODID, "automatic_purifying/" + material),
                        Ingredient.of(ores.stream().map(ItemStack::new)), new ItemStack(raw)));
            }
        }
        Map<Item, PurifyingRecipe> byItem = new HashMap<>();
        List<PurifyingRecipe> recipes = new ArrayList<>();
        for (PurifyingRecipe candidate : candidates) {
            List<ItemStack> inputs = Arrays.stream(candidate.ingredient().getItems())
                    .filter(stack -> !ambiguous.contains(stack.getItem()) && !byItem.containsKey(stack.getItem())).toList();
            if (!inputs.isEmpty()) {
                PurifyingRecipe recipe = new PurifyingRecipe(candidate.id(), Ingredient.of(inputs.stream()), candidate.result());
                inputs.forEach(stack -> byItem.put(stack.getItem(), recipe));
                recipes.add(recipe);
            }
        }
        return new Snapshot(preferred, Map.copyOf(byItem), List.copyOf(recipes));
    }

    private static boolean isOreTag(ResourceLocation tag) {
        return tag.getNamespace().equals("forge") && tag.getPath().startsWith("ores/");
    }

    private record Snapshot(List<String> preferred, Map<Item, PurifyingRecipe> byItem, List<PurifyingRecipe> recipes) {
    }
}
