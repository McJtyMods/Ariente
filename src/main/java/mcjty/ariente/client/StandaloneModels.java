package mcjty.ariente.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

public final class StandaloneModels {

    private static final Map<Identifier, StandaloneModelKey<QuadCollection>> KEYS = new HashMap<>();

    private StandaloneModels() {
    }

    public static void register(ModelEvent.RegisterStandalone event, Identifier id) {
        var key = KEYS.computeIfAbsent(id, name -> new StandaloneModelKey<>(name::toString));
        event.register(key, SimpleUnbakedStandaloneModel.quadCollection(id));
    }

    public static QuadCollection get(Identifier id) {
        return Minecraft.getInstance().getModelManager().getStandaloneModel(KEYS.get(id));
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
