package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.client.ArienteRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class FluxShipRender extends EntityRenderer<FluxShipEntity> {

    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("ariente", "entity/flux_ship");

    public FluxShipRender(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 1.5F;
    }

    @Override
    public void render(FluxShipEntity ship, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, .5, 0);
        pose.mulPose(Axis.YP.rotationDegrees(-ship.getFlightYaw(partialTick)));
        pose.mulPose(Axis.XP.rotationDegrees(ship.getFlightPitch(partialTick)));
        pose.mulPose(Axis.ZP.rotationDegrees(ship.getBank(partialTick)));
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-90));
        Minecraft mc = Minecraft.getInstance();
        int overlay = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(ship.getHurtTime() > 0));
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(ArienteRenderType.SHIP_HULL),
                null, mc.getModelManager().getModel(MODEL), 1, 1, 1, light, overlay);
        pose.popPose();
        FluxShipWeaponRender.render(pose, buffers, light, overlay);
        FluxShipExhaustRender.render(ship, partialTick, pose);
        pose.popPose();
        super.render(ship, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(FluxShipEntity ship) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    public static class Factory implements EntityRendererProvider<FluxShipEntity> {

        @Override
        public EntityRenderer<FluxShipEntity> create(Context context) {
            return new FluxShipRender(context);
        }
    }
}
