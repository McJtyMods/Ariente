package mcjty.ariente.display.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Optional renderer adapters. Only mod-owned names are reflected; no dependency is required to run Ariente. */
final class LiveCameraRenderCompat {

    private static Iris iris;
    private static Embeddium embeddium;
    private static boolean initialized;
    private static String unavailable;

    static String unavailable() {
        if (!initialized) {
            initialized = true;
            try {
                if (ModList.get().isLoaded("oculus") || ModList.get().isLoaded("iris")) {
                    iris = new Iris();
                }
                if (ModList.get().isLoaded("embeddium") || ModList.get().isLoaded("rubidium")) {
                    embeddium = new Embeddium();
                }
            } catch (ReflectiveOperationException | LinkageError exception) {
                unavailable = "Unsupported live camera renderer";
                LogUtils.getLogger().warn("Cannot initialize live camera renderer compatibility", exception);
            }
        }
        if (unavailable != null) {
            return unavailable;
        }
        if (iris != null) {
            Object pipeline = call(iris.current, call(iris.manager, null));
            if (pipeline == null) {
                return "Waiting for the world renderer";
            }
            // Shader packs use different vertex formats and framebuffer layouts. The prototype uses vanilla shading.
            if (!pipeline.getClass().getName().equals("net.irisshaders.iris.pipeline.VanillaRenderingPipeline")) {
                return "Live cameras require shaders off";
            }
        }
        return null;
    }

    static Scope open() {
        if (iris == null) {
            return () -> {};
        }
        LevelRenderer renderer = Minecraft.getInstance().levelRenderer;
        Object previous = get(iris.pipeline, renderer);
        // Oculus clears this field at the end of renderLevel, before Forge's AFTER_LEVEL event.
        // Reuse its existing vanilla pipeline; constructing one would reset global shader settings.
        set(iris.pipeline, renderer, call(iris.current, call(iris.manager, null)));
        return () -> set(iris.pipeline, renderer, previous);
    }

    static Terrain terrain(Frustum frustum, Vec3 eye, int radius) {
        return embeddium == null ? null : new Terrain(embeddium, frustum, eye, radius);
    }

    interface Scope extends AutoCloseable {

        @Override
        void close();
    }

    private static class Iris {

        private final Method manager;
        private final Method current;
        private final Field pipeline;

        private Iris() throws ReflectiveOperationException {
            manager = Class.forName("net.irisshaders.iris.Iris").getMethod("getPipelineManager");
            current = manager.getReturnType().getMethod("getPipelineNullable");
            pipeline = field(LevelRenderer.class, "pipeline");
        }
    }

    private static class Embeddium {

        private static final String ROOT = "me.jellysquid.mods.sodium.client.";
        private final Method instance;
        private final Field manager;
        private final Field sections;
        private final Field lists;
        private final Field rebuilds;
        private final Field entityCulling;
        private final Method region;
        private final Method pending;
        private final Method cancellation;
        private final Method tiles;
        private final Method globalTiles;
        private final Constructor<?> list;
        private final Constructor<?> sorted;
        private final Method add;
        private final Method draw;
        private final Method enter;
        private final Method exit;

        private Embeddium() throws ReflectiveOperationException {
            Class<?> renderer = Class.forName(ROOT + "render.SodiumWorldRenderer");
            Class<?> section = Class.forName(ROOT + "render.chunk.RenderSection");
            Class<?> renderList = Class.forName(ROOT + "render.chunk.lists.ChunkRenderList");
            Class<?> device = Class.forName(ROOT + "gl.device.RenderDevice");
            instance = renderer.getMethod("instance");
            manager = field(renderer, "renderSectionManager");
            sections = field(manager.getType(), "sectionByPosition");
            lists = field(manager.getType(), "renderLists");
            rebuilds = field(manager.getType(), "rebuildLists");
            entityCulling = field(renderer, "useEntityCulling");
            region = section.getMethod("getRegion");
            pending = section.getMethod("getPendingUpdate");
            cancellation = section.getMethod("getBuildCancellationToken");
            tiles = section.getMethod("getCulledBlockEntities");
            globalTiles = section.getMethod("getGlobalBlockEntities");
            list = renderList.getConstructor(region.getReturnType());
            sorted = lists.getType().getDeclaredConstructor(ObjectArrayList.class);
            sorted.setAccessible(true);
            add = renderList.getMethod("add", section);
            draw = renderer.getMethod("drawChunkLayer", RenderType.class, PoseStack.class, double.class, double.class, double.class);
            enter = device.getMethod("enterManagedCode");
            exit = device.getMethod("exitManagedCode");
        }
    }

    static class Terrain implements Scope {

        private final Embeddium access;
        private final Object renderer;
        private final Object manager;
        private final Object previousLists;
        private final Object previousCulling;
        private final List<BlockEntity> tiles = new ArrayList<>();

        private Terrain(Embeddium access, Frustum frustum, Vec3 eye, int radius) {
            this.access = access;
            renderer = call(access.instance, null);
            manager = get(access.manager, renderer);
            previousLists = get(access.lists, manager);
            previousCulling = get(access.entityCulling, renderer);
            Long2ReferenceMap<?> sections = (Long2ReferenceMap<?>) get(access.sections, manager);
            List<Section> visible = new ArrayList<>();
            int distance = radius + 14;
            for (int x = SectionPos.blockToSectionCoord(eye.x - distance); x <= SectionPos.blockToSectionCoord(eye.x + distance); x++) {
                for (int y = SectionPos.blockToSectionCoord(eye.y - distance); y <= SectionPos.blockToSectionCoord(eye.y + distance); y++) {
                    for (int z = SectionPos.blockToSectionCoord(eye.z - distance); z <= SectionPos.blockToSectionCoord(eye.z + distance); z++) {
                        Object section = sections.get(SectionPos.asLong(x, y, z));
                        if (section != null) {
                            AABB box = new AABB(x * 16, y * 16, z * 16, x * 16 + 16, y * 16 + 16, z * 16 + 16);
                            double squaredDistance = box.getCenter().distanceToSqr(eye);
                            if (squaredDistance <= distance * distance && frustum.isVisible(box)) {
                                visible.add(new Section(section, squaredDistance));
                            }
                        }
                    }
                }
            }
            visible.sort(Comparator.comparingDouble(Section::distance));
            Map<Object, Object> regions = new IdentityHashMap<>();
            ObjectArrayList<Object> ordered = new ObjectArrayList<>();
            int queued = 0;
            for (Section entry : visible) {
                Object section = entry.section();
                Object region = call(access.region, section);
                Object list = regions.get(region);
                if (list == null) {
                    list = construct(access.list, region);
                    regions.put(region, list);
                    ordered.add(list);
                }
                call(access.add, list, section);
                addTiles((BlockEntity[]) call(access.tiles, section));
                addTiles((BlockEntity[]) call(access.globalTiles, section));
                if (queued < 2 && call(access.cancellation, section) == null) {
                    Object pending = call(access.pending, section);
                    if (pending != null) {
                        @SuppressWarnings("unchecked")
                        ArrayDeque<Object> queue = (ArrayDeque<Object>) ((Map<?, ?>) get(access.rebuilds, manager)).get(pending);
                        if (queue != null && !queue.contains(section) && queue.size() < 32) {
                            // The normal world pass submits these next frame, within Embeddium's worker budget.
                            queue.add(section);
                            queued++;
                        }
                    }
                }
            }
            // Lists belong to this feed. The player's mutable region lists and visibility graph stay intact.
            set(access.lists, manager, construct(access.sorted, ordered));
            set(access.entityCulling, renderer, false);
        }

        private void addTiles(BlockEntity[] entities) {
            if (entities != null) {
                tiles.addAll(List.of(entities));
            }
        }

        List<BlockEntity> tiles() {
            return tiles;
        }

        void draw(RenderType type, PoseStack poses, Vec3 eye, RenderTarget target) {
            if (type != RenderType.solid() && type != RenderType.translucent()) {
                return;
            }
            type.setupRenderState();
            target.bindWrite(false);
            call(access.enter, null);
            try {
                call(access.draw, renderer, type, poses, eye.x, eye.y, eye.z);
            } finally {
                call(access.exit, null);
                type.clearRenderState();
                target.bindWrite(false);
            }
        }

        @Override
        public void close() {
            set(access.lists, manager, previousLists);
            set(access.entityCulling, renderer, previousCulling);
        }
    }

    private record Section(Object section, double distance) {
    }

    private static Field field(Class<?> type, String name) throws ReflectiveOperationException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static Object get(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot read live camera renderer state", exception);
        }
    }

    private static void set(Field field, Object owner, Object value) {
        try {
            field.set(owner, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot restore live camera renderer state", exception);
        }
    }

    private static Object call(Method method, Object owner, Object... args) {
        try {
            return method.invoke(owner, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot invoke live camera renderer", exception);
        }
    }

    private static Object construct(Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot create live camera render list", exception);
        }
    }
}
