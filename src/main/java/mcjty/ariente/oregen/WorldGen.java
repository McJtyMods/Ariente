package mcjty.ariente.oregen;

import mcjty.ariente.Ariente;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WorldGen {

    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Ariente.MODID);
    public static final RegistryObject<ArienteOreGen> OVERWORLD_ORES = FEATURES.register("overworld_ores", ArienteOreGen::new);

    public static void register() {
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
