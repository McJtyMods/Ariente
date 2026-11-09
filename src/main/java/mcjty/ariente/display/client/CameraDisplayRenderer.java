package mcjty.ariente.display.client;

import net.minecraft.world.item.DyeColor;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.CameraView;
import mcjty.ariente.display.CameraViewClipper;
import mcjty.ariente.display.InstalledModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Cached camera geometry. Orthographic views are projected onto the display plane;
 * perspective views render only this mesh into a small depth-buffered target.
 */
public class CameraDisplayRenderer {

    private static final int MAX_QUADS = 131072;
    private static final int MIN_VIEW_SPAN = 16;
    private static final Map<InstalledModule, Mesh> CACHE = new WeakHashMap<>();
    private static final Comparator<Face> DEPTH_ORDER = Comparator.comparingDouble(face -> face.depth);
    private static final ByteBufferBuilder PERSPECTIVE_MEMORY = new ByteBufferBuilder(262144);

    public static void clearCache() {
        CACHE.clear();
        CameraPerspectiveRenderer.clear();
        LiveCameraRenderer.clear();
    }

    public static void render(DisplayCanvas canvas, InstalledModule module) {
        int count = CameraDisplayModule.cameras(module.stack()).size();
        DisplayModuleRenderers.panel(canvas, module, count == 0 ? "CAMERA" : "CAMERA " + (CameraDisplayModule.cameraIndex(module.stack()) + 1) + "/" + count);
        int width = module.size().width();
        if (module.snapshot().contains("Status")) {
            canvas.text(module.snapshot().getStringOr("Status", ""), 6, 21, 0xffbc7a, .5f, width - 12);
            return;
        }
        Matrix4f pose = canvas.poses().last().pose();
        if (pose.m30() * pose.m30() + pose.m31() * pose.m31() + pose.m32() * pose.m32()
                > CameraDisplayModule.VIEW_DISTANCE * CameraDisplayModule.VIEW_DISTANCE) {
            canvas.text("Move closer for camera", 6, 21, 0x83eaff, .5f, width - 12);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        if (module.snapshot().getBooleanOr("Live", false)) {
            LiveCameraRenderer.render(canvas, module);
            return;
        }
        if (CameraDisplayModule.perspective(module.stack())) {
            CameraPerspectiveRenderer.render(canvas, module);
            return;
        }
        Mesh mesh = CACHE.get(module);
        double now = mc.level.getGameTime() + (double) mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        if (mesh == null || mesh.version != module.snapshotVersion()) {
            mesh = build(module, mesh == null ? now : mesh.startedAt);
            CACHE.put(module, mesh);
        }
        double yaw = CameraDisplayModule.viewYaw(module.stack(), now - mesh.startedAt);
        if (mesh.yaw != yaw || mesh.tilt != CameraDisplayModule.tilt(module.stack())
                || mesh.zoom != CameraDisplayModule.zoom(module.stack()) || mesh.rotating != CameraDisplayModule.autoRotate(module.stack())) {
            mesh.project(module, yaw);
        }
        VertexConsumer vertices = canvas.buffer().getBuffer(DisplayRenderType.CAMERA);
        for (Face face : mesh.visibleFaces) {
            if (face.clippedCount > 0) {
                if (face.clippedCount == 4) {
                    for (int i = 0; i < 4; i++) {
                        clippedVertex(vertices, pose, face.clipped, i);
                    }
                } else {
                    for (int i = 1; i < face.clippedCount - 1; i++) {
                        clippedVertex(vertices, pose, face.clipped, 0);
                        clippedVertex(vertices, pose, face.clipped, i);
                        clippedVertex(vertices, pose, face.clipped, i + 1);
                        clippedVertex(vertices, pose, face.clipped, i + 1);
                    }
                }
                continue;
            }
            for (int i = 0; i < 4; i++) {
                Point p = face.points[i];
                int rgb = face.colors[i];
                vertices.addVertex(pose, face.screen[i * 2], face.screen[i * 2 + 1], .04f).setUv(p.u(), p.v())
                        .setColor(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, 255);
            }
        }
        if (module.snapshot().getLongArray("Blocks").orElseGet(() -> new long[0]).length == 0 && module.snapshot().getListOrEmpty("Entities").isEmpty()) {
            canvas.text("No terrain in range", 6, module.size().height() - 11, 0x83eaff, .45f, width - 12);
        }
    }

    private static void clippedVertex(VertexConsumer vertices, Matrix4f pose, float[] data, int index) {
        int offset = index * CameraViewClipper.STRIDE;
        vertices.addVertex(pose, data[offset], data[offset + 1], .04f).setUv(data[offset + 2], data[offset + 3])
                .setColor((int) data[offset + 4], (int) data[offset + 5], (int) data[offset + 6], 255);
    }

    static CameraPerspectiveRenderer.Geometry perspectiveGeometry(InstalledModule module) {
        Mesh mesh = build(module, 0);
        List<CameraPerspectiveRenderer.Quad> quads = new ArrayList<>();
        for (Face face : mesh.faces) {
            float[] vertices = new float[20];
            for (int i = 0; i < 4; i++) {
                Point p = face.points[i];
                vertices[i * 5] = p.x();
                vertices[i * 5 + 1] = p.y();
                vertices[i * 5 + 2] = p.z();
                vertices[i * 5 + 3] = p.u();
                vertices[i * 5 + 4] = p.v();
            }
            quads.add(new CameraPerspectiveRenderer.Quad(vertices, face.colors.clone(), face.translucent));
        }
        return new CameraPerspectiveRenderer.Geometry(List.copyOf(quads), mesh.center, mesh.radius);
    }

    private static Mesh build(InstalledModule module, double startedAt) {
        Minecraft mc = Minecraft.getInstance();
        CompoundTag data = module.snapshot();
        ListTag paletteTag = data.getListOrEmpty("Palette");
        List<BlockState> palette = new ArrayList<>();
        for (int i = 0; i < Math.min(CameraCapture.MAX_BLOCKS, paletteTag.size()); i++) {
            palette.add(NbtUtils.readBlockState(mc.level.holderLookup(Registries.BLOCK), paletteTag.getCompoundOrEmpty(i)));
        }
        Map<BlockPos, BlockState> scene = new HashMap<>();
        long[] blocks = data.getLongArray("Blocks").orElseGet(() -> new long[0]);
        for (int i = 0; i < Math.min(CameraCapture.MAX_BLOCKS, blocks.length); i++) {
            int id = CameraCapture.paletteId(blocks[i]);
            if (id < palette.size()) {
                scene.put(CameraCapture.offset(blocks[i]), palette.get(id));
            }
        }
        List<Face> faces = new ArrayList<>();
        RandomSource random = RandomSource.create(42);
        Map<BlockState, Boolean> translucentStates = new IdentityHashMap<>();
        List<Map.Entry<BlockPos, BlockState>> ordered = new ArrayList<>(scene.entrySet());
        ordered.sort(Comparator.comparingDouble(entry -> entry.getKey().distSqr(BlockPos.ZERO)));
        int layerDistance = -1, layerStart = 0;
        for (Map.Entry<BlockPos, BlockState> entry : ordered) {
            BlockPos pos = entry.getKey();
            int distance = (int) pos.distSqr(BlockPos.ZERO);
            if (distance != layerDistance) {
                layerDistance = distance;
                layerStart = faces.size();
            }
            BlockState state = entry.getValue();
            BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
            List<BlockStateModelPart> parts = new ArrayList<>();
            model.collectParts(BlockAndTintGetter.EMPTY, pos, state, random, parts);
            boolean translucent = translucentStates.computeIfAbsent(state,
                    key -> (model.materialFlags() & BakedQuad.FLAG_TRANSLUCENT) != 0);
            if (state.getRenderShape() != RenderShape.MODEL) {
                // Fluids and blocks with special renderers use a textured silhouette, never a live renderer.
                if (!state.getFluidState().isEmpty()) {
                    var fluid = state.getFluidState();
                    var visuals = mc.getModelManager().getFluidStateModelSet().get(fluid).fluidTintSource();
                    var sprite = mc.getModelManager().getFluidStateModelSet().get(fluid).stillMaterial().sprite();
                    box(faces, pos.getX() - .5, pos.getY() - .5, pos.getZ() - .5,
                            pos.getX() + .5, pos.getY() - .5 + fluid.getOwnHeight(), pos.getZ() + .5, sprite, visuals == null ? -1 : visuals.color(fluid),
                            sprite.transparency().hasTranslucent());
                } else if (!state.getShape(EmptyBlockGetter.INSTANCE, pos).isEmpty()) {
                    var bounds = state.getShape(EmptyBlockGetter.INSTANCE, pos).bounds();
                    box(faces, pos.getX() + bounds.minX - .5, pos.getY() + bounds.minY - .5, pos.getZ() + bounds.minZ - .5,
                            pos.getX() + bounds.maxX - .5, pos.getY() + bounds.maxY - .5, pos.getZ() + bounds.maxZ - .5,
                            model.particleMaterial().sprite(), 0xffffff, translucent);
                }
                if (faces.size() > MAX_QUADS) {
                    faces.subList(layerStart, faces.size()).clear();
                    break;
                }
                continue;
            }
            for (int side = 0; side <= 6; side++) {
                Direction direction = side == 6 ? null : Direction.values()[side];
                BlockState neighbor = direction == null ? null : scene.get(pos.relative(direction));
                if (neighbor != null && neighbor.isSolidRender()) {
                    continue;
                }
                random.setSeed(state.getSeed(pos));
                for (var part : parts) for (BakedQuad quad : part.getQuads(direction)) {
                    if (faces.size() > MAX_QUADS) {
                        break;
                    }
                    var tintSource = quad.materialInfo().isTinted() ? mc.getBlockColors().getTintSource(state, quad.materialInfo().tintIndex()) : null;
                    int tint = tintSource == null ? -1 : tintSource.color(state);
                    float shade = quad.materialInfo().shade() ? switch (quad.direction()) {
                        case DOWN -> .55f;
                        case NORTH, SOUTH -> .8f;
                        case WEST, EAST -> .65f;
                        case UP -> 1;
                    } : 1;

                    Point[] points = new Point[4];
                    int[] colors = new int[4];
                    for (int i = 0; i < 4; i++) {
                        var position = quad.position(i);
                        long uv = quad.packedUV(i);
                        points[i] = new Point(pos.getX() + position.x() - .5f, pos.getY() + position.y() - .5f,
                                pos.getZ() + position.z() - .5f, UVPair.unpackU(uv), UVPair.unpackV(uv));
                        int baked = quad.bakedColors().color(i);
                        colors[i] = (int) ((baked >> 16 & 255) * (tint >> 16 & 255) / 255f * shade) << 16
                                | (int) ((baked >> 8 & 255) * (tint >> 8 & 255) / 255f * shade) << 8
                                | (int) ((baked & 255) * (tint & 255) / 255f * shade);
                    }
                    faces.add(new Face(points, colors, translucent));
                }
            }
            if (faces.size() > MAX_QUADS) {
                // Never discard arbitrary blocks in hash-map order: trim the entire outer shell.
                faces.subList(layerStart, faces.size()).clear();
                break;
            }
        }
        // Living entities are conspicuous moving markers, independent of client entity tracking.
        var white = mc.getModelManager().getBlockStateModelSet().get(Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState()).particleMaterial().sprite();
        ListTag entities = data.getListOrEmpty("Entities");
        for (int i = 0; i < Math.min(CameraCapture.MAX_ENTITIES, entities.size()); i++) {
            CompoundTag entity = entities.getCompoundOrEmpty(i);
            float x = entity.getFloatOr("X", 0), y = entity.getFloatOr("Y", 0), z = entity.getFloatOr("Z", 0);
            box(faces, x - .2, y - .4, z - .2, x + .2, y + .4, z + .2, white, entity.getIntOr("Color", 0), false);
        }
        box(faces, -.15, -.15, -.15, .15, .15, .15, white, 0x77ffff, false);
        return new Mesh(module.snapshotVersion(), faces, startedAt);
    }

    private static void box(List<Face> faces, double x0, double y0, double z0, double x1, double y1, double z1,
                            TextureAtlasSprite sprite, int rgb, boolean translucent) {
        double[][] corners = {{x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0},
                {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}};
        int[][] sides = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7}, {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
        for (int[] side : sides) {
            if (faces.size() > MAX_QUADS) {
                return;
            }
            Point[] points = new Point[4];
            for (int i = 0; i < 4; i++) {
                double[] corner = corners[side[i]];
                points[i] = new Point((float) corner[0], (float) corner[1], (float) corner[2], i == 0 || i == 3 ? sprite.getU0() : sprite.getU1(),
                        i < 2 ? sprite.getV1() : sprite.getV0());
            }
            faces.add(new Face(points, new int[]{rgb, rgb, rgb, rgb}, translucent));
        }
    }

    private record Point(float x, float y, float z, float u, float v) {
    }

    private static class Face {

        private final Point[] points;
        private final int[] colors;
        private final float[] screen = new float[8];
        private float depth;
        private float[] clipped;
        private int clippedCount;
        private final boolean translucent;

        private Face(Point[] points, int[] colors, boolean translucent) {
            this.points = points;
            this.colors = colors;
            this.translucent = translucent;
        }

        private boolean clip(CameraViewClipper clipper, float right, float bottom) {
            clippedCount = 0;
            int union = 0, intersection = 15;
            for (int i = 0; i < 4; i++) {
                int code = CameraViewClipper.outcode(screen[i * 2], screen[i * 2 + 1], 6, 18, right, bottom);
                union |= code;
                intersection &= code;
            }
            if (union == 0) {
                return true;
            }
            if (intersection != 0) {
                return false;
            }
            clipper.begin();
            for (int i = 0; i < 4; i++) {
                clipper.vertex(screen[i * 2], screen[i * 2 + 1], points[i].u(), points[i].v(), colors[i]);
            }
            clippedCount = clipper.clip(6, 18, right, bottom);
            if (clippedCount < 3) {
                return false;
            }
            int size = clippedCount * CameraViewClipper.STRIDE;
            if (clipped == null || clipped.length < size) {
                clipped = new float[size];
            }
            System.arraycopy(clipper.vertices(), 0, clipped, 0, size);
            return true;
        }
    }

    private static class Mesh {

        private final int version;
        private final List<Face> faces;
        private final List<Face> visibleFaces = new ArrayList<>();
        private final CameraViewClipper clipper = new CameraViewClipper();
        private final double startedAt;
        private final Vec3 center;
        private final float radius;
        private double yaw = Double.NaN;
        private int tilt = -1;
        private int zoom = -1;
        private boolean rotating;

        private Mesh(int version, List<Face> faces, double startedAt) {
            this.version = version;
            this.faces = faces;
            this.startedAt = startedAt;
            float minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;
            for (Face face : faces) {
                for (Point p : face.points) {
                    minX = Math.min(minX, p.x());
                    maxX = Math.max(maxX, p.x());
                    minY = Math.min(minY, p.y());
                    maxY = Math.max(maxY, p.y());
                    minZ = Math.min(minZ, p.z());
                    maxZ = Math.max(maxZ, p.z());
                }
            }
            center = new Vec3((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
            double radiusSquared = 0;
            for (Face face : faces) {
                for (Point p : face.points) {
                    radiusSquared = Math.max(radiusSquared, center.distanceToSqr(p.x(), p.y(), p.z()));
                }
            }
            radius = (float) Math.max(.5, Math.sqrt(radiusSquared));
        }

        private void project(InstalledModule module, double yaw) {
            this.yaw = yaw;
            tilt = CameraDisplayModule.tilt(module.stack());
            zoom = CameraDisplayModule.zoom(module.stack());
            rotating = CameraDisplayModule.autoRotate(module.stack());
            CameraView view = CameraView.of(yaw, tilt);
            float minX = 0, maxX = 0, minY = 0, maxY = 0;
            for (Face face : faces) {
                face.depth = 0;
                for (int i = 0; i < 4; i++) {
                    Point p = face.points[i];
                    float x = view.screenX(p.x(), p.y(), p.z()), y = view.screenY(p.x(), p.y(), p.z());
                    face.screen[i * 2] = x;
                    face.screen[i * 2 + 1] = y;
                    face.depth += view.depth(p.x(), p.y(), p.z()) / 4;
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
            float width = module.size().width() - 12, height = module.size().height() - 24;
            float scale = Math.min(width / MIN_VIEW_SPAN, Math.min(width / Math.max(1, maxX - minX), height / Math.max(1, maxY - minY)));
            float centerX = (minX + maxX) / 2, centerY = (minY + maxY) / 2;
            if (rotating) {
                // A fixed pivot and radius keep automatic rotation from repeatedly zooming or recentering.
                centerX = view.screenX((float) center.x, (float) center.y, (float) center.z);
                centerY = view.screenY((float) center.x, (float) center.y, (float) center.z);
                scale = Math.min(width / MIN_VIEW_SPAN, Math.min(width, height) / (2 * radius));
            }
            scale *= zoom / 100f;
            visibleFaces.clear();
            for (Face face : faces) {
                for (int i = 0; i < 4; i++) {
                    face.screen[i * 2] = 6 + width / 2 + (face.screen[i * 2] - centerX) * scale;
                    face.screen[i * 2 + 1] = 18 + height / 2 + (face.screen[i * 2 + 1] - centerY) * scale;
                }
                face.clippedCount = 0;
                if (zoom == 100 || face.clip(clipper, width + 6, height + 18)) {
                    visibleFaces.add(face);
                }
            }
            visibleFaces.sort(DEPTH_ORDER);
        }
    }
}
