package mcjty.ariente.client.textures;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import mcjty.ariente.Ariente;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/** Only the texture traits used by Ariente: quadrant connections, cutout and minimum light. */
@EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class SurfaceTextures extends SimplePreparableReloadListener<Map<ResourceLocation, SurfaceTextures.Rule>> {

    public record Rule(ResourceLocation connectedTexture, int light, boolean cutout, ResourceLocation glowTexture) {
    }

    private static volatile int generation;
    private static volatile Map<ResourceLocation, Rule> rules = Map.of();

    @SubscribeEvent
    public static void register(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SurfaceTextures());
    }

    @SubscribeEvent
    public static void wrapModels(ModelEvent.ModifyBakingResult event) {
        Map<BakedModel, BakedModel> wrappers = new HashMap<>();
        event.getModels().replaceAll((id, model) -> id.id().getNamespace().equals(Ariente.MODID)
                ? wrappers.computeIfAbsent(model, SurfaceModel::new) : model);
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
