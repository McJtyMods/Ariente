package mcjty.ariente.entities.soldier;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.ZombieModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

public class SoldierRender extends BipedRenderer<SoldierEntity, BipedModel<SoldierEntity>> {

    private ResourceLocation mobTexture = new ResourceLocation(Ariente.MODID, "textures/entity/soldier.png");

    public static final Factory FACTORY = new Factory();
    public static final MasterFactory MASTER_FACTORY = new MasterFactory();

    public SoldierRender(EntityRendererManager rendermanagerIn, float scale) {
        super(rendermanagerIn, new ZombieModel(scale-1.0f, false), 0.5F * scale);
// @todo 1.14
        //        BipedArmorLayer layerbipedarmor = new BipedArmorLayer(this) {
//
//            @Override
//            protected void initArmor() {
//                this.modelLeggings = new ZombieModel(0.5F * scale, true);
//                this.modelArmor = new ZombieModel(1.0F * scale, true);
//            }
//        };
//        this.addLayer(layerbipedarmor);
    }

    @Override
    protected void preRenderCallback(SoldierEntity entitylivingbaseIn, float partialTickTime) {
        if (entitylivingbaseIn instanceof MasterSoldierEntity) {
            GlStateManager.scaled(1.4, 1.4, 1.4);
        }
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull SoldierEntity entity) {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<SoldierEntity> {

        @Override
        public EntityRenderer<? super SoldierEntity> createRenderFor(EntityRendererManager manager) {
            return new SoldierRender(manager, 1.0f);
        }

    }

    public static class MasterFactory implements IRenderFactory<MasterSoldierEntity> {

        @Override
        public EntityRenderer<? super MasterSoldierEntity> createRenderFor(EntityRendererManager manager) {
            return new SoldierRender(manager, 1.5f);
        }

    }

}
