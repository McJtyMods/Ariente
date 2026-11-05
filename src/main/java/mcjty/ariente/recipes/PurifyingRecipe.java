package mcjty.ariente.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;

/** The result is one unit of raw metal; the purifier's tube count determines recovery. */
public record PurifyingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result) implements Recipe<Container> {

    @Override
    public boolean matches(Container inventory, Level level) {
        return ingredient.test(inventory.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inventory, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
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

    @Override
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

        @Override
        public PurifyingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            if (ingredient.isEmpty() || result.isEmpty() || result.getCount() != 1) {
                throw new IllegalArgumentException("Purifying recipes require an ingredient and exactly one raw-metal result unit: " + id);
            }
            return new PurifyingRecipe(id, ingredient, result);
        }

        @Override
        public PurifyingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new PurifyingRecipe(id, Ingredient.fromNetwork(buffer), buffer.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PurifyingRecipe recipe) {
            recipe.ingredient().toNetwork(buffer);
            buffer.writeItem(recipe.result());
        }
    }
}
