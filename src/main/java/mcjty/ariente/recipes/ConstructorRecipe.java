package mcjty.ariente.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstructorRecipe implements IRecipe<ConstructorContext> {

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
    public boolean matches(ConstructorContext inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(ConstructorContext inv) {
        return null;
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return destination;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return BlueprintRecipeRegistry.CONSTRUCTOR_RECIPES.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return BlueprintRecipeRegistry.CONSTRUCTOR;
    }
}
