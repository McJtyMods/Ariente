package mcjty.ariente.blocks.decorative;

import net.minecraft.world.level.block.SoundType;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Supplier;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.api.MarbleType;
import mcjty.ariente.api.TechType;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.modules.IModule;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

import static mcjty.ariente.setup.Registration.BLOCKS;
import static mcjty.ariente.setup.Registration.ITEMS;

/**
 * Simple decorative full and slab blocks
 */
public class DecorativeBlockModule implements IModule {

    // BlackTech
    public static final Map<TechType, Supplier<BaseBlock>> BLACK_TECH = createBlockMap("blacktech_", TechType.values());
    public static final Map<TechType, Supplier<Item>> BLACK_TECH_ITEMS = createItemMap("blacktech_", TechType.values(), BLACK_TECH);

    // Marble Tech
    public static final Map<MarbleType, Supplier<BaseBlock>> MARBLE_TECH = createBlockMap("marbletech_", MarbleType.values());
    public static final Map<MarbleType, Supplier<Item>> MARBLE_TECH_ITEMS = createItemMap("marbletech_", MarbleType.values(), MARBLE_TECH);

    // Marble Block
    public static final Map<MarbleColor, Supplier<BaseBlock>> MARBLE = createBlockMap("marble_", MarbleColor.values());
    public static final Map<MarbleColor, Supplier<Item>> MARBLE_ITEMS = createItemMap("marble_", MarbleColor.values(), MARBLE);

    public static final Map<MarbleColor, Supplier<BaseBlock>> MARBLE_SMOOTH = createBlockMap("marble_smooth_", MarbleColor.values());
    public static final Map<MarbleColor, Supplier<Item>> MARBLE_SMOOTH_ITEMS = createItemMap("marble_smooth_", MarbleColor.values(), MARBLE_SMOOTH);

    public static final Map<MarbleColor, Supplier<BaseBlock>> MARBLE_PILAR = createBlockMap("marble_pilar_", MarbleColor.values());
    public static final Map<MarbleColor, Supplier<Item>> MARBLE_PILAR_ITEMS = createItemMap("marble_pilar_", MarbleColor.values(), MARBLE_PILAR);

    public static final Map<MarbleColor, Supplier<BaseBlock>> MARBLE_BRICKS = createBlockMap("marble_bricks_", MarbleColor.values());
    public static final Map<MarbleColor, Supplier<Item>> MARBLE_BRICKS_ITEMS = createItemMap("marble_bricks_", MarbleColor.values(), MARBLE_BRICKS);

    public static final Map<MarbleColor, Supplier<SlabBlock>> MARBLE_SLAB = createSlabBlockMap("marble_slab_", MarbleColor.values(), Properties.of().mapColor(MapColor.STONE));
    public static final Map<MarbleColor, Supplier<Item>> MARBLE_SLAB_ITEMS = createItemMap("marble_slab_", MarbleColor.values(), MARBLE_SLAB);

    private static <T extends StringRepresentable> Map<T, Supplier<BaseBlock>> createBlockMap(String prefix, T[] variants) {
        ImmutableMap.Builder<T, Supplier<BaseBlock>> builder = new ImmutableMap.Builder<>();

        for (T variant: variants) {
            builder.put(variant, BLOCKS.registerBlock(prefix + variant.getSerializedName(), properties -> new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2).requiresCorrectToolForDrops().sound(SoundType.METAL)))));
        }

        return builder.build();
    }

    private static <T extends StringRepresentable> Map<T, Supplier<SlabBlock>> createSlabBlockMap(String prefix, T[] variants, Properties properties) {
        ImmutableMap.Builder<T, Supplier<SlabBlock>> builder = new ImmutableMap.Builder<>();

        for (T variant: variants) {
            builder.put(variant, BLOCKS.registerBlock(prefix + variant.getSerializedName(), registered -> new SlabBlock(registered.mapColor(MapColor.STONE).strength(2, 4))));
        }

        return builder.build();
    }

    private static <T extends StringRepresentable, B extends Block> Map<T, Supplier<Item>> createItemMap(String prefix, T[] variants, Map<T, Supplier<B>> blocks) {
        ImmutableMap.Builder<T, Supplier<Item>> builder = new ImmutableMap.Builder<>();

        for (T variant: variants) {
            String name = prefix + variant.getSerializedName();
            builder.put(variant, ITEMS.registerItem(name, properties -> new BlockItem(blocks.get(variant).get(), properties.useBlockDescriptionPrefix())));
        }

        return builder.build();
    }

    @Override
    public void init() {
        // no-op
    }

    @Override
    public void initClient() {
        // no-op
    }

    @Override
    public void initConfig() {
        // no-op
    }
}
