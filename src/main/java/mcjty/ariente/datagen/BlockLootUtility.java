package mcjty.ariente.datagen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.data.loot.BlockLoot;

/**
 * Proxy to MC built in protected builder methods
 */
public class BlockLootUtility extends BlockLoot {
    public static LootTable.Builder createSlabItemTable(Block block) {
        return BlockLoot.createSlabItemTable(block);
    }
}