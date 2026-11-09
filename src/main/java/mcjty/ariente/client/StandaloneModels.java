package mcjty.ariente.client;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;

public final class StandaloneModels {

    private static final Map<Identifier, ExtraModelKey<QuadCollection>> KEYS = new HashMap<>();

    private StandaloneModels() {
    }

    public static void register(Identifier id) {
        var key = KEYS.computeIfAbsent(id, name -> ExtraModelKey.create(name::toString));
        ModelLoadingPlugin.register(plugin -> plugin.addModel(key, new SimpleUnbakedExtraModel<>(id, (model, baker) -> model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY))));
    }

    public static QuadCollection get(Identifier id) {
        return Minecraft.getInstance().getModelManager().getModel(KEYS.get(id));
    }

    public static void render(Identifier id, PoseStack poses, RenderBufferSource buffer, RenderType type, int light, int overlay) {
        var quads = get(id);
        var output = buffer.getBuffer(type);
        QuadInstance instance = new QuadInstance();
        instance.setColor(-1);
        instance.setLightCoords(light);
        instance.setOverlayCoords(overlay);
        for (var quad : quads.getAll()) output.putBakedQuad(poses.last(), quad, instance);
    }
}
