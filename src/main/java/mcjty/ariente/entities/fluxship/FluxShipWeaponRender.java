package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import mcjty.ariente.client.StandaloneModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class FluxShipWeaponRender {

    public static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "entity/flux_ship_laser");

    public static void registerModel() {
        StandaloneModels.register(MODEL);
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        pose.pushPose();
        pose.translate(FluxShipLaser.MOUNT.x, FluxShipLaser.MOUNT.y, FluxShipLaser.MOUNT.z);
        mc.getBlockRenderer().getModelRenderer().renderModel(pose.last(), buffers.getBuffer(ArienteRenderType.SHIP_HULL),
                null, mc.getModelManager().getModel(MODEL), 1, 1, 1, light, overlay);
        pose.popPose();
    }
}
