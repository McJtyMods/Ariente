package mcjty.ariente.oregen;

import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class ArienteOreGen extends Feature<NoneFeatureConfiguration> {

    public ArienteOreGen() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (context.level().getLevel().dimension() != Level.OVERWORLD || !WorldgenConfiguration.doWorldGen()) {
            return false;
        }
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        ore(level, context, random, origin, DecorativeBlockModule.MARBLE.get(MarbleColor.WHITE).get().defaultBlockState(), 10, 20, 2, 16, 70);
        ore(level, context, random, origin, Registration.ORE_LITHIUM.get().defaultBlockState(), 2, 3, 2, 8, 20);
        ore(level, context, random, origin, Registration.ORE_MANGANESE.get().defaultBlockState(), 3, 6, 3, 20, 50);
        ore(level, context, random, origin, Registration.ORE_PLATINUM.get().defaultBlockState(), 3, 6, 4, 30, 80);
        ore(level, context, random, origin, Registration.ORE_SILICON.get().defaultBlockState(), 3, 7, 4, 30, 60);
        ore(level, context, random, origin, Registration.ORE_SILVER.get().defaultBlockState(), 4, 8, 4, 10, 20);
        ore(level, context, random, origin, Registration.ORE_NEGARITE.get().defaultBlockState(), 4, 8, 5, 6, 30);
        ore(level, context, random, origin, Registration.ORE_POSIRITE.get().defaultBlockState(), 4, 8, 5, 6, 30);
        return true;
    }

    private void ore(WorldGenLevel level, FeaturePlaceContext<?> context, RandomSource random, BlockPos origin,
                     BlockState state, int minSize, int maxSize, int attempts, int minY, int maxY) {
        OreConfiguration config = new OreConfiguration(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                state, minSize + random.nextInt(maxSize - minSize + 1));
        for (int i = 0; i < attempts; i++) {
            BlockPos pos = new BlockPos(origin.getX() + random.nextInt(16), minY + random.nextInt(maxY - minY),
                    origin.getZ() + random.nextInt(16));
            Feature.ORE.place(config, level, context.chunkGenerator(), random, pos);
        }
    }
}
