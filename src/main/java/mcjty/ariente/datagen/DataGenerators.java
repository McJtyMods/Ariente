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

@EventBusSubscriber(modid = Ariente.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        BlockTags blockTags = new BlockTags(output, event.getLookupProvider(), event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), new Recipes(generator, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(provider -> new LootTables(provider), LootContextParamSets.BLOCK)), event.getLookupProvider()));
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ItemTags(output, event.getLookupProvider(), blockTags, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FluxBeamTextures(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ChargerTextures(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new BlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new Items(generator, event.getExistingFileHelper()));
    }
}
