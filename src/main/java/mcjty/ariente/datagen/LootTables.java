package mcjty.ariente.datagen;

import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.blocks.decorative.PatternBlock;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.datagen.BaseLootTableProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.registries.RegistryObject;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        addStandardTable(Registration.AICORE.get(), Registration.AICORE_TILE.get());
        addStandardTable(Registration.FORCEFIELD.get(), Registration.FORCEFIELD_TILE.get());
        addSimpleTable(Registration.NEGARITE_TANK.get());
        addSimpleTable(Registration.POSIRITE_TANK.get());
        addStandardTable(Registration.NEGARITE_GENERATOR.get(), Registration.NEGARITE_GENERATOR_TILE.get());
        addStandardTable(Registration.POSIRITE_GENERATOR.get(), Registration.POSIRITE_GENERATOR_TILE.get());
        addStandardTable(Registration.POWER_COMBINER.get(), Registration.POWER_COMBINER_TILE.get());
        addStandardTable(Registration.AUTOMATION_FIELD.get(), Registration.AUTOFIELD_TILE.get());
        addStandardTable(Registration.FIELD_MARKER.get(), Registration.FIELD_MARKER_TILE.get());
        addStandardTable(Registration.INPUT_ITEM_NODE.get(), Registration.INPUT_ITEM_TILE.get());
        addStandardTable(Registration.OUTPUT_ITEM_NODE.get(), Registration.OUTPUT_ITEM_TILE.get());
        addStandardTable(Registration.ROUND_ROBIN_NODE.get(), Registration.ROUND_ROBIN_TILE.get());
        addStandardTable(Registration.SENSOR_ITEM_NODE.get(), Registration.SENSOR_ITEM_TILE.get());
        addStandardTable(Registration.DOOR_MARKER.get(), Registration.DOOR_MARKER_TILE.get());
        addStandardTable(Registration.SIGNAL_TRANSMITTER.get(), Registration.SIGNAL_TRANSMITTER_TILE.get());
        addStandardTable(Registration.SIGNAL_RECEIVER.get(), Registration.SIGNAL_RECEIVER_TILE.get());
        addStandardTable(Registration.WIRELESS_BUTTON.get(), Registration.WIRELESS_BUTTON_TILE.get());
        addStandardTable(Registration.WIRELESS_LOCK.get(), Registration.WIRELESS_LOCK_TILE.get());
        addSimpleTable(Registration.ALARM.get());
        addStandardTable(Registration.AUTO_CONSTRUCTOR.get(), Registration.AUTO_CONSTRUCTOR_TILE.get());
        addStandardTable(Registration.BLUEPRINT_STORAGE.get(), Registration.BLUEPRINT_STORAGE_TILE.get());
        addSimpleTable(Registration.CONSTRUCTOR.get());
        addStandardTable(Registration.ELEVATOR.get(), Registration.ELEVATOR_TILE.get());
        addSimpleTable(Registration.LEVEL_MARKER.get());
        addStandardTable(Registration.LOCK.get(), Registration.LOCK_TILE.get());
        addStandardTable(Registration.STORAGE.get(), Registration.STORAGE_TILE.get());
        addStandardTable(Registration.WARPER.get(), Registration.WARPER_TILE.get());

        for (RegistryObject<BaseBlock> entry : DecorativeBlockModule.BLACK_TECH.values()) {
            addSimpleTable(entry.get());
        }
        for (RegistryObject<BaseBlock> entry : DecorativeBlockModule.MARBLE_TECH.values()) {
            addSimpleTable(entry.get());
        }
        for (RegistryObject<BaseBlock> entry : DecorativeBlockModule.MARBLE.values()) {
            addSimpleTable(entry.get());
        }
        for (RegistryObject<BaseBlock> entry : DecorativeBlockModule.MARBLE_SMOOTH.values()) {
            addSimpleTable(entry.get());
        }
        for (RegistryObject<BaseBlock> entry : DecorativeBlockModule.MARBLE_PILAR.values()) {
            addSimpleTable(entry.get());
        }
        for (RegistryObject<BaseBlock> entry : DecorativeBlockModule.MARBLE_BRICKS.values()) {
            addSimpleTable(entry.get());
        }
        for (RegistryObject<SlabBlock> entry : DecorativeBlockModule.MARBLE_SLAB.values()) {
            addSimpleSlab(entry.get());
        }

        addBlockStateTable(Registration.PATTERN.get(), PatternBlock.TYPE);

        addOreDrop(Registration.ORE_POSIRITE, Registration.DUST_POSIRITE, 1F, 3F);
        addOreDrop(Registration.ORE_NEGARITE, Registration.DUST_NEGARITE, 1F, 3F);
        addOreDrop(Registration.ORE_SILICON, Registration.DUST_SILICON, 1F, 3F);
        addSimpleTable(Registration.ORE_SILVER.get());
        addSimpleTable(Registration.ORE_LITHIUM.get());
        addSimpleTable(Registration.ORE_MANGANESE.get());
        addSimpleTable(Registration.ORE_PLATINUM.get());
    }

    protected void addSimpleSlab(Block block) {
        lootTables.put(block, BlockLootUtility.createSlabItemTable(block));
    }

    protected <T extends Block> void addOreDrop(RegistryObject<T> block, RegistryObject<Item> drop, float min, float max) {
        lootTables.put(
            block.get(),
            BlockLootUtility.createOreDrops(block.get(), drop.get(), min, max)
        );
    }

    @Override
    public String getName() {
        return "Ariente LootTables";
    }
}
