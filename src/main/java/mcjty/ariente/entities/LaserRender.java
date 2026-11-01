package mcjty.ariente.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.ArienteSpriteUploader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LaserRender extends EntityRenderer<LaserEntity> {

    private static final float BEAM_WIDTH = 0.08F;

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
        Vec3 direction = entity.getBeamDirection();
        Vec3 origin = new Vec3(Mth.lerp(partialTicks, entity.xOld, entity.getX()),
                Mth.lerp(partialTicks, entity.yOld, entity.getY()), Mth.lerp(partialTicks, entity.zOld, entity.getZ()));
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(origin);
        var sprite = ArienteSpriteUploader.INSTANCE.getSprite(BEAMS[(entity.tickCount / 2) % BEAMS.length]);
        VertexConsumer vertices = buffer.getBuffer(ArienteRenderType.ARIENTE_ADDITIVE);
        if (camera.cross(direction).lengthSqr() > 1.0E-10D * camera.lengthSqr()) {
            // Keep geometry in projectile-local coordinates. The pipeline supplies the camera transform.
            Vec3 side = camera.cross(direction).normalize().scale(BEAM_WIDTH);
            Vec3 tail = direction.scale(-entity.getBeamLength(origin));
            // U follows the shot; V spans the horizontal texture's glowing cross-section.
            vertex(poses, vertices, tail.add(side), sprite.getU0(), sprite.getV0());
            vertex(poses, vertices, side, sprite.getU1(), sprite.getV0());
            vertex(poses, vertices, side.scale(-1), sprite.getU1(), sprite.getV1());
            vertex(poses, vertices, tail.subtract(side), sprite.getU0(), sprite.getV1());
        } else {
            var activeCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 right = new Vec3(activeCamera.getLeftVector()).scale(BEAM_WIDTH);
            Vec3 up = new Vec3(activeCamera.getUpVector()).scale(BEAM_WIDTH);
            vertex(poses, vertices, right.add(up), sprite.getU(6), sprite.getV(5));
            vertex(poses, vertices, right.scale(-1).add(up), sprite.getU(10), sprite.getV(5));
            vertex(poses, vertices, right.scale(-1).subtract(up), sprite.getU(10), sprite.getV(11));
            vertex(poses, vertices, right.subtract(up), sprite.getU(6), sprite.getV(11));
        }
        super.render(entity, yaw, partialTicks, poses, buffer, light);
    }

    private static void vertex(PoseStack poses, VertexConsumer vertices, Vec3 point, float u, float v) {
        vertices.vertex(poses.last().pose(), (float) point.x, (float) point.y, (float) point.z).color(255, 255, 255, 255)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(poses.last().normal(), 0, 1, 0).endVertex();
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
