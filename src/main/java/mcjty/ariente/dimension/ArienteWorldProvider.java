package mcjty.ariente.dimension;

import mcjty.ariente.biomes.ArienteBiomeProvider;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks) {
        Vec3d v = world.getSkyColorBody(cameraEntity, partialTicks);
        return new Vec3d(Math.max(1.0f, v.x * 2.5f), v.y, Math.max(1.0f, v.z * 1.2f));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Vec3d getFogColor(float p_76562_1_, float p_76562_2_) {
        Vec3d v = super.getFogColor(p_76562_1_, p_76562_2_);
        return new Vec3d(v.x * .8f, v.y, v.z * .8f);
    }

    @Nullable
    @Override
    public IRenderHandler getSkyRenderer() {
        if (super.getSkyRenderer() == null) {
            setSkyRenderer(new ArienteSkyRenderer());
        }
        return super.getSkyRenderer();
    }
}
