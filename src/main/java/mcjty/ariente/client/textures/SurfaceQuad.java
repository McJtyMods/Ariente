package mcjty.ariente.client.textures;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.sprite.Material;
import mcjty.lib.client.AbstractDynamicBakedModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** Geometry/UV interpolation is independent of the connection rule and preserves model rotations. */
public class SurfaceQuad {

    private static final Map<BakedQuad, Face> FACES = new ConcurrentHashMap<>();

    public static void clearCache() {
        FACES.clear();
    }

    private record Face(float[][] vertices, float minU, float maxU, float minV, float maxV, Direction u, Direction v) {
    }

    private static Face face(BakedQuad quad) {
        return FACES.computeIfAbsent(quad, SurfaceQuad::createFace);
    }

    private static Face createFace(BakedQuad quad) {
        TextureAtlasSprite sprite = quad.materialInfo().sprite();
        float[][] points = new float[4][5];
        float minU = Float.POSITIVE_INFINITY;
        float minV = Float.POSITIVE_INFINITY;
        float maxU = Float.NEGATIVE_INFINITY;
        float maxV = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            for (int c = 0; c < 3; c++) {
                points[i][c] = quad.position(i).get(c);
            }
            points[i][3] = (UVPair.unpackU(quad.packedUV(i)) - sprite.getU0()) / (sprite.getU1() - sprite.getU0());
            points[i][4] = (UVPair.unpackV(quad.packedUV(i)) - sprite.getV0()) / (sprite.getV1() - sprite.getV0());
            minU = Math.min(minU, points[i][3]);
            maxU = Math.max(maxU, points[i][3]);
            minV = Math.min(minV, points[i][4]);
            maxV = Math.max(maxV, points[i][4]);
        }
        Direction u = Direction.EAST;
        Direction v = Direction.DOWN;
        for (int i = 1; i < 4; i++) {
            float du = points[i][3] - points[0][3];
            float dv = points[i][4] - points[0][4];
            if (Math.abs(du) > .01f && Math.abs(dv) < .01f) {
                u = Direction.getApproximateNearest((points[i][0] - points[0][0]) / du, (points[i][1] - points[0][1]) / du, (points[i][2] - points[0][2]) / du);
            }
            if (Math.abs(dv) > .01f && Math.abs(du) < .01f) {
                v = Direction.getApproximateNearest((points[i][0] - points[0][0]) / dv, (points[i][1] - points[0][1]) / dv, (points[i][2] - points[0][2]) / dv);
            }
        }
        return new Face(points, minU, maxU, minV, maxV, u, v);
    }

    public static int connectionMask(BakedQuad quad, int neighbors) {
        Face face = face(quad);
        int mask = 0;
        int i = 0;
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if (x != 0 || y != 0) {
                    if (SurfaceModel.connected(neighbors, face.u, face.v, x, y)) {
                        mask |= 1 << i;
                    }
                    i++;
                }
            }
        }
        return mask;
    }

    private static boolean connected(int mask, int x, int y) {
        int index = (y + 1) * 3 + x + 1;
        if (index > 4) {
            index--;
        }
        return (mask & (1 << index)) != 0;
    }

    /** Four source quadrants plus a 4x4 supplemental atlas express all edge/corner combinations. */

    public static int tile(int quadrantX, int quadrantY, boolean horizontal, boolean vertical, boolean diagonal) {
        if (!horizontal && !vertical) {
            return -1;
        }
        int x = quadrantX + (!horizontal || vertical && !diagonal ? 2 : 0);
        int y = quadrantY + (!vertical || horizontal && !diagonal ? 2 : 0);
        // A lone edge is independent of the diagonal.
        if (!horizontal) {
            y = quadrantY;
        }
        if (!vertical) {
            x = quadrantX;
        }
        return x + y * 4;
    }

    public static List<BakedQuad> transform(BakedQuad quad, SurfaceTextures.Rule rule, int mask) {
        if (rule.connectedTexture() == null || mask == 0) {
            return List.of(withLight(quad, rule.light()));
        }
        Face face = face(quad);
        if (face.maxU - face.minU < .001f || face.maxV - face.minV < .001f) {
            return List.of(withLight(quad, rule.light()));
        }
        TextureAtlasSprite connectedSprite = AbstractDynamicBakedModel.getTexture(rule.connectedTexture());
        List<BakedQuad> result = new ArrayList<>(4);
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                float u0 = Math.max(face.minU, x * .5f);
                float u1 = Math.min(face.maxU, (x + 1) * .5f);
                float v0 = Math.max(face.minV, y * .5f);
                float v1 = Math.min(face.maxV, (y + 1) * .5f);
                if (u0 >= u1 || v0 >= v1) {
                    continue;
                }
                int tile = tile(x, y, connected(mask, x == 0 ? -1 : 1, 0), connected(mask, 0, y == 0 ? -1 : 1),
                        connected(mask, x == 0 ? -1 : 1, y == 0 ? -1 : 1));
                result.add(quarter(quad, face, tile < 0 ? quad.materialInfo().sprite() : connectedSprite, tile, x, y, u0, u1, v0, v1, rule.light()));
            }
        }
        return result;
    }

    private static BakedQuad quarter(BakedQuad original, Face face, TextureAtlasSprite sprite, int tile, int qx, int qy,
                                     float u0, float u1, float v0, float v1, int light) {
        Vector3fc[] positions = new Vector3fc[4];
        long[] uvs = new long[4];
        for (int i = 0; i < 4; i++) {
            float u = face.vertices[i][3] < (face.minU + face.maxU) / 2 ? u0 : u1;
            float v = face.vertices[i][4] < (face.minV + face.maxV) / 2 ? v0 : v1;
            Vector3f position = new Vector3f();
            for (int c = 0; c < 3; c++) {
                float value = 0;
                for (int j = 0; j < 4; j++) {
                    float wu = face.vertices[j][3] < (face.minU + face.maxU) / 2 ? (face.maxU - u) / (face.maxU - face.minU) : (u - face.minU) / (face.maxU - face.minU);
                    float wv = face.vertices[j][4] < (face.minV + face.maxV) / 2 ? (face.maxV - v) / (face.maxV - face.minV) : (v - face.minV) / (face.maxV - face.minV);
                    value += face.vertices[j][c] * wu * wv;
                }
                position.setComponent(c, value);
            }
            positions[i] = position;
            float su = tile < 0 ? u : ((tile % 4) + (u - qx * .5f) * 2) / 4;
            float sv = tile < 0 ? v : ((tile / 4) + (v - qy * .5f) * 2) / 4;
            uvs[i] = UVPair.pack(sprite.getU(su), sprite.getV(sv));
        }
        return copy(original, positions, uvs, sprite, light);
    }

    public static BakedQuad retexture(BakedQuad original, Identifier texture) {
        TextureAtlasSprite from = original.materialInfo().sprite();
        TextureAtlasSprite to = AbstractDynamicBakedModel.getTexture(texture);
        long[] uvs = new long[4];
        Vector3fc[] positions = new Vector3fc[4];
        for (int i = 0; i < 4; i++) {
            positions[i] = original.position(i);
            float u = (UVPair.unpackU(original.packedUV(i)) - from.getU0()) / (from.getU1() - from.getU0());
            float v = (UVPair.unpackV(original.packedUV(i)) - from.getV0()) / (from.getV1() - from.getV0());
            uvs[i] = UVPair.pack(to.getU(u), to.getV(v));
        }
        return copy(original, positions, uvs, to, original.materialInfo().lightEmission());
    }

    private static BakedQuad withLight(BakedQuad original, int light) {
        if (light == 0) return original;
        return copy(original, new Vector3fc[]{original.position0(), original.position1(), original.position2(), original.position3()},
                new long[]{original.packedUV0(), original.packedUV1(), original.packedUV2(), original.packedUV3()}, original.materialInfo().sprite(), light);
    }

    private static BakedQuad copy(BakedQuad source, Vector3fc[] positions, long[] uvs, TextureAtlasSprite sprite, int light) {
        var info = source.materialInfo();
        var material = BakedQuad.MaterialInfo.of(new Material.Baked(sprite, false), sprite.transparency(),
                info.tintIndex(), light == 0 ? info.shadeDirectionOverride() : Direction.UP, Math.max(light, info.lightEmission()), light == 0 && info.ambientOcclusion());
        return new BakedQuad(positions[0], positions[1], positions[2], positions[3], uvs[0], uvs[1], uvs[2], uvs[3],
                source.direction(), material, source.bakedNormals(), source.bakedColors());
    }
}
