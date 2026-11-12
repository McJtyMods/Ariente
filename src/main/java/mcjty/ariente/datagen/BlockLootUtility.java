package mcjty.ariente.datagen;

import java.util.Set;
import java.util.function.Function;
import mcjty.ariente.cables.GenericCableBlock;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public class BlockLootUtility extends BlockLootSubProvider {

    private BlockLootUtility(LootTableSubProvider.Context provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    public void generate() {
    }

    public static LootTable.Builder oreTable(LootTableSubProvider.Context provider, Block block, Item drop) {
        return new BlockLootUtility(provider).createOreDrop(block, drop);
    }

    public static LootTable.Builder slabTable(LootTableSubProvider.Context provider, Block block) {
        return new BlockLootUtility(provider).createSlabItemTable(block);
    }

    public static LootTable.Builder cableTable(LootTableSubProvider.Context provider, Block block, Item negarite, Item posirite, Item combined, Item data) {
        return variantTable(provider, block, GenericCableBlock.COLOR, color -> switch (color) {
            case NEGARITE -> negarite;
            case POSIRITE -> posirite;
            case COMBINED -> combined;
            case DATA -> data;
        });
    }

    public static <T extends Comparable<T>> LootTable.Builder variantTable(LootTableSubProvider.Context provider, Block block, Property<T> property, Function<T, ? extends Item> items) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .when(ExplosionCondition.survivesExplosion());
        for (T value : property.getPossibleValues()) {
            pool.add(LootItem.lootTableItem(items.apply(value))
                    .when(MatchBlock.blockMatches(provider.lookup(Registries.BLOCK), block,
                            StatePropertiesPredicate.Builder.properties().hasProperty(property, property.getName(value)))));
        }
        return LootTable.lootTable().withPool(pool);
    }
}
