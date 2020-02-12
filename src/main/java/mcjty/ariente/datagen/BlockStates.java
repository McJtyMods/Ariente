package mcjty.ariente.datagen;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.AlarmType;
import mcjty.ariente.api.TechType;
import mcjty.ariente.blocks.utility.AlarmTile;
import mcjty.ariente.blocks.utility.AutoConstructorTile;
import mcjty.ariente.setup.Registration;
import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
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

    @Override
    protected void registerStatesAndModels() {
        registerBaseModels();
        registerAICore();
        registerAlarm();
        registerAutoConstructor();
        registerAutomationField();

        VariantBlockStateBuilder builder = getVariantBuilder(Registration.BLACK_TECH.get());
        for (TechType type : TechType.values()) {
            builder.partialState().with(TechType.TYPE, type)
                    .modelForState()
                    .modelFile(fullGlowModel("block/decoration/blacktech_" + type.getName(), "block/blacktech/" + type.getTexture(), "block/blacktech/" + type.getTextureGlow()))
                    .addModel();
        }

    }

    private void registerAutomationField() {
        BlockModelBuilder model = models().getBuilder("block/machines/automation_field")
                .parent(front)
                .texture("front", modLoc("block/machines/automation_field"));
        horizontalOrientedBlock(Registration.AUTOMATION_FIELD.get(), model);
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
    }

    private BlockModelBuilder fullGlowModel(String modelName, String texture, String glowTexture) {
        return models().getBuilder(modelName)
                .parent(fullglow)
                .texture("all", modLoc(texture));
    }
}
