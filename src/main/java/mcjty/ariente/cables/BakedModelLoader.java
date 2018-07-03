package mcjty.ariente.cables;

import com.google.common.collect.ImmutableSet;
import mcjty.ariente.Ariente;
import mcjty.ariente.facade.FacadeBlock;
import mcjty.ariente.facade.FacadeModel;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.util.Set;

public class BakedModelLoader implements ICustomModelLoader {

    public static final GenericCableModel GENERIC_MODEL = new GenericCableModel();
    public static final FacadeModel FACADE_MODEL = new FacadeModel();

    private static final Set<String> NAMES = ImmutableSet.of(
            ConnectorBlock.CONNECTOR,
            NetCableBlock.NETCABLE,
            FacadeBlock.FACADE);

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(Ariente.MODID)) {
            return false;
        }
        return NAMES.contains(modelLocation.getResourcePath());
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        if (FacadeBlock.FACADE.equals(modelLocation.getResourcePath())) {
            return FACADE_MODEL;
        } else {
            return GENERIC_MODEL;
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
