package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public final class StandaloneModels {

    public static void register(ResourceLocation id) {
        ModelLoadingPlugin.register(plugin -> plugin.addModels(id));
    }

    public static BakedModel get(ResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getModel(id);
    }

    public static void render(
            ResourceLocation id,
            PoseStack poses,
            MultiBufferSource buffer,
            RenderType type,
            int light,
            int overlay) {
        Minecraft.getInstance()
                .getBlockRenderer()
                .getModelRenderer()
                .renderModel(
                        poses.last(),
                        buffer.getBuffer(type),
                        null,
                        get(id),
                        1,
                        1,
                        1,
                        light,
                        overlay);
    }
}
