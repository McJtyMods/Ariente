package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.api.MarbleType;
import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.blocks.decorative.PatternType;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.BlockStateItem;
import mcjty.lib.datagen.BaseItemModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.RegistryObject;

import java.util.Map;
import java.util.Map.Entry;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Ariente.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedItem(Registration.AICORE_ITEM.get(), "block/aicore");

        parentedItem(Registration.ALARM_ITEM.get(), "block/machines/alarm_safe");
        parentedItem(Registration.LOCK_ITEM.get(), "block/machines/lock_unlocked");
        parentedItem(Registration.SIGNAL_RECEIVER_ITEM.get(), "block/machines/signal_receiver_on");
        parentedItem(Registration.SIGNAL_TRANSMITTER_ITEM.get(), "block/machines/signal_transmitter_on");
        parentedItem(Registration.WIRELESS_BUTTON_ITEM.get(), "block/machines/wireless_button_unlocked");
        parentedItem(Registration.WIRELESS_LOCK_ITEM.get(), "block/machines/wireless_lock_unlocked");
        parentedItem(Registration.FLAT_LIGHT_ITEM.get(), "block/machines/flatglow");
        parentedItem(Registration.AUTO_CONSTRUCTOR_ITEM.get(), "block/machines/auto_constructor_on");
        parentedItem(Registration.CONSTRUCTOR_ITEM.get(), "block/machines/constructor");
        parentedItem(Registration.AUTOMATION_FIELD_ITEM.get(), "block/machines/automation_field");
        parentedItem(Registration.BLUEPRINT_STORAGE_ITEM.get(), "block/machines/blueprint_storage");
        parentedItem(Registration.POWER_COMBINER_ITEM.get(), "block/machines/power_combiner");

        getBuilder(Registration.NETCABLE_POSIRITE.get().getRegistryName().getPath())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/smallblock")))
                .texture("all", modLoc("block/cables/posirite/connector"));

        cableItem(Registration.NETCABLE_POSIRITE.get(), "posirite");
        cableItem(Registration.NETCABLE_NEGARITE.get(), "negarite");
        cableItem(Registration.NETCABLE_COMBINED.get(), "combined");
        cableItem(Registration.NETCABLE_DATA.get(), "data");
        connectorItem(Registration.CONNECTOR_POSIRITE.get(), "posirite");
        connectorItem(Registration.CONNECTOR_NEGARITE.get(), "negarite");
        connectorItem(Registration.CONNECTOR_COMBINED.get(), "combined");
        connectorItem(Registration.CONNECTOR_DATA.get(), "data");

        for (Entry<TechType, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.BLACK_TECH.entrySet()) {
            parentedBlock(entry.getValue().get(), "block/decorative/blacktech_" + entry.getKey().getSerializedName());
        }
        for (Entry<MarbleType, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE_TECH.entrySet()) {
            parentedBlock(entry.getValue().get(), "block/decorative/marbletech_" + entry.getKey().getSerializedName());
        }
        for (Map.Entry<PatternType, RegistryObject<BlockStateItem>> entry : Registration.PATTERN_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/pattern_" + entry.getKey().getSerializedName());
        }
        for (Entry<MarbleColor, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE.entrySet()) {
            parentedBlock(entry.getValue().get(), "block/decorative/marble_" + entry.getKey().getSerializedName());
        }
        for (Entry<MarbleColor, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE_SMOOTH.entrySet()) {
            parentedBlock(entry.getValue().get(), "block/decorative/marble_smooth_" + entry.getKey().getSerializedName());
        }
        for (Entry<MarbleColor, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE_PILAR.entrySet()) {
            parentedBlock(entry.getValue().get(), "block/decorative/marble_pilar_" + entry.getKey().getSerializedName());
        }
        for (Entry<MarbleColor, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE_BRICKS.entrySet()) {
            parentedBlock(entry.getValue().get(), "block/decorative/marble_bricks_" + entry.getKey().getSerializedName());
        }
        for (Map.Entry<MarbleColor, RegistryObject<BlockStateItem>> entry : Registration.MARBLE_SLAB_ITEMS.entrySet()) {
            parentedItem(entry.getValue().get(), "block/decorative/marble_slab_" + entry.getKey().getSerializedName());
        }

        fenceItem(Registration.BLUE_GLASS_FENCE_ITEM.get(), "block/decorative/blue_glass_fence");
        fenceItem(Registration.GLASS_FENCE_ITEM.get(), "block/decorative/glass_fence");
        fenceItem(Registration.MARBLE_FENCE_ITEM.get(), "block/decorative/marble_fence");
        fenceItem(Registration.TECH_FENCE_ITEM.get(), "block/decorative/tech_fence");

        parentedItem(Registration.DOOR_MARKER_ITEM.get(), "block/utility/door_marker");
        parentedItem(Registration.LEVEL_MARKER_ITEM.get(), "block/utility/level_marker");
        parentedItem(Registration.FIELD_MARKER_ITEM.get(), "block/utility/field_marker");
        parentedItem(Registration.FLUX_BEAM_ITEM.get(), "block/utility/flux_beam");
        parentedItem(Registration.FLUX_BEND_BEAM_ITEM.get(), "block/utility/flux_bend_beam");
        parentedItem(Registration.SENSOR_ITEM_NODE_ITEM.get(), "block/utility/sensor_item_node_tl");
        parentedItem(Registration.INPUT_ITEM_NODE_ITEM.get(), "block/utility/input_item_node_tl");
        parentedItem(Registration.OUTPUT_ITEM_NODE_ITEM.get(), "block/utility/output_item_node_tl");
        parentedItem(Registration.ROUND_ROBIN_NODE_ITEM.get(), "block/utility/round_robin_node_tl");
        parentedItem(Registration.RAMP_ITEM.get(), "block/decorative/ramp");
        parentedItem(Registration.REINFORCED_MARBLE_ITEM.get(), "block/decorative/reinforced_marble");
        parentedItem(Registration.FLUX_GLOW_ITEM.get(), "block/decorative/flux_glow");
        parentedItem(Registration.NEGARITE_TANK_ITEM.get(), "block/machines/negarite_tank_both");
        parentedItem(Registration.POSIRITE_TANK_ITEM.get(), "block/machines/posirite_tank_both");
        parentedItem(Registration.NEGARITE_GENERATOR_ITEM.get(), "block/machines/negarite_generator_on");
        parentedItem(Registration.POSIRITE_GENERATOR_ITEM.get(), "block/machines/posirite_generator_on");
        parentedItem(Registration.STORAGE_ITEM.get(), "block/utility/storage");
        parentedItem(Registration.ELEVATOR_ITEM.get(), "block/utility/elevator");
        parentedItem(Registration.FORCEFIELD_ITEM.get(), "block/machines/forcefield");
        parentedItem(Registration.WARPER_ITEM.get(), "block/utility/warper");
        parentedItem(Registration.SLOPE_ITEM.get(), "block/slope");

        parentedItem(Registration.ORE_LITHIUM_ITEM.get(), "block/ores/lithium");
        parentedItem(Registration.ORE_MANGANESE_ITEM.get(), "block/ores/manganese");
        parentedItem(Registration.ORE_SILVER_ITEM.get(), "block/ores/silver");
        parentedItem(Registration.ORE_SILICON_ITEM.get(), "block/ores/silicon");
        parentedItem(Registration.ORE_PLATINUM_ITEM.get(), "block/ores/platinum");
        parentedItem(Registration.ORE_NEGARITE_ITEM.get(), "block/ores/negarite");
        parentedItem(Registration.ORE_POSIRITE_ITEM.get(), "block/ores/posirite");

        itemGenerated(Registration.INGOT_LITHIUM.get(), "item/ingots/ingot_lithium");
        itemGenerated(Registration.INGOT_MANGANESE.get(), "item/ingots/ingot_manganese");
        itemGenerated(Registration.INGOT_SILVER.get(), "item/ingots/ingot_silver");
        itemGenerated(Registration.INGOT_PLATINUM.get(), "item/ingots/ingot_platinum");

        itemGenerated(Registration.DUST_SILICON.get(), "item/dusts/silicon");
        itemGenerated(Registration.DUST_NEGARITE.get(), "item/dusts/dust_negarite");
        itemGenerated(Registration.DUST_POSIRITE.get(), "item/dusts/dust_posirite");

        itemGenerated(Registration.ENERGY_HOLDER.get(), "item/tools/energy_holder");
        itemGenerated(Registration.ENERGY_SABRE.get(), "item/weapons/energy_sabre");
        itemGenerated(Registration.ENHANCED_ENERGY_SABRE.get(), "item/weapons/enhanced_energy_sabre");
        itemGenerated(Registration.KEY_CARD.get(), "item/tools/key_card");
        itemGenerated(Registration.BLUEPRINT.get(), "item/tools/blueprint");
        itemGenerated(Registration.ARIENTE_PEARL.get(), "item/tools/ariente_pearl");
        itemGenerated(Registration.FLUX_LEVITATOR.get(), "item/tools/flux_levitator");
        itemGenerated(Registration.FLUX_SHIP.get(), "item/tools/flux_ship");
        itemGenerated(Registration.FLUX_CAPACITOR.get(), "item/tools/flux_capacitor");
        itemGenerated(Registration.CIRCUIT.get(), "item/tools/circuit");
        itemGenerated(Registration.ADVANCED_CIRCUIT.get(), "item/tools/advanced_circuit");
        itemGenerated(Registration.ENERGY_HOLDER.get(), "item/tools/energy_holder");

        itemGenerated(Registration.POWERSUIT_CHEST.get(), "item/armor/powersuit_chest");
        itemGenerated(Registration.POWERSUIT_FEET.get(), "item/armor/powersuit_feet");
        itemGenerated(Registration.POWERSUIT_HEAD.get(), "item/armor/powersuit_head");
        itemGenerated(Registration.POWERSUIT_LEGS.get(), "item/armor/powersuit_legs");

        itemGenerated(Registration.MODULE_ARMOR.get(), "item/modules/module_armor");
        itemGenerated(Registration.MODULE_ENERGY.get(), "item/modules/module_energy");
        itemGenerated(Registration.MODULE_FEATHERFALLING.get(), "item/modules/module_featherfalling");
        itemGenerated(Registration.MODULE_FLIGHT.get(), "item/modules/module_flight");
        itemGenerated(Registration.MODULE_HOVER.get(), "item/modules/module_hover");
        itemGenerated(Registration.MODULE_FORCEFIELD.get(), "item/modules/module_forcefield");
        itemGenerated(Registration.MODULE_INVISIBILITY.get(), "item/modules/module_invisibility");
        itemGenerated(Registration.MODULE_NIGHTVISION.get(), "item/modules/module_nightvision");
        itemGenerated(Registration.MODULE_REGENERATION.get(), "item/modules/module_regeneration");
        itemGenerated(Registration.MODULE_SCRAMBLE.get(), "item/modules/module_scramble");
        itemGenerated(Registration.MODULE_AUTOFEED.get(), "item/modules/module_autofeed");
        itemGenerated(Registration.MODULE_SPEED.get(), "item/modules/module_speed");
        itemGenerated(Registration.MODULE_STEPASSIST.get(), "item/modules/module_stepassist");
        itemGenerated(Registration.MODULE_INHIBIT.get(), "item/modules/module_inhibit");
        itemGenerated(Registration.MODULE_POWER.get(), "item/modules/module_power");
        itemGenerated(Registration.MODULE_LOOTING.get(), "item/modules/module_looting");
        itemGenerated(Registration.MODULE_FIRE.get(), "item/modules/module_fire");
    }

    private void cableItem(Item item, String type) {
        getBuilder(item.getRegistryName().getPath())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/smallblock")))
                .texture("all", modLoc("block/cables/" + type + "/cable_item"));
    }

    private void connectorItem(Item item, String type) {
        getBuilder(item.getRegistryName().getPath())
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/mediumblock")))
                .texture("all", modLoc("block/cables/" + type + "/connector"));
    }

    private void fenceItem(Item item, String texture) {
        ModelFile.UncheckedModelFile parent = new ModelFile.UncheckedModelFile(mcLoc(BLOCK_FOLDER + "/fence_inventory"));
        getBuilder(item.getRegistryName().getPath())
                .parent(parent)
                .texture("texture", modLoc(texture));
    }

    @Override
    public String getName() {
        return "Ariente Item Models";
    }
}
