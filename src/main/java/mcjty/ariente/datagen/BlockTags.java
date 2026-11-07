package mcjty.ariente.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTags extends BlockTagsProvider {

    public BlockTags(PackOutput generator, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper helper) {
        super(generator, lookup, Ariente.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, Registration.DISPLAY_BASE.get());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, Registration.CAMERA.get());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, Registration.SMELTER.get());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, Registration.RECYCLER.get());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, Registration.PURIFIER_START.get(), Registration.PURIFIER_FINISH.get(), Registration.PURIFIER_TUBE.get());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, Registration.CHARGER.get());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE,
                Registration.HOLO_STORAGE_1.get(), Registration.HOLO_STORAGE_2.get(), Registration.HOLO_STORAGE_3.get());
        Block[] ores = {Registration.ORE_LITHIUM.get(), Registration.ORE_MANGANESE.get(), Registration.ORE_PLATINUM.get(),
                Registration.ORE_SILVER.get(), Registration.ORE_SILICON.get(), Registration.ORE_NEGARITE.get(), Registration.ORE_POSIRITE.get()};
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, ores);
        addTag(net.minecraft.tags.BlockTags.NEEDS_STONE_TOOL, ores);
        addTag(Tags.Blocks.ORE_RATES_SINGULAR, Registration.ORE_LITHIUM.get(), Registration.ORE_MANGANESE.get(),
                Registration.ORE_PLATINUM.get(), Registration.ORE_SILVER.get(), Registration.ORE_SILICON.get());
        addTag(Tags.Blocks.ORE_RATES_DENSE, Registration.ORE_NEGARITE.get(), Registration.ORE_POSIRITE.get());
        addTag(Tags.Blocks.ORES_IN_GROUND_STONE, ores);

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

    private void addTag(TagKey<Block> tag, Block... items) {
        tag(tag)
                .add(items);
    }

    @Override
    public String getName() {
        return "Ariente Block Tags";
    }
}
