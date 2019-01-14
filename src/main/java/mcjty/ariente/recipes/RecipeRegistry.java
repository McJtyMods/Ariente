package mcjty.ariente.recipes;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.decorative.MarbleColor;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.varia.WeightedRandom;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeRegistry {

    private static List<ConstructorRecipe> recipes = new ArrayList<>();
    private static WeightedRandom<ConstructorRecipe> randomRecipes = null;

    // Materials
    // - Lithium (RARE): advanced circuits
    // - Silicon (COMMON): circuits
    // - Manganese (MEDIUM):
    // - Platinum (COMMON): armor/force
    // - Silver (COMMON): basic building
    // - Negarite: power
    // - Posirite: power
    // - Lapis: luck/fortune
    // - Glowstone: light/vision
    // - Enderpearls: wireless

    public static final float EXTREME = 0.001f;
    public static final float RARE = 0.01f;
    public static final float UNCOMMON = 0.1f;
    public static final float COMMON = 1.0f;


    public static void init() {
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleAutofeed), COMMON,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleNightvision), UNCOMMON,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 10)
                ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleArmor), RARE,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(Items.DIAMOND, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleEnergy), RARE,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFeatherFalling), RARE,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.FEATHER, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFire), UNCOMMON,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Blocks.MAGMA, 1),
                new ItemStack(Items.FLINT_AND_STEEL, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFlight), EXTREME,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 2),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleForcefield), EXTREME,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.negariteDust, 5),
                new ItemStack(ModItems.posiriteDust, 5),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleInhibit), RARE,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.STRING, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleLooting), RARE,
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.DYE, 10, EnumDyeColor.BLUE.getMetadata())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.fluxLevitatorItem), UNCOMMON,
                new ItemStack(ModItems.silverIngot, 10),
                new ItemStack(ModItems.manganeseIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.energySabre), COMMON,
                new ItemStack(ModItems.silverIngot, 10),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.enhancedEnergySabreItem), RARE,
                new ItemStack(ModItems.energySabre, 1),
                new ItemStack(ModItems.silicon, 10),
                new ItemStack(ModItems.negariteDust, 5),
                new ItemStack(ModItems.posiriteDust, 5),
                new ItemStack(ModItems.platinumIngot, 3),
                new ItemStack(ModItems.lithiumIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.powerSuitLegs), RARE,
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.platinumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.powerSuitHelmet), RARE,
                new ItemStack(ModItems.silverIngot, 4),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.platinumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.powerSuitChest), RARE,
                new ItemStack(ModItems.silverIngot, 7),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.platinumIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.powerSuitBoots), RARE,
                new ItemStack(ModItems.silverIngot, 3),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.platinumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.forceFieldBlock), RARE,
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.platinumIngot, 3),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.posiriteDust, 10),
                new ItemStack(ModItems.negariteDust, 10),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.negariteGeneratorBlock), COMMON,
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.manganeseIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.negariteDust, 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.posiriteGeneratorBlock), COMMON,
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.manganeseIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.posiriteDust, 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.negariteTankBlock), COMMON,
                new ItemStack(ModItems.silverIngot, 4),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(ModItems.negariteDust, 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.posiriteTankBlock), COMMON,
                new ItemStack(ModItems.silverIngot, 4),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(ModItems.posiriteDust, 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.fluxBeamBlock), COMMON,
                new ItemStack(ModBlocks.marble, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.negariteDust, 1),
                new ItemStack(ModItems.posiriteDust, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.levelMarkerBlock), COMMON,
                new ItemStack(ModBlocks.marble, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.alarmBlock), UNCOMMON,
                new ItemStack(ModBlocks.marble, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.signalReceiverBlock), UNCOMMON,
                new ItemStack(ModBlocks.marble, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.slopeBlock), COMMON,
                new ItemStack(ModBlocks.marble, 1, MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.marbleSlabBlock, 2), COMMON,
                new ItemStack(ModBlocks.marble, 1, MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.reinforcedMarble, 1), UNCOMMON,
                new ItemStack(ModBlocks.marble, 4),
                new ItemStack(Blocks.OBSIDIAN, 4)
        ));
    }

    public static List<ConstructorRecipe> getRecipes() {
        return recipes;
    }

    public static ConstructorRecipe findRecipe(ItemStack destination) {
        // @todo optimize
        for (ConstructorRecipe recipe : recipes) {
            if (ItemHandlerHelper.canItemStacksStack(recipe.getDestination(), destination)) {
                return recipe;
            }
        }
        return null;
    }

    public static WeightedRandom<ConstructorRecipe> getRandomRecipes() {
        if (randomRecipes == null) {
            randomRecipes = new WeightedRandom<>();
            for (ConstructorRecipe recipe : recipes) {
                randomRecipes.add(recipe, recipe.getChance());
            }
        }
        return randomRecipes;
    }
}
