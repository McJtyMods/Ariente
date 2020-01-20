package mcjty.ariente.entities.soldier;

import mcjty.ariente.Ariente;
import net.minecraft.client.model.ModelZombie;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

public class SoldierRender extends RenderBiped<SoldierEntity> {

    private ResourceLocation mobTexture = new ResourceLocation(Ariente.MODID, "textures/entity/soldier.png");

    public static final Factory FACTORY = new Factory();
    public static final MasterFactory MASTER_FACTORY = new MasterFactory();

    public SoldierRender(RenderManager rendermanagerIn, float scale) {
        super(rendermanagerIn, new ModelZombie(scale-1.0f, false), 0.5F * scale);
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
            @Override
            protected void initArmor() {
                this.modelLeggings = new ModelZombie(0.5F * scale, true);
                this.modelArmor = new ModelZombie(1.0F * scale, true);
            }
        };
        this.addLayer(layerbipedarmor);
    }

    @Override
    protected void preRenderCallback(SoldierEntity entitylivingbaseIn, float partialTickTime) {
        if (entitylivingbaseIn instanceof MasterSoldierEntity) {
            GlStateManager.scale(1.4, 1.4, 1.4);
        }
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull SoldierEntity entity) {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<SoldierEntity> {

        @Override
        public Render<? super SoldierEntity> createRenderFor(RenderManager manager) {
            return new SoldierRender(manager, 1.0f);
        }

    }

    public static class MasterFactory implements IRenderFactory<MasterSoldierEntity> {

        @Override
        public Render<? super MasterSoldierEntity> createRenderFor(RenderManager manager) {
            return new SoldierRender(manager, 1.5f);
        }

    }

}
