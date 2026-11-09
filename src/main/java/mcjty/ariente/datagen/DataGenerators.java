package mcjty.ariente.datagen;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class DataGenerators implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();
        pack.addProvider(Recipes.Runner::new);
        pack.addProvider((output, lookup) -> new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(LootTables::new, LootContextParamSets.BLOCK)), lookup));
        pack.addProvider(BlockTags::new);
        pack.addProvider((output, lookup) -> new ItemTags(output, lookup, null));
        pack.addProvider((FabricDataGenerator.Pack.Factory<FluxBeamTextures>) FluxBeamTextures::new);
        pack.addProvider((FabricDataGenerator.Pack.Factory<ChargerTextures>) ChargerTextures::new);
        pack.addProvider(BlockStates::new);
        pack.addProvider(Items::new);
    }
}
