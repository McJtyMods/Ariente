package mcjty.ariente.client.textures;

import net.minecraft.core.registries.BuiltInRegistries;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import mcjty.ariente.Ariente;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

/** Only the texture traits used by Ariente: quadrant connections, cutout and minimum light. */
@EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class SurfaceTextures extends SimplePreparableReloadListener<Map<Identifier, SurfaceTextures.Rule>> {

    public record Rule(Identifier connectedTexture, int light, boolean cutout, Identifier glowTexture) {
    }

    private static volatile int generation;
    private static volatile Map<Identifier, Rule> rules = Map.of();

    @SubscribeEvent
    public static void register(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Ariente.MODID, "surfaces"), new SurfaceTextures());
    }

    @SubscribeEvent
    public static void wrapModels(ModelEvent.ModifyBakingResult event) {
        Map<BlockStateModel, BlockStateModel> wrappers = new HashMap<>();
        event.getBakingResult().blockStateModels().replaceAll((state, model) -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals(Ariente.MODID)
                ? wrappers.computeIfAbsent(model, SurfaceModel::new) : model);
    }

    public static int getGeneration() {
        return generation;
    }

    public static Rule get(Identifier texture) {
        return rules.get(texture);
    }

    private static final Codec<Rule> RULE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("connected_texture").forGetter(rule -> Optional.ofNullable(rule.connectedTexture())),
            Codec.intRange(0, 15).optionalFieldOf("light", 0).forGetter(Rule::light),
            Codec.STRING.optionalFieldOf("layer", "default").forGetter(rule -> rule.cutout() ? "cutout" : "default"),
            Identifier.CODEC.optionalFieldOf("glow_texture").forGetter(rule -> Optional.ofNullable(rule.glowTexture()))
    ).apply(instance, (connected, light, layer, glow) -> {
        if (!layer.equals("default") && !layer.equals("cutout")) throw new IllegalArgumentException("Unsupported Ariente layer: " + layer);
        return new Rule(connected.orElse(null), light, layer.equals("cutout"), glow.orElse(null));
    }));
    private static final MetadataSectionType<Rule> METADATA = new MetadataSectionType<>("ariente", RULE_CODEC);

    @Override
    protected Map<Identifier, Rule> prepare(ResourceManager resources, ProfilerFiller profiler) {
        Map<Identifier, Rule> loaded = new HashMap<>();
        resources.listResources("textures/block", id -> id.getNamespace().equals(Ariente.MODID) && id.getPath().endsWith(".png"))
                .forEach((id, resource) -> {
                    try {
                        resource.metadata().getSection(METADATA).ifPresent(rule -> {
                            String path = id.getPath().substring("textures/".length(), id.getPath().length() - ".png".length());
                            loaded.put(Identifier.fromNamespaceAndPath(id.getNamespace(), path), rule);
                        });
                    } catch (IOException e) {
                        throw new UncheckedIOException("Cannot read texture traits: " + id, e);
                    }
                });
        return Map.copyOf(loaded);
    }

    @Override
    protected void apply(Map<Identifier, Rule> loaded, ResourceManager resources, ProfilerFiller profiler) {
        rules = loaded;
        generation++;
        SurfaceQuad.clearCache();
    }
}
