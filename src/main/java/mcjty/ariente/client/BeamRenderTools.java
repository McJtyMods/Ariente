package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

/** Camera-facing entity geometry shared by the elevator and Warper. */
public class BeamRenderTools {

    public static void drawBeam(PoseStack poses, VertexConsumer vertices, TextureAtlasSprite sprite,
                                Vec3 start, Vec3 end, Vec3 camera, float width) {
        Vec3 half = start.subtract(camera).cross(end.subtract(start)).normalize().scale(width);
        Vec3 first = start.add(half);
        Vec3 second = end.add(half);
        Vec3 third = end.subtract(half);
        Vec3 fourth = start.subtract(half);
        Vec3 normal = second.subtract(first).cross(third.subtract(first)).normalize();
        vertex(poses, vertices, first, normal, sprite.getU0(), sprite.getV0());
        vertex(poses, vertices, second, normal, sprite.getU1(), sprite.getV0());
        vertex(poses, vertices, third, normal, sprite.getU1(), sprite.getV1());
        vertex(poses, vertices, fourth, normal, sprite.getU0(), sprite.getV1());
    }

    private static void vertex(PoseStack poses, VertexConsumer vertices, Vec3 pos, Vec3 normal, float u, float v) {
        vertices.vertex(poses.last().pose(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color(255, 255, 255, 255)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(poses.last().normal(), (float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }
}
