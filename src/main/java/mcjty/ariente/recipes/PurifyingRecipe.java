package mcjty.ariente.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** The result is one unit of raw metal; the purifier's tube count determines recovery. */
public record PurifyingRecipe(Identifier id, Ingredient ingredient, ItemStack result) implements Recipe<SingleRecipeInput> {

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean matches(SingleRecipeInput inventory, Level level) {
        return ingredient.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput inventory) {
        return result.copy();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(ingredient, ingredient);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<PurifyingRecipe> getSerializer() {
        return BlueprintRecipeRegistry.PURIFYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<PurifyingRecipe> getType() {
        return BlueprintRecipeRegistry.PURIFYING.get();
    }

    public static class Serializer  {

        public static final MapCodec<PurifyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(PurifyingRecipe::ingredient),
                ItemStack.CODEC.fieldOf("result").forGetter(PurifyingRecipe::result)
        ).apply(instance, (ingredient, result) -> new PurifyingRecipe(BuiltInRegistries.ITEM.getKey(result.getItem()), ingredient, result)));
        public static final StreamCodec<RegistryFriendlyByteBuf, PurifyingRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    buffer.writeIdentifier(recipe.id());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
                }, buffer -> new PurifyingRecipe(buffer.readIdentifier(), Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), ItemStack.STREAM_CODEC.decode(buffer)));

        public MapCodec<PurifyingRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, PurifyingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
