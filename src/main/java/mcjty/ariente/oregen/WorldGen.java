package mcjty.ariente.oregen;

import net.minecraft.core.registries.Registries;
import java.util.function.Supplier;
import mcjty.ariente.Ariente;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import mcjty.lib.setup.DeferredRegister;

public class WorldGen {

    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Ariente.MODID);
    public static final Supplier<ArienteOreGen> OVERWORLD_ORES = FEATURES.register("overworld_ores", ArienteOreGen::new);
    public static final Supplier<OverworldOreGroup> OVERWORLD_ORE_GROUP = FEATURES.register("overworld_ore_group", OverworldOreGroup::new);

    public static void register() {
        FEATURES.register();
    }
}
