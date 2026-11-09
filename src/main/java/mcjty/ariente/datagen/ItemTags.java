package mcjty.ariente.datagen;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(PackOutput generator, CompletableFuture<HolderLookup.Provider> lookup, BlockTagsProvider blockTags) {
        super(generator, lookup, Ariente.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addTag(Tags.Items.ORES,
                Registration.ORE_POSIRITE_ITEM.get(),
                Registration.ORE_NEGARITE_ITEM.get(),
                Registration.ORE_SILICON_ITEM.get(),
                Registration.ORE_SILVER_ITEM.get(),
                Registration.ORE_LITHIUM_ITEM.get(),
                Registration.ORE_MANGANESE_ITEM.get(),
                Registration.ORE_PLATINUM_ITEM.get());
        addTag(Registration.TAG_ORE_POSIRITE_ITEM, Registration.ORE_POSIRITE_ITEM.get());
        addTag(Registration.TAG_ORE_NEGARITE_ITEM, Registration.ORE_NEGARITE_ITEM.get());
        addTag(Registration.TAG_ORE_SILICON_ITEM, Registration.ORE_SILICON_ITEM.get());
        addTag(Registration.TAG_ORE_SILVER_ITEM, Registration.ORE_SILVER_ITEM.get());
        addTag(Registration.TAG_ORE_LITHIUM_ITEM, Registration.ORE_LITHIUM_ITEM.get());
        addTag(Registration.TAG_ORE_MANGANESE_ITEM, Registration.ORE_MANGANESE_ITEM.get());
        addTag(Registration.TAG_ORE_PLATINUM_ITEM, Registration.ORE_PLATINUM_ITEM.get());
        addTag(Tags.Items.ORE_RATES_SINGULAR, Registration.ORE_LITHIUM_ITEM.get(), Registration.ORE_MANGANESE_ITEM.get(),
                Registration.ORE_PLATINUM_ITEM.get(), Registration.ORE_SILVER_ITEM.get(), Registration.ORE_SILICON_ITEM.get());
        addTag(Tags.Items.ORE_RATES_DENSE, Registration.ORE_NEGARITE_ITEM.get(), Registration.ORE_POSIRITE_ITEM.get());
        addTag(Tags.Items.ORES_IN_GROUND_STONE, Registration.ORE_LITHIUM_ITEM.get(), Registration.ORE_MANGANESE_ITEM.get(),
                Registration.ORE_PLATINUM_ITEM.get(), Registration.ORE_SILVER_ITEM.get(), Registration.ORE_SILICON_ITEM.get(),
                Registration.ORE_NEGARITE_ITEM.get(), Registration.ORE_POSIRITE_ITEM.get());

        tag(Tags.Items.RAW_MATERIALS).addTags(Registration.TAG_RAW_LITHIUM, Registration.TAG_RAW_SILVER,
                Registration.TAG_RAW_PLATINUM, Registration.TAG_RAW_MANGANESE);
        addTag(Registration.TAG_RAW_LITHIUM, Registration.RAW_LITHIUM.get());
        addTag(Registration.TAG_RAW_SILVER, Registration.RAW_SILVER.get());
        addTag(Registration.TAG_RAW_PLATINUM, Registration.RAW_PLATINUM.get());
        addTag(Registration.TAG_RAW_MANGANESE, Registration.RAW_MANGANESE.get());

        addTag(Tags.Items.INGOTS,
                Registration.INGOT_LITHIUM.get(),
                Registration.INGOT_SILVER.get(),
                Registration.INGOT_PLATINUM.get(),
                Registration.INGOT_MANGANESE.get());
        addTag(Registration.TAG_INGOT_LITHIUM, Registration.INGOT_LITHIUM.get());
        addTag(Registration.TAG_INGOT_SILVER, Registration.INGOT_SILVER.get());
        addTag(Registration.TAG_INGOT_PLATINUM, Registration.INGOT_PLATINUM.get());
        addTag(Registration.TAG_INGOT_MANGANESE, Registration.INGOT_MANGANESE.get());

        addTag(Tags.Items.DUSTS,
                Registration.DUST_NEGARITE.get(),
                Registration.DUST_POSIRITE.get(),
                Registration.DUST_SILICON.get());
        addTag(Registration.TAG_DUSTS_NEGARITE, Registration.DUST_NEGARITE.get());
        addTag(Registration.TAG_DUSTS_POSIRITE, Registration.DUST_POSIRITE.get());
        addTag(Registration.TAG_DUSTS_SILICON, Registration.DUST_SILICON.get());

        addTag(Registration.TAG_MARBLE, DecorativeBlockModule.MARBLE_ITEMS.values());
    }

    private void addTag(TagKey<Item> tag, Collection<Supplier<Item>> items) {
        var builder = tag(tag);
        for (Supplier<Item> item : items) {
            builder.add(item.get());
        }
    }

    private void addTag(TagKey<Item> tag, Item... items) {
        tag(tag)
                .add(items);
    }

    @Override
    public String getName() {
        return "Ariente Item Tags";
    }
}
