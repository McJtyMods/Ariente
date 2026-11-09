package mcjty.ariente.oregen;

import com.mojang.serialization.MapCodec;
import mcjty.ariente.config.WorldgenConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Retains the aggregate feature key and visits every ore entry in order. */
public record OverworldOreGroup(HolderSet<PlacedFeature> features) implements Feature {

    public static final MapCodec<OverworldOreGroup> CODEC = PlacedFeature.LIST_CODEC
            .fieldOf("features").xmap(OverworldOreGroup::new, OverworldOreGroup::features);

    @Override
    public MapCodec<OverworldOreGroup> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (level.getLevel().dimension() != Level.OVERWORLD || !WorldgenConfiguration.doWorldGen()) return false;
        boolean placed = false;
        var placer = new FeaturePlacer(level, generator);
        for (var feature : features) placed |= placer.place(feature.value(), random, origin);
        return placed;
    }
}
