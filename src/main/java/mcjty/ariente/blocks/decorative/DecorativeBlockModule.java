package mcjty.ariente.blocks.decorative;

import com.google.common.collect.ImmutableMap;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.modules.IModule;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.api.MarbleType;
import mcjty.ariente.api.TechType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;

import static mcjty.ariente.setup.Registration.BLOCKS;
import static mcjty.ariente.setup.Registration.ITEMS;

/**
 * Simple decorative full blocks
 */
public class DecorativeBlockModule implements IModule {

    // BlackTech
    public static final Map<TechType, RegistryObject<BaseBlock>> BLACK_TECH = createBlockMap("blacktech_", TechType.values());
    public static final Map<TechType, RegistryObject<Item>> BLACK_TECH_ITEMS = createItemMap("blacktech_", TechType.values(), BLACK_TECH);

    // Marble Tech
    public static final Map<MarbleType, RegistryObject<BaseBlock>> MARBLE_TECH = createBlockMap("marbletech_", MarbleType.values());
    public static final Map<MarbleType, RegistryObject<Item>> MARBLE_TECH_ITEMS = createItemMap("marbletech_", MarbleType.values(), MARBLE_TECH);

    // Marble Block
    public static final Map<MarbleColor, RegistryObject<BaseBlock>> MARBLE = createBlockMap("marble_", MarbleColor.values());
    public static final Map<MarbleColor, RegistryObject<Item>> MARBLE_ITEMS = createItemMap("marble_", MarbleColor.values(), MARBLE);

    public static final Map<MarbleColor, RegistryObject<BaseBlock>> MARBLE_SMOOTH = createBlockMap("marble_smooth_", MarbleColor.values());
    public static final Map<MarbleColor, RegistryObject<Item>> MARBLE_SMOOTH_ITEMS = createItemMap("marble_smooth_", MarbleColor.values(), MARBLE_SMOOTH);

    public static final Map<MarbleColor, RegistryObject<BaseBlock>> MARBLE_PILAR = createBlockMap("marble_pilar_", MarbleColor.values());
    public static final Map<MarbleColor, RegistryObject<Item>> MARBLE_PILAR_ITEMS = createItemMap("marble_pilar_", MarbleColor.values(), MARBLE_PILAR);

    public static final Map<MarbleColor, RegistryObject<BaseBlock>> MARBLE_BRICKS = createBlockMap("marble_bricks_", MarbleColor.values());
    public static final Map<MarbleColor, RegistryObject<Item>> MARBLE_BRICKS_ITEMS = createItemMap("marble_bricks_", MarbleColor.values(), MARBLE_BRICKS);

    private static <T extends IStringSerializable> Map<T, RegistryObject<BaseBlock>> createBlockMap(String prefix, T[] variants) {
        ImmutableMap.Builder<T, RegistryObject<BaseBlock>> builder = new ImmutableMap.Builder<>();

        for (T variant: variants) {
            builder.put(variant, BLOCKS.register(prefix + variant.getSerializedName(), () -> new BaseBlock(new BlockBuilder())));
        }

        return builder.build();
    }

    private static <T extends IStringSerializable> Map<T, RegistryObject<Item>> createItemMap(String prefix, T[] variants, Map<T, RegistryObject<BaseBlock>> blocks) {
        ImmutableMap.Builder<T, RegistryObject<Item>> builder = new ImmutableMap.Builder<>();

        for (T variant: variants) {
            String name = prefix + variant.getSerializedName();
            builder.put(variant, ITEMS.register(name, () -> new BlockItem(blocks.get(variant).get(), Registration.createStandardProperties())));
        }

        return builder.build();
    }

    @Override
    public void init(FMLCommonSetupEvent event) {
        // no-op
    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        // no-op
    }

    @Override
    public void initConfig() {
        // no-op
    }
}
