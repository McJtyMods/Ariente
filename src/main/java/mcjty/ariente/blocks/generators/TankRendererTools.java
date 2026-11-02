package mcjty.ariente.blocks.generators;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class TankRendererTools {

    // Keep every tank's glass and beam in one sorted batch, independently of other entity renderers.
    private static final MultiBufferSource.BufferSource WORLD_TANKS = MultiBufferSource.immediate(new BufferBuilder(4096));

    public static void renderWorldTank(PoseStack stack, ResourceLocation beam, ResourceLocation window, boolean working, long gameTime, Vec3 cameraOffset, int light) {
        renderTank(stack, WORLD_TANKS, beam, window, working, gameTime, cameraOffset, light);
    }

    @SubscribeEvent
    public static void renderStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            WORLD_TANKS.endBatch();
        }
    }

    private static void vertex(VertexConsumer renderer, PoseStack stack, TextureAtlasSprite sprite, float x, float y, float z, float u, float v, int light, Vec3 normal) {
        renderer.vertex(stack.last().pose(), x, y, z)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(sprite.getU(u * 16), sprite.getV(v * 16))
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(stack.last().normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }

    public static void renderTank(PoseStack stack, MultiBufferSource buffer, ResourceLocation beam, ResourceLocation window, boolean working, long gameTime, Vec3 cameraOffset, int light) {
        // Glass and beams must be sorted together rather than drawing chunk glass over the beam.
        VertexConsumer builder = buffer.getBuffer(ArienteRenderType.TANK_CONTENTS);
        TextureAtlasSprite glass = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(window);
        glassFace(builder, stack, glass, .0625f, .0625f, .9375f, .0625f, light, new Vec3(0, 0, -1));
        glassFace(builder, stack, glass, .9375f, .9375f, .0625f, .9375f, light, new Vec3(0, 0, 1));
        glassFace(builder, stack, glass, .0625f, .9375f, .0625f, .0625f, light, new Vec3(-1, 0, 0));
        glassFace(builder, stack, glass, .9375f, .0625f, .9375f, .9375f, light, new Vec3(1, 0, 0));
        if (!working) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(beam);
        Vec3 normal = new Vec3(cameraOffset.x, 0, cameraOffset.z);
        if (normal.lengthSqr() < 1.0E-8) {
            normal = new Vec3(0, 0, 1);
        } else {
            normal = normal.normalize();
        }
        // Fit the broad billboard inside the glass even when viewed diagonally.
        double radius = .42 / Math.max(Math.abs(normal.x), Math.abs(normal.z));
        float dx = (float) (-normal.z * radius);
        float dz = (float) (normal.x * radius);
        // Sample one animated horizontal strip. Adjacent sections have identical
        // edge UVs, so a column has no horizontal seams or repeated vertical tiles.
        float v = ((gameTime / 2) % 10 + .5f) / 10;
        // The lightmap-only tank shader keeps the beam fullbright regardless of item lighting.
        Vec3 glowNormal = new Vec3(0, 1, 0);
        vertex(builder, stack, sprite, .5f - dx, 1, .5f - dz, 0, v, LightTexture.FULL_BRIGHT, glowNormal);
        vertex(builder, stack, sprite, .5f + dx, 1, .5f + dz, 1, v, LightTexture.FULL_BRIGHT, glowNormal);
        vertex(builder, stack, sprite, .5f + dx, 0, .5f + dz, 1, v, LightTexture.FULL_BRIGHT, glowNormal);
        vertex(builder, stack, sprite, .5f - dx, 0, .5f - dz, 0, v, LightTexture.FULL_BRIGHT, glowNormal);
    }

    private static void glassFace(VertexConsumer builder, PoseStack stack, TextureAtlasSprite sprite, float x1, float z1, float x2, float z2, int light, Vec3 normal) {
        vertex(builder, stack, sprite, x1, 1, z1, 0, 0, light, normal);
        vertex(builder, stack, sprite, x2, 1, z2, 1, 0, light, normal);
        vertex(builder, stack, sprite, x2, 0, z2, 1, 1, light, normal);
        vertex(builder, stack, sprite, x1, 0, z1, 0, 1, light, normal);
    }
}
