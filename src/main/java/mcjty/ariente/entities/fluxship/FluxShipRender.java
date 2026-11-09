package mcjty.ariente.entities.fluxship;

import net.minecraft.client.renderer.texture.TextureAtlas;

import mcjty.ariente.client.ExtractedEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.client.ArienteRenderType;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import mcjty.ariente.client.StandaloneModels;
import net.minecraft.resources.Identifier;

public class FluxShipRender extends ExtractedEntityRenderer<FluxShipEntity> {

    public static final Identifier MODEL = Identifier.fromNamespaceAndPath("ariente", "entity/flux_ship");

    public FluxShipRender(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 1.5F;
    }

    @Override
    public void render(FluxShipEntity ship, float yaw, float partialTick, PoseStack pose, RenderBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, .5, 0);
        pose.rotate(Axis.YP.rotationDegrees(-ship.getFlightYaw(partialTick)));
        pose.rotate(Axis.XP.rotationDegrees(ship.getFlightPitch(partialTick)));
        pose.rotate(Axis.ZP.rotationDegrees(ship.getBank(partialTick)));
        pose.pushPose();
        pose.rotate(Axis.YP.rotationDegrees(-90));
        Minecraft mc = Minecraft.getInstance();
        int overlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(ship.getHurtTime() > 0));
        StandaloneModels.render(MODEL, pose, buffers, ArienteRenderType.SHIP_HULL, light, overlay);
        pose.popPose();
        FluxShipWeaponRender.render(pose, buffers, light, overlay);
        FluxShipExhaustRender.render(ship, partialTick, pose, buffers);
        pose.popPose();
        super.render(ship, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public Identifier getTextureLocation(FluxShipEntity ship) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public static class Factory implements EntityRendererProvider<FluxShipEntity> {

        @Override
        public EntityRenderer<FluxShipEntity, ?> create(Context context) {
            return new FluxShipRender(context);
        }
    }
}
