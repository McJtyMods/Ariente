package mcjty.ariente.recipes;

import mcjty.ariente.blocks.ModBlocks;
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
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLUX_CAPACITOR.get()), RARE,
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 2),
                new ItemStack(ModBlocks.INGOT_MANGANESE.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 6),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 6)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ENERGY_HOLDER.get()), COMMON,
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 5),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CIRCUIT.get()), COMMON,
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_MANGANESE.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 3),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ADVANCED_CIRCUIT.get()), RARE,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 2),
                new ItemStack(ModBlocks.INGOT_MANGANESE.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 6),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 6)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_AUTOFEED.get()), COMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 3),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_NIGHTVISION.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 10)
                ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_ARMOR.get()), RARE,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1),
                new ItemStack(Items.DIAMOND, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_ENERGY.get()), RARE,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 3),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_FEATHERFALLING.get()), RARE,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.FEATHER, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_FIRE.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(Blocks.MAGMA_BLOCK, 1),
                new ItemStack(Items.FLINT_AND_STEEL, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_FLIGHT.get()), EXTREME,
                new ItemStack(ModBlocks.ADVANCED_CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 2),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_FORCEFIELD.get()), EXTREME,
                new ItemStack(ModBlocks.ADVANCED_CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 5),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 5),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_INHIBIT.get()), RARE,
                new ItemStack(ModBlocks.ADVANCED_CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(Items.STRING, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MODULE_LOOTING.get()), RARE,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(Items.BLUE_DYE, 10) // @todo 1.14 DyeColor.BLUE.getMetadata())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLUX_LEVITATOR.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 10),
                new ItemStack(ModBlocks.INGOT_MANGANESE.get(), 2),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ENERGY_SABRE.get()), COMMON,
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 10),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 3),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 3),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ENHANCED_ENERGY_SABRE.get()), RARE,
                new ItemStack(ModBlocks.ENERGY_SABRE.get(), 1),
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 3),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 5),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 5),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 3),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POWERSUIT_LEGS.get()), RARE,
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 5),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POWERSUIT_HEAD.get()), RARE,
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 4),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POWERSUIT_CHEST.get()), RARE,
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 7),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POWERSUIT_FEET.get()), RARE,
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 3),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FORCEFIELD.get()), RARE,
                new ItemStack(ModBlocks.ADVANCED_CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.FLUX_CAPACITOR.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 5),
                new ItemStack(ModBlocks.SILICON.get(), 5),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 3),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 2),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 10),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 10),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NEGARITE_GENERATOR.get()), COMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 5),
                new ItemStack(ModBlocks.SILICON.get(), 5),
                new ItemStack(ModBlocks.INGOT_MANGANESE.get(), 2),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 2),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POSIRITE_GENERATOR.get()), COMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 5),
                new ItemStack(ModBlocks.SILICON.get(), 5),
                new ItemStack(ModBlocks.INGOT_MANGANESE.get(), 2),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 2),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POWER_COMBINER.get()), COMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 3),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 20),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CONSTRUCTOR.get()), COMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 2),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.AUTO_CONSTRUCTOR.get()), COMMON,
                new ItemStack(ModBlocks.CONSTRUCTOR.get(), 1),
                new ItemStack(ModBlocks.CIRCUIT.get(), 2),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.INGOT_PLATINUM.get(), 2),
                new ItemStack(ModBlocks.SILICON.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.STORAGE.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 3),
                new ItemStack(Blocks.CHEST, 1),
                new ItemStack(Blocks.GLASS_PANE, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.BLUEPRINT_STORAGE.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 3),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(Blocks.CHEST, 1),
                new ItemStack(Blocks.GLASS_PANE, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ELEVATOR.get()), COMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NEGARITE_TANK.get()), COMMON,
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 4),
                new ItemStack(ModBlocks.INGOT_MANGANESE.get(), 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.POSIRITE_TANK.get()), COMMON,
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 4),
                new ItemStack(ModBlocks.INGOT_MANGANESE.get(), 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLUX_BEAM.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 1),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLUX_BEND_BEAM.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 1),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.LEVEL_MARKER.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.DOOR_MARKER.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 3),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ALARM.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.LOCK.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.SIGNAL_RECEIVER.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.SIGNAL_TRANSMITTER.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.WIRELESS_BUTTON.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.WIRELESS_LOCK.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 4),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 2),
                new ItemStack(ModBlocks.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.SLOPE.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1) // @todo 1.14 , MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.MARBLE_SLAB.get(), 2), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1) // @todo 1.14 , MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.REINFORCED_MARBLE.get(), 1), UNCOMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 4),
                new ItemStack(Blocks.OBSIDIAN, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.AUTOMATION_FIELD.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.FLUX_CAPACITOR.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 3),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.SENSOR_ITEM_NODE.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.INPUT_ITEM_NODE.get()), COMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.OUTPUT_ITEM_NODE.get()), COMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 2),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.ROUND_ROBIN_NODE.get()), UNCOMMON,
                new ItemStack(ModBlocks.CIRCUIT.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FIELD_MARKER.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLUX_GLOW.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 8)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.FLAT_LIGHT.get()), COMMON,
                new ItemStack(ModBlocks.MARBLE_SLAB.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 8)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.NEGARITE.ordinal())*/), COMMON,
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.POSIRITE.ordinal())*/), COMMON,
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.COMBINED.ordinal())*/), COMMON,
                new ItemStack(ModBlocks.SILICON.get(), 1),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(ModBlocks.DUST_NEGARITE.get(), 2),
                new ItemStack(ModBlocks.DUST_POSIRITE.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.NEGARITE.ordinal())*/), COMMON,
                new ItemStack(ModBlocks.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.NEGARITE.ordinal()*/),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.POSIRITE.ordinal()*/), COMMON,
                new ItemStack(ModBlocks.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.POSIRITE.ordinal()*/),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(ModBlocks.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.COMBINED.ordinal()*/), COMMON,
                new ItemStack(ModBlocks.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.COMBINED.ordinal()*/),
                new ItemStack(ModBlocks.INGOT_SILVER.get(), 1),
                new ItemStack(ModBlocks.SILICON.get(), 1)
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
