package mcjty.ariente.entities;

import mcjty.ariente.client.ExtractedEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.ArienteSpriteUploader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LaserRender extends ExtractedEntityRenderer<LaserEntity> {

    private static final float BEAM_WIDTH = 0.08F;

    private static final Identifier[] BEAMS = {
            Identifier.fromNamespaceAndPath(Ariente.MODID, "negarite_laserbeam1"),
            Identifier.fromNamespaceAndPath(Ariente.MODID, "negarite_laserbeam2"),
            Identifier.fromNamespaceAndPath(Ariente.MODID, "negarite_laserbeam3"),
            Identifier.fromNamespaceAndPath(Ariente.MODID, "negarite_laserbeam4")
    };

    public LaserRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LaserEntity entity, float yaw, float partialTicks, PoseStack poses, RenderBufferSource buffer, int light) {
        Vec3 direction = entity.getBeamDirection();
        Vec3 origin = new Vec3(Mth.lerp(partialTicks, entity.xOld, entity.getX()),
                Mth.lerp(partialTicks, entity.yOld, entity.getY()), Mth.lerp(partialTicks, entity.zOld, entity.getZ()));
        Vec3 camera = Minecraft.getInstance().gameRenderer.mainCamera().position().subtract(origin);
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
            var activeCamera = Minecraft.getInstance().gameRenderer.mainCamera();
            Vec3 right = new Vec3(activeCamera.leftVector()).scale(BEAM_WIDTH);
            Vec3 up = new Vec3(activeCamera.upVector()).scale(BEAM_WIDTH);
            vertex(poses, vertices, right.add(up), sprite.getU(6 / 16f), sprite.getV(5 / 16f));
            vertex(poses, vertices, right.scale(-1).add(up), sprite.getU(10 / 16f), sprite.getV(5 / 16f));
            vertex(poses, vertices, right.scale(-1).subtract(up), sprite.getU(10 / 16f), sprite.getV(11 / 16f));
            vertex(poses, vertices, right.subtract(up), sprite.getU(6 / 16f), sprite.getV(11 / 16f));
        }
        super.render(entity, yaw, partialTicks, poses, buffer, light);
    }

    private static void vertex(PoseStack poses, VertexConsumer vertices, Vec3 point, float u, float v) {
        vertices.addVertex(poses.last().pose(), (float) point.x, (float) point.y, (float) point.z).setColor(255, 255, 255, 255)
                .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(poses.last(), 0, 1, 0);
    }

    @Override
    public Identifier getTextureLocation(LaserEntity entity) {
        return ArienteSpriteUploader.ARIENTE_ATLAS;
    }

    public static class Factory implements EntityRendererProvider<LaserEntity> {

        @Override
        public EntityRenderer<LaserEntity, ?> create(Context context) {
            return new LaserRender(context);
        }
    }
}
