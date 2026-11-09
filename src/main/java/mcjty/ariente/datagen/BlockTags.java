package mcjty.ariente.datagen;

import java.util.concurrent.CompletableFuture;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import mcjty.ariente.datagen.CommonTags;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;

public class BlockTags extends IntrinsicHolderTagsProvider<Block> {

    public BlockTags(PackOutput generator, CompletableFuture<HolderLookup.Provider> lookup) {
        super(generator, Registries.BLOCK, lookup, value -> BuiltInRegistries.BLOCK.getResourceKey(value).orElseThrow());
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Include the decorative variants and multipart/machine blocks as well as ores.
        for (var entry : Registration.BLOCKS.getEntries()) {
            Block block = entry.get();
            if (block != Registration.FLUX_GLOW.get() && block != Registration.INVISIBLE_DOOR.get()) {
                addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, block);
            }
        }
        Block[] ores = {Registration.ORE_LITHIUM.get(), Registration.ORE_MANGANESE.get(), Registration.ORE_PLATINUM.get(),
                Registration.ORE_SILVER.get(), Registration.ORE_SILICON.get(), Registration.ORE_NEGARITE.get(), Registration.ORE_POSIRITE.get()};
        addTag(net.minecraft.tags.BlockTags.NEEDS_STONE_TOOL, ores);
        addTag(CommonTags.Blocks.ORE_RATES_SINGULAR, Registration.ORE_LITHIUM.get(), Registration.ORE_MANGANESE.get(),
                Registration.ORE_PLATINUM.get(), Registration.ORE_SILVER.get(), Registration.ORE_SILICON.get());
        addTag(CommonTags.Blocks.ORE_RATES_DENSE, Registration.ORE_NEGARITE.get(), Registration.ORE_POSIRITE.get());
        addTag(CommonTags.Blocks.ORES_IN_GROUND_STONE, ores);

        addTag(CommonTags.Blocks.ORES,
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
