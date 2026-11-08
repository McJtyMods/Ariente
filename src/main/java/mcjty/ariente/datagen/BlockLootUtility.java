package mcjty.ariente.datagen;

import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

public class BlockLootUtility extends BlockLootSubProvider {

    private BlockLootUtility(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
    }

    public static LootTable.Builder oreTable(HolderLookup.Provider provider, Block block, Item drop) {
        return new BlockLootUtility(provider).createOreDrop(block, drop);
    }

    public static LootTable.Builder slabTable(HolderLookup.Provider provider, Block block) {
        return new BlockLootUtility(provider).createSlabItemTable(block);
    }
}
