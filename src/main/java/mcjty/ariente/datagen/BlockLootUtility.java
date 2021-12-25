package mcjty.ariente.datagen;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Proxy to MC built in protected builder methods
 */
public class BlockLootUtility extends BlockLoot {
    public static LootTable.Builder createSlabItemTable(Block block) {
        return BlockLoot.createSlabItemTable(block);
    }

    /**
     * Create a drop table for ore
     *
     * - Silk touch and fortune effects drop
     */
    public static LootTable.Builder createOreDrops(Block block, Item drop, float min, float max) {
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(drop).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }
}