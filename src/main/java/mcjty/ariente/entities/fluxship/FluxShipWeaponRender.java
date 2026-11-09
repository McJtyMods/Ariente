package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import mcjty.ariente.client.StandaloneModels;
import net.minecraft.resources.Identifier;

public class FluxShipWeaponRender {

    public static final Identifier MODEL = Identifier.fromNamespaceAndPath(Ariente.MODID, "entity/flux_ship_laser");

    public static void registerModel() {
        StandaloneModels.register(MODEL);
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        pose.pushPose();
        pose.translate(FluxShipLaser.MOUNT.x, FluxShipLaser.MOUNT.y, FluxShipLaser.MOUNT.z);
        StandaloneModels.render(MODEL, pose, buffers, ArienteRenderType.SHIP_HULL, light, overlay);
        pose.popPose();
    }
}
