package mcjty.ariente.cables;

import mcjty.ariente.setup.Registration;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.core.registries.BuiltInRegistries;

public final class CableModelLoader {

    public static void register() {
        ModelLoadingPlugin.register(plugin -> plugin.modifyModelAfterBake().register((model, context) -> {
            if (context.topLevelId() != null) {
                var id = context.topLevelId().id();
                if (id.equals(BuiltInRegistries.BLOCK.getKey(Registration.NETCABLE.get()))
                        || id.equals(BuiltInRegistries.BLOCK.getKey(Registration.CONNECTOR.get()))
                        || id.equals(BuiltInRegistries.BLOCK.getKey(Registration.FACADE.get()))) {
                    return new GenericCableBakedModel();
                }
            }
            return model;
        }));
    }
}
