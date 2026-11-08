package mcjty.ariente.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** The result is one unit of raw metal; the purifier's tube count determines recovery. */
public record PurifyingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result) implements Recipe<SingleRecipeInput> {

    @Override
    public boolean matches(SingleRecipeInput inventory, Level level) {
        return ingredient.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput inventory, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BlueprintRecipeRegistry.PURIFYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BlueprintRecipeRegistry.PURIFYING.get();
    }

    public static class Serializer implements RecipeSerializer<PurifyingRecipe> {

        private static final MapCodec<PurifyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(PurifyingRecipe::ingredient),
                ItemStack.SINGLE_ITEM_CODEC.fieldOf("result").forGetter(PurifyingRecipe::result)
        ).apply(instance, (ingredient, result) -> new PurifyingRecipe(BuiltInRegistries.ITEM.getKey(result.getItem()), ingredient, result)));
        private static final StreamCodec<RegistryFriendlyByteBuf, PurifyingRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    buffer.writeResourceLocation(recipe.id());
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
                }, buffer -> new PurifyingRecipe(buffer.readResourceLocation(), Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), ItemStack.STREAM_CODEC.decode(buffer)));

        @Override
        public MapCodec<PurifyingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PurifyingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
