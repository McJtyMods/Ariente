package mcjty.ariente.datagen;

import java.util.Set;
import java.util.function.Function;
import mcjty.ariente.cables.GenericCableBlock;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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

    public static LootTable.Builder cableTable(Block block, Item negarite, Item posirite, Item combined, Item data) {
        return variantTable(block, GenericCableBlock.COLOR, color -> switch (color) {
            case NEGARITE -> negarite;
            case POSIRITE -> posirite;
            case COMBINED -> combined;
            case DATA -> data;
        });
    }

    public static <T extends Comparable<T>> LootTable.Builder variantTable(Block block, Property<T> property, Function<T, ? extends Item> items) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion());
        for (T value : property.getPossibleValues()) {
            pool.add(LootItem.lootTableItem(items.apply(value))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, property.getName(value)))));
        }
        return LootTable.lootTable().withPool(pool);
    }
}
