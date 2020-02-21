package mcjty.ariente.datagen;

import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.setup.Registration;
import mcjty.lib.datagen.BaseLootTableProvider;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        addStandardTable(Registration.AICORE.get());
        addStandardTable(Registration.FORCEFIELD.get());
        addSimpleTable(Registration.NEGARITE_TANK.get());
        addSimpleTable(Registration.POSIRITE_TANK.get());
        addStandardTable(Registration.NEGARITE_GENERATOR.get());
        addStandardTable(Registration.POSIRITE_GENERATOR.get());
        addStandardTable(Registration.POWER_COMBINER.get());
        addStandardTable(Registration.AUTOMATION_FIELD.get());
        addStandardTable(Registration.FIELD_MARKER.get());
        addStandardTable(Registration.INPUT_ITEM_NODE.get());
        addStandardTable(Registration.OUTPUT_ITEM_NODE.get());
        addStandardTable(Registration.ROUND_ROBIN_NODE.get());
        addStandardTable(Registration.SENSOR_ITEM_NODE.get());
        addStandardTable(Registration.DOOR_MARKER.get());
        addStandardTable(Registration.SIGNAL_TRANSMITTER.get());
        addStandardTable(Registration.SIGNAL_RECEIVER.get());
        addStandardTable(Registration.WIRELESS_BUTTON.get());
        addStandardTable(Registration.WIRELESS_LOCK.get());
        addSimpleTable(Registration.ALARM.get());
        addStandardTable(Registration.AUTO_CONSTRUCTOR.get());
        addStandardTable(Registration.BLUEPRINT_STORAGE.get());
        addSimpleTable(Registration.CONSTRUCTOR.get());
        addStandardTable(Registration.ELEVATOR.get());
        addSimpleTable(Registration.LEVEL_MARKER.get());
        addStandardTable(Registration.LOCK.get());
        addStandardTable(Registration.STORAGE.get());
        addStandardTable(Registration.WARPER.get());

        // @todo marble and decorative stuff
        addBlockStateTable(Registration.MARBLE.get(), MarbleColor.COLOR);

    }


    @Override
    public String getName() {
        return "Ariente LootTables";
    }
}
