package mcjty.ariente.datagen;

import java.util.concurrent.CompletableFuture;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.blocks.decorative.PatternType;
import mcjty.ariente.recipes.BlueprintExchangeRecipe;
import mcjty.ariente.setup.Registration;
import mcjty.lib.datagen.BaseRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn, CompletableFuture<HolderLookup.Provider> provider) {
        super(generatorIn, provider);
    }

    @Override
    protected void buildRecipes(RecipeOutput consumer) {
        blueprintExchange(consumer);
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.TABLET.get())
                        .define('I', Items.IRON_INGOT)
                        .define('P', Items.GLASS_PANE)
                        .unlockedBy("redstone", has(Items.REDSTONE)),
                "IPI", "IrI", "III");

        metalProcessing(consumer, Registration.TAG_ORE_LITHIUM_ITEM, Registration.INGOT_LITHIUM.get(), "");
        metalProcessing(consumer, Registration.TAG_ORE_MANGANESE_ITEM, Registration.INGOT_MANGANESE.get(), "");
        metalProcessing(consumer, Registration.TAG_ORE_PLATINUM_ITEM, Registration.INGOT_PLATINUM.get(), "");
        metalProcessing(consumer, Registration.TAG_ORE_SILVER_ITEM, Registration.INGOT_SILVER.get(), "");
        metalProcessing(consumer, Registration.TAG_RAW_LITHIUM, Registration.INGOT_LITHIUM.get(), "_raw_lithium");
        metalProcessing(consumer, Registration.TAG_RAW_MANGANESE, Registration.INGOT_MANGANESE.get(), "_raw_manganese");
        metalProcessing(consumer, Registration.TAG_RAW_PLATINUM, Registration.INGOT_PLATINUM.get(), "_raw_platinum");
        metalProcessing(consumer, Registration.TAG_RAW_SILVER, Registration.INGOT_SILVER.get(), "_raw_silver");
        oreProcessing(consumer, Registration.ORE_NEGARITE.get(), Registration.DUST_NEGARITE.get(), 4);
        oreProcessing(consumer, Registration.ORE_POSIRITE.get(), Registration.DUST_POSIRITE.get(), 4);

        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.ARIENTE_PEARL.get())
                        .define('m', Items.QUARTZ)
                        .unlockedBy("enderpearl", has(Items.ENDER_PEARL)),
                "mmm", "mom", "mmm");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.CONSTRUCTOR.get())
                        .define('l', Registration.TAG_INGOT_SILVER)
                        .define('s', Registration.TAG_INGOT_PLATINUM)
                        .unlockedBy("silver", has(Registration.INGOT_SILVER.get())),
                "iii", "isi", "lll");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.BLUEPRINT_STORAGE.get())
                        .define('s', Registration.TAG_INGOT_SILVER)
                        .define('g', Items.GLASS_PANE)
                        .define('Q', Registration.TAG_DUSTS_SILICON)
                        .define('C', Items.CHEST)
                        .define('m', Registration.TAG_MARBLE)
                        .unlockedBy("silver", has(Registration.INGOT_SILVER.get())),
                "sgs", "QCQ", "mmm");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.ENHANCED_ENERGY_SABRE.get())
                        .define('x', Registration.TAG_INGOT_LITHIUM)
                        .define('M', Registration.ENERGY_SABRE.get())
                        .unlockedBy("sabre", has(Registration.ENERGY_SABRE.get())),
                "xxx", "xMx", "xxx");

        for (MarbleColor color : MarbleColor.VALUES) {
            build(consumer, ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "marble_" + color.getSerializedName() + "_dye"), ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, DecorativeBlockModule.MARBLE_ITEMS.get(color).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(color).get()))
                        .requires(Registration.TAG_MARBLE)
                        .requires(color.getColor().getTag()));
            build(consumer, ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, DecorativeBlockModule.MARBLE_SMOOTH_ITEMS.get(color).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(color).get()))
                        .requires(DecorativeBlockModule.MARBLE_ITEMS.get(color).get()));
            build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.MARBLE_BRICKS_ITEMS.get(color).get(), 4)
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(color).get()))
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(color).get()),
                        "mm", "mm");
            build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.MARBLE_PILAR_ITEMS.get(color).get(), 2)
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(color).get()))
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(color).get()),
                        "m", "m");
        }

        for (MarbleColor color : MarbleColor.VALUES) {
            build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.MARBLE_SLAB_ITEMS.get(color).get(), 6)
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(color).get()))
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(color).get()),
                        "mmm");
        }

        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.DARKBLUE_BLUE).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MMM", "MmM", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.DARKBLUE_RED).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MMM", "MmM", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.DOTS).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "mMm", "MmM", "mMm");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.DOTS2).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "mMm", "MmM", "mMm");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.LINES).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MMM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.RED_LINES).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MMM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.LINES_GLOW).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('g', Items.GLOWSTONE_DUST)
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MgM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.RED_LINES_GLOW).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('g', Items.GLOWSTONE_DUST)
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MgM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.PANEL).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('S', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MMM", "Smr", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.RED_VAR1).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MmM", "mmm", "MmM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.RED_VAR1_ANIM).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MmM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.VAR1).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MmM", "mmm", "MmM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DecorativeBlockModule.BLACK_TECH_ITEMS.get(TechType.VAR1_ANIM).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MmM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.PATTERN_ITEMS.get(PatternType.DOTS).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.GRAY).get()),
                "MmM", "mMm", "MmM");
        build(consumer, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.PATTERN_ITEMS.get(PatternType.LINES).get())
                        .unlockedBy("marble", has(DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get()))
                        .define('M', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .define('m', DecorativeBlockModule.MARBLE_ITEMS.get(MarbleColor.GRAY).get()),
                "mMm", "MmM", "mMm");
    }

    private void blueprintExchange(RecipeOutput consumer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.BLUEPRINT.get())
                .requires(Registration.BLUEPRINT.get(), 3)
                .unlockedBy("has_blueprint", has(Registration.BLUEPRINT.get()))
                .save(new RecipeOutput() {

                    @Override
                    public Advancement.Builder advancement() {
                        return consumer.advancement();
                    }

                    @Override
                    public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
                        consumer.accept(id, new BlueprintExchangeRecipe(CraftingBookCategory.MISC), advancement, conditions);
                    }
                }, BlueprintExchangeRecipe.ID);
    }

    private void metalProcessing(RecipeOutput consumer, TagKey<Item> material, ItemLike result, String suffix) {
        String name = BuiltInRegistries.ITEM.getKey(result.asItem()).getPath();
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(material), RecipeCategory.MISC, result, 0, 200)
                .unlockedBy("has_material", has(material)).save(consumer,
                        ResourceLocation.fromNamespaceAndPath(Ariente.MODID, name + "_from_smelting" + suffix));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(material), RecipeCategory.MISC, result, 0, 100)
                .unlockedBy("has_material", has(material)).save(consumer,
                        ResourceLocation.fromNamespaceAndPath(Ariente.MODID, name + "_from_blasting" + suffix));
    }

    private void oreProcessing(RecipeOutput consumer, ItemLike ore, ItemLike result, int count) {
        String name = BuiltInRegistries.ITEM.getKey(result.asItem()).getPath();
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ore), RecipeCategory.MISC, result, 0, 200)
                .unlockedBy("has_ore", has(ore)).save(withResultCount(consumer, count),
                        ResourceLocation.fromNamespaceAndPath(Ariente.MODID, name + "_from_smelting"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ore), RecipeCategory.MISC, result, 0, 100)
                .unlockedBy("has_ore", has(ore)).save(withResultCount(consumer, count),
                        ResourceLocation.fromNamespaceAndPath(Ariente.MODID, name + "_from_blasting"));
    }

    private RecipeOutput withResultCount(RecipeOutput consumer, int count) {
        return new RecipeOutput() {

            @Override
            public Advancement.Builder advancement() {
                return consumer.advancement();
            }

            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
                if (recipe instanceof SmeltingRecipe cooking) {
                    recipe = new SmeltingRecipe(cooking.getGroup(), cooking.category(), cooking.getIngredients().getFirst(),
                            new ItemStack(cooking.getResultItem(null).getItem(), count), cooking.getExperience(), cooking.getCookingTime());
                } else if (recipe instanceof BlastingRecipe cooking) {
                    recipe = new BlastingRecipe(cooking.getGroup(), cooking.category(), cooking.getIngredients().getFirst(),
                            new ItemStack(cooking.getResultItem(null).getItem(), count), cooking.getExperience(), cooking.getCookingTime());
                }
                consumer.accept(id, recipe, advancement, conditions);
            }
        };
    }
}
