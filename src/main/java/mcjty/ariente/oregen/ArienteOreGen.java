package mcjty.ariente.oregen;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mcjty.ariente.config.WorldgenConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;

public class ArienteOreGen extends AbstractOreFeature {

    public static final MapCodec<ArienteOreGen> CODEC = makeCodec(ArienteOreGen::new);

    public ArienteOreGen(List<BlockReplacement> targets, int size, float discardChanceOnAirExposure) {
        super(targets, size, discardChanceOnAirExposure);
    }

    @Override
    public MapCodec<? extends AbstractOreFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return level.getLevel().dimension() == Level.OVERWORLD && WorldgenConfiguration.doWorldGen()
                && new OreFeature(targetStates, size, discardChanceOnAirExposure).place(level, generator, random, origin);
    }
}
