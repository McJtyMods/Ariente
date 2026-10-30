package mcjty.ariente.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.ArienteSpriteUploader;
import mcjty.lib.client.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class LaserRender extends EntityRenderer<LaserEntity> {

    private static final ResourceLocation[] BEAMS = {
            new ResourceLocation(Ariente.MODID, "negarite_laserbeam1"),
            new ResourceLocation(Ariente.MODID, "negarite_laserbeam2"),
            new ResourceLocation(Ariente.MODID, "negarite_laserbeam3"),
            new ResourceLocation(Ariente.MODID, "negarite_laserbeam4")
    };

    public LaserRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LaserEntity entity, float yaw, float partialTicks, PoseStack poses, MultiBufferSource buffer, int light) {
        Vec3 direction = entity.getDeltaMovement().normalize();
        if (direction.lengthSqr() < .00001) {
            direction = entity.getLookAngle();
        }
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(entity.getPosition(partialTicks));
        var sprite = ArienteSpriteUploader.INSTANCE.getSprite(BEAMS[(entity.tickCount / 2) % BEAMS.length]);
        RenderHelper.drawBeam(poses, buffer.getBuffer(ArienteRenderType.ARIENTE_TRANSLUCENT), sprite,
                direction.scale(-2), Vec3.ZERO, camera, .05f);
        super.render(entity, yaw, partialTicks, poses, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserEntity entity) {
        return ArienteSpriteUploader.ARIENTE_ATLAS;
    }

    public static class Factory implements EntityRendererProvider<LaserEntity> {

        @Override
        public EntityRenderer<LaserEntity> create(Context context) {
            return new LaserRender(context);
        }
    }
}
