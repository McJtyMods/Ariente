package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.blocks.BlockProperties;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.blocks.decorative.PatternBlock;
import mcjty.ariente.blocks.decorative.PatternType;
import mcjty.ariente.blocks.utility.AlarmTile;
import mcjty.ariente.blocks.utility.autofield.AbstractNodeTile;
import mcjty.ariente.blocks.utility.autofield.NodeOrientation;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.minecraft.core.Direction.*;
import static net.minecraftforge.client.model.generators.ModelProvider.BLOCK_FOLDER;

public class BlockStates extends BaseBlockStateProvider {

    public static final String TXT_MACHINEBOTTOM = "block/base/machinebottom";
    public static final String TXT_MACHINESIDE = "block/base/machineside";
    public static final String TXT_MACHINETOP = "block/base/machinetop";
    public static final String TXT_REINFORCED_MARBLE = "block/base/reinforced_marble";
    public static final String TXT_GLOWING_FLUX = "block/base/glowing_flux";
    public static final String TXT_AUTOMATION_FIELD = "block/machines/automation_field";
    public static final String TXT_BLUEPRINT_STORAGE = "block/machines/blueprint_storage";
    public static final String TXT_CONSTRUCTOR = "block/machines/constructor";
    public static final String TXT_POWER_COMBINER = "block/machines/power_combiner";
    public static final String TXT_LOCK_UNLOCKED = "block/machines/lock";
    public static final String TXT_LOCK_LOCKED = "block/machines/lock_locked";
    public static final String TXT_WIRELESS_BUTTON_UNLOCKED = "block/machines/lock";
    public static final String TXT_WIRELESS_BUTTON_LOCKED = "block/machines/lock_locked";
    public static final String TXT_WIRELESS_LOCK_UNLOCKED = "block/machines/lock";
    public static final String TXT_WIRELESS_LOCK_LOCKED = "block/machines/lock_locked";
    public static final String TXT_SIGNAL_RECEIVER_ON = "block/machines/signal_receiver_on";
    public static final String TXT_SIGNAL_RECEIVER_OFF = "block/machines/signal_receiver_off";
    public static final String TXT_SIGNAL_TRANSMITTER_ON = "block/machines/signal_transmitter_on";
    public static final String TXT_SIGNAL_TRANSMITTER_OFF = "block/machines/signal_transmitter_off";
    public static final String TXT_WARPER = "block/machines/warper";
    public static final String TXT_ELEVATOR = "block/machines/elevator";
    public static final String TXT_FORCEFIELD = "block/machines/forcefield";
    public static final String TXT_STORAGE_WINDOW = "block/machines/storage_window";
    public static final String TXT_BLACKMARBLE = "block/marble/blackmarble";
    public static final String TXT_FLUXBEAM = "block/machines/flux_beam";
    public static final String TXT_ORE_LITHIUMORE = "block/ores/lithiumore";
    public static final String TXT_ORE_MANGANESEORE = "block/ores/manganeseore";
    public static final String TXT_ORE_SILICONORE = "block/ores/siliconore";
    public static final String TXT_ORE_SILVERORE = "block/ores/silverore";
    public static final String TXT_ORE_PLATINUMORE = "block/ores/platinumore";
    public static final String TXT_ORE_POSIRITE = "block/ores/posirite";
    public static final String TXT_ORE_NEGARITE = "block/ores/negarite";
    public static final String TXT_LEVEL_MARKER = "block/machines/level_marker";
    public static final String TXT_DOOR_MARKER = "block/machines/door_marker";
    public static final String TXT_FIELD_MARKER = "block/machines/field_marker";
    public static final String TXT_BLUE_GLASS_FENCE = "block/decorative/blue_glass_fence";
    public static final String TXT_GLASS_FENCE = "block/decorative/glass_fence";
    public static final String TXT_MARBLE_FENCE = "block/decorative/marble_fence";
    public static final String TXT_TECH_FENCE = "block/decorative/tech_fence";
    public static final String TXT_ALARM_SAFE = "block/machines/alarm_safe";
    public static final String TXT_ALARM_DEAD = "block/machines/alarm_dead";
    public static final String TXT_ALARM_ALERT = "block/machines/alarm_alert";
    public static final String TXT_AICORE_WINDOW = "block/machines/aicore_window";
    public static final String TXT_LIGHT_ON = "block/machines/light_on";

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Ariente.MODID, exFileHelper);
    }

    private BlockModelBuilder flatglow;
    private BlockModelBuilder frontglow;
    private BlockModelBuilder fullglow;
    private BlockModelBuilder sideglow;
    private BlockModelBuilder topglow;
    private BlockModelBuilder front;
    private BlockModelBuilder thin;
    private BlockModelBuilder empty;
    private BlockModelBuilder nodeblock_tl;
    private BlockModelBuilder nodeblock_tr;
    private BlockModelBuilder nodeblock_bl;
    private BlockModelBuilder nodeblock_br;

    @Override
    protected void registerStatesAndModels() {
        registerBaseModels();
        registerAICore();
        registerAlarm();
        registerBlackTech();
        registerMarbleTech();
        registerMarble();
        registerMarbleSmooth();
        registerMarblePilar();
        registerMarbleBricks();
        registerPattern();
        registerDoorMarker();
        registerLevelMarker();
        registerFieldMarker();
        registerFences();
        registerOres();
        registerMarbleSlabs();
        registerFluxBeams();
        registerNodes();
        registerRamp();
        registerStorage();
        registerSmelter();
        registerElevator();
        registerForcefield();
        registerWarper();
        registerLock();
        registerWirelessButton();
        registerWirelessLock();
        registerSignalReceiver();
        registerSignalTransmitter();
        registerFlatLight();
        registerSlope();

        simpleBlock(Registration.INVISIBLE_DOOR.get(), empty);

        registerTank(Registration.NEGARITE_TANK.get(), "negarite");
        registerTank(Registration.POSIRITE_TANK.get(), "posirite");

        singleTextureBlock(Registration.REINFORCED_MARBLE.get(), BLOCK_FOLDER + "/decorative/reinforced_marble", TXT_REINFORCED_MARBLE);
        singleTextureBlock(Registration.FLUX_GLOW.get(), BLOCK_FOLDER + "/decorative/flux_glow", TXT_GLOWING_FLUX);

        registerWorkingHorizontalGlowBlock("auto_constructor", Registration.AUTO_CONSTRUCTOR.get());
        registerWorkingHorizontalGlowBlock("negarite_generator", Registration.NEGARITE_GENERATOR.get());
        registerWorkingHorizontalGlowBlock("posirite_generator", Registration.POSIRITE_GENERATOR.get());

        registerHorizontalParentedBlock(Registration.AUTOMATION_FIELD.get(), front, "machines/automation_field", TXT_AUTOMATION_FIELD);
        registerHorizontalParentedBlock(Registration.BLUEPRINT_STORAGE.get(), front, "machines/blueprint_storage", TXT_BLUEPRINT_STORAGE);
        registerHorizontalParentedBlock(Registration.CONSTRUCTOR.get(), frontglow, "machines/constructor", TXT_CONSTRUCTOR);
        registerOrientedParentedBlock(Registration.POWER_COMBINER.get(), frontglow, "machines/power_combiner", TXT_POWER_COMBINER);
    }

    private void registerSlope() {
        ModelFile model = models().getExistingFile(modLoc("block/slope"));
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.SLOPE.get());
        builder.partialState().with(EnumFacingUpDown.FACING, EnumFacingUpDown.NORTH)
                .modelForState().modelFile(model)
                .addModel();
        builder.partialState().with(EnumFacingUpDown.FACING, EnumFacingUpDown.SOUTH)
                .modelForState().modelFile(model)
                .rotationY(180)
                .addModel();
        builder.partialState().with(EnumFacingUpDown.FACING, EnumFacingUpDown.WEST)
                .modelForState().modelFile(model)
                .rotationY(270)
                .addModel();
        builder.partialState().with(EnumFacingUpDown.FACING, EnumFacingUpDown.EAST)
                .modelForState().modelFile(model)
                .rotationY(90)
                .addModel();
        builder.partialState().with(EnumFacingUpDown.FACING, EnumFacingUpDown.NORTH_UP)
                .modelForState().modelFile(model)
                .rotationX(180)
                .rotationY(180)
                .addModel();
        builder.partialState().with(EnumFacingUpDown.FACING, EnumFacingUpDown.SOUTH_UP)
                .modelForState().modelFile(model)
                .rotationX(180)
                .addModel();
        builder.partialState().with(EnumFacingUpDown.FACING, EnumFacingUpDown.WEST_UP)
                .modelForState().modelFile(model)
                .rotationX(180)
                .rotationY(90)
                .addModel();
        builder.partialState().with(EnumFacingUpDown.FACING, EnumFacingUpDown.EAST_UP)
                .modelForState().modelFile(model)
                .rotationX(180)
                .rotationY(270)
                .addModel();
    }

    private void registerFlatLight() {
        BlockModelBuilder model = models().withExistingParent(BLOCK_FOLDER + "/machines/flatglow", mcLoc("block/block"))
                .texture("particle", "#all");
        model.element().from(1, 1, 14).to(15, 15, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#all").end()
                .face(UP).uvs(0, 0, 16, 16).texture("#all").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#all").end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#all").end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#all").end()
                .end()
                .texture("back", modLoc(TXT_MACHINEBOTTOM))
                .texture("all", modLoc(TXT_LIGHT_ON));
        orientedBlock(Registration.FLAT_LIGHT.get(), model);
    }

    private void registerWirelessButton() {
        BlockModelBuilder modelUnlocked = createdParentedModel("/machines/wireless_button_unlocked", flatglow, TXT_MACHINEBOTTOM, "front", TXT_WIRELESS_BUTTON_UNLOCKED, "back", TXT_MACHINEBOTTOM);
        BlockModelBuilder modelLocked = createdParentedModel("/machines/wireless_button_locked", flatglow, TXT_MACHINEBOTTOM, "front", TXT_WIRELESS_BUTTON_LOCKED, "back", TXT_MACHINEBOTTOM);
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.WIRELESS_BUTTON.get());
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.POWER, false)
                    .modelForState().modelFile(modelUnlocked), direction);
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.POWER, true)
                    .modelForState().modelFile(modelLocked), direction);
        }
    }

    private void registerWirelessLock() {
        BlockModelBuilder modelUnlocked = createdParentedModel("/machines/wireless_lock_unlocked", flatglow, TXT_MACHINEBOTTOM, "front", TXT_WIRELESS_LOCK_UNLOCKED, "back", TXT_MACHINEBOTTOM);
        BlockModelBuilder modelLocked = createdParentedModel("/machines/wireless_lock_locked", flatglow, TXT_MACHINEBOTTOM, "front", TXT_WIRELESS_LOCK_LOCKED, "back", TXT_MACHINEBOTTOM);
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.WIRELESS_LOCK.get());
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.LOCKED, false)
                    .modelForState().modelFile(modelUnlocked), direction);
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.LOCKED, true)
                    .modelForState().modelFile(modelLocked), direction);
        }
    }

    private void registerLock() {
        BlockModelBuilder modelUnlocked = createdParentedModel("/machines/lock_unlocked", flatglow, TXT_MACHINEBOTTOM, "front", TXT_LOCK_UNLOCKED, "back", TXT_MACHINEBOTTOM);
        BlockModelBuilder modelLocked = createdParentedModel("/machines/lock_locked", flatglow, TXT_MACHINEBOTTOM, "front", TXT_LOCK_LOCKED, "back", TXT_MACHINEBOTTOM);
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.LOCK.get());
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.LOCKED, false)
                    .modelForState().modelFile(modelUnlocked), direction);
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.LOCKED, true)
                    .modelForState().modelFile(modelLocked), direction);
        }
    }

    private void registerSignalReceiver() {
        BlockModelBuilder modelOff = createdParentedModel("/machines/signal_receiver_off", flatglow, TXT_MACHINEBOTTOM, "front", TXT_SIGNAL_RECEIVER_OFF, "back", TXT_MACHINEBOTTOM);
        BlockModelBuilder modelOn = createdParentedModel("/machines/signal_receiver_on", flatglow, TXT_MACHINEBOTTOM, "front", TXT_SIGNAL_RECEIVER_ON, "back", TXT_MACHINEBOTTOM);
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.SIGNAL_RECEIVER.get());
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.POWER, false)
                    .modelForState().modelFile(modelOff), direction);
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.POWER, true)
                    .modelForState().modelFile(modelOn), direction);
        }
    }

    private void registerSignalTransmitter() {
        BlockModelBuilder modelOff = createdParentedModel("/machines/signal_transmitter_off", flatglow, TXT_MACHINEBOTTOM, "front", TXT_SIGNAL_TRANSMITTER_OFF, "back", TXT_MACHINEBOTTOM);
        BlockModelBuilder modelOn = createdParentedModel("/machines/signal_transmitter_on", flatglow, TXT_MACHINEBOTTOM, "front", TXT_SIGNAL_TRANSMITTER_ON, "back", TXT_MACHINEBOTTOM);
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.SIGNAL_TRANSMITTER.get());
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.POWER, false)
                    .modelForState().modelFile(modelOff), direction);
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(BlockProperties.POWER, true)
                    .modelForState().modelFile(modelOn), direction);
        }
    }

    private BlockModelBuilder createdParentedModel(String s, BlockModelBuilder parent, String sideTxt, String key1, String txt1, String key2, String txt2) {
        return models().getBuilder(BLOCK_FOLDER + s)
                .parent(parent)
                .texture("side", modLoc(sideTxt))
                .texture(key1, modLoc(txt1))
                .texture(key2, modLoc(txt2));
    }

    private void registerWarper() {
        BlockModelBuilder model = createdParentedModel("/utility/warper", topglow, TXT_MACHINESIDE, "bottom", TXT_MACHINEBOTTOM, "top", TXT_WARPER);
        simpleBlock(Registration.WARPER.get(), model);
    }

    private void registerElevator() {
        BlockModelBuilder model = models().getBuilder(BLOCK_FOLDER + "/utility/elevator")
                .parent(sideglow)
                .texture("side", modLoc(TXT_ELEVATOR));
        simpleBlock(Registration.ELEVATOR.get(), model);
    }

    private void registerForcefield() {
        BlockModelBuilder model = models().getBuilder(BLOCK_FOLDER + "/machines/forcefield")
                .parent(sideglow)
                .texture("side", modLoc(TXT_FORCEFIELD));
        simpleBlock(Registration.FORCEFIELD.get(), model);
    }

    private void registerStorage() {
        orientedBlock(Registration.STORAGE.get(), storageModel("storage"));
        orientedBlock(Registration.STORAGE_3X3.get(), storageModel("storage_3x3"));
    }

    private BlockModelBuilder storageModel(String name) {
        BlockModelBuilder model = models().withExistingParent(BLOCK_FOLDER + "/utility/" + name, mcLoc("block/block"))
                .texture("particle", "#side");
        ResourceLocation sideTxt = modLoc(TXT_MACHINESIDE);
        ResourceLocation topTxt = modLoc(TXT_MACHINETOP);
        ResourceLocation botTxt = modLoc(TXT_MACHINEBOTTOM);
        model.element()
                .from(0, 0, 0).to(16, 16, 16)
                .face(SOUTH).cullface(SOUTH).texture("#side").end()
                .face(EAST).cullface(EAST).texture("#side").end()
                .face(WEST).cullface(WEST).texture("#side").end()
                .face(UP).cullface(UP).texture("#top").end()
                .face(DOWN).cullface(DOWN).texture("#bottom").end();
        model.element()
                .from(0, 0, 0).to(16, 1, 2)
                .face(NORTH).texture("#front").end()
                .face(UP).texture("#sideIn").end();
        model.element()
                .from(0, 15, 0).to(16, 16, 2)
                .face(NORTH).texture("#front").end()
                .face(DOWN).texture("#sideIn").end();
        model.element()
                .from(0, 1, 0).to(1, 15, 2)
                .face(NORTH).texture("#front").end()
                .face(EAST).texture("#sideIn").end();
        model.element()
                .from(15, 1, 0).to(16, 15, 2)
                .face(NORTH).texture("#front").end()
                .face(WEST).texture("#sideIn").end();
        model.element()
                .from(1, 1, 2).to(15, 15, 2)
                .face(NORTH).texture("#sideIn").end();

        model   .texture("front", sideTxt)
                .texture("side", sideTxt)
                .texture("sideIn", modLoc(TXT_BLACKMARBLE))
                .texture("top", topTxt)
                .texture("bottom", botTxt);

        return model;
    }

    private void registerSmelter() {
        BlockModelBuilder idle = smelterModel("smelter");
        BlockModelBuilder working = smelterModel("smelter_working")
                .texture("workingGlow", modLoc("block/machines/smelter_front_working_glow"));
        working.element().from(1, 1, 1.99f).to(15, 15, 1.99f)
                .face(NORTH).texture("#workingGlow").end();
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.SMELTER.get());
        for (Direction direction : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
            applyHorizRotation(builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction).with(BlockProperties.WORKING, false)
                    .modelForState().modelFile(idle), direction);
            applyHorizRotation(builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction).with(BlockProperties.WORKING, true)
                    .modelForState().modelFile(working), direction);
        }
    }

    private BlockModelBuilder smelterModel(String name) {
        BlockModelBuilder model = storageModel(name)
                .texture("output", modLoc("block/base/machineside_output"))
                .texture("sideIn", modLoc("block/machines/smelter_front"));
        model.element(0).face(SOUTH).texture("#output").end();
        for (float x : new float[]{1.5f, 10}) {
            smelterSlotEdge(model, x, 5.75f, x + 4.5f, 6);
            smelterSlotEdge(model, x, 10, x + 4.5f, 10.25f);
            smelterSlotEdge(model, x, 6, x + .25f, 10);
            smelterSlotEdge(model, x + 4.25f, 6, x + 4.5f, 10);
        }
        return model;
    }

    private void smelterSlotEdge(BlockModelBuilder model, float x1, float y1, float x2, float y2) {
        model.element().from(x1, y1, 1.97f).to(x2, y2, 1.97f)
                .face(NORTH).uvs(0, 0, 16, 16).texture("#side").end();
    }

    private void registerTank(BaseBlock block, String prefix) {
        BlockModelBuilder modelTop = tankModel(prefix, "top", true, false);
        BlockModelBuilder modelBottom = tankModel(prefix, "bottom", false, true);
        BlockModelBuilder modelMiddle = tankModel(prefix, "middle", false, false);
        BlockModelBuilder modelBoth = tankModel(prefix, "both", true, true);
        tankItemModel(prefix, modelBoth);

        VariantBlockStateBuilder builder = getVariantBuilder(block);
        builder.partialState()
                .with(BlockProperties.LOWER, false).with(BlockProperties.UPPER, false)
                .modelForState().modelFile(modelBoth).addModel();
        builder.partialState()
                .with(BlockProperties.LOWER, true).with(BlockProperties.UPPER, false)
                .modelForState().modelFile(modelTop).addModel();
        builder.partialState()
                .with(BlockProperties.LOWER, false).with(BlockProperties.UPPER, true)
                .modelForState().modelFile(modelBottom).addModel();
        builder.partialState()
                .with(BlockProperties.LOWER, true).with(BlockProperties.UPPER, true)
                .modelForState().modelFile(modelMiddle).addModel();
    }

    private BlockModelBuilder tankModel(String prefix, String variant, boolean top, boolean bottom) {
        String name = BLOCK_FOLDER + "/machines/" + prefix + "_tank_" + variant;
        BlockModelBuilder frame = models().getBuilder(name)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("particle", modLoc(TXT_BLACKMARBLE))
                .texture("frame", modLoc(TXT_BLACKMARBLE))
                .renderType("minecraft:solid");
        createFrame(frame, "#frame", 1f, top, bottom);
        return frame;
    }

    private void tankItemModel(String prefix, BlockModelBuilder frameModel) {
        String name = BLOCK_FOLDER + "/machines/" + prefix + "_tank_inventory";
        BlockModelBuilder frame = new BlockModelBuilder(modLoc(name + "_frame"), models().existingFileHelper)
                .parent(frameModel);
        BlockModelBuilder window = new BlockModelBuilder(modLoc(name + "_window"), models().existingFileHelper);
        window.element().from(1, 0, 1).to(15, 16, 15)
                .face(EAST).texture("#window").end()
                .face(WEST).texture("#window").end()
                .face(SOUTH).texture("#window").end()
                .face(NORTH).texture("#window").end()
                .end()
                .texture("window", modLoc(BLOCK_FOLDER + "/machines/" + prefix + "_tank_window"))
                .renderType("minecraft:translucent");

        models().getBuilder(name)
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("particle", modLoc(TXT_BLACKMARBLE))
                .customLoader(CompositeModelBuilder::begin)
                .child("frame", frame)
                .child("window", window)
                .end();
    }

    private void registerRamp() {
        BlockModelBuilder model = models().withExistingParent(BLOCK_FOLDER + "/decorative/ramp", mcLoc("block/block"));
        float step = 16.0f / 5;
        for (int i = 0; i < 5; i++) {
            innerCube(model, "#all", 0, i * step, i * step, 16, (i + 1) * step, (i + 1) * step);
        }
        model.texture("all", modLoc(TXT_BLACKMARBLE)).texture("particle", "#all");
        horizontalOrientedBlock(Registration.RAMP.get(), model);
    }

    private void registerNodes() {
        registerNode("input_item", Registration.INPUT_ITEM_NODE.get());
        registerNode("output_item", Registration.OUTPUT_ITEM_NODE.get());
        registerNode("sensor_item", Registration.SENSOR_ITEM_NODE.get());
        registerNode("round_robin", Registration.ROUND_ROBIN_NODE.get());
    }

    private void registerFluxBeams() {
        BlockModelBuilder beamModel = models().withExistingParent(BLOCK_FOLDER + "/utility/flux_beam", mcLoc("block/block"))
                .texture("particle", "#side");
        beamModel.element().from(0, 0, 5).to(16, 3, 11)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").end()
                .face(NORTH).uvs(0, 11, 16, 16).texture("#side").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 11, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 11, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 11, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .texture("bottom", modLoc(TXT_FLUXBEAM))
                .texture("top", modLoc(TXT_FLUXBEAM))
                .texture("side", modLoc(TXT_FLUXBEAM));
        horizontalOrientedBlock(Registration.FLUX_BEAM.get(), beamModel);

        BlockModelBuilder bendModel = models().withExistingParent(BLOCK_FOLDER + "/utility/flux_bend_beam", mcLoc("block/block"))
                .texture("particle", "#side");
        bendModel.element().from(0, 0, 5).to(11, 3, 11)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 5, 11, 11).texture("#top").end()
                .face(NORTH).uvs(0, 11, 16, 16).texture("#side").end()
                .face(SOUTH).uvs(0, 11, 16, 16).texture("#side").end()
                .face(WEST).uvs(0, 11, 16, 16).texture("#side").end()
                .face(EAST).uvs(0, 11, 16, 16).texture("#side").end()
                .end()
                .element().from(5, 0, 11).to(11, 3, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(5, 11, 11, 16).texture("#top").end()
                .face(NORTH).uvs(0, 11, 16, 16).texture("#side").end()
                .face(SOUTH).uvs(0, 11, 16, 16).texture("#side").end()
                .face(WEST).uvs(0, 11, 16, 16).texture("#side").end()
                .face(EAST).uvs(0, 11, 16, 16).texture("#side").end()
                .end()
                .texture("bottom", modLoc(TXT_FLUXBEAM))
                .texture("top", modLoc("block/machines/flux_bend_beam"))
                .texture("side", modLoc(TXT_FLUXBEAM));
        horizontalOrientedBlock(Registration.FLUX_BEND_BEAM.get(), bendModel);
    }

    private void registerMarbleSlabs() {
        for (MarbleColor color : MarbleColor.VALUES) {
            VariantBlockStateBuilder builder = getVariantBuilder(DecorativeBlockModule.MARBLE_SLAB.get(color).get());
            ResourceLocation marble = modLoc("block/marble/" + color.getSerializedName() + "marble_smooth");
            String name = BLOCK_FOLDER + "/decorative/marble_slab_" + color.getSerializedName();
            builder
                    .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(new ConfiguredModel(models().slab(name, marble, marble, marble)))
                    .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(new ConfiguredModel(models().slabTop(name + "_top", marble, marble, marble)))
                    .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(new ConfiguredModel(models().getExistingFile(modLoc("block/decorative/double_marble_slab_" + color.getSerializedName()))));
        }
    }

    private void registerOres() {
        simpleBlock(Registration.ORE_LITHIUM.get(), models().cubeAll(BLOCK_FOLDER + "/ores/lithium", modLoc(TXT_ORE_LITHIUMORE)));
        simpleBlock(Registration.ORE_MANGANESE.get(), models().cubeAll(BLOCK_FOLDER + "/ores/manganese", modLoc(TXT_ORE_MANGANESEORE)));
        simpleBlock(Registration.ORE_SILICON.get(), models().cubeAll(BLOCK_FOLDER + "/ores/silicon", modLoc(TXT_ORE_SILICONORE)));
        simpleBlock(Registration.ORE_SILVER.get(), models().cubeAll(BLOCK_FOLDER + "/ores/silver", modLoc(TXT_ORE_SILVERORE)));
        simpleBlock(Registration.ORE_PLATINUM.get(), models().cubeAll(BLOCK_FOLDER + "/ores/platinum", modLoc(TXT_ORE_PLATINUMORE)));
        simpleBlock(Registration.ORE_POSIRITE.get(), models().cubeAll(BLOCK_FOLDER + "/ores/posirite", modLoc(TXT_ORE_POSIRITE)));
        simpleBlock(Registration.ORE_NEGARITE.get(), models().cubeAll(BLOCK_FOLDER + "/ores/negarite", modLoc(TXT_ORE_NEGARITE)));
    }

    private void registerLevelMarker() {
        BlockModelBuilder model = createdParentedModel("/utility/level_marker", thin, TXT_BLACKMARBLE, "top", TXT_LEVEL_MARKER, "bottom", TXT_BLACKMARBLE);
        simpleBlock(Registration.LEVEL_MARKER.get(), model);
    }

    private void registerDoorMarker() {
        BlockModelBuilder display = createdParentedModel("/utility/display_base", thin, TXT_BLACKMARBLE,
                "top", "block/machines/display_base", "bottom", TXT_BLACKMARBLE);
        horizontalOrientedBlock(Registration.DISPLAY_BASE.get(), display);
        BlockModelBuilder model = createdParentedModel("/utility/door_marker", thin, TXT_BLACKMARBLE, "top", TXT_DOOR_MARKER, "bottom", TXT_BLACKMARBLE);
        horizontalOrientedBlock(Registration.DOOR_MARKER.get(), model);
    }

    private void registerFieldMarker() {
        BlockModelBuilder model = createdParentedModel("/utility/field_marker", thin, TXT_BLACKMARBLE, "top", TXT_FIELD_MARKER, "bottom", TXT_BLACKMARBLE);
        simpleBlock(Registration.FIELD_MARKER.get(), model);
    }

    private void registerFences() {
        paneBlock(Registration.BLUE_GLASS_FENCE.get(), BLOCK_FOLDER + "/decorative/blue_glass", modLoc(TXT_BLUE_GLASS_FENCE), modLoc(TXT_BLUE_GLASS_FENCE));
        paneBlock(Registration.GLASS_FENCE.get(), BLOCK_FOLDER + "/decorative/glass_fence", modLoc(TXT_GLASS_FENCE), modLoc(TXT_GLASS_FENCE));
        paneBlock(Registration.MARBLE_FENCE.get(), BLOCK_FOLDER + "/decorative/marble_fence", modLoc(TXT_MARBLE_FENCE), modLoc(TXT_MARBLE_FENCE));
        paneBlock(Registration.TECH_FENCE.get(), BLOCK_FOLDER + "/decorative/tech_fence", modLoc(TXT_TECH_FENCE), modLoc(TXT_TECH_FENCE));
    }

    private void registerPattern() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.PATTERN.get());
        for (PatternType type : PatternType.values()) {
            builder.partialState().with(PatternBlock.TYPE, type)
                    .modelForState()
                    .modelFile(models().cubeAll(BLOCK_FOLDER + "/decorative/pattern_" + type.getSerializedName(), modLoc("block/pattern/" + type.getTexture())))
                    .addModel();
        }
    }

    private void registerMarble() {
        for (Map.Entry<MarbleColor, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE.entrySet()) {
                MarbleColor type = entry.getKey();
                singleTextureBlock(entry.getValue().get(), BLOCK_FOLDER + "/decorative/marble_" + type.getSerializedName(), "block/marble/" + type.getTexture());
        }
    }

    private void registerMarbleSmooth() {
        for (Map.Entry<MarbleColor, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE_SMOOTH.entrySet()) {
                MarbleColor type = entry.getKey();
                singleTextureBlock(entry.getValue().get(), BLOCK_FOLDER + "/decorative/marble_smooth_" + type.getSerializedName(), "block/marble/" + type.getTexture() + "_smooth");
        }
    }

    private void registerMarblePilar() {
        for (Map.Entry<MarbleColor, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE_PILAR.entrySet()) {
                MarbleColor type = entry.getKey();
                singleTextureBlock(entry.getValue().get(), BLOCK_FOLDER + "/decorative/marble_pilar_" + type.getSerializedName(), "block/marble/" + type.getTexture() + "_pilar");
        }
    }

    private void registerMarbleBricks() {
        for (Map.Entry<MarbleColor, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE_BRICKS.entrySet()) {
                MarbleColor type = entry.getKey();
                singleTextureBlock(entry.getValue().get(), BLOCK_FOLDER + "/decorative/marble_bricks_" + type.getSerializedName(), "block/marble/" + type.getTexture() + "_bricks");
        }
    }

    private void registerMarbleTech() {
        for (Map.Entry<MarbleType, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.MARBLE_TECH.entrySet()) {
                MarbleType type = entry.getKey();
                singleTextureBlock(entry.getValue().get(), BLOCK_FOLDER + "/decorative/marbletech_" + type.getSerializedName(), "block/marbletech/" + type.getTexture());
        }
    }

    private void registerBlackTech() {
        for (Map.Entry<TechType, RegistryObject<BaseBlock>> entry : DecorativeBlockModule.BLACK_TECH.entrySet()) {
                TechType type = entry.getKey();
                singleTextureBlock(entry.getValue().get(), BLOCK_FOLDER + "/decorative/blacktech_" + type.getSerializedName(), "block/blacktech/" + type.getTexture());
        }
    }

    private void registerAlarm() {
        BlockModelBuilder alarmModelSafe = createdParentedModel("/machines/alarm_safe", flatglow, TXT_MACHINEBOTTOM, "front", TXT_ALARM_SAFE, "back", TXT_MACHINEBOTTOM);
        BlockModelBuilder alarmModelDead = createdParentedModel("/machines/alarm_dead", flatglow, TXT_MACHINEBOTTOM, "front", TXT_ALARM_DEAD, "back", TXT_MACHINEBOTTOM);
        BlockModelBuilder alarmModelAlert = createdParentedModel("/machines/alarm_alert", flatglow, TXT_MACHINEBOTTOM, "front", TXT_ALARM_ALERT, "back", TXT_MACHINEBOTTOM);
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.ALARM.get());
        for (Pair<AlarmType, BlockModelBuilder> pair : Arrays.asList(Pair.of(AlarmType.SAFE, alarmModelSafe),
                Pair.of(AlarmType.ALERT, alarmModelAlert), Pair.of(AlarmType.DEAD, alarmModelDead))) {
            AlarmType type = pair.getKey();
            BlockModelBuilder model = pair.getValue();
            for (Direction direction : OrientationTools.DIRECTION_VALUES) {
                applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(AlarmTile.ALARM, type)
                        .modelForState().modelFile(model), direction);
            }
        }
    }

    private void registerAICore() {
        BlockModelBuilder frame = new BlockModelBuilder(modLoc("block/aicore_frame"), models().existingFileHelper);
        createFrame(frame, "#frame", 2f);
        frame.texture("frame", modLoc(TXT_BLACKMARBLE)).renderType("minecraft:solid");
        BlockModelBuilder window = new BlockModelBuilder(modLoc("block/aicore_glass"), models().existingFileHelper);
        innerCube(window, "#window", 2, 2, 2, 14, 14, 14);
        window.texture("window", modLoc(TXT_AICORE_WINDOW)).renderType("minecraft:translucent");
        BlockModelBuilder core = models().getBuilder(BLOCK_FOLDER + "/aicore")
                .parent(models().getExistingFile(mcLoc("block/block")))
                .texture("particle", modLoc(TXT_BLACKMARBLE))
                .customLoader(CompositeModelBuilder::begin)
                .child("frame", frame)
                .child("window", window)
                .end();
        simpleBlock(Registration.AICORE.get(), core);
    }

    private void registerBaseModels() {
        flatglow = models().withExistingParent(BLOCK_FOLDER + "/base/flatglow", mcLoc("block"))
                .texture("particle", "#front");
        // @todo support CTM
        flatglow.parent(models().getExistingFile(mcLoc("block")));
        flatglow.element().from(2, 2, 13).to(14, 14, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").end()
                .end();

        frontglow = models().withExistingParent(BLOCK_FOLDER + "/base/frontglow", mcLoc("block"))
                .texture("particle", "#front");
        // @todo support CTM
        frontglow.element().from(0, 0, 0).to(16, 16, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .texture("side", modLoc(TXT_MACHINESIDE))
                .texture("bottom", modLoc(TXT_MACHINEBOTTOM))
                .texture("top", modLoc(TXT_MACHINEBOTTOM));

        topglow = models().withExistingParent(BLOCK_FOLDER + "/base/topglow", mcLoc("block"))
                .texture("particle", "#top");
        // @todo support CTM
        topglow.element().from(0, 0, 0).to(16, 16, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#side").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end();

        front = models().withExistingParent(BLOCK_FOLDER + "/base/front", mcLoc("block"))
                .texture("particle", "#front");
        front.element().from(0, 0, 0).to(16, 16, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .texture("side", modLoc(TXT_MACHINESIDE))
                .texture("bottom", modLoc(TXT_MACHINEBOTTOM))
                .texture("top", modLoc(TXT_MACHINETOP));

        fullglow = models().withExistingParent(BLOCK_FOLDER + "/base/fullglow", mcLoc("block"))
                .texture("particle", "#all");
        // @todo support CTM
        fullglow.element().from(0, 0, 0).to(16, 16, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#all").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#all").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#all").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#all").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#all").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#all").cullface(EAST).end()
                .end();

        sideglow = models().withExistingParent(BLOCK_FOLDER + "/base/sideglow", mcLoc("block"))
                .texture("particle", "#side");
        // @todo support CTM
        sideglow.element().from(0, 0, 0).to(16, 16, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#side").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .texture("bottom", modLoc(TXT_MACHINEBOTTOM))
                .texture("top", modLoc(TXT_MACHINETOP));

        thin = models().withExistingParent(BLOCK_FOLDER + "/base/thin", mcLoc("block"))
                .texture("particle", "#top");
        thin.element().from(0, 0, 0).to(16, 1, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#side").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end();

        nodeblock_tl = models().withExistingParent(BLOCK_FOLDER + "/base/nodeblock_topleft", mcLoc("block"))
                .texture("particle", "#front");
        nodeblock_tl.element().from(2, 10, 14).to(6, 14, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").end()
                .end();

        nodeblock_tr = models().withExistingParent(BLOCK_FOLDER + "/base/nodeblock_topright", mcLoc("block"))
                .texture("particle", "#front");
        nodeblock_tr.element().from(10, 10, 14).to(14, 14, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").end()
                .end();

        nodeblock_bl = models().withExistingParent(BLOCK_FOLDER + "/base/nodeblock_botleft", mcLoc("block"))
                .texture("particle", "#front");
        nodeblock_bl.element().from(2, 2, 14).to(6, 6, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").end()
                .end();

        nodeblock_br = models().withExistingParent(BLOCK_FOLDER + "/base/nodeblock_botright", mcLoc("block"))
                .texture("particle", "#front");
        nodeblock_br.element().from(10, 2, 14).to(14, 6, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").end()
                .end();

        empty = models().getBuilder(BLOCK_FOLDER + "/base/empty")
                .texture("particle", "#empty");
        empty.element().from(0, 0, 0).to(0, 0, 0)
                .face(DOWN).texture("#empty").cullface(DOWN).end()
                .end()
                .texture("empty", modLoc("block/base/empty_glow"));
    }

    private void slabBlock(VariantBlockStateBuilder builder, String name, ResourceLocation doubleslab, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        slabBlock(builder, models().slab(name, side, bottom, top), models().slabTop(name + "_top", side, bottom, top), models().getExistingFile(doubleslab));
    }

    private void slabBlock(VariantBlockStateBuilder builder, ModelFile bottom, ModelFile top, ModelFile doubleslab) {
        builder
                .partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(new ConfiguredModel(bottom))
                .partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(new ConfiguredModel(top))
                .partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(new ConfiguredModel(doubleslab));
    }

    private void registerHorizontalParentedBlock(Block block, BlockModelBuilder parent, String modelName, String textureName) {
        BlockModelBuilder model = models().getBuilder(BLOCK_FOLDER + "/" + modelName)
                .parent(parent)
                .texture("front", modLoc(textureName));
        horizontalOrientedBlock(block, model);
    }

    private void registerOrientedParentedBlock(Block block, BlockModelBuilder parent, String modelName, String textureName) {
        BlockModelBuilder model = models().getBuilder(BLOCK_FOLDER + "/" + modelName)
                .parent(parent)
                .texture("front", modLoc(textureName));
        orientedBlock(block, model);
    }

    private Map<String, BlockModelBuilder> getPartModelMap(Function<Pair<String,BlockModelBuilder>, BlockModelBuilder> transformer) {
        Map<String, BlockModelBuilder> modelMap = new HashMap<>();
        modelMap.put("tl", transformer.apply(Pair.of("tl", nodeblock_tl)));
        modelMap.put("tr", transformer.apply(Pair.of("tr", nodeblock_tr)));
        modelMap.put("bl", transformer.apply(Pair.of("bl", nodeblock_bl)));
        modelMap.put("br", transformer.apply(Pair.of("br", nodeblock_br)));
        return modelMap;
    }

    private void registerNode(String nodeName, BaseBlock block) {
        ResourceLocation texture = modLoc(TXT_MACHINEBOTTOM);
        ResourceLocation frontTexture = modLoc("block/machines/" + nodeName + "_node");
        Map<String, BlockModelBuilder> modelMap = getPartModelMap(pair -> models().getBuilder("block/utility/" + nodeName + "_node_" + pair.getKey())
                .parent(pair.getValue())
                .texture("front", frontTexture)
                .texture("back", texture)
                .texture("side", texture));
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (NodeOrientation orientation : NodeOrientation.VALUES) {
            ConfiguredModel.Builder<VariantBlockStateBuilder> bld = builder.partialState().with(AbstractNodeTile.ORIENTATION, orientation)
                    .modelForState().modelFile(modelMap.get(orientation.getModelSuffix()));
            applyRotation(bld, orientation.getMainDirection().getOpposite());
        }
    }

    private void registerWorkingHorizontalGlowBlock(String name, BaseBlock block) {
        BlockModelBuilder offModel = models().getBuilder(BLOCK_FOLDER + "/machines/" + name + "_off")
                .parent(frontglow)
                .texture("front", modLoc("block/machines/" + name + "_off"));
        BlockModelBuilder onModel = models().getBuilder("block/machines/" + name + "_on")
                .parent(frontglow)
                .texture("front", modLoc("block/machines/" + name + "_on"));
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (Direction direction : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
            applyHorizRotation(builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction).with(BlockProperties.WORKING, false)
                    .modelForState().modelFile(offModel), direction);
            applyHorizRotation(builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction).with(BlockProperties.WORKING, true)
                    .modelForState().modelFile(onModel), direction);
        }
    }

}
