package mcjty.ariente.recipes;

import mcjty.ariente.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RecipeRegistry {

    private static List<ConstructorRecipe> recipes = new ArrayList<>();

    public static void init() {
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleAutofeed),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleNightvision),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 10)
                ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleNightvision),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 10)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleArmor),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(Items.DIAMOND, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleEnergy),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFeatherFalling),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.FEATHER, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFire),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(Blocks.MAGMA, 1),
                new ItemStack(Items.FLINT_AND_STEEL, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFlight),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 2),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
//        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleForcefield),
//                new ItemStack(Items.PAPER),
//                new ItemStack(ModItems.silverIngot, 1),
//                new ItemStack(ModItems.platinumIngot, 1),
//                new ItemStack(ModItems.lithiumIngot, 1),
//                new ItemStack(Items., 2),
//                new ItemStack(Items.NETHER_STAR, 1)
//        ));
    }

    public static ConstructorRecipe findRecipe(ItemStack destination) {
        // @todo optimize
        for (ConstructorRecipe recipe : recipes) {
            if (ItemStack.areItemStacksEqual(recipe.getDestination(), destination)) {
                return recipe;
            }
        }
        return null;
    }

}
