package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.recipes.BlueprintExchangeRecipe;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.blocks.decorative.PatternType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.datagen.BaseRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
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

        for (MarbleColor color : MarbleColor.VALUES) {
            build(consumer, new ResourceLocation(Ariente.MODID, "marble_" + color.getSerializedName() + "_dye"), ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, DecorativeBlockModule.MARBLE_ITEMS.get(color).get())
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

    private void blueprintExchange(Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Registration.BLUEPRINT.get())
                .requires(Registration.BLUEPRINT.get(), 3)
                .unlockedBy("has_blueprint", has(Registration.BLUEPRINT.get()))
                .save(recipe -> consumer.accept(new FinishedRecipe() {

                    @Override
                    public void serializeRecipeData(JsonObject json) {
                        json.addProperty("category", "misc");
                    }

                    @Override
                    public RecipeSerializer<?> getType() {
                        return BlueprintRecipeRegistry.BLUEPRINT_EXCHANGE.get();
                    }

                    @Override
                    public ResourceLocation getId() {
                        return recipe.getId();
                    }

                    @Override
                    public JsonObject serializeAdvancement() {
                        return recipe.serializeAdvancement();
                    }

                    @Override
                    public ResourceLocation getAdvancementId() {
                        return recipe.getAdvancementId();
                    }
                }), BlueprintExchangeRecipe.ID);
    }

    private void metalProcessing(Consumer<FinishedRecipe> consumer, TagKey<Item> material, ItemLike result, String suffix) {
        String name = ForgeRegistries.ITEMS.getKey(result.asItem()).getPath();
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(material), RecipeCategory.MISC, result, 0, 200)
                .unlockedBy("has_material", has(material)).save(consumer,
                        new ResourceLocation(Ariente.MODID, name + "_from_smelting" + suffix));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(material), RecipeCategory.MISC, result, 0, 100)
                .unlockedBy("has_material", has(material)).save(consumer,
                        new ResourceLocation(Ariente.MODID, name + "_from_blasting" + suffix));
    }

    private void oreProcessing(Consumer<FinishedRecipe> consumer, ItemLike ore, ItemLike result, int count) {
        String name = ForgeRegistries.ITEMS.getKey(result.asItem()).getPath();
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ore), RecipeCategory.MISC, result, 0, 200)
                .unlockedBy("has_ore", has(ore)).save(recipe -> consumer.accept(withResultCount(recipe, count)),
                        new ResourceLocation(Ariente.MODID, name + "_from_smelting"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ore), RecipeCategory.MISC, result, 0, 100)
                .unlockedBy("has_ore", has(ore)).save(recipe -> consumer.accept(withResultCount(recipe, count)),
                        new ResourceLocation(Ariente.MODID, name + "_from_blasting"));
    }

    private FinishedRecipe withResultCount(FinishedRecipe recipe, int count) {
        return new FinishedRecipe() {

            @Override
            public void serializeRecipeData(JsonObject json) {
                recipe.serializeRecipeData(json);
                if (count > 1) {
                    // Forge's cooking serializer accepts a full result stack, including its count.
                    JsonObject result = new JsonObject();
                    result.addProperty("item", json.get("result").getAsString());
                    result.addProperty("count", count);
                    json.add("result", result);
                }
            }

            @Override
            public RecipeSerializer<?> getType() {
                return recipe.getType();
            }

            @Override
            public ResourceLocation getId() {
                return recipe.getId();
            }

            @Override
            public JsonObject serializeAdvancement() {
                return recipe.serializeAdvancement();
            }

            @Override
            public ResourceLocation getAdvancementId() {
                return recipe.getAdvancementId();
            }
        };
    }

}
