package mcjty.ariente.cables;

public class BakedModelLoader {} /* @todo 1.14 implements ICustomModelLoader {

    public static final GenericCableModel GENERIC_MODEL = new GenericCableModel();

    private static final Set<String> NAMES = ImmutableSet.of(
            ConnectorBlock.CONNECTOR,
            NetCableBlock.NETCABLE);

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(Ariente.MODID)) {
            return false;
        }
        return NAMES.contains(modelLocation.getResourcePath());
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        return GENERIC_MODEL;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
*/