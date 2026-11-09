package mcjty.ariente.recipes;

import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mcjty.lib.varia.Tools;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ConstructorRecipe implements Recipe<ConstructorContext> {

    private final Identifier id;
    private final ItemStack destination;
    private final List<ItemStack> ingredients;
    private final float chance;

    public ConstructorRecipe(Identifier id, ItemStack destination, float chance, ItemStack... ingredients) {
        this.id = id;
        this.destination = destination;
        this.ingredients = new ArrayList<>(ingredients.length);
        this.chance = chance;
        Collections.addAll(this.ingredients, ingredients);
    }

    public ConstructorRecipe(ItemStack destination, float chance, ItemStack... ingredients) {
        this.id = Tools.getId(destination.getItem());
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
    public boolean matches(ConstructorContext inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(ConstructorContext inv) {
        return destination.copy();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return destination;
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<ConstructorRecipe> getSerializer() {
        return BlueprintRecipeRegistry.CONSTRUCTOR_RECIPES.get();
    }

    @Override
    public RecipeType<ConstructorRecipe> getType() {
        return BlueprintRecipeRegistry.CONSTRUCTOR.get();
    }
}
