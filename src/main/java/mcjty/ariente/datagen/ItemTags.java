package mcjty.ariente.datagen;

import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BlockStateItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collection;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerTags() {
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

    private void addTag(Tag<Item> tag, Collection<RegistryObject<BlockStateItem>> items) {
        Tag.Builder<Item> builder = getBuilder(tag);
        for (RegistryObject<BlockStateItem> item : items) {
            builder.add(item.get());
        }
        builder.build(tag.getId());
    }

    private void addTag(Tag<Item> tag, Item... items) {
        getBuilder(tag)
                .add(items)
                .build(tag.getId());
    }

    @Override
    public String getName() {
        return "Ariente Item Tags";
    }
}