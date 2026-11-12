package mcjty.ariente.datagen;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.blocks.decorative.PatternBlock;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.datagen.BaseLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class LootTables extends BaseLootTableProvider implements LootTableSubProvider {

    private final HolderLookup.Provider provider;

    public LootTables(HolderLookup.Provider provider) {
        this.provider = provider;
        addTables();
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        generate(output, LootContextParamSets.BLOCK);
    }

    protected void addTables() {
        addSimpleTable(Registration.FLUX_BEAM.get());
        addSimpleTable(Registration.FLUX_BEND_BEAM.get());
        addSimpleTable(Registration.RAMP.get());
        addSimpleTable(Registration.SLOPE.get());
        addSimpleTable(Registration.GLASS_FENCE.get());
        addSimpleTable(Registration.BLUE_GLASS_FENCE.get());
        addSimpleTable(Registration.MARBLE_FENCE.get());
        addSimpleTable(Registration.TECH_FENCE.get());
        addSimpleTable(Registration.REINFORCED_MARBLE.get());
        addSimpleTable(Registration.FLUX_GLOW.get());
        addSimpleTable(Registration.FLAT_LIGHT.get());

        addLootTable(Registration.NETCABLE.get(), BlockLootUtility.cableTable(Registration.NETCABLE.get(),
                Registration.NETCABLE_NEGARITE.get(), Registration.NETCABLE_POSIRITE.get(), Registration.NETCABLE_COMBINED.get(), Registration.NETCABLE_DATA.get()));
        addLootTable(Registration.CONNECTOR.get(), BlockLootUtility.cableTable(Registration.CONNECTOR.get(),
                Registration.CONNECTOR_NEGARITE.get(), Registration.CONNECTOR_POSIRITE.get(), Registration.CONNECTOR_COMBINED.get(), Registration.CONNECTOR_DATA.get()));

        addLootTable(Registration.ORE_LITHIUM.get(), BlockLootUtility.oreTable(provider, Registration.ORE_LITHIUM.get(), Registration.RAW_LITHIUM.get()));
        addLootTable(Registration.ORE_MANGANESE.get(), BlockLootUtility.oreTable(provider, Registration.ORE_MANGANESE.get(), Registration.RAW_MANGANESE.get()));
        addLootTable(Registration.ORE_PLATINUM.get(), BlockLootUtility.oreTable(provider, Registration.ORE_PLATINUM.get(), Registration.RAW_PLATINUM.get()));
        addLootTable(Registration.ORE_SILVER.get(), BlockLootUtility.oreTable(provider, Registration.ORE_SILVER.get(), Registration.RAW_SILVER.get()));
        addSimpleTable(Registration.ORE_NEGARITE.get());
        addSimpleTable(Registration.ORE_POSIRITE.get());
        addLootTable(Registration.ORE_SILICON.get(), BlockLootUtility.oreTable(provider, Registration.ORE_SILICON.get(), Registration.DUST_SILICON.get()));

        addStandardTable(Registration.AICORE.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.FORCEFIELD.get(), Registration.MACHINE_DATA.get());
        addSimpleTable(Registration.NEGARITE_TANK.get());
        addSimpleTable(Registration.POSIRITE_TANK.get());
        addStandardTable(Registration.NEGARITE_GENERATOR.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.POSIRITE_GENERATOR.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.POWER_COMBINER.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.AUTOMATION_FIELD.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.FIELD_MARKER.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.INPUT_ITEM_NODE.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.OUTPUT_ITEM_NODE.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.ROUND_ROBIN_NODE.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.SENSOR_ITEM_NODE.get(), Registration.MACHINE_DATA.get());
        addSimpleTable(Registration.DISPLAY_BASE.get());
        addSimpleTable(Registration.CAMERA.get());
        addStandardTable(Registration.DOOR_MARKER.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.SIGNAL_TRANSMITTER.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.SIGNAL_RECEIVER.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.WIRELESS_BUTTON.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.WIRELESS_LOCK.get(), Registration.MACHINE_DATA.get());
        addSimpleTable(Registration.ALARM.get());
        addStandardTable(Registration.AUTO_CONSTRUCTOR.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.RECYCLER.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.BLUEPRINT_STORAGE.get(), Registration.MACHINE_DATA.get());
        addSimpleTable(Registration.CONSTRUCTOR.get());
        addStandardTable(Registration.ELEVATOR.get(), Registration.MACHINE_DATA.get());
        addSimpleTable(Registration.LEVEL_MARKER.get());
        addSimpleTable(Registration.FLUX_SLOPE_BEAM.get());
        addStandardTable(Registration.LOCK.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.STORAGE.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.STORAGE_3X3.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.HOLO_STORAGE_1.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.HOLO_STORAGE_2.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.HOLO_STORAGE_3.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.SMELTER.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.PURIFIER_START.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.PURIFIER_FINISH.get(), Registration.MACHINE_DATA.get());
        addSimpleTable(Registration.PURIFIER_TUBE.get());
        addStandardTable(Registration.CHARGER.get(), Registration.MACHINE_DATA.get());
        addStandardTable(Registration.WARPER.get(), Registration.MACHINE_DATA.get());

        for (Supplier<BaseBlock> entry : DecorativeBlockModule.BLACK_TECH.values()) {
            addSimpleTable(entry.get());
        }
        for (Supplier<BaseBlock> entry : DecorativeBlockModule.MARBLE_TECH.values()) {
            addSimpleTable(entry.get());
        }
        for (Supplier<BaseBlock> entry : DecorativeBlockModule.MARBLE.values()) {
            addSimpleTable(entry.get());
        }
        for (Supplier<BaseBlock> entry : DecorativeBlockModule.MARBLE_SMOOTH.values()) {
            addSimpleTable(entry.get());
        }
        for (Supplier<BaseBlock> entry : DecorativeBlockModule.MARBLE_PILAR.values()) {
            addSimpleTable(entry.get());
        }
        for (Supplier<BaseBlock> entry : DecorativeBlockModule.MARBLE_BRICKS.values()) {
            addSimpleTable(entry.get());
        }
        for (Supplier<SlabBlock> entry : DecorativeBlockModule.MARBLE_SLAB.values()) {
            addSimpleSlab(entry.get());
        }

        addLootTable(Registration.PATTERN.get(), BlockLootUtility.variantTable(Registration.PATTERN.get(), PatternBlock.TYPE,
                type -> Registration.PATTERN_ITEMS.get(type).get()));
    }

    protected void addSimpleSlab(Block block) {
        lootTables.put(block, BlockLootUtility.slabTable(provider, block));
    }

    public String getName() {
        return "Ariente LootTables";
    }
}
