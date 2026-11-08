package mcjty.ariente.oregen;

import java.util.function.Supplier;
import mcjty.ariente.Ariente;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WorldGen {

    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, Ariente.MODID);
    public static final Supplier<ArienteOreGen> OVERWORLD_ORES = FEATURES.register("overworld_ores", ArienteOreGen::new);
    public static final Supplier<OverworldOreGroup> OVERWORLD_ORE_GROUP = FEATURES.register("overworld_ore_group", OverworldOreGroup::new);

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}
