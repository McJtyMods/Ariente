package mcjty.ariente.datagen;

import java.util.List;
import java.util.Set;
import mcjty.ariente.Ariente;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Ariente.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        BlockTags blockTags = new BlockTags(output, event.getLookupProvider());
        generator.addProvider(true, new Recipes.Runner(output, event.getLookupProvider()));
        generator.addProvider(true, new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(provider -> new LootTables(provider), LootContextParamSets.BLOCK)), event.getLookupProvider()));
        generator.addProvider(true, blockTags);
        generator.addProvider(true, new ItemTags(output, event.getLookupProvider(), blockTags));
        generator.addProvider(true, new FluxBeamTextures(output));
        generator.addProvider(true, new ChargerTextures(output));
        generator.addProvider(true, new BlockStates(generator));
        generator.addProvider(true, new Items(generator));
    }
}
