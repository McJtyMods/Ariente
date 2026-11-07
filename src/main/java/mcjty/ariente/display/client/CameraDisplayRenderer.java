package mcjty.ariente.display.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.CameraView;
import mcjty.ariente.display.InstalledModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Cached orthographic geometry, composited in depth order onto the display plane.
 * No second world render, framebuffer, chunk meshes or recursive block-entity renderers are needed.
 */
public class CameraDisplayRenderer {

    private static final int MAX_QUADS = 131072;
    private static final int MIN_VIEW_SPAN = 16;
    private static final Map<InstalledModule, Mesh> CACHE = new WeakHashMap<>();
    private static final Comparator<Face> DEPTH_ORDER = Comparator.comparingDouble(face -> face.depth);

    public static void clearCache() {
        CACHE.clear();
    }

    public static void render(DisplayCanvas canvas, InstalledModule module) {
        int count = CameraDisplayModule.cameras(module.stack()).size();
        DisplayModuleRenderers.panel(canvas, module, count == 0 ? "CAMERA" : "CAMERA " + (CameraDisplayModule.cameraIndex(module.stack()) + 1) + "/" + count);
        int width = module.size().width();
        if (module.snapshot().contains("Status")) {
            canvas.text(module.snapshot().getString("Status"), 6, 21, 0xffbc7a, .5f, width - 12);
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
        Mesh mesh = CACHE.get(module);
        double now = mc.level.getGameTime() + (double) mc.getFrameTime();
        if (mesh == null || mesh.version != module.snapshotVersion()) {
            mesh = build(module, mesh == null ? now : mesh.startedAt);
            CACHE.put(module, mesh);
        }
        double yaw = CameraDisplayModule.viewYaw(module.stack(), now - mesh.startedAt);
        if (mesh.yaw != yaw) {
            mesh.project(module, yaw);
        }
        VertexConsumer vertices = canvas.buffer().getBuffer(DisplayRenderType.CAMERA);
        for (Face face : mesh.faces) {
            for (int i = 0; i < 4; i++) {
                Point p = face.points[i];
                int rgb = face.colors[i];
                vertices.vertex(pose, face.screen[i * 2], face.screen[i * 2 + 1], .04f).uv(p.u(), p.v())
                        .color(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, 255).endVertex();
            }
        }
        if (module.snapshot().getLongArray("Blocks").length == 0 && module.snapshot().getList("Entities", Tag.TAG_COMPOUND).isEmpty()) {
            canvas.text("No terrain in range", 6, module.size().height() - 11, 0x83eaff, .45f, width - 12);
        }
    }

    private static Mesh build(InstalledModule module, double startedAt) {
        Minecraft mc = Minecraft.getInstance();
        CompoundTag data = module.snapshot();
        ListTag paletteTag = data.getList("Palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int i = 0; i < Math.min(CameraCapture.MAX_BLOCKS, paletteTag.size()); i++) {
            palette.add(NbtUtils.readBlockState(mc.level.holderLookup(Registries.BLOCK), paletteTag.getCompound(i)));
        }
        Map<BlockPos, BlockState> scene = new HashMap<>();
        long[] blocks = data.getLongArray("Blocks");
        for (int i = 0; i < Math.min(CameraCapture.MAX_BLOCKS, blocks.length); i++) {
            int id = CameraCapture.paletteId(blocks[i]);
            if (id < palette.size()) {
                scene.put(CameraCapture.offset(blocks[i]), palette.get(id));
            }
        }
        List<Face> faces = new ArrayList<>();
        RandomSource random = RandomSource.create(42);
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
            BakedModel model = mc.getBlockRenderer().getBlockModel(state);
            if (state.getRenderShape() != RenderShape.MODEL) {
                // Fluids and blocks with special renderers use a textured silhouette, never a live renderer.
                if (!state.getFluidState().isEmpty()) {
                    var fluid = state.getFluidState();
                    var visuals = IClientFluidTypeExtensions.of(fluid);
                    var sprite = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(visuals.getStillTexture());
                    box(faces, pos.getX() - .5, pos.getY() - .5, pos.getZ() - .5,
                            pos.getX() + .5, pos.getY() - .5 + fluid.getOwnHeight(), pos.getZ() + .5, sprite, visuals.getTintColor());
                } else if (!state.getShape(EmptyBlockGetter.INSTANCE, pos).isEmpty()) {
                    var bounds = state.getShape(EmptyBlockGetter.INSTANCE, pos).bounds();
                    box(faces, pos.getX() + bounds.minX - .5, pos.getY() + bounds.minY - .5, pos.getZ() + bounds.minZ - .5,
                            pos.getX() + bounds.maxX - .5, pos.getY() + bounds.maxY - .5, pos.getZ() + bounds.maxZ - .5,
                            model.getParticleIcon(ModelData.EMPTY), 0xffffff);
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
                if (neighbor != null && neighbor.isSolidRender(EmptyBlockGetter.INSTANCE, pos.relative(direction))) {
                    continue;
                }
                random.setSeed(state.getSeed(pos));
                for (BakedQuad quad : model.getQuads(state, direction, random, ModelData.EMPTY, null)) {
                    if (faces.size() > MAX_QUADS) {
                        break;
                    }
                    int tint = quad.isTinted() ? mc.getBlockColors().getColor(state, (BlockAndTintGetter) null, null, quad.getTintIndex()) : 0xffffff;
                    float shade = quad.isShade() ? switch (quad.getDirection()) {
                        case DOWN -> .55f;
                        case NORTH, SOUTH -> .8f;
                        case WEST, EAST -> .65f;
                        case UP -> 1;
                    } : 1;
                    int[] source = quad.getVertices();
                    int stride = source.length / 4;
                    Point[] points = new Point[4];
                    int[] colors = new int[4];
                    for (int i = 0; i < 4; i++) {
                        int index = i * stride;
                        points[i] = new Point(pos.getX() + Float.intBitsToFloat(source[index]) - .5f,
                                pos.getY() + Float.intBitsToFloat(source[index + 1]) - .5f,
                                pos.getZ() + Float.intBitsToFloat(source[index + 2]) - .5f,
                                Float.intBitsToFloat(source[index + 4]), Float.intBitsToFloat(source[index + 5]));
                        int baked = source[index + 3];
                        colors[i] = (int) ((baked & 255) * (tint >> 16 & 255) / 255f * shade) << 16
                                | (int) ((baked >> 8 & 255) * (tint >> 8 & 255) / 255f * shade) << 8
                                | (int) ((baked >> 16 & 255) * (tint & 255) / 255f * shade);
                    }
                    faces.add(new Face(points, colors));
                }
            }
            if (faces.size() > MAX_QUADS) {
                // Never discard arbitrary blocks in hash-map order: trim the entire outer shell.
                faces.subList(layerStart, faces.size()).clear();
                break;
            }
        }
        // Living entities are conspicuous moving markers, independent of client entity tracking.
        var white = mc.getBlockRenderer().getBlockModel(Blocks.WHITE_CONCRETE.defaultBlockState()).getParticleIcon();
        ListTag entities = data.getList("Entities", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(CameraCapture.MAX_ENTITIES, entities.size()); i++) {
            CompoundTag entity = entities.getCompound(i);
            float x = entity.getFloat("X"), y = entity.getFloat("Y"), z = entity.getFloat("Z");
            box(faces, x - .2, y - .4, z - .2, x + .2, y + .4, z + .2, white, entity.getInt("Color"));
        }
        box(faces, -.15, -.15, -.15, .15, .15, .15, white, 0x77ffff);
        return new Mesh(module.snapshotVersion(), faces, startedAt);
    }

    private static void box(List<Face> faces, double x0, double y0, double z0, double x1, double y1, double z1,
                            TextureAtlasSprite sprite, int rgb) {
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
            faces.add(new Face(points, new int[]{rgb, rgb, rgb, rgb}));
        }
    }

    private record Point(float x, float y, float z, float u, float v) {
    }

    private static class Face {

        private final Point[] points;
        private final int[] colors;
        private final float[] screen = new float[8];
        private float depth;

        private Face(Point[] points, int[] colors) {
            this.points = points;
            this.colors = colors;
        }
    }

    private static class Mesh {

        private final int version;
        private final List<Face> faces;
        private final double startedAt;
        private final Vec3 center;
        private final float radius;
        private double yaw = Double.NaN;

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
            CameraView view = CameraView.of(yaw, CameraDisplayModule.tilt(module.stack()));
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
            if (CameraDisplayModule.autoRotate(module.stack())) {
                // A fixed pivot and radius keep automatic rotation from repeatedly zooming or recentering.
                centerX = view.screenX((float) center.x, (float) center.y, (float) center.z);
                centerY = view.screenY((float) center.x, (float) center.y, (float) center.z);
                scale = Math.min(width / MIN_VIEW_SPAN, Math.min(width, height) / (2 * radius));
            }
            for (Face face : faces) {
                for (int i = 0; i < 4; i++) {
                    face.screen[i * 2] = 6 + width / 2 + (face.screen[i * 2] - centerX) * scale;
                    face.screen[i * 2 + 1] = 18 + height / 2 + (face.screen[i * 2 + 1] - centerY) * scale;
                }
            }
            faces.sort(DEPTH_ORDER);
        }
    }
}
