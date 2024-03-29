package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTags extends BlockTagsProvider {

    public BlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, Ariente.MODID, helper);
    }

    @Override
    protected void addTags() {
        addTag(Tags.Blocks.ORES,
                Registration.ORE_POSIRITE.get(),
                Registration.ORE_NEGARITE.get(),
                Registration.ORE_SILICON.get(),
                Registration.ORE_SILVER.get(),
                Registration.ORE_LITHIUM.get(),
                Registration.ORE_MANGANESE.get(),
                Registration.ORE_PLATINUM.get());
        addTag(Registration.TAG_ORE_POSIRITE, Registration.ORE_POSIRITE.get());
        addTag(Registration.TAG_ORE_NEGARITE, Registration.ORE_NEGARITE.get());
        addTag(Registration.TAG_ORE_SILICON, Registration.ORE_SILICON.get());
        addTag(Registration.TAG_ORE_SILVER, Registration.ORE_SILVER.get());
        addTag(Registration.TAG_ORE_LITHIUM, Registration.ORE_LITHIUM.get());
        addTag(Registration.TAG_ORE_MANGANESE, Registration.ORE_MANGANESE.get());
        addTag(Registration.TAG_ORE_PLATINUM, Registration.ORE_PLATINUM.get());
    }

    private void addTag(Tag.Named<Block> tag, Block... items) {
        tag(tag)
                .add(items);
    }

    @Override
    public String getName() {
        return "Ariente Block Tags";
    }
}
