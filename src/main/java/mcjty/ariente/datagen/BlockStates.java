package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.AlarmType;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.api.MarbleType;
import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.decorative.PatternBlock;
import mcjty.ariente.blocks.decorative.PatternType;
import mcjty.ariente.blocks.utility.AlarmTile;
import mcjty.ariente.blocks.utility.AutoConstructorTile;
import mcjty.ariente.blocks.utility.autofield.AbstractNodeTile;
import mcjty.ariente.blocks.utility.autofield.NodeOrientation;
import mcjty.ariente.setup.Registration;
import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

import static net.minecraft.util.Direction.*;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Ariente.MODID, exFileHelper);
    }

    private BlockModelBuilder flatglow;
    private BlockModelBuilder frontglow;
    private BlockModelBuilder fullglow;
    private BlockModelBuilder front;
    private BlockModelBuilder thin;
    private BlockModelBuilder nodeblock_tl;
    private BlockModelBuilder nodeblock_tr;
    private BlockModelBuilder nodeblock_bl;
    private BlockModelBuilder nodeblock_br;

    @Override
    protected void registerStatesAndModels() {
        registerBaseModels();
        registerAICore();
        registerAlarm();
        registerAutoConstructor();
        registerBlackTech();
        registerMarbleTech();
        registerMarble();
        registerMarbleSmooth();
        registerMarblePilar();
        registerMarbleBricks();
        registerPattern();
        registerDoorMarker();
        registerFences();
        registerOres();
        registerMarbleSlabs();
        registerFluxBeams();

        ResourceLocation texture = modLoc("block/base/machinebottom");
        ResourceLocation frontTexture = modLoc("block/machines/sensor_item_node");
        BlockModelBuilder sensorModelTL = models().getBuilder("block/utility/sensor_item_node")
                .parent(nodeblock_tl)
                .texture("front", frontTexture)
                .texture("back", texture)
                .texture("side", texture);
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.SENSOR_ITEM_NODE.get());
        for (NodeOrientation orientation : NodeOrientation.VALUES) {
            ConfiguredModel.Builder<VariantBlockStateBuilder> bld = builder.partialState().with(AbstractNodeTile.ORIENTATION, orientation)
                    .modelForState().modelFile(sensorModelTL);
            applyRotation(bld, orientation.getMainDirection());
//            bld.
            // @todo
            bld.addModel();
        }

        registerHorizontalParentedBlock(Registration.AUTOMATION_FIELD.get(), front, "block/machines/automation_field", "block/machines/automation_field");
        registerHorizontalParentedBlock(Registration.BLUEPRINT_STORAGE.get(), front, "block/machines/blueprint_storage", "block/machines/blueprint_storage");
        registerHorizontalParentedBlock(Registration.CONSTRUCTOR.get(), frontglow, "block/machines/constructor", "block/machines/constructor");
    }

    private void registerFluxBeams() {
        BlockModelBuilder beamModel = models().getBuilder("block/utility/flux_beam");
        beamModel.element().from(0, 0, 5).to(16, 3, 11)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").end()
                .face(NORTH).uvs(0, 11, 16, 16).texture("#side").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 11, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 11, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 11, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .texture("bottom", modLoc("block/machines/flux_beam"))
                .texture("top", modLoc("block/machines/flux_beam"))
                .texture("side", modLoc("block/machines/flux_beam"));
        horizontalOrientedBlock(Registration.FLUX_BEAM.get(), beamModel);

        BlockModelBuilder bendModel = models().getBuilder("block/utility/flux_bend_beam");
        bendModel.element().from(0, 0, 5).to(11, 3, 11)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").end()
                .face(NORTH).uvs(0, 11, 16, 16).texture("#side").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 11, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 11, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 11, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .element().from(5, 0, 11).to(11, 3, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").end()
                .face(NORTH).uvs(0, 11, 16, 16).texture("#side").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 11, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 11, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 11, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .texture("bottom", modLoc("block/machines/flux_beam"))
                .texture("top", modLoc("block/machines/flux_beam"))
                .texture("side", modLoc("block/machines/flux_beam"));
        horizontalOrientedBlock(Registration.FLUX_BEND_BEAM.get(), bendModel);
    }

    private void registerMarbleSlabs() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.MARBLE_SLAB.get());
        for (MarbleColor color : MarbleColor.VALUES) {
            ResourceLocation marble = modLoc("block/marble/" + color.getName() + "marble_smooth");
            BlockModelBuilder model = models().cubeBottomTop("block/decorative/double_marble_slab_" + color.getName(),
                    modLoc("block/marble/" + color.getName() + "marble_double_slab"), marble, marble);
            slabBlock(builder, "block/decorative/marble_slab_" + color.getName(),
                    modLoc("block/decorative/double_marble_slab_" + color.getName()), marble, marble, marble);
        }
    }

    private void registerOres() {
        simpleBlock(Registration.ORE_LITHIUM.get(), models().cubeAll("block/ores/lithium", modLoc("block/ores/lithiumore")));
        simpleBlock(Registration.ORE_MANGANESE.get(), models().cubeAll("block/ores/manganese", modLoc("block/ores/manganeseore")));
        simpleBlock(Registration.ORE_SILICON.get(), models().cubeAll("block/ores/silicon", modLoc("block/ores/siliconeore")));
        simpleBlock(Registration.ORE_SILVER.get(), models().cubeAll("block/ores/silver", modLoc("block/ores/silverore")));
        simpleBlock(Registration.ORE_PLATINUM.get(), models().cubeAll("block/ores/platinum", modLoc("block/ores/platinumore")));
        simpleBlock(Registration.ORE_POSIRITE.get(), models().cubeAll("block/ores/posirite", modLoc("block/ores/posirite")));
        simpleBlock(Registration.ORE_NEGARITE.get(), models().cubeAll("block/ores/negarite", modLoc("block/ores/negarite")));
    }

    private void registerDoorMarker() {
        BlockModelBuilder model = models().getBuilder("block/utility/door_marker")
                .parent(thin)
                .texture("side", modLoc("block/marble/blackmarble"))
                .texture("top", modLoc("block/machines/door_marker"))
                .texture("bottom", modLoc("block/marble/blackmarble"));
        simpleBlock(Registration.DOOR_MARKER.get(), model);
    }

    private void registerFences() {
        paneBlock(Registration.BLUE_GLASS_FENCE.get(), "block/decorative/blue_glass", modLoc("block/decorative/blue_glass_fence"), modLoc("block/decorative/blue_glass_fence"));
        paneBlock(Registration.GLASS_FENCE.get(), "block/decorative/glass_fence", modLoc("block/decorative/glass_fence"), modLoc("block/decorative/glass_fence"));
        paneBlock(Registration.MARBLE_FENCE.get(), "block/decorative/marble_fence", modLoc("block/decorative/marble_fence"), modLoc("block/decorative/marble_fence"));
        paneBlock(Registration.TECH_FENCE.get(), "block/decorative/tech_fence", modLoc("block/decorative/tech_fence"), modLoc("block/decorative/tech_fence"));
    }

    private void registerPattern() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.PATTERN.get());
        for (PatternType type : PatternType.values()) {
            builder.partialState().with(PatternBlock.TYPE, type)
                    .modelForState()
                    .modelFile(models().cubeAll("block/decorative/pattern_" + type.getName(), modLoc("block/pattern/" + type.getTexture())))
                    .addModel();
        }
    }

    private void registerMarble() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.MARBLE.get());
        for (MarbleColor type : MarbleColor.values()) {
            builder.partialState().with(MarbleColor.COLOR, type)
                    .modelForState()
                    .modelFile(models().cubeAll("block/decorative/marble_" + type.getName(), modLoc("block/marble/" + type.getTexture())))
                    .addModel();
        }
    }

    private void registerMarbleSmooth() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.MARBLE_SMOOTH.get());
        for (MarbleColor type : MarbleColor.values()) {
            builder.partialState().with(MarbleColor.COLOR, type)
                    .modelForState()
                    .modelFile(models().cubeAll("block/decorative/marble_smooth" + type.getName(), modLoc("block/marble/" + type.getTexture() + "_smooth")))
                    .addModel();
        }
    }

    private void registerMarblePilar() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.MARBLE_PILAR.get());
        for (MarbleColor type : MarbleColor.values()) {
            builder.partialState().with(MarbleColor.COLOR, type)
                    .modelForState()
                    .modelFile(models().cubeAll("block/decorative/marble_pilar" + type.getName(), modLoc("block/marble/" + type.getTexture() + "_pilar")))
                    .addModel();
        }
    }

    private void registerMarbleBricks() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.MARBLE_BRICKS.get());
        for (MarbleColor type : MarbleColor.values()) {
            builder.partialState().with(MarbleColor.COLOR, type)
                    .modelForState()
                    .modelFile(models().cubeAll("block/decorative/marble_bricks" + type.getName(), modLoc("block/marble/" + type.getTexture() + "_bricks")))
                    .addModel();
        }
    }

    private void registerMarbleTech() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.MARBLE_TECH.get());
        for (MarbleType type : MarbleType.values()) {
            builder.partialState().with(MarbleType.TYPE, type)
                    .modelForState()
                    .modelFile(models().cubeAll("block/decorative/marbletech_" + type.getName(), modLoc("block/marbletech/" + type.getTexture())))
                    .addModel();
        }
    }

    private void registerBlackTech() {
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.BLACK_TECH.get());
        for (TechType type : TechType.values()) {
            builder.partialState().with(TechType.TYPE, type)
                    .modelForState()
                    .modelFile(fullGlowModel("block/decorative/blacktech_" + type.getName(), "block/blacktech/" + type.getTexture(), "block/blacktech/" + type.getTextureGlow()))
                    .addModel();
        }
    }

    private void registerAutoConstructor() {
        BlockModelBuilder autoConstructorOffModel = models().getBuilder("block/machines/auto_constructor_off")
                .parent(frontglow)
                .texture("front", modLoc("block/machines/auto_constructor_off"));
        BlockModelBuilder autoConstructorOnModel = models().getBuilder("block/machines/auto_constructor_on")
                .parent(frontglow)
                .texture("front", modLoc("block/machines/auto_constructor"));
        VariantBlockStateBuilder builder = getVariantBuilder(Registration.AUTO_CONSTRUCTOR.get());
        for (Direction direction : OrientationTools.HORIZONTAL_DIRECTION_VALUES) {
            applyHorizRotation(builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction).with(AutoConstructorTile.WORKING, false)
                    .modelForState().modelFile(autoConstructorOffModel), direction);
            applyHorizRotation(builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, direction).with(AutoConstructorTile.WORKING, true)
                    .modelForState().modelFile(autoConstructorOnModel), direction);
        }
    }

    private void registerAlarm() {
        BlockModelBuilder alarmModelSafe = models().getBuilder("block/machines/alarm_safe")
                .parent(flatglow)
                .texture("side", modLoc("block/base/machinebottom"))
                .texture("front", modLoc("block/machines/alarm_safe"))
                .texture("back", modLoc("block/base/machinebottom"));
        BlockModelBuilder alarmModelDead = models().getBuilder("block/machines/alarm_dead")
                .parent(flatglow)
                .texture("side", modLoc("block/base/machinebottom"))
                .texture("front", modLoc("block/machines/alarm_dead"))
                .texture("back", modLoc("block/base/machinebottom"));
        BlockModelBuilder alarmModelAlert = models().getBuilder("block/machines/alarm_alert")
                .parent(flatglow)
                .texture("side", modLoc("block/base/machinebottom"))
                .texture("front", modLoc("block/machines/alarm_alert"))
                .texture("back", modLoc("block/base/machinebottom"));
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
        BlockModelBuilder frame = models().getBuilder("block/aicore");
        createFrame(frame, "#frame", 2f);
        innerCube(frame, "#window", 2f, 2f, 2f, 14f, 14f, 14f);
        frame.texture("window", modLoc("block/machines/negarite_tank_window"))
                .texture("frame", modLoc("block/marble/blackmarble"));
        simpleBlock(Registration.AICORE.get(), frame);
    }

    private void registerBaseModels() {
        flatglow = models().getBuilder("/block/base/flatglow");
        // @todo support CTM
        flatglow.element().from(2, 2, 13).to(14, 14, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").end()
                .end();

        frontglow = models().getBuilder("/block/base/frontglow");
        // @todo support CTM
        frontglow.element().from(0, 0, 0).to(16, 16, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .texture("side", modLoc("block/base/machineside"))
                .texture("bottom", modLoc("block/base/machinebottom"))
                .texture("top", modLoc("block/base/machinebottom"));

        front = models().getBuilder("/block/base/front");
        front.element().from(0, 0, 0).to(16, 16, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end()
                .texture("side", modLoc("block/base/machineside"))
                .texture("bottom", modLoc("block/base/machinebottom"))
                .texture("top", modLoc("block/base/machinebottom"));

        fullglow = models().getBuilder("/block/base/fullglow");
        // @todo support CTM
        fullglow.element().from(0, 0, 0).to(16, 16, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#all").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#all").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#all").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#all").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#all").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#all").cullface(EAST).end()
                .end();

        thin = models().getBuilder("/block/base/thin");
        thin.element().from(0, 0, 0).to(16, 1, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#top").end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#side").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#side").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end();

        nodeblock_tl = models().getBuilder("/block/base/nodeblock_topleft");
        nodeblock_tl.element().from(2, 2, 14).to(6, 6, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end();

        nodeblock_tr = models().getBuilder("/block/base/nodeblock_topright");
        nodeblock_tr.element().from(10, 2, 14).to(14, 6, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end();

        nodeblock_bl = models().getBuilder("/block/base/nodeblock_botleft");
        nodeblock_bl.element().from(2, 10, 14).to(6, 14, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end();

        nodeblock_br = models().getBuilder("/block/base/nodeblock_botright");
        nodeblock_br.element().from(10, 10, 14).to(14, 14, 16)
                .face(DOWN).uvs(0, 0, 16, 16).texture("#side").cullface(DOWN).end()
                .face(UP).uvs(0, 0, 16, 16).texture("#side").cullface(UP).end()
                .face(NORTH).uvs(0, 0, 16, 16).texture("#front").cullface(NORTH).end()
                .face(SOUTH).uvs(0, 0, 16, 16).texture("#back").cullface(SOUTH).end()
                .face(WEST).uvs(0, 0, 16, 16).texture("#side").cullface(WEST).end()
                .face(EAST).uvs(0, 0, 16, 16).texture("#side").cullface(EAST).end()
                .end();
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

    private BlockModelBuilder fullGlowModel(String modelName, String texture, String glowTexture) {
        return models().getBuilder(modelName)
                .parent(fullglow)
                .texture("all", modLoc(texture));
    }

    private void registerHorizontalParentedBlock(Block block, BlockModelBuilder parent, String modelName, String textureName) {
        BlockModelBuilder model = models().getBuilder(modelName)
                .parent(parent)
                .texture("front", modLoc(textureName));
        horizontalOrientedBlock(block, model);
    }

}
