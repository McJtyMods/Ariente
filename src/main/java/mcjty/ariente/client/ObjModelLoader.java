package mcjty.ariente.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import mcjty.lib.client.AbstractDynamicBakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class ObjModelLoader {

    private record Vertex(Vector3f position, Vector2f uv) {}

    private record Face(List<Vertex> vertices, String texture) {}

    private record Prepared(JsonObject json, List<Face> faces) {}

    public static void register() {
        PreparableModelLoadingPlugin.register(
                (resources, executor) ->
                        CompletableFuture.supplyAsync(
                                () -> {
                                    Map<ResourceLocation, Prepared> models = new HashMap<>();
                                    Map<ResourceLocation, JsonObject> definitions = new HashMap<>();
                                    resources
                                            .listResources(
                                                    "models",
                                                    id ->
                                                            id.getNamespace().equals("ariente")
                                                                    && id.getPath()
                                                                            .endsWith(".json"))
                                            .forEach(
                                                    (id, resource) -> {
                                                        try (var reader = resource.openAsReader()) {
                                                            JsonObject json =
                                                                    JsonParser.parseReader(reader)
                                                                            .getAsJsonObject();
                                                            ResourceLocation modelId =
                                                                    ResourceLocation
                                                                            .fromNamespaceAndPath(
                                                                                    id
                                                                                            .getNamespace(),
                                                                                    id.getPath()
                                                                                            .substring(
                                                                                                    7,
                                                                                                    id.getPath()
                                                                                                                    .length()
                                                                                                            - 5));
                                                            definitions.put(modelId, json);
                                                            boolean obj =
                                                                    json.has("model")
                                                                            && (json.has("loader")
                                                                                            && json.get(
                                                                                                            "loader")
                                                                                                    .getAsString()
                                                                                                    .endsWith(
                                                                                                            ":obj")
                                                                                    || json.has(
                                                                                                    "fabric:type")
                                                                                            && json.get(
                                                                                                            "fabric:type")
                                                                                                    .getAsString()
                                                                                                    .equals(
                                                                                                            "ariente:obj"));
                                                            if (obj) {
                                                                List<Face> faces =
                                                                        read(
                                                                                resources,
                                                                                ResourceLocation
                                                                                        .parse(
                                                                                                json.get(
                                                                                                                "model")
                                                                                                        .getAsString()),
                                                                                json.has("flip_v")
                                                                                        && json.get(
                                                                                                        "flip_v")
                                                                                                .getAsBoolean());
                                                                models.put(
                                                                        ResourceLocation
                                                                                .fromNamespaceAndPath(
                                                                                        id
                                                                                                .getNamespace(),
                                                                                        id.getPath()
                                                                                                .substring(
                                                                                                        7,
                                                                                                        id.getPath()
                                                                                                                        .length()
                                                                                                                - 5)),
                                                                        new Prepared(json, faces));
                                                            }
                                                        } catch (IOException error) {
                                                            throw new UncheckedIOException(error);
                                                        }
                                                    });
                                    for (ResourceLocation id : definitions.keySet())
                                        inheritModel(id, definitions, models, new HashSet<>());
                                    return models;
                                },
                                executor),
                (models, plugin) ->
                        plugin.resolveModel()
                                .register(
                                        context -> {
                                            Prepared prepared = models.get(context.id());
                                            return prepared == null ? null : new ObjModel(prepared);
                                        }));
    }

    private static Prepared inheritModel(
            ResourceLocation id,
            Map<ResourceLocation, JsonObject> definitions,
            Map<ResourceLocation, Prepared> models,
            Set<ResourceLocation> visiting) {
        if (models.containsKey(id)) return models.get(id);
        JsonObject json = definitions.get(id);
        if (json == null || !json.has("parent") || !visiting.add(id)) return null;
        Prepared parent =
                inheritModel(
                        ResourceLocation.parse(json.get("parent").getAsString()),
                        definitions,
                        models,
                        visiting);
        visiting.remove(id);
        if (parent == null) return null;
        Prepared inherited = new Prepared(json, parent.faces());
        models.put(id, inherited);
        return inherited;
    }

    private static Map<String, Either<Material, String>> textures(JsonObject json) {
        Map<String, Either<Material, String>> textures = new HashMap<>();
        if (json.has("textures")) {
            json.getAsJsonObject("textures")
                    .entrySet()
                    .forEach(
                            entry -> {
                                String texture = entry.getValue().getAsString();
                                textures.put(
                                        entry.getKey(),
                                        texture.startsWith("#")
                                                ? Either.right(texture.substring(1))
                                                : Either.left(
                                                        new Material(
                                                                TextureAtlas.LOCATION_BLOCKS,
                                                                ResourceLocation.parse(texture))));
                            });
        }
        return textures;
    }

    private static final class ObjModel extends BlockModel {

        private final Prepared prepared;

        private ObjModel(Prepared prepared) {
            super(
                    prepared.json().has("parent")
                            ? ResourceLocation.parse(prepared.json().get("parent").getAsString())
                            : null,
                    List.of(),
                    textures(prepared.json()),
                    prepared.json().has("ambientocclusion")
                            ? prepared.json().get("ambientocclusion").getAsBoolean()
                            : null,
                    null,
                    BlockModel.fromString(prepared.json().toString()).getTransforms(),
                    List.of());
            this.prepared = prepared;
        }

        @Override
        public BakedModel bake(
                ModelBaker baker,
                Function<Material, TextureAtlasSprite> sprites,
                ModelState state) {
            List<BakedQuad> quads = new ArrayList<>();
            TextureAtlasSprite particle = sprites.apply(getMaterial("particle"));
            for (Face face : prepared.faces()) {
                String name = face.texture();
                Material material =
                        name.contains(":")
                                ? new Material(
                                        TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(name))
                                : getMaterial(name);
                TextureAtlasSprite sprite = sprites.apply(material);
                if (particle.contents().name().getPath().equals("missingno")) particle = sprite;
                int[] data = new int[32];
                Vector3f[] positions = new Vector3f[4];
                for (int i = 0; i < 4; i++) {
                    Vertex vertex = face.vertices().get(i);
                    positions[i] = new Vector3f(vertex.position()).sub(.5f, .5f, .5f);
                    state.getRotation().getMatrix().transformPosition(positions[i]);
                    positions[i].add(.5f, .5f, .5f);
                    int offset = i * 8;
                    data[offset] = Float.floatToRawIntBits(positions[i].x);
                    data[offset + 1] = Float.floatToRawIntBits(positions[i].y);
                    data[offset + 2] = Float.floatToRawIntBits(positions[i].z);
                    data[offset + 3] = -1;
                    data[offset + 4] = Float.floatToRawIntBits(sprite.getU(vertex.uv().x));
                    data[offset + 5] = Float.floatToRawIntBits(sprite.getV(vertex.uv().y));
                }
                Vector3f normal =
                        new Vector3f(positions[1])
                                .sub(positions[0])
                                .cross(new Vector3f(positions[2]).sub(positions[0]))
                                .normalize();
                quads.add(
                        new BakedQuad(
                                data,
                                -1,
                                Direction.getNearest(normal.x, normal.y, normal.z),
                                sprite,
                                true));
            }
            return new ObjBaked(quads, particle, getTransforms());
        }
    }

    private static final class ObjBaked extends AbstractDynamicBakedModel {

        private final List<BakedQuad> quads;
        private final TextureAtlasSprite particle;
        private final ItemTransforms transforms;

        private ObjBaked(
                List<BakedQuad> quads, TextureAtlasSprite particle, ItemTransforms transforms) {
            this.quads = List.copyOf(quads);
            this.particle = particle;
            this.transforms = transforms;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random) {
            return side == null ? quads : List.of();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return particle;
        }

        @Override
        public ItemTransforms getTransforms() {
            return transforms;
        }
    }

    private static List<Face> read(ResourceManager resources, ResourceLocation id, boolean flipV) {
        var positions = new ArrayList<Vector3f>();
        var uv = new ArrayList<Vector2f>();
        var materials = new HashMap<String, String>();
        var faces = new ArrayList<Face>();
        String material = "";
        try (var reader = resources.getResourceOrThrow(id).openAsReader()) {
            for (String line; (line = reader.readLine()) != null; ) {
                String[] parts = line.strip().split("\\s+");
                switch (parts[0]) {
                    case "v" ->
                            positions.add(
                                    new Vector3f(
                                            Float.parseFloat(parts[1]),
                                            Float.parseFloat(parts[2]),
                                            Float.parseFloat(parts[3])));
                    case "vt" ->
                            uv.add(
                                    new Vector2f(
                                            Float.parseFloat(parts[1]),
                                            flipV
                                                    ? 1 - Float.parseFloat(parts[2])
                                                    : Float.parseFloat(parts[2])));
                    case "mtllib" ->
                            readMaterials(
                                    resources,
                                    ResourceLocation.fromNamespaceAndPath(
                                            id.getNamespace(),
                                            id.getPath()
                                                            .substring(
                                                                    0,
                                                                    id.getPath().lastIndexOf('/')
                                                                            + 1)
                                                    + parts[1]),
                                    materials);
                    case "usemtl" -> material = parts[1];
                    case "f" -> {
                        var vertices = new ArrayList<Vertex>();
                        for (int i = 1; i < parts.length; i++) {
                            var indices = parts[i].split("/");
                            var position = positions.get(index(indices[0], positions.size()));
                            var tex =
                                    indices.length > 1 && !indices[1].isEmpty()
                                            ? uv.get(index(indices[1], uv.size()))
                                            : new Vector2f();
                            vertices.add(new Vertex(position, tex));
                        }
                        String texture = materials.getOrDefault(material, "particle");
                        if (vertices.size() == 4)
                            faces.add(new Face(List.copyOf(vertices), texture));
                        else
                            for (int i = 1; i + 1 < vertices.size(); i++)
                                faces.add(
                                        new Face(
                                                List.of(
                                                        vertices.get(0),
                                                        vertices.get(i),
                                                        vertices.get(i + 1),
                                                        vertices.get(i + 1)),
                                                texture));
                    }
                    default -> {}
                }
            }
        } catch (IOException | RuntimeException exception) {
            throw new JsonParseException("Cannot load OBJ model " + id, exception);
        }
        return List.copyOf(faces);
    }

    private static int index(String text, int size) {
        int value = Integer.parseInt(text);
        return value < 0 ? size + value : value - 1;
    }

    private static void readMaterials(
            ResourceManager resources, ResourceLocation id, Map<String, String> materials)
            throws IOException {
        String material = "";
        try (var reader = resources.getResourceOrThrow(id).openAsReader()) {
            for (String line; (line = reader.readLine()) != null; ) {
                String[] parts = line.strip().split("\\s+");
                if (parts[0].equals("newmtl")) material = parts[1];
                else if (parts[0].equals("map_Kd")) materials.put(material, parts[1]);
            }
        }
    }
}
