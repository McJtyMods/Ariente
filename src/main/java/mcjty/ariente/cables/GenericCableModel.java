package mcjty.ariente.cables;

public class GenericCableModel {} /* @todo 1.14 implements IModel {

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new GenericCableBakedModel(format);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableSet.of(
                new ResourceLocation(Ariente.MODID, "blocks/cables/negarite/connector"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/negarite/normal_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/negarite/normal_corner_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/negarite/normal_cross_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/negarite/normal_end_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/negarite/normal_none_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/negarite/normal_three_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/posirite/connector"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/posirite/normal_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/posirite/normal_corner_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/posirite/normal_cross_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/posirite/normal_end_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/posirite/normal_none_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/posirite/normal_three_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/combined/connector"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/combined/normal_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/combined/normal_corner_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/combined/normal_cross_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/combined/normal_end_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/combined/normal_none_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/combined/normal_three_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/data/connector"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/data/normal_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/data/normal_corner_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/data/normal_cross_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/data/normal_end_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/data/normal_none_netcable"),
                new ResourceLocation(Ariente.MODID, "blocks/cables/data/normal_three_netcable"),

                new ResourceLocation(Ariente.MODID, "blocks/cables/connector_side")
                );
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}
*/