package mcjty.ariente.datagen;

import mcjty.lib.datagen.ReloadableDataProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class DataGenerators implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();
        var entries = new RegistrySetBuilder();
        entries.add(new MultiRegistryBootstrap() {
            @Override
            public Set<ResourceKey<? extends Registry<?>>> requestedRegistries() {
                return Set.of(Registries.RECIPE, Registries.ADVANCEMENT);
            }

            @Override
            public void run(BootstrapGetter contexts) {
                new Recipes(contexts.get(Registries.RECIPE),
                        contexts.get(Registries.ADVANCEMENT)).buildRecipes();
            }
        });
        entries.add(Registries.LOOT_TABLE, new LootTableProvider(Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(LootTables::new, LootContextParamSets.BLOCK))));
        pack.addProvider((output, lookup) -> new ReloadableDataProvider(output, lookup, entries));
        pack.addProvider(BlockTags::new);
        pack.addProvider((output, lookup) -> new ItemTags(output, lookup, null));
        pack.addProvider((FabricDataGenerator.Pack.Factory<FluxBeamTextures>) FluxBeamTextures::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<ChargerTextures>) ChargerTextures::new);
        pack.addProvider(BlockStates::new);
        pack.addProvider(Items::new);
    }
}
