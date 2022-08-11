package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.setup.Registration;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.Iterator;

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

        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, DecorativeBlockModule.BLACK_TECH.values());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, DecorativeBlockModule.MARBLE_TECH.values());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, DecorativeBlockModule.MARBLE.values());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, DecorativeBlockModule.MARBLE_SMOOTH.values());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, DecorativeBlockModule.MARBLE_PILAR.values());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, DecorativeBlockModule.MARBLE_BRICKS.values());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE, DecorativeBlockModule.MARBLE_SLAB.values());
        addTag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE,
                Registration.PATTERN.get(),
                Registration.ORE_LITHIUM.get(),
                Registration.ORE_MANGANESE.get(),
                Registration.ORE_SILICON.get(),
                Registration.ORE_SILVER.get(),
                Registration.ORE_PLATINUM.get(),
                Registration.ORE_POSIRITE.get(),
                Registration.ORE_NEGARITE.get(),
                Registration.FLUX_BEAM.get(),
                Registration.FLUX_BEND_BEAM.get(),
                Registration.SENSOR_ITEM_NODE.get(),
                Registration.INPUT_ITEM_NODE.get(),
                Registration.OUTPUT_ITEM_NODE.get(),
                Registration.ROUND_ROBIN_NODE.get(),
                Registration.FIELD_MARKER.get(),
                Registration.RAMP.get(),
                Registration.SLOPE.get(),
                Registration.GLASS_FENCE.get(),
                Registration.BLUE_GLASS_FENCE.get(),
                Registration.MARBLE_FENCE.get(),
                Registration.TECH_FENCE.get(),
                Registration.REINFORCED_MARBLE.get(),
                Registration.FLUX_GLOW.get(),
                Registration.POWER_COMBINER.get(),
                Registration.NEGARITE_GENERATOR.get(),
                Registration.POSIRITE_GENERATOR.get(),
                Registration.NEGARITE_TANK.get(),
                Registration.POSIRITE_TANK.get(),
                Registration.DOOR_MARKER.get(),
                Registration.AICORE.get(),
                Registration.ALARM.get(),
                Registration.CONSTRUCTOR.get(),
                Registration.AUTO_CONSTRUCTOR.get(),
                Registration.BLUEPRINT_STORAGE.get(),
                Registration.AUTOMATION_FIELD.get(),
                Registration.STORAGE.get(),
                Registration.ELEVATOR.get(),
                Registration.LEVEL_MARKER.get(),
                Registration.INVISIBLE_DOOR.get(),
                Registration.FORCEFIELD.get(),
                Registration.WARPER.get(),
                Registration.LOCK.get(),
                Registration.SIGNAL_RECEIVER.get(),
                Registration.SIGNAL_TRANSMITTER.get(),
                Registration.WIRELESS_BUTTON.get(),
                Registration.WIRELESS_LOCK.get(),
                Registration.FLAT_LIGHT.get(),
                Registration.NETCABLE.get(),
                Registration.CONNECTOR.get(),
                Registration.FACADE.get()
                );
    }

    private void addTag(TagKey<Block> tag, Block... items) {
        tag(tag)
                .add(items);
    }

    private <T extends Block> void addTag(TagKey<Block> tag, Collection<RegistryObject<T>> items) {
        TagAppender<Block> t = tag(tag);
        for (Iterator<RegistryObject<T>> it = items.iterator(); it.hasNext(); ) {
            RegistryObject<T> block = it.next();
            t.add(block.get());
        }
    }

    @Override
    public String getName() {
        return "Ariente Block Tags";
    }
}
