package mcjty.ariente.client;

import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

public record SnapshotModelPart(List<List<BakedQuad>> faces, Material.Baked particleMaterial, boolean useAmbientOcclusion) implements BlockStateModelPart {

    public static SnapshotModelPart unculled(List<BakedQuad> quads, Material.Baked particle) {
        return new SnapshotModelPart(List.of(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.copyOf(quads)), particle, true);
    }

    @Override
    public List<BakedQuad> getQuads(Direction side) {
        return faces.get(side == null ? 6 : side.ordinal());
    }

    @Override
    public int materialFlags() {
        int flags = 0;
        for (var face : faces) for (var quad : face) flags |= quad.materialInfo().flags();
        return flags;
    }
}
