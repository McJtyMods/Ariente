package mcjty.ariente.oregen;

import mcjty.ariente.config.WorldgenConfiguration;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.CompositeFeatureConfiguration;

/** Keeps the aggregate feature key available while the biome modifier uses individual ore entries. */
public class OverworldOreGroup extends Feature<CompositeFeatureConfiguration> {

    public OverworldOreGroup() {
        super(CompositeFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<CompositeFeatureConfiguration> context) {
        if (context.level().getLevel().dimension() != Level.OVERWORLD || !WorldgenConfiguration.doWorldGen()) {
            return false;
        }
        boolean placed = false;
        for (var feature : context.config().features()) {
            placed |= feature.value().placeWithBiomeCheck(context.level(), context.chunkGenerator(), context.random(), context.origin());
        }
        return placed;
    }
}
