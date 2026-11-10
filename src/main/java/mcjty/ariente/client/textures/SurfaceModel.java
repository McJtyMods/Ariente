package mcjty.ariente.client.textures;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import mcjty.lib.client.BakedModelWrapper;
import mcjty.lib.client.ChunkRenderTypeSet;
import mcjty.lib.client.ModelData;
import mcjty.lib.client.ModelProperty;
import mcjty.lib.client.ModelTools;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class SurfaceModel extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<Integer> NEIGHBORS = new ModelProperty<>();
    private volatile int generation = -1;
    private int traits;
    private final Map<QuadKey, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    private record QuadKey(BakedQuad quad, int mask, boolean glow) {
    }

    public SurfaceModel(BakedModel original) {
        super(original);
    }

    private int traits(BlockState state, ModelData data) {
        int current = SurfaceTextures.getGeneration();
        if (generation != current) {
            int flags = 0;
            RandomSource random = RandomSource.create(0);
            for (int side = 0; side < 7; side++) {
                for (BakedQuad quad : ModelTools.quads(originalModel, state, side == 6 ? null : Direction.values()[side], random, data, null)) {
                    var rule = SurfaceTextures.get(quad.getSprite().contents().name());
                    if (rule != null) {
                        flags |= 1;
                        if (rule.connectedTexture() != null) {
                            flags |= 2;
                        }
                        if (rule.cutout()) {
                            flags |= 4;
                        }
                        if (rule.glowTexture() != null) {
                            var glow = SurfaceTextures.get(rule.glowTexture());
                            if (glow != null) {
                                if (glow.connectedTexture() != null) {
                                    flags |= 2;
                                }
                                if (glow.cutout()) {
                                    flags |= 4;
                                }
                            }
                        }
                    }
                }
            }
            traits = flags;
            cache.clear();
            generation = current;
        }
        return traits;
    }

    private static int bit(int x, int y, int z) {
        return 1 << ((x + 1) * 9 + (y + 1) * 3 + z + 1);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        ModelData base = ModelTools.data(originalModel, level, pos, state, data);
        if ((traits(state, base) & 2) == 0) {
            return base;
        }
        int neighbors = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (level.getBlockState(pos.offset(x, y, z)).is(state.getBlock())) {
                        neighbors |= bit(x, y, z);
                    }
                }
            }
        }
        return base.derive().with(NEIGHBORS, neighbors).build();
    }

    public static boolean connected(int neighbors, Direction u, Direction v, int x, int y) {
        int dx = u.getStepX() * x + v.getStepX() * y;
        int dy = u.getStepY() * x + v.getStepY() * y;
        int dz = u.getStepZ() * x + v.getStepZ() * y;
        return (neighbors & bit(dx, dy, dz)) != 0;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        ChunkRenderTypeSet base = ModelTools.renderTypes(originalModel, state, random, data);
        return (traits(state, data) & 4) == 0 ? base : ChunkRenderTypeSet.union(base, ChunkRenderTypeSet.of(RenderType.cutout()));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType layer) {
        if ((traits(state, data) & 1) == 0) {
            return ModelTools.quads(originalModel, state, side, random, data, layer);
        }
        List<BakedQuad> result = new ArrayList<>();
        Integer neighbors = data.get(NEIGHBORS);
        var baseLayers = state == null ? null : ModelTools.renderTypes(originalModel, state, random, data);
        for (BakedQuad quad : ModelTools.quads(originalModel, state, side, random, data, null)) {
            SurfaceTextures.Rule rule = SurfaceTextures.get(quad.getSprite().contents().name());
            boolean baseLayer = layer == null || (rule != null && rule.cutout() ? layer == RenderType.cutout() : baseLayers == null || baseLayers.contains(layer));
            if (baseLayer) {
                if (rule == null) {
                    result.add(quad);
                } else {
                    int mask = rule.connectedTexture() == null || neighbors == null ? 0 : SurfaceQuad.connectionMask(quad, neighbors);
                    result.addAll(cache.computeIfAbsent(new QuadKey(quad, mask, false), key -> SurfaceQuad.transform(quad, rule, mask)));
                }
            }
            if (rule != null && rule.glowTexture() != null) {
                var glow = SurfaceTextures.get(rule.glowTexture());
                if (glow != null && (layer == null || layer == RenderType.cutout())) {
                    int mask = glow.connectedTexture() == null || neighbors == null ? 0 : SurfaceQuad.connectionMask(quad, neighbors);
                    result.addAll(cache.computeIfAbsent(new QuadKey(quad, mask, true), key ->
                            SurfaceQuad.transform(SurfaceQuad.retexture(quad, rule.glowTexture()), glow, mask)));
                }
            }
        }
        return result;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext displayContext, PoseStack poses, boolean leftHand) {
        BakedModel transformed = super.applyTransform(displayContext, poses, leftHand);
        return transformed == originalModel ? this : new SurfaceModel(transformed);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        List<BakedModel> passes = super.getRenderPasses(stack, fabulous);
        if (passes.size() == 1 && passes.get(0) == originalModel) {
            return List.of(this);
        }
        return passes.stream().map(model -> (BakedModel) new SurfaceModel(model)).toList();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }
}
