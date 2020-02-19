package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.api.MarbleType;
import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.decorative.PatternType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BlockStateItem;
import mcjty.lib.datagen.BaseItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

import java.util.Map;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Ariente.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedItem(Registration.AICORE_ITEM.get(), "block/aicore");

        parentedItem(Registration.ALARM_ITEM.get(), "block/machines/alarm_safe");
        parentedItem(Registration.LOCK_ITEM.get(), "block/machines/lock_unlocked");
        parentedItem(Registration.SIGNAL_RECEIVER_ITEM.get(), "block/machines/signal_receiver");
        parentedItem(Registration.SIGNAL_TRANSMITTER_ITEM.get(), "block/machines/signal_transmitter");
        parentedItem(Registration.WIRELESS_BUTTON_ITEM.get(), "block/machines/wireless_button");
        parentedItem(Registration.WIRELESS_LOCK_ITEM.get(), "block/machines/wireless_lock");
        parentedItem(Registration.FLAT_LIGHT_ITEM.get(), "block/machines/flatlight");
        parentedItem(Registration.AUTO_CONSTRUCTOR_ITEM.get(), "block/machines/auto_constructor");
        parentedItem(Registration.AUTOMATION_FIELD_ITEM.get(), "block/machines/automation_field");
        parentedItem(Registration.BLUEPRINT_STORAGE_ITEM.get(), "block/machines/blueprint_storage");
        parentedItem(Registration.POWER_COMBINER_ITEM.get(), "block/machines/power_combiner");
        parentedItem(Registration.NEGARITE_GENERATOR_ITEM.get(), "block/machines/negarite_generator");
        parentedItem(Registration.POSIRITE_GENERATOR_ITEM.get(), "block/machines/posirite_generator");

        for (Map.Entry<TechType, RegistryObject<BlockStateItem>> entry : Registration.BLACK_TECH_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/blacktech_" + entry.getKey().getName());
        }
        for (Map.Entry<MarbleType, RegistryObject<BlockStateItem>> entry : Registration.MARBLE_TECH_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/marbletech_" + entry.getKey().getName());
        }
        for (Map.Entry<PatternType, RegistryObject<BlockStateItem>> entry : Registration.PATTERN_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/pattern_" + entry.getKey().getName());
        }
        for (Map.Entry<MarbleColor, RegistryObject<BlockStateItem>> entry : Registration.MARBLE_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/marble_" + entry.getKey().getName());
        }
        for (Map.Entry<MarbleColor, RegistryObject<BlockStateItem>> entry : Registration.MARBLE_SMOOTH_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/marble_smooth_" + entry.getKey().getName());
        }
        for (Map.Entry<MarbleColor, RegistryObject<BlockStateItem>> entry : Registration.MARBLE_PILAR_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/marble_pilar_" + entry.getKey().getName());
        }
        for (Map.Entry<MarbleColor, RegistryObject<BlockStateItem>> entry : Registration.MARBLE_BRICKS_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/marble_bricks_" + entry.getKey().getName());
        }
        for (Map.Entry<MarbleColor, RegistryObject<BlockStateItem>> entry : Registration.MARBLE_SLAB_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/marble_slab_" + entry.getKey().getName());
        }
        parentedItem(Registration.BLUE_GLASS_FENCE_ITEM.get(), "block/decorative/blue_glass_fence");
        parentedItem(Registration.GLASS_FENCE_ITEM.get(), "block/decorative/glass_fence");
        parentedItem(Registration.MARBLE_FENCE_ITEM.get(), "block/decorative/marble_fence");
        parentedItem(Registration.TECH_FENCE_ITEM.get(), "block/decorative/tech_fence");
        parentedItem(Registration.DOOR_MARKER_ITEM.get(), "block/utility/door_marker");
        parentedItem(Registration.LEVEL_MARKER_ITEM.get(), "block/utility/level_marker");
        parentedItem(Registration.FIELD_MARKER_ITEM.get(), "block/utility/field_marker");
        parentedItem(Registration.FLUX_BEAM_ITEM.get(), "block/utility/flux_beam");
        parentedItem(Registration.FLUX_BEND_BEAM_ITEM.get(), "block/utility/flux_beam_beam");
        parentedItem(Registration.SENSOR_ITEM_NODE_ITEM.get(), "block/utility/sensor_item_node");
        parentedItem(Registration.INPUT_ITEM_NODE_ITEM.get(), "block/utility/input_item_node");
        parentedItem(Registration.OUTPUT_ITEM_NODE_ITEM.get(), "block/utility/output_item_node");
        parentedItem(Registration.ROUND_ROBIN_NODE_ITEM.get(), "block/utility/round_robin_node");
        parentedItem(Registration.RAMP_ITEM.get(), "block/decorative/ramp");
        parentedItem(Registration.REINFORCED_MARBLE_ITEM.get(), "block/decorative/reinforced_marble");
        parentedItem(Registration.FLUX_GLOW_ITEM.get(), "block/decorative/flux_glow");
        parentedItem(Registration.NEGARITE_TANK_ITEM.get(), "block/machines/negarite_tank");
        parentedItem(Registration.POSIRITE_TANK_ITEM.get(), "block/machines/posirite_tank");
        parentedItem(Registration.STORAGE_ITEM.get(), "block/utility/storage");
        parentedItem(Registration.ELEVATOR_ITEM.get(), "block/utility/elevator");
        parentedItem(Registration.FORCEFIELD_ITEM.get(), "block/machines/forcefield");
        parentedItem(Registration.WARPER_ITEM.get(), "block/utility/warper");

        parentedItem(Registration.ORE_LITHIUM_ITEM.get(), "block/ores/lithium");
        parentedItem(Registration.ORE_MANGANESE_ITEM.get(), "block/ores/manganese");
        parentedItem(Registration.ORE_SILVER_ITEM.get(), "block/ores/silver");
        parentedItem(Registration.ORE_SILICON_ITEM.get(), "block/ores/silicon");
        parentedItem(Registration.ORE_PLATINUM_ITEM.get(), "block/ores/platinum");
        parentedItem(Registration.ORE_NEGARITE_ITEM.get(), "block/ores/negarite");
        parentedItem(Registration.ORE_POSIRITE_ITEM.get(), "block/ores/posirite");
    }

    @Override
    public String getName() {
        return "Ariente Item Models";
    }
}
