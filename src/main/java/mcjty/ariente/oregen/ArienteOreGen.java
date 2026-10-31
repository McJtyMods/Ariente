package mcjty.ariente.oregen;

import mcjty.ariente.config.WorldgenConfiguration;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ArienteOreGen extends Feature<OreConfiguration> {

    public ArienteOreGen() {
        super(OreConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        if (context.level().getLevel().dimension() != Level.OVERWORLD || !WorldgenConfiguration.doWorldGen()) {
            return false;
        }
        // Decode distributions from datapacks; read the world config only at placement time.
        return Feature.ORE.place(context.config(), context.level(), context.chunkGenerator(), context.random(), context.origin());
    }
}
