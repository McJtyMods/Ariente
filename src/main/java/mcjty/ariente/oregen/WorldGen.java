package mcjty.ariente.oregen;

import java.util.function.Supplier;
import mcjty.ariente.Ariente;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WorldGen {

    private static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, Ariente.MODID);
    public static final Supplier<MapCodec<ArienteOreGen>> OVERWORLD_ORES = FEATURES.register("overworld_ores", () -> ArienteOreGen.CODEC);
    public static final Supplier<MapCodec<OverworldOreGroup>> OVERWORLD_ORE_GROUP = FEATURES.register("overworld_ore_group", () -> OverworldOreGroup.CODEC);

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}
