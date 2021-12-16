package mcjty.ariente.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstructorRecipe implements Recipe<ConstructorContext> {

    private final ResourceLocation id;
    private final ItemStack destination;
    private final List<ItemStack> ingredients;
    private final float chance;

    public ConstructorRecipe(ResourceLocation id, ItemStack destination, float chance, ItemStack... ingredients) {
        this.id = id;
        this.destination = destination;
        this.ingredients = new ArrayList<>(ingredients.length);
        this.chance = chance;
        Collections.addAll(this.ingredients, ingredients);
    }

    public ConstructorRecipe(ItemStack destination, float chance, ItemStack... ingredients) {
        this.id = destination.getItem().getRegistryName();
        this.destination = destination;
        this.ingredients = new ArrayList<>(ingredients.length);
        this.chance = chance;
        Collections.addAll(this.ingredients, ingredients);
    }

    public float getChance() {
        return chance;
    }

    public ItemStack getDestination() {
        return destination;
    }

    public List<ItemStack> getIngredientList() {
        return ingredients;
    }

    @Override
    public boolean matches(ConstructorContext inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(ConstructorContext inv) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return destination;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BlueprintRecipeRegistry.CONSTRUCTOR_RECIPES.get();
    }

    @Override
    public RecipeType<?> getType() {
        return BlueprintRecipeRegistry.CONSTRUCTOR;
    }
}
