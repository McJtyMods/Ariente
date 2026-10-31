package mcjty.ariente.entities.soldier;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class SoldierRender extends HumanoidMobRenderer<SoldierEntity, HumanoidModel<SoldierEntity>> {

    private ResourceLocation mobTexture = new ResourceLocation(Ariente.MODID, "textures/entity/soldier.png");

    public static final Factory FACTORY = new Factory();
    public static final MasterFactory MASTER_FACTORY = new MasterFactory();

    public SoldierRender(Context rendermanagerIn, float scale) {
        super(rendermanagerIn, new SoldierModel<>(scale-1.0f, false), 0.5F * scale);
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(rendermanagerIn.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(rendermanagerIn.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), rendermanagerIn.getModelManager()));
    }

    @Override
    protected void scale(SoldierEntity entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime) {
        if (entitylivingbaseIn instanceof MasterSoldierEntity) {
            matrixStackIn.scale(1.4F, 1.4F, 1.4F);
        }
    }

    @Override
    @Nonnull
    public ResourceLocation getTextureLocation(@Nonnull SoldierEntity entity) {
        return mobTexture;
    }

    public static class Factory implements EntityRendererProvider<SoldierEntity> {

        @Override
        public EntityRenderer<SoldierEntity> create(Context manager) {
            return new SoldierRender(manager, 1.0f);
        }

    }

    public static class MasterFactory implements EntityRendererProvider<SoldierEntity> {

        @Override
        public EntityRenderer<SoldierEntity> create(Context manager) {
            return new SoldierRender(manager, 1.5f);
        }

    }

}
