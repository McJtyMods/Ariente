package mcjty.ariente.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import mcjty.ariente.Ariente;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.items.ItemData;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

public class BlueprintExchangeRecipe extends ShapelessRecipe {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "blueprint_exchange");
    private static final String EXCHANGE = "BlueprintExchange";

    public BlueprintExchangeRecipe(CraftingBookCategory category) {
        super("", category, preview(), ingredients());
    }

    private static NonNullList<Ingredient> ingredients() {
        Ingredient blueprint = Ingredient.of(BlueprintRecipeRegistry.getRecipes().stream()
                .filter(recipe -> BlueprintRarity.of(recipe) != null)
                .map(recipe -> BlueprintItem.makeBluePrint(recipe.getDestination())));
        return NonNullList.of(Ingredient.EMPTY, blueprint, blueprint, blueprint);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BlueprintRecipeRegistry.BLUEPRINT_EXCHANGE.get();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return readInputs(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return createPreview(input);
    }

    private static ItemStack preview() {
        ItemStack stack = new ItemStack(Registration.BLUEPRINT.get());
        ItemData.setSection(stack, EXCHANGE, new CompoundTag());
        return stack;
    }

    static ItemStack createPreview(CraftingInput input) {
        Exchange exchange = readInputs(input);
        if (exchange == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = preview();
        CompoundTag data = ItemData.section(stack, EXCHANGE);
        // Identify the delivered copy on shift-click; this chooses no reward or rarity.
        data.putUUID("craft", UUID.randomUUID());
        data.putInt("tier", exchange.rarity().ordinal());
        ListTag excluded = new ListTag();
        exchange.excluded().forEach(id -> excluded.add(StringTag.valueOf(id.toString())));
        data.put("excluded", excluded);
        ItemData.setSection(stack, EXCHANGE, data);
        return stack;
    }

    public static boolean isPreview(ItemStack stack) {
        return stack.is(Registration.BLUEPRINT.get()) && previewData(stack) != null;
    }

    @Nullable
    static CompoundTag previewData(ItemStack stack) {
        return (ItemData.get(stack).contains(EXCHANGE) ? ItemData.section(stack, EXCHANGE) : null);
    }

    @Nullable
    public static BlueprintRarity averageRarity(ItemStack stack) {
        Exchange exchange = readPreview(stack);
        return exchange == null ? null : exchange.rarity();
    }

    /** Resolve a paid crafting result, never the result-slot preview. */
    public static boolean resolve(ItemStack stack, RandomSource random) {
        Exchange exchange = readPreview(stack);
        if (exchange == null) {
            return false;
        }
        Map<BlueprintRarity, List<ConstructorRecipe>> choices = new EnumMap<>(BlueprintRarity.class);
        for (ConstructorRecipe recipe : BlueprintRecipeRegistry.getRecipes()) {
            BlueprintRarity rarity = BlueprintRarity.of(recipe);
            if (rarity != null && !exchange.excluded().contains(recipe.getId())) {
                choices.computeIfAbsent(rarity, key -> new ArrayList<>()).add(recipe);
            }
        }
        if (choices.isEmpty()) {
            return false;
        }
        int roll = random.nextInt(10);
        int change = roll == 0 ? -1 : roll >= 8 ? 1 : 0;
        int tier = Math.max(0, Math.min(BlueprintRarity.values().length - 1, exchange.rarity().ordinal() + change));
        for (int distance = 0; distance < BlueprintRarity.values().length; distance++) {
            // Prefer the lower tier when equally close nonempty tiers are available.
            for (int candidate : distance == 0 ? new int[]{tier} : new int[]{tier - distance, tier + distance}) {
                if (candidate < 0 || candidate >= BlueprintRarity.values().length) {
                    continue;
                }
                List<ConstructorRecipe> recipes = choices.get(BlueprintRarity.values()[candidate]);
                if (recipes != null) {
                    ItemStack result = BlueprintItem.makeBluePrint(recipes.get(random.nextInt(recipes.size())).getDestination());
                    ItemData.set(stack, ItemData.get(result));
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private static Exchange readInputs(CraftingInput input) {
        List<ResourceLocation> excluded = new ArrayList<>();
        int total = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.is(Registration.BLUEPRINT.get()) || isPreview(stack) || excluded.size() == 3) {
                return null;
            }
            ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(BlueprintItem.getDestination(stack));
            BlueprintRarity rarity = recipe == null ? null : BlueprintRarity.of(recipe);
            if (rarity == null) {
                return null;
            }
            excluded.add(recipe.getId());
            total += rarity.ordinal();
        }
        if (excluded.size() != 3) {
            return null;
        }
        excluded.sort(Comparator.comparing(ResourceLocation::toString));
        return new Exchange(BlueprintRarity.values()[Math.round(total / 3.0f)], List.copyOf(excluded));
    }

    @Nullable
    private static Exchange readPreview(ItemStack stack) {
        CompoundTag data = previewData(stack);
        if (!isPreview(stack) || data == null || !data.hasUUID("craft") || !data.contains("tier", Tag.TAG_INT)) {
            return null;
        }
        int tier = data.getInt("tier");
        ListTag ids = data.getList("excluded", Tag.TAG_STRING);
        if (tier < 0 || tier >= BlueprintRarity.values().length || ids.size() != 3) {
            return null;
        }
        List<ResourceLocation> excluded = new ArrayList<>();
        for (int index = 0; index < ids.size(); index++) {
            ResourceLocation id = ResourceLocation.tryParse(ids.getString(index));
            if (id == null) {
                return null;
            }
            excluded.add(id);
        }
        return new Exchange(BlueprintRarity.values()[tier], excluded);
    }

    private record Exchange(BlueprintRarity rarity, List<ResourceLocation> excluded) {
    }
}
