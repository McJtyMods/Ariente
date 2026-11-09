package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import mcjty.ariente.client.StandaloneModels;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipWeaponRender {

    public static final Identifier MODEL = Identifier.fromNamespaceAndPath(Ariente.MODID, "entity/flux_ship_laser");

    @SubscribeEvent
    public static void registerModel(ModelEvent.RegisterStandalone event) {
        StandaloneModels.register(event, MODEL);
    }

    public static void render(PoseStack pose, RenderBufferSource buffers, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        pose.pushPose();
        pose.translate(FluxShipLaser.MOUNT.x, FluxShipLaser.MOUNT.y, FluxShipLaser.MOUNT.z);
        StandaloneModels.render(MODEL, pose, buffers, ArienteRenderType.SHIP_HULL, light, overlay);
        pose.popPose();
    }
}
