package mcjty.ariente.dimension;

import mcjty.ariente.biomes.ArienteBiomeProvider;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;

public class ArienteWorldProvider extends WorldProvider {

    @Override
    @Nonnull
    public DimensionType getDimensionType() {
        return DimensionRegister.dimensionType;
    }

    @Override
    @Nonnull
    public String getSaveFolder() {
        return "ARIENTE";
    }

    @Override
    @Nonnull
    public IChunkGenerator createChunkGenerator() {
        return new ArienteChunkGenerator(world);
    }

    @Override
    protected void init() {
        super.init();
        this.biomeProvider = new ArienteBiomeProvider(world);
    }

}
