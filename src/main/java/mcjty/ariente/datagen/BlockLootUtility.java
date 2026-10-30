package mcjty.ariente.datagen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;

import java.util.Set;

public class BlockLootUtility extends BlockLootSubProvider {

    private BlockLootUtility() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
    }

    public static LootTable.Builder slabTable(Block block) {
        return new BlockLootUtility().createSlabItemTable(block);
    }
}
