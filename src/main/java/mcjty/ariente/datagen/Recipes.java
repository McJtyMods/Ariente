package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.decorative.PatternType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.datagen.BaseRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.ARIENTE_PEARL.get())
                        .key('m', Items.QUARTZ)
                        .addCriterion("enderpearl", hasItem(Items.ENDER_PEARL)),
                "mmm", "mom", "mmm");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.CONSTRUCTOR.get())
                        .key('l', Registration.TAG_INGOT_SILVER)
                        .key('s', Registration.TAG_INGOT_PLATINUM)
                        .addCriterion("silver", hasItem(Registration.INGOT_SILVER.get())),
                "iii", "isi", "lll");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.ENHANCED_ENERGY_SABRE.get())
                        .key('x', Registration.TAG_INGOT_LITHIUM)
                        .key('M', Registration.ENERGY_SABRE.get())
                        .addCriterion("sabre", hasItem(Registration.ENERGY_SABRE.get())),
                "xxx", "xMx", "xxx");

        for (MarbleColor color : MarbleColor.VALUES) {
            build(consumer, new ResourceLocation(Ariente.MODID, "marble_" + color.getName() + "_dye"), ShapelessRecipeBuilder.shapelessRecipe(Registration.MARBLE_ITEMS.get(color).get())
                    .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                    .addIngredient(Registration.TAG_MARBLE)
                    .addIngredient(color.getColor().getTag()));
            build(consumer, ShapelessRecipeBuilder.shapelessRecipe(Registration.MARBLE_SMOOTH_ITEMS.get(color).get())
                    .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                    .addIngredient(Registration.MARBLE_ITEMS.get(color).get()));
            build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.MARBLE_BRICKS_ITEMS.get(color).get(), 4)
                            .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                            .key('m', Registration.MARBLE_ITEMS.get(color).get()),
                    "mm", "mm");
            build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.MARBLE_PILAR_ITEMS.get(color).get(), 2)
                            .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                            .key('m', Registration.MARBLE_ITEMS.get(color).get()),
                    "m", "m");
            build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.MARBLE_SLAB_ITEMS.get(color).get(), 6)
                            .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                            .key('m', Registration.MARBLE_ITEMS.get(color).get()),
                    "mmm");
        }

        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.DARKBLUE_BLUE).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MMM", "MmM", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.DARKBLUE_RED).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MMM", "MmM", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.DOTS).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "mMm", "MmM", "mMm");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.DOTS2).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "mMm", "MmM", "mMm");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.LINES).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MMM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.RED_LINES).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MMM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.LINES_GLOW).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('g', Items.GLOWSTONE_DUST)
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MgM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.RED_LINES_GLOW).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('g', Items.GLOWSTONE_DUST)
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MgM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.PANEL).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('S', Registration.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MMM", "Smr", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.RED_VAR1).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MmM", "mmm", "MmM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.RED_VAR1_ANIM).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.RED).get()),
                "MmM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.VAR1).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MmM", "mmm", "MmM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.BLACK_TECH_ITEMS.get(TechType.VAR1_ANIM).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.DARKBLUE).get()),
                "MmM", "mmm", "MMM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.PATTERN_ITEMS.get(PatternType.DOTS).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.GRAY).get()),
                "MmM", "mMm", "MmM");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(Registration.PATTERN_ITEMS.get(PatternType.LINES).get())
                        .addCriterion("marble", hasItem(Registration.MARBLE.get()))
                        .key('M', Registration.MARBLE_ITEMS.get(MarbleColor.BLACK).get())
                        .key('m', Registration.MARBLE_ITEMS.get(MarbleColor.GRAY).get()),
                "mMm", "MmM", "mMm");
    }
}
