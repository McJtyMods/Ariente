package mcjty.ariente;

import com.mojang.authlib.GameProfile;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class MiningTests {

    @GameTest(template = "empty")
    public static void pickaxesWorkOnAllPhysicalBlocks(GameTestHelper helper) {
        ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
        int checked = 0;
        for (var entry : Registration.BLOCKS.getEntries()) {
            Block block = entry.get();
            if (block == Registration.FLUX_GLOW.get() || block == Registration.INVISIBLE_DOOR.get()) {
                continue;
            }
            var state = block.defaultBlockState();
            helper.assertTrue(state.is(BlockTags.MINEABLE_WITH_PICKAXE), "Missing pickaxe tag: " + entry.getId());
            helper.assertTrue(pickaxe.getDestroySpeed(state) == pickaxe.getDestroySpeed(Blocks.COBBLESTONE.defaultBlockState()),
                    "Netherite pickaxe is ineffective on " + entry.getId());
            helper.assertTrue(!state.requiresCorrectToolForDrops() || pickaxe.isCorrectToolForDrops(state),
                    "Netherite pickaxe cannot harvest " + entry.getId());
            checked++;
        }
        Ariente.setup.getLogger().info("MINING CHECK: netherite pickaxe is effective on {} physical blocks", checked);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void decorativeVariantsMineNoSlowerThanCobblestone(GameTestHelper helper) {
        Player player = miner(helper);
        checkVariants(helper, player, DecorativeBlockModule.MARBLE);
        checkVariants(helper, player, DecorativeBlockModule.MARBLE_SMOOTH);
        checkVariants(helper, player, DecorativeBlockModule.MARBLE_PILAR);
        checkVariants(helper, player, DecorativeBlockModule.MARBLE_BRICKS);
        checkVariants(helper, player, DecorativeBlockModule.MARBLE_SLAB);
        checkVariants(helper, player, DecorativeBlockModule.MARBLE_TECH);
        checkVariants(helper, player, DecorativeBlockModule.BLACK_TECH);
        Ariente.setup.getLogger().info("MINING CHECK: decorative variants take at most {} ticks with netherite, matching cobblestone",
                miningTicks(helper, player, Blocks.COBBLESTONE));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aiCoreTakesTenSecondsWithNetherite(GameTestHelper helper) {
        int ticks = miningTicks(helper, miner(helper), Registration.AICORE.get());
        helper.assertTrue(Math.abs(ticks - 200) <= 1, "AI Core should take 10 seconds with unenchanted netherite, got " + ticks + " ticks");
        helper.assertTrue(Registration.AICORE.get().getExplosionResistance() == 800, "AI Core blast resistance must stay at 800");
        Ariente.setup.getLogger().info("MINING CHECK: AI Core takes {} ticks with netherite", ticks);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oresStillRequireStoneOrBetter(GameTestHelper helper) {
        ItemStack wood = new ItemStack(Items.WOODEN_PICKAXE);
        ItemStack stone = new ItemStack(Items.STONE_PICKAXE);
        for (Block ore : new Block[]{Registration.ORE_LITHIUM.get(), Registration.ORE_MANGANESE.get(), Registration.ORE_PLATINUM.get(),
                Registration.ORE_SILVER.get(), Registration.ORE_SILICON.get(), Registration.ORE_NEGARITE.get(), Registration.ORE_POSIRITE.get()}) {
            helper.assertTrue(!wood.isCorrectToolForDrops(ore.defaultBlockState()) && stone.isCorrectToolForDrops(ore.defaultBlockState()),
                    "Ore harvest tiers must stay unchanged: " + ore);
        }
        helper.succeed();
    }

    private static Player miner(GameTestHelper helper) {
        Player player = new Player(helper.getLevel(), BlockPos.ZERO, 0, new GameProfile(UUID.randomUUID(), "mining-test")) {

            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_PICKAXE));
        player.setOnGround(true);
        return player;
    }

    private static int miningTicks(GameTestHelper helper, Player player, Block block) {
        float progress = block.defaultBlockState().getDestroyProgress(player, helper.getLevel(), BlockPos.ZERO);
        return (int) Math.ceil(1d / progress - .0001);
    }

    private static void checkVariants(GameTestHelper helper, Player player, Map<?, ? extends Supplier<? extends Block>> variants) {
        int cobblestone = miningTicks(helper, player, Blocks.COBBLESTONE);
        for (var entry : variants.entrySet()) {
            helper.assertTrue(miningTicks(helper, player, entry.getValue().get()) <= cobblestone,
                    "Decorative block mines slower than cobblestone: " + entry.getValue().get());
        }
    }
}
