package mcjty.ariente;

import mcjty.ariente.loot.DungeonLootModifier;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import mcjty.ariente.api.IArienteMod;
import mcjty.ariente.api.IArienteSystem;
import mcjty.ariente.apiimpl.ArienteSystem;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.config.Config;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.ModSetup;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IHoloGuiHandler;
import mcjty.lib.modules.Modules;
import net.fabricmc.api.ModInitializer;
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
        ConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, Config.SERVER_CONFIG);
        Registration.register();
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse("ariente:overworld_marble")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse("ariente:overworld_lithium")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse("ariente:overworld_manganese")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse("ariente:overworld_platinum")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse("ariente:overworld_silicon")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse("ariente:overworld_silver")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse("ariente:overworld_negarite")));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse("ariente:overworld_posirite")));
        BlueprintRecipeRegistry.register();
        RecipeSynchronization.synchronizeRecipeSerializer(BlueprintRecipeRegistry.PURIFYING_SERIALIZER.get());
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
