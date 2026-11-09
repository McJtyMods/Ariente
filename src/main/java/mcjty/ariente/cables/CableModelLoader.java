package mcjty.ariente.cables;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public record CableModelLoader() implements CustomUnbakedBlockStateModel {
    public static final MapCodec<CableModelLoader> CODEC = MapCodec.unit(CableModelLoader::new);

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        return new GenericCableBakedModel();
    }

    @Override
    public MapCodec<CableModelLoader> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
    }
}
