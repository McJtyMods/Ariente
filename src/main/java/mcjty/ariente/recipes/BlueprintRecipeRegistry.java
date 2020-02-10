package mcjty.ariente.recipes;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.items.ModItems;
import mcjty.lib.varia.WeightedRandom;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class BlueprintRecipeRegistry {

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

    private static boolean initialized = false;


    private static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.fluxCapacitorItem), RARE,
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(ModItems.negariteDust, 6),
                new ItemStack(ModItems.posiriteDust, 6)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.energyHolderItem), COMMON,
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.negariteDust, 5),
                new ItemStack(ModItems.posiriteDust, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.circuitItem), COMMON,
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.advancedCircuitItem), RARE,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(ModItems.negariteDust, 6),
                new ItemStack(ModItems.posiriteDust, 6)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleAutofeed), COMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleNightvision), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 10)
                ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleArmor), RARE,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(Items.DIAMOND, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleEnergy), RARE,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(ModItems.negariteDust, 3),
                new ItemStack(ModItems.posiriteDust, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFeatherFalling), RARE,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.FEATHER, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFire), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Blocks.MAGMA_BLOCK, 1),
                new ItemStack(Items.FLINT_AND_STEEL, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleFlight), EXTREME,
                new ItemStack(ModItems.advancedCircuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 2),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleForcefield), EXTREME,
                new ItemStack(ModItems.advancedCircuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.negariteDust, 5),
                new ItemStack(ModItems.posiriteDust, 5),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleInhibit), RARE,
                new ItemStack(ModItems.advancedCircuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.STRING, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.moduleLooting), RARE,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(Items.BLUE_DYE, 10) // @todo 1.14 DyeColor.BLUE.getMetadata())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModItems.fluxLevitatorItem), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
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
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silicon, 3),
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
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FORCEFIELD.get()), RARE,
                new ItemStack(ModItems.advancedCircuitItem, 1),
                new ItemStack(ModItems.fluxCapacitorItem, 1),
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.platinumIngot, 3),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.posiriteDust, 10),
                new ItemStack(ModItems.negariteDust, 10),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NEGARITE_GENERATOR.get()), COMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.manganeseIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.negariteDust, 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POSIRITE_GENERATOR.get()), COMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 5),
                new ItemStack(ModItems.silicon, 5),
                new ItemStack(ModItems.manganeseIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 2),
                new ItemStack(ModItems.posiriteDust, 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POWER_COMBINER.get()), COMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 3),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.negariteDust, 20),
                new ItemStack(ModItems.posiriteDust, 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CONSTRUCTOR.get()), COMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silverIngot, 2),
                new ItemStack(ModItems.platinumIngot, 1),
                new ItemStack(ModItems.silicon, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.AUTO_CONSTRUCTOR.get()), COMMON,
                new ItemStack(ModBlocks.CONSTRUCTOR.get(), 1),
                new ItemStack(ModItems.circuitItem, 2),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.platinumIngot, 2),
                new ItemStack(ModItems.silicon, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.STORAGE_BLOCK.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModItems.silverIngot, 3),
                new ItemStack(Blocks.CHEST, 1),
                new ItemStack(Blocks.GLASS_PANE, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.BLUEPRINT_STORAGE.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModItems.silverIngot, 3),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(Blocks.CHEST, 1),
                new ItemStack(Blocks.GLASS_PANE, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ELEVATOR_BLOCK.get()), COMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(ModItems.silverIngot, 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NEGARITE_TANK.get()), COMMON,
                new ItemStack(ModItems.silverIngot, 4),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(ModItems.negariteDust, 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POSIRITE_TANK.get()), COMMON,
                new ItemStack(ModItems.silverIngot, 4),
                new ItemStack(ModItems.manganeseIngot, 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(ModItems.posiriteDust, 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLUX_BEAM_BLOCK.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.negariteDust, 1),
                new ItemStack(ModItems.posiriteDust, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLUX_BEND_BEAM_BLOCK.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.negariteDust, 1),
                new ItemStack(ModItems.posiriteDust, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.LEVEL_MARKER.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.DOOR_MARKER.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(ModItems.silicon, 3),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ALARM.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.GLOWSTONE_DUST, 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.LOCK.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.SIGNAL_RECEIVER.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModItems.silverIngot, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.SIGNAL_TRANSMITTER.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.WIRELESS_BUTTON.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.WIRELESS_LOCK.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(ModItems.silicon, 4),
                new ItemStack(ModItems.silverIngot, 2),
                new ItemStack(ModItems.lithiumIngot, 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.SLOPE_BLOCK.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1) // @todo 1.14 , MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 2), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1) // @todo 1.14 , MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.REINFORCED_MARBLE.get(), 1), UNCOMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 4),
                new ItemStack(Blocks.OBSIDIAN, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.AUTOMATION_FIELD.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.fluxCapacitorItem, 1),
                new ItemStack(ModItems.silicon, 3),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModItems.silverIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.SENSOR_ITEM_NODE.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.silverIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.INPUT_ITEM_NODE.get()), COMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.silverIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.OUTPUT_ITEM_NODE.get()), COMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silicon, 2),
                new ItemStack(ModItems.silverIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ROUND_ROBIN_NODE.get()), UNCOMMON,
                new ItemStack(ModItems.circuitItem, 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.silverIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FIELD_MARKER.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.silverIngot, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLUX_GLOW.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 8)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLAT_LIGHT_BLOCK.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE_SLAB_BLOCK.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 8)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.NEGARITE.ordinal())*/), COMMON,
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(ModItems.negariteDust, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.POSIRITE.ordinal())*/), COMMON,
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(ModItems.posiriteDust, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.COMBINED.ordinal())*/), COMMON,
                new ItemStack(ModItems.silicon, 1),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(ModItems.negariteDust, 2),
                new ItemStack(ModItems.posiriteDust, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.NEGARITE.ordinal())*/), COMMON,
                new ItemStack(ModBlocks.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.NEGARITE.ordinal()*/),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.POSIRITE.ordinal()*/), COMMON,
                new ItemStack(ModBlocks.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.POSIRITE.ordinal()*/),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.COMBINED.ordinal()*/), COMMON,
                new ItemStack(ModBlocks.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.COMBINED.ordinal()*/),
                new ItemStack(ModItems.silverIngot, 1),
                new ItemStack(ModItems.silicon, 1)
        ));

    }

    public static List<ConstructorRecipe> getRecipes() {
        init();
        return recipes;
    }

    public static ConstructorRecipe findRecipe(ItemStack destination) {
        // @todo optimize
        for (ConstructorRecipe recipe : getRecipes()) {
            if (ItemHandlerHelper.canItemStacksStack(recipe.getDestination(), destination)) {
                return recipe;
            }
        }
        return null;
    }

    public static WeightedRandom<ConstructorRecipe> getRandomRecipes() {
        if (randomRecipes == null) {
            randomRecipes = new WeightedRandom<>();
            for (ConstructorRecipe recipe : getRecipes()) {
                randomRecipes.add(recipe, recipe.getChance());
            }
        }
        return randomRecipes;
    }
}
