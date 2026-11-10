package mcjty.ariente.client.textures;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import mcjty.ariente.Ariente;
import mcjty.ariente.cables.GenericCableBakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Only the texture traits used by Ariente: quadrant connections, cutout and minimum light. */
public class SurfaceTextures extends SimplePreparableReloadListener<Map<ResourceLocation, SurfaceTextures.Rule>> implements IdentifiableResourceReloadListener {

    public record Rule(ResourceLocation connectedTexture, int light, boolean cutout, ResourceLocation glowTexture) {
    }

    private static volatile int generation;
    private static volatile Map<ResourceLocation, Rule> rules = Map.of();

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SurfaceTextures());
        ModelLoadingPlugin.register(plugin ->
                plugin.modifyModelAfterBake().register((model, context) ->
                        context.topLevelId() != null && context.topLevelId().id().getNamespace().equals(Ariente.MODID)
                                && !(model instanceof GenericCableBakedModel)
                                ? new SurfaceModel(model) : model));
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(Ariente.MODID, "surfaces");
    }

    public static int getGeneration() {
        return generation;
    }

    public static Rule get(ResourceLocation texture) {
        return rules.get(texture);
    }

    private static final MetadataSectionSerializer<Rule> METADATA = new MetadataSectionSerializer<>() {

        @Override
        public String getMetadataSectionName() {
            return "ariente";
        }

        @Override
        public Rule fromJson(JsonObject data) {
            ResourceLocation connected = data.has("connected_texture") ? ResourceLocation.parse(data.get("connected_texture").getAsString()) : null;
            ResourceLocation glow = data.has("glow_texture") ? ResourceLocation.parse(data.get("glow_texture").getAsString()) : null;
            int light = data.has("light") ? data.get("light").getAsInt() : 0;
            if (light < 0 || light > 15) {
                throw new IllegalArgumentException("Texture light must be between 0 and 15");
            }
            String layer = data.has("layer") ? data.get("layer").getAsString() : "default";
            if (!layer.equals("default") && !layer.equals("cutout")) {
                throw new IllegalArgumentException("Unsupported Ariente texture layer: " + layer);
            }
            return new Rule(connected, light, layer.equals("cutout"), glow);
        }
    };

    @Override
    protected Map<ResourceLocation, Rule> prepare(ResourceManager resources, ProfilerFiller profiler) {
        Map<ResourceLocation, Rule> loaded = new HashMap<>();
        resources.listResources("textures/block", id -> id.getNamespace().equals(Ariente.MODID) && id.getPath().endsWith(".png"))
                .forEach((id, resource) -> {
                    try {
                        resource.metadata().getSection(METADATA).ifPresent(rule -> {
                            String path = id.getPath().substring("textures/".length(), id.getPath().length() - ".png".length());
                            loaded.put(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), path), rule);
                        });
                    } catch (IOException e) {
                        throw new UncheckedIOException("Cannot read texture traits: " + id, e);
                    }
                });
        return Map.copyOf(loaded);
    }

    @Override
    protected void apply(Map<ResourceLocation, Rule> loaded, ResourceManager resources, ProfilerFiller profiler) {
        rules = loaded;
        generation++;
        SurfaceQuad.clearCache();
    }
}
