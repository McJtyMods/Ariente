package mcjty.ariente.recipes;

import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.setup.Registration;
import mcjty.lib.varia.WeightedRandom;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static mcjty.ariente.Ariente.MODID;

public class BlueprintRecipeRegistry {


    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static void register() {
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<IRecipeSerializer<ConstructorRecipe>> CONSTRUCTOR_RECIPES = RECIPE_SERIALIZERS.register("constructor",
            ConstructorRecipeSerializer::new);

    public static final ResourceLocation RECIPE_CONSTRUCTOR = new ResourceLocation(MODID, "constructor");
    public static IRecipeType<ConstructorRecipe> CONSTRUCTOR = Registry.register(Registry.RECIPE_TYPE, RECIPE_CONSTRUCTOR,
            new IRecipeType<ConstructorRecipe>() {
                @Override
                public String toString() {
                    return RECIPE_CONSTRUCTOR.toString();
                }
            });

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

        recipes.add(new ConstructorRecipe(new ItemStack(Registration.FLUX_CAPACITOR.get()), RARE,
                new ItemStack(Registration.INGOT_LITHIUM.get(), 2),
                new ItemStack(Registration.INGOT_MANGANESE.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 6),
                new ItemStack(Registration.DUST_POSIRITE.get(), 6)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.ENERGY_HOLDER.get()), COMMON,
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 5),
                new ItemStack(Registration.DUST_POSIRITE.get(), 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.CIRCUIT.get()), COMMON,
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_MANGANESE.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 3),
                new ItemStack(Registration.DUST_POSIRITE.get(), 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.ADVANCED_CIRCUIT.get()), RARE,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 2),
                new ItemStack(Registration.INGOT_MANGANESE.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 6),
                new ItemStack(Registration.DUST_POSIRITE.get(), 6)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_AUTOFEED.get()), COMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 3),
                new ItemStack(Registration.DUST_POSIRITE.get(), 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_NIGHTVISION.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 10)
                ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_ARMOR.get()), RARE,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1),
                new ItemStack(Items.DIAMOND, 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_ENERGY.get()), RARE,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 3),
                new ItemStack(Registration.DUST_POSIRITE.get(), 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_FEATHERFALLING.get()), RARE,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.FEATHER, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_FIRE.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Blocks.MAGMA_BLOCK, 1),
                new ItemStack(Items.FLINT_AND_STEEL, 5)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_FLIGHT.get()), EXTREME,
                new ItemStack(Registration.ADVANCED_CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 2),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_FORCEFIELD.get()), EXTREME,
                new ItemStack(Registration.ADVANCED_CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 5),
                new ItemStack(Registration.DUST_POSIRITE.get(), 5),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_INHIBIT.get()), RARE,
                new ItemStack(Registration.ADVANCED_CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Items.STRING, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MODULE_LOOTING.get()), RARE,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Items.BLUE_DYE, 10) // @todo 1.14 DyeColor.BLUE.getMetadata())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.FLUX_LEVITATOR.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 10),
                new ItemStack(Registration.INGOT_MANGANESE.get(), 2),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.ENERGY_SABRE.get()), COMMON,
                new ItemStack(Registration.INGOT_SILVER.get(), 10),
                new ItemStack(Registration.DUST_NEGARITE.get(), 3),
                new ItemStack(Registration.DUST_POSIRITE.get(), 3),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.ENHANCED_ENERGY_SABRE.get()), RARE,
                new ItemStack(Registration.ENERGY_SABRE.get(), 1),
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 3),
                new ItemStack(Registration.DUST_NEGARITE.get(), 5),
                new ItemStack(Registration.DUST_POSIRITE.get(), 5),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 3),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.POWERSUIT_LEGS.get()), RARE,
                new ItemStack(Registration.INGOT_SILVER.get(), 5),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.POWERSUIT_HEAD.get()), RARE,
                new ItemStack(Registration.INGOT_SILVER.get(), 4),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.POWERSUIT_CHEST.get()), RARE,
                new ItemStack(Registration.INGOT_SILVER.get(), 7),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.POWERSUIT_FEET.get()), RARE,
                new ItemStack(Registration.INGOT_SILVER.get(), 3),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.FORCEFIELD.get()), RARE,
                new ItemStack(Registration.ADVANCED_CIRCUIT.get(), 1),
                new ItemStack(Registration.FLUX_CAPACITOR.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 5),
                new ItemStack(Registration.DUST_SILICON.get(), 5),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 3),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 2),
                new ItemStack(Registration.DUST_POSIRITE.get(), 10),
                new ItemStack(Registration.DUST_NEGARITE.get(), 10),
                new ItemStack(Items.NETHER_STAR, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.NEGARITE_GENERATOR.get()), COMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 5),
                new ItemStack(Registration.DUST_SILICON.get(), 5),
                new ItemStack(Registration.INGOT_MANGANESE.get(), 2),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 2),
                new ItemStack(Registration.DUST_NEGARITE.get(), 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.POSIRITE_GENERATOR.get()), COMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 5),
                new ItemStack(Registration.DUST_SILICON.get(), 5),
                new ItemStack(Registration.INGOT_MANGANESE.get(), 2),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 2),
                new ItemStack(Registration.DUST_POSIRITE.get(), 40)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.POWER_COMBINER.get()), COMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 3),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.DUST_NEGARITE.get(), 20),
                new ItemStack(Registration.DUST_POSIRITE.get(), 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.CONSTRUCTOR.get()), COMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 2),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.AUTO_CONSTRUCTOR.get()), COMMON,
                new ItemStack(Registration.CONSTRUCTOR.get(), 1),
                new ItemStack(Registration.CIRCUIT.get(), 2),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.INGOT_PLATINUM.get(), 2),
                new ItemStack(Registration.DUST_SILICON.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.STORAGE.get()), COMMON,
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 3),
                new ItemStack(Blocks.CHEST, 1),
                new ItemStack(Blocks.GLASS_PANE, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.BLUEPRINT_STORAGE.get()), COMMON,
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 3),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Blocks.CHEST, 1),
                new ItemStack(Blocks.GLASS_PANE, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.ELEVATOR.get()), COMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 3)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.NEGARITE_TANK.get()), COMMON,
                new ItemStack(Registration.INGOT_SILVER.get(), 4),
                new ItemStack(Registration.INGOT_MANGANESE.get(), 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(Registration.DUST_NEGARITE.get(), 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.POSIRITE_TANK.get()), COMMON,
                new ItemStack(Registration.INGOT_SILVER.get(), 4),
                new ItemStack(Registration.INGOT_MANGANESE.get(), 1),
                new ItemStack(Blocks.GLASS, 4),
                new ItemStack(Registration.DUST_POSIRITE.get(), 20)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.FLUX_BEAM.get()), COMMON,
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 1),
                new ItemStack(Registration.DUST_POSIRITE.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.FLUX_BEND_BEAM.get()), COMMON,
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_NEGARITE.get(), 1),
                new ItemStack(Registration.DUST_POSIRITE.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.LEVEL_MARKER.get()), COMMON,
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.DOOR_MARKER.get()), COMMON,
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 3),
                new ItemStack(Registration.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.ALARM.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.LOCK.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.SIGNAL_RECEIVER.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.SIGNAL_TRANSMITTER.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.WIRELESS_BUTTON.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.WIRELESS_LOCK.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 4),
                new ItemStack(Registration.INGOT_SILVER.get(), 2),
                new ItemStack(Registration.INGOT_LITHIUM.get(), 1),
                new ItemStack(Items.ENDER_PEARL, 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.SLOPE.get()), COMMON,
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 1) // @todo 1.14 , MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.MARBLE_SLAB.get(), 2), COMMON,
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 1) // @todo 1.14 , MarbleColor.BLACK.ordinal())
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.REINFORCED_MARBLE.get(), 1), UNCOMMON,
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 4),
                new ItemStack(Blocks.OBSIDIAN, 4)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.AUTOMATION_FIELD.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.FLUX_CAPACITOR.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 3),
                new ItemStack(Items.ENDER_PEARL, 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.SENSOR_ITEM_NODE.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.INPUT_ITEM_NODE.get()), COMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.OUTPUT_ITEM_NODE.get()), COMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 2),
                new ItemStack(Registration.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.ROUND_ROBIN_NODE.get()), UNCOMMON,
                new ItemStack(Registration.CIRCUIT.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.FIELD_MARKER.get()), COMMON,
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.FLUX_GLOW.get()), COMMON,
                new ItemStack(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 8)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.FLAT_LIGHT.get()), COMMON,
                new ItemStack(Registration.MARBLE_SLAB.get(), 1),
                new ItemStack(Items.GLOWSTONE_DUST, 8)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.NEGARITE.ordinal())*/), COMMON,
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(Registration.DUST_NEGARITE.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.POSIRITE.ordinal())*/), COMMON,
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(Registration.DUST_POSIRITE.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.NETCABLE.get(), 8 /* @todo 1.14 , CableColor.COMBINED.ordinal())*/), COMMON,
                new ItemStack(Registration.DUST_SILICON.get(), 1),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Blocks.GLASS_PANE, 3),
                new ItemStack(Registration.DUST_NEGARITE.get(), 2),
                new ItemStack(Registration.DUST_POSIRITE.get(), 2)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.NEGARITE.ordinal())*/), COMMON,
                new ItemStack(Registration.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.NEGARITE.ordinal()*/),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.POSIRITE.ordinal()*/), COMMON,
                new ItemStack(Registration.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.POSIRITE.ordinal()*/),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1)
        ));
        recipes.add(new ConstructorRecipe(new ItemStack(Registration.CONNECTOR.get(), 1 /* @todo 1.14 , CableColor.COMBINED.ordinal()*/), COMMON,
                new ItemStack(Registration.NETCABLE.get(), 1 /* @todo 1.14 , CableColor.COMBINED.ordinal()*/),
                new ItemStack(Registration.INGOT_SILVER.get(), 1),
                new ItemStack(Registration.DUST_SILICON.get(), 1)
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
