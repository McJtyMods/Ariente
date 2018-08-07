package mcjty.ariente.entities;

import mcjty.ariente.Ariente;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

public class RenderSoldier extends RenderLiving<EntitySoldier> {

    private ResourceLocation mobTexture = new ResourceLocation(Ariente.MODID, "textures/entity/soldier.png");

    public static final Factory FACTORY = new Factory();

    public RenderSoldier(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelZombie(), 0.5F);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntitySoldier entity) {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<EntitySoldier> {

        @Override
        public Render<? super EntitySoldier> createRenderFor(RenderManager manager) {
            return new RenderSoldier(manager);
        }

    }

}
