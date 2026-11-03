package mcjty.ariente;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.ariente.blocks.utility.autofield.AbstractNodeTile;
import mcjty.ariente.blocks.utility.autofield.NodeOrientation;
import mcjty.ariente.blocks.utility.autofield.OutputItemNodeTile;
import mcjty.ariente.blocks.utility.autofield.SensorItemNodeTile;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.gui.components.GuiComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static mcjty.hologui.api.IGuiTile.TAG_MAIN;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class AutoFieldNodeTests {

    @GameTest(template = "empty", batch = "node_ui")
    public static void selectionMatchesAuthoredModelsOnEveryFace(GameTestHelper helper) throws IOException {
        List<Block> blocks = List.of(Registration.INPUT_ITEM_NODE.get(), Registration.OUTPUT_ITEM_NODE.get(), Registration.SENSOR_ITEM_NODE.get());
        for (Block block : blocks) {
            String id = block.builtInRegistryHolder().key().location().getPath();
            JsonObject variants = json("blockstates/" + id).getAsJsonObject("variants");
            for (NodeOrientation orientation : NodeOrientation.VALUES) {
                JsonObject variant = variants.getAsJsonObject("orientation=" + orientation.getSerializedName());
                JsonObject model = json("models/block/base/nodeblock_" + switch (orientation.getModelSuffix()) {
                    case "tl" -> "topleft";
                    case "tr" -> "topright";
                    case "bl" -> "botleft";
                    case "br" -> "botright";
                    default -> throw new IllegalStateException("Unknown model quadrant");
                });
                JsonObject element = model.getAsJsonArray("elements").get(0).getAsJsonObject();
                JsonArray from = element.getAsJsonArray("from");
                JsonArray to = element.getAsJsonArray("to");
                double rx = variant.has("x") ? Math.toRadians(-variant.get("x").getAsInt()) : 0;
                double ry = variant.has("y") ? Math.toRadians(-variant.get("y").getAsInt()) : 0;
                AABB expected = null;
                for (int corner = 0; corner < 8; corner++) {
                    double x = ((corner & 1) == 0 ? from : to).get(0).getAsDouble() / 16 - .5;
                    double y = ((corner & 2) == 0 ? from : to).get(1).getAsDouble() / 16 - .5;
                    double z = ((corner & 4) == 0 ? from : to).get(2).getAsDouble() / 16 - .5;
                    double rotatedY = y * Math.cos(rx) - z * Math.sin(rx);
                    double rotatedZ = y * Math.sin(rx) + z * Math.cos(rx);
                    Vec3 point = new Vec3(x * Math.cos(ry) + rotatedZ * Math.sin(ry) + .5, rotatedY + .5,
                            -x * Math.sin(ry) + rotatedZ * Math.cos(ry) + .5);
                    AABB bounds = new AABB(point, point);
                    expected = expected == null ? bounds : expected.minmax(bounds);
                }
                AABB actual = block.defaultBlockState().setValue(AbstractNodeTile.ORIENTATION, orientation)
                        .getShape(helper.getLevel(), BlockPos.ZERO).bounds();
                helper.assertTrue(new Vec3(actual.minX, actual.minY, actual.minZ).distanceToSqr(new Vec3(expected.minX, expected.minY, expected.minZ)) < 1e-12
                                && new Vec3(actual.maxX, actual.maxY, actual.maxZ).distanceToSqr(new Vec3(expected.maxX, expected.maxY, expected.maxZ)) < 1e-12,
                        "Selection must match model bounds for " + id + "/" + orientation + ": " + actual + " vs " + expected);
            }
        }
        helper.succeed();
    }

    private static JsonObject json(String path) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(AutoFieldNodeTests.class.getResourceAsStream("/assets/ariente/" + path + ".json"), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @GameTest(template = "empty", batch = "node_ui")
    public static void arrowControlsChangeBatchesAndThresholdWithoutOverflow(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        OutputItemNodeTile output = new OutputItemNodeTile(pos, Registration.OUTPUT_ITEM_NODE.get().defaultBlockState());
        output.setLevel(helper.getLevel());
        IGuiComponent<?> panel = output.createGui("UP_NW:" + TAG_MAIN, new GuiComponentRegistry());
        click(helper, panel, "batch_increase_8");
        click(helper, panel, "batch_increase_1");
        helper.assertTrue(output.getOutputStackSize() == 10, "Double/single arrows must increase the batch by 8/1");
        click(helper, panel, "batch_decrease_8");
        click(helper, panel, "batch_decrease_1");
        click(helper, panel, "batch_decrease_8");
        helper.assertTrue(output.getOutputStackSize() == 1, "Batch arrows must respect the minimum of 1");
        output.setOutputStackSize(63);
        click(helper, panel, "batch_increase_8");
        helper.assertTrue(output.getOutputStackSize() == 64, "Batch arrows must respect the maximum of 64");

        SensorItemNodeTile sensor = new SensorItemNodeTile(pos, Registration.SENSOR_ITEM_NODE.get().defaultBlockState());
        sensor.setLevel(helper.getLevel());
        CompoundTag saved = sensor.saveWithoutMetadata();
        saved.getCompound("Info").putInt("amount", 20);
        sensor.load(saved);
        panel = sensor.createGui("UP_NW:" + TAG_MAIN, new GuiComponentRegistry());
        click(helper, panel, "sensor_increase_10");
        helper.assertTrue(sensor.saveWithoutMetadata().getCompound("Info").getInt("amount") == 30, "Sensor double arrow must add 10");
        click(helper, panel, "sensor_decrease_10");
        helper.assertTrue(sensor.saveWithoutMetadata().getCompound("Info").getInt("amount") == 20, "Sensor double arrow must subtract 10");
        saved.getCompound("Info").putInt("amount", 1);
        sensor.load(saved);
        click(helper, panel, "sensor_decrease_10");
        helper.assertTrue(sensor.saveWithoutMetadata().getCompound("Info").getInt("amount") == 1, "Sensor threshold must stay at least 1");
        saved.getCompound("Info").putInt("amount", Integer.MAX_VALUE - 5);
        sensor.load(saved);
        click(helper, panel, "sensor_increase_10");
        helper.assertTrue(sensor.saveWithoutMetadata().getCompound("Info").getInt("amount") == Integer.MAX_VALUE, "Sensor threshold must not overflow");
        helper.succeed();
    }

    private static void click(GameTestHelper helper, IGuiComponent<?> panel, String name) {
        IGuiComponent<?> control = panel.findChild(name).orElseThrow();
        panel.hit(helper.makeMockPlayer(), null, control.getX() + control.getW() / 2, control.getY() + control.getH() / 2);
    }
}
