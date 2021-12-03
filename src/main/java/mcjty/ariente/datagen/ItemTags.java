package mcjty.ariente.datagen;

import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BlockStateItem;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.data.TagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collection;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(DataGenerator generator, BlockTagsProvider blockTags) {
        super(generator, blockTags, "ariente", null);
    }

    @Override
    protected void registerTags() {
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
        addTag(Registration.TAG_DUSTS_POSIRITE, Registration.DUST_NEGARITE.get());
        addTag(Registration.TAG_DUSTS_SILICON, Registration.DUST_NEGARITE.get());

        addTag(Registration.TAG_MARBLE, Registration.MARBLE_ITEMS.values());
    }

    private void addTag(ITag.INamedTag<Item> tag, Collection<RegistryObject<BlockStateItem>> items) {
        TagsProvider.Builder<Item> builder = getOrCreateBuilder(tag);
        for (RegistryObject<BlockStateItem> item : items) {
            builder.add(item.get());
        }
    }

    private void addTag(ITag.INamedTag<Item> tag, Item... items) {
        getOrCreateBuilder(tag)
                .add(items);
    }

    @Override
    public String getName() {
        return "Ariente Item Tags";
    }
}
