package mcjty.ariente.oregen;

import net.minecraft.core.registries.Registries;
import java.util.function.Supplier;
import mcjty.ariente.Ariente;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.feature.Feature;
import mcjty.lib.setup.DeferredRegister;

public class WorldGen {

    private static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, Ariente.MODID);
    public static final Supplier<MapCodec<ArienteOreGen>> OVERWORLD_ORES = FEATURES.register("overworld_ores", () -> ArienteOreGen.CODEC);
    public static final Supplier<MapCodec<OverworldOreGroup>> OVERWORLD_ORE_GROUP = FEATURES.register("overworld_ore_group", () -> OverworldOreGroup.CODEC);

    public static void register() {
        FEATURES.register();
    }
}
