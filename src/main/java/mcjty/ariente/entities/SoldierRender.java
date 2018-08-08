package mcjty.ariente.entities;

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

    public SoldierRender(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelZombie(), 0.5F);
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
            @Override
            protected void initArmor() {
                this.modelLeggings = new ModelZombie(0.5F, true);
                this.modelArmor = new ModelZombie(1.0F, true);
            }
        };
        this.addLayer(layerbipedarmor);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull SoldierEntity entity) {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<SoldierEntity> {

        @Override
        public Render<? super SoldierEntity> createRenderFor(RenderManager manager) {
            return new SoldierRender(manager);
        }

    }

}
