package mcjty.ariente.datagen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.data.loot.BlockLootTables;

/**
 * Proxy to MC built in protected builder methods
 */
public class BlockLootUtility extends BlockLootTables {
    public static LootTable.Builder createSlabItemTable(Block block) {
        return BlockLootTables.createSlabItemTable(block);
    }
}