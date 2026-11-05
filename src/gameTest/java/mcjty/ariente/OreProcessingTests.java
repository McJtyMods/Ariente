package mcjty.ariente;

import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class OreProcessingTests {

    @GameTest(template = "empty")
    public static void metalMiningSupportsRawDropsFortuneAndSilkTouch(GameTestHelper helper) {
        ItemStack pickaxe = new ItemStack(Items.STONE_PICKAXE);
        ItemStack fortune = new ItemStack(Items.STONE_PICKAXE);
        fortune.enchant(Enchantments.BLOCK_FORTUNE, 3);
        ItemStack silk = new ItemStack(Items.STONE_PICKAXE);
        silk.enchant(Enchantments.SILK_TOUCH, 1);
        // Silk Touch must take precedence even when both enchantments are present.
        silk.enchant(Enchantments.BLOCK_FORTUNE, 3);
        for (Metal metal : metals()) {
            helper.assertTrue(pickaxe.isCorrectToolForDrops(metal.ore().defaultBlockState()), "Stone pickaxes harvest " + metal.name());
            helper.assertTrue(!new ItemStack(Items.WOODEN_PICKAXE).isCorrectToolForDrops(metal.ore().defaultBlockState()),
                    "Wooden pickaxes cannot harvest " + metal.name());
            assertDrop(helper, metal.ore(), pickaxe, metal.raw(), 1, 1);
            assertDrop(helper, metal.ore(), silk, metal.ore().asItem(), 1, 1);
            int total = 0;
            for (int i = 0; i < 64; i++) {
                total += assertDrop(helper, metal.ore(), fortune, metal.raw(), 1, 4);
            }
            helper.assertTrue(total > 64, "Fortune III increases raw " + metal.name() + " drops");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void taggedRawMetalsAndSilkTouchOresCookIntoOneIngot(GameTestHelper helper) {
        for (Metal metal : metals()) {
            assertItemTag(helper, metal.raw(), "raw_materials/" + metal.name());
            helper.assertTrue(new ItemStack(metal.raw()).is(Tags.Items.RAW_MATERIALS), "Raw metals belong to the shared category");
            assertItemTag(helper, metal.ingot(), "ingots/" + metal.name());
            helper.assertTrue(new ItemStack(metal.ingot()).is(Tags.Items.INGOTS), "Ingots belong to the shared category");
            assertItemTag(helper, metal.ore().asItem(), "ores/" + metal.name());
            helper.assertTrue(metal.ore().defaultBlockState().is(Tags.Blocks.ORES), "Ores belong to the shared block category");
            helper.assertTrue(new ItemStack(metal.ore()).is(Tags.Items.ORES), "Ores belong to the shared item category");
            assertCooking(helper, metal.raw(), metal.ingot(), 1);
            assertCooking(helper, metal.ore().asItem(), metal.ingot(), 1);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fuelAndSiliconDropsAndFuelYieldsStayUnchanged(GameTestHelper helper) {
        ItemStack pickaxe = new ItemStack(Items.STONE_PICKAXE);
        assertDrop(helper, Registration.ORE_NEGARITE.get(), pickaxe, Registration.ORE_NEGARITE_ITEM.get(), 1, 1);
        assertDrop(helper, Registration.ORE_POSIRITE.get(), pickaxe, Registration.ORE_POSIRITE_ITEM.get(), 1, 1);
        assertDrop(helper, Registration.ORE_SILICON.get(), pickaxe, Registration.DUST_SILICON.get(), 1, 1);
        assertCooking(helper, Registration.ORE_NEGARITE_ITEM.get(), Registration.DUST_NEGARITE.get(), 4);
        assertCooking(helper, Registration.ORE_POSIRITE_ITEM.get(), Registration.DUST_POSIRITE.get(), 4);
        helper.succeed();
    }

    private static int assertDrop(GameTestHelper helper, Block ore, ItemStack tool, Item expected, int min, int max) {
        List<ItemStack> drops = Block.getDrops(ore.defaultBlockState(), helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 2, 1)), null, null, tool);
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(expected), "Mining " + ore + " must drop " + expected);
        int count = drops.get(0).getCount();
        helper.assertTrue(count >= min && count <= max, "Expected " + min + " to " + max + " drops, got " + count);
        return count;
    }

    private static void assertCooking(GameTestHelper helper, Item input, Item result, int count) {
        SimpleContainer inventory = new SimpleContainer(new ItemStack(input));
        for (RecipeType<? extends AbstractCookingRecipe> type : List.of(RecipeType.SMELTING, RecipeType.BLASTING)) {
            AbstractCookingRecipe recipe = helper.getLevel().getRecipeManager().getRecipeFor(type, inventory, helper.getLevel()).orElseThrow();
            ItemStack output = recipe.assemble(inventory, helper.getLevel().registryAccess());
            helper.assertTrue(output.is(result) && output.getCount() == count, type + " must yield " + count + " " + result + " from " + input);
            helper.assertTrue(recipe.getCookingTime() == (type == RecipeType.SMELTING ? 200 : 100) && recipe.getExperience() == 0,
                    "Cooking retains the existing timing and experience");
        }
    }

    private static void assertItemTag(GameTestHelper helper, Item item, String path) {
        helper.assertTrue(new ItemStack(item).is(ItemTags.create(new ResourceLocation("forge", path))), item + " must be in forge:" + path);
    }

    private static List<Metal> metals() {
        return List.of(
                new Metal("lithium", Registration.ORE_LITHIUM.get(), Registration.RAW_LITHIUM.get(), Registration.INGOT_LITHIUM.get()),
                new Metal("manganese", Registration.ORE_MANGANESE.get(), Registration.RAW_MANGANESE.get(), Registration.INGOT_MANGANESE.get()),
                new Metal("platinum", Registration.ORE_PLATINUM.get(), Registration.RAW_PLATINUM.get(), Registration.INGOT_PLATINUM.get()),
                new Metal("silver", Registration.ORE_SILVER.get(), Registration.RAW_SILVER.get(), Registration.INGOT_SILVER.get()));
    }

    private record Metal(String name, Block ore, Item raw, Item ingot) {
    }
}
