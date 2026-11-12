package mcjty.ariente;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mcjty.ariente.blocks.decorative.PatternBlock;
import mcjty.ariente.blocks.decorative.PatternType;
import mcjty.ariente.cables.CableColor;
import mcjty.ariente.cables.GenericCableBlock;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class LootDropTests {

    @GameTest(template = "empty")
    public static void registeredBlocksHaveLootTables(GameTestHelper helper) {
        // Multipart containers provide their own drops. The other two blocks are internal helpers.
        Set<Block> exceptions = Set.of(Registration.MULTIPART_BLOCK.get(), Registration.INVISIBLE_DOOR.get(), Registration.FACADE.get());
        for (Block block : BuiltInRegistries.BLOCK) {
            if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Ariente.MODID) && !exceptions.contains(block)) {
                LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(block.getLootTable());
                helper.assertTrue(table != LootTable.EMPTY, "Missing loot table for " + BuiltInRegistries.BLOCK.getKey(block));
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivalPickaxeDropsMatchingVariants(GameTestHelper helper) {
        List<Variant> variants = new ArrayList<>();
        Item[] cables = {Registration.NETCABLE_NEGARITE.get(), Registration.NETCABLE_POSIRITE.get(),
                Registration.NETCABLE_COMBINED.get(), Registration.NETCABLE_DATA.get()};
        Item[] connectors = {Registration.CONNECTOR_NEGARITE.get(), Registration.CONNECTOR_POSIRITE.get(),
                Registration.CONNECTOR_COMBINED.get(), Registration.CONNECTOR_DATA.get()};
        for (CableColor color : CableColor.VALUES) {
            variants.add(new Variant(Registration.NETCABLE.get().defaultBlockState().setValue(GenericCableBlock.COLOR, color), cables[color.ordinal()]));
            variants.add(new Variant(Registration.CONNECTOR.get().defaultBlockState().setValue(GenericCableBlock.COLOR, color), connectors[color.ordinal()]));
        }
        for (PatternType type : PatternType.VALUES) {
            variants.add(new Variant(Registration.PATTERN.get().defaultBlockState().setValue(PatternBlock.TYPE, type), Registration.PATTERN_ITEMS.get(type).get()));
        }
        for (Variant variant : variants) {
            assertSurvivalDrop(helper, variant);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void survivalPickaxeDropsPreviouslyMissingBlocks(GameTestHelper helper) {
        List<Block> blocks = List.of(Registration.FLUX_BEAM.get(), Registration.FLUX_BEND_BEAM.get(), Registration.RAMP.get(),
                Registration.SLOPE.get(), Registration.GLASS_FENCE.get(), Registration.BLUE_GLASS_FENCE.get(), Registration.MARBLE_FENCE.get(),
                Registration.TECH_FENCE.get(), Registration.REINFORCED_MARBLE.get(), Registration.FLUX_GLOW.get(), Registration.FLAT_LIGHT.get());
        for (Block block : blocks) {
            assertSurvivalDrop(helper, new Variant(block.defaultBlockState(), block.asItem()));
        }
        helper.succeed();
    }

    private static void assertSurvivalDrop(GameTestHelper helper, Variant variant) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        AABB bounds = new AABB(pos).inflate(1);
        level.getEntitiesOfClass(ItemEntity.class, bounds).forEach(ItemEntity::discard);
        level.setBlockAndUpdate(pos, variant.state());
        BlockState state = level.getBlockState(pos);
        BlockEntity tile = level.getBlockEntity(pos);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        helper.assertTrue(player.hasCorrectToolForDrops(state), "Pickaxe must harvest " + state);
        helper.assertTrue(state.onDestroyedByPlayer(level, pos, player, true, level.getFluidState(pos)), "Survival breaking removes " + state);
        state.getBlock().playerDestroy(level, player, pos, state, tile, pickaxe);
        List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, bounds);
        helper.assertTrue(drops.size() == 1 && drops.getFirst().getItem().is(variant.item()) && drops.getFirst().getItem().getCount() == 1,
                "Breaking " + state + " must drop exactly one " + BuiltInRegistries.ITEM.getKey(variant.item()));
    }

    private record Variant(BlockState state, Item item) {
    }
}
