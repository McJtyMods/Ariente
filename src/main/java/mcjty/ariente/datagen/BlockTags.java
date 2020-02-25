package mcjty.ariente.datagen;

import mcjty.ariente.setup.Registration;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

public class BlockTags extends BlockTagsProvider {

    public BlockTags(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerTags() {
        addTag(Tags.Blocks.ORES,
                Registration.ORE_POSIRITE.get(),
                Registration.ORE_NEGARITE.get(),
                Registration.ORE_SILICON.get(),
                Registration.ORE_SILVER.get(),
                Registration.ORE_MANGANESE.get(),
                Registration.ORE_PLATINUM.get());
        addTag(Registration.TAG_ORE_POSIRITE, Registration.ORE_POSIRITE.get());
        addTag(Registration.TAG_ORE_NEGARITE, Registration.ORE_NEGARITE.get());
        addTag(Registration.TAG_ORE_SILICON, Registration.ORE_SILICON.get());
        addTag(Registration.TAG_ORE_SILVER, Registration.ORE_SILVER.get());
        addTag(Registration.TAG_ORE_MANGANESE, Registration.ORE_MANGANESE.get());
        addTag(Registration.TAG_ORE_PLATINUM, Registration.ORE_PLATINUM.get());
    }

    private void addTag(Tag<Block> tag, Block... items) {
        getBuilder(tag)
                .add(items)
                .build(tag.getId());
    }

    @Override
    public String getName() {
        return "Ariente Block Tags";
    }
}
