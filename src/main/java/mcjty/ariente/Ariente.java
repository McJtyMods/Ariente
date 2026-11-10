package mcjty.ariente;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import mcjty.ariente.api.IArienteMod;
import mcjty.ariente.api.IArienteSystem;
import mcjty.ariente.apiimpl.ArienteSystem;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.config.Config;
import mcjty.ariente.loot.DungeonLootModifier;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.ModSetup;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IHoloGuiHandler;
import mcjty.lib.modules.Modules;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.fml.config.ModConfig;

public class Ariente implements IArienteMod, ModInitializer {

    public static final String MODID = "ariente";
    private final Modules modules = new Modules();
    public static final ModSetup setup = new ModSetup();
    public static Ariente instance;
    public static IHoloGuiHandler guiHandler;
    public static final IArienteSystem arienteSystem = new ArienteSystem();

    @Override
    public void onInitialize() {
        instance = this;
        modules.register(new DecorativeBlockModule());
        NeoForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        NeoForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        Registration.register();
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse("ariente:overworld_marble")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse("ariente:overworld_lithium")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse("ariente:overworld_manganese")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse("ariente:overworld_platinum")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse("ariente:overworld_silicon")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse("ariente:overworld_silver")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse("ariente:overworld_negarite")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse("ariente:overworld_posirite")));
        BlueprintRecipeRegistry.register();

        setup.init();
        modules.init();
        ArienteMessages.registerMessages();
        ForgeEventHandlers.register();
        DungeonLootModifier.register();
    }

    @Override
    public IArienteSystem getSystem() {
        return arienteSystem;
    }
}
