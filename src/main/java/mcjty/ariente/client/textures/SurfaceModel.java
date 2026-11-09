package mcjty.ariente.client.textures;

import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mcjty.ariente.client.SnapshotModelPart;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import mcjty.ariente.client.DelegateModel;

public final class SurfaceModel extends DelegateModel {

    private volatile int generation = -1;
    private final Map<QuadKey, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    private record QuadKey(BakedQuad quad, int mask, boolean glow) {
    }

    public SurfaceModel(BlockStateModel original) {
        super(original);
    }

    private static int bit(int x, int y, int z) {
        return 1 << ((x + 1) * 9 + (y + 1) * 3 + z + 1);
    }

    public static boolean connected(int neighbors, Direction u, Direction v, int x, int y) {
        int dx = u.getStepX() * x + v.getStepX() * y;
        int dy = u.getStepY() * x + v.getStepY() * y;
        int dz = u.getStepZ() * x + v.getStepZ() * y;
        return (neighbors & bit(dx, dy, dz)) != 0;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> output) {
        int current = SurfaceTextures.getGeneration();
        if (generation != current) {
            cache.clear();
            generation = current;
        }
        int neighbors = 0;
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
            if (level.getBlockState(pos.offset(x, y, z)).is(state.getBlock())) neighbors |= bit(x, y, z);
        }
        List<BlockStateModelPart> parts = new ArrayList<>();
        delegate.collectParts(random, parts);
        for (var part : parts) {
            List<List<BakedQuad>> faces = new ArrayList<>(7);
            for (int face = 0; face < 7; face++) {
                List<BakedQuad> quads = new ArrayList<>();
                for (BakedQuad quad : part.getQuads(face == 6 ? null : Direction.values()[face])) {
                    var rule = SurfaceTextures.get(quad.materialInfo().sprite().contents().name());
                    if (rule == null) quads.add(quad);
                    else {
                        int mask = rule.connectedTexture() == null ? 0 : SurfaceQuad.connectionMask(quad, neighbors);
                        quads.addAll(cache.computeIfAbsent(new QuadKey(quad, mask, false), key -> SurfaceQuad.transform(quad, rule, mask)));
                        if (rule.glowTexture() != null) {
                            var glow = SurfaceTextures.get(rule.glowTexture());
                            if (glow != null) {
                                int glowMask = glow.connectedTexture() == null ? 0 : SurfaceQuad.connectionMask(quad, neighbors);
                                quads.addAll(cache.computeIfAbsent(new QuadKey(quad, glowMask, true), key ->
                                        SurfaceQuad.transform(SurfaceQuad.retexture(quad, rule.glowTexture()), glow, glowMask)));
                            }
                        }
                    }
                }
                faces.add(List.copyOf(quads));
            }
            output.add(new SnapshotModelPart(List.copyOf(faces), part.particleMaterial(), part.useAmbientOcclusion()));
        }
    }

    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        // Supplemental material alpha can add translucency to an otherwise opaque base model.
        return ((FabricBlockStateModel) delegate).materialFlags(level, pos, state, random) | BakedQuad.FLAG_TRANSLUCENT;
    }
}
