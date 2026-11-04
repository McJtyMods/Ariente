package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.client.ArienteRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FluxShipWeaponRender {

    public static final ResourceLocation MODEL = new ResourceLocation(Ariente.MODID, "entity/flux_ship_laser");

    @SubscribeEvent
    public static void registerModel(ModelEvent.RegisterAdditional event) {
        event.register(MODEL);
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
