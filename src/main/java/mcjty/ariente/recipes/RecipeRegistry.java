package mcjty.ariente.recipes;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.decorative.MarbleColor;
import mcjty.ariente.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeRegistry {

    private static List<ConstructorRecipe> recipes = new ArrayList<>();

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


    public static void init() {
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleAutofeed),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleNightvision),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 10)
                ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleArmor),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(Items.DIAMOND, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleEnergy),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFeatherFalling),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.FEATHER, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFire),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Blocks.MAGMA, 1),
                new ItemStack(Items.FLINT_AND_STEEL, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFlight),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 2),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleForcefield),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.negariteDust, 5),
                new ItemStack(ModItems.posiriteDust, 5),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleInhibit),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.STRING, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleLooting),
                new ItemStack(Items.PAPER),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.DYE, 10, EnumDyeColor.BLUE.getMetadata())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.fluxLevitatorItem),
                new ItemStack(ModItems.silverIngot, 10),
                new ItemStack(ModItems.manganeseIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.energySabre),
                new ItemStack(ModItems.silverIngot, 10),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.enhancedEnergySabreItem),
                new ItemStack(ModItems.energySabre, 1),
                new ItemStack(ModItems.silicon, 10),
                new ItemStack(ModItems.negariteDust, 5),
                new ItemStack(ModItems.posiriteDust, 5),
                new ItemStack(ModItems.platinumIngot, 3),
                new ItemStack(ModItems.lithiumIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.powerSuitLegs),
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.platinumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.powerSuitHelmet),
                new ItemStack(ModItems.silverIngot, 4),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.platinumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.powerSuitChest),
                new ItemStack(ModItems.silverIngot, 7),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.platinumIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.powerSuitBoots),
                new ItemStack(ModItems.silverIngot, 3),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.platinumIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.forceFieldBlock),
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.platinumIngot, 3),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.posiriteDust, 10),
                new ItemStack(ModItems.negariteDust, 10),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.negariteGeneratorBlock),
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.manganeseIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.negariteDust, 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.posiriteGeneratorBlock),
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.manganeseIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.posiriteDust, 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.negariteTankBlock),
                new ItemStack(ModItems.silverIngot, 4),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(ModItems.negariteDust, 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.posiriteTankBlock),
                new ItemStack(ModItems.silverIngot, 4),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(ModItems.posiriteDust, 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.fluxBeamBlock),
                new ItemStack(ModBlocks.marble, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.negariteDust, 1),
                new ItemStack(ModItems.posiriteDust, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.levelMarkerBlock),
                new ItemStack(ModBlocks.marble, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.alarmBlock),
                new ItemStack(ModBlocks.marble, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.signalReceiverBlock),
                new ItemStack(ModBlocks.marble, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.slopeBlock),
                new ItemStack(ModBlocks.marble, 1, MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.marbleSlabBlock, 2),
                new ItemStack(ModBlocks.marble, 1, MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.reinforcedMarble, 1),
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

}
