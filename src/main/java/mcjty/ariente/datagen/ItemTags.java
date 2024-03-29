package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.setup.Registration;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Collection;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(DataGenerator generator, BlockTagsProvider blockTags, ExistingFileHelper helper) {
        super(generator, blockTags, Ariente.MODID, helper);
    }

    @Override
    protected void addTags() {
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

        addTag(Registration.TAG_MARBLE, DecorativeBlockModule.MARBLE_ITEMS.values());
    }

    private void addTag(Tag.Named<Item> tag, Collection<RegistryObject<Item>> items) {
        TagsProvider.TagAppender<Item> builder = tag(tag);
        for (RegistryObject<Item> item : items) {
            builder.add(item.get());
        }
    }

    private void addTag(Tag.Named<Item> tag, Item... items) {
        tag(tag)
                .add(items);
    }

    @Override
    public String getName() {
        return "Ariente Item Tags";
    }
}
