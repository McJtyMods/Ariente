package mcjty.ariente.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class CommonTags {

    public static final class Blocks {

        public static final TagKey<Block> ORES =
                TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores"));

        public static final TagKey<Block> ORES_IN_GROUND_STONE =
                TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores_in_ground/stone"));

        public static final TagKey<Block> ORE_RATES_DENSE =
                TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ore_rates/dense"));

        public static final TagKey<Block> ORE_RATES_SINGULAR =
                TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ore_rates/singular"));
    }

    public static final class Items {

        public static final TagKey<Item> DUSTS =
                TagKey.create(Registries.ITEM, ResourceLocation.parse("c:dusts"));

        public static final TagKey<Item> INGOTS =
                TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ingots"));

        public static final TagKey<Item> ORES =
                TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ores"));

        public static final TagKey<Item> ORES_IN_GROUND_STONE =
                TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ores_in_ground/stone"));

        public static final TagKey<Item> ORE_RATES_DENSE =
                TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ore_rates/dense"));

        public static final TagKey<Item> ORE_RATES_SINGULAR =
                TagKey.create(Registries.ITEM, ResourceLocation.parse("c:ore_rates/singular"));

        public static final TagKey<Item> RAW_MATERIALS =
                TagKey.create(Registries.ITEM, ResourceLocation.parse("c:raw_materials"));
    }
}
