package mcjty.ariente;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class MountedDeviceShapeTests {

    @GameTest(template = "empty", batch = "mounted_devices")
    public static void selectionsMatchModelsForEveryVariant(GameTestHelper helper) throws IOException {
        List<Block> blocks = List.of(Registration.LOCK.get(), Registration.WIRELESS_LOCK.get(),
                Registration.SIGNAL_TRANSMITTER.get(), Registration.SIGNAL_RECEIVER.get(),
                Registration.WIRELESS_BUTTON.get(), Registration.ALARM.get(), Registration.FLAT_LIGHT.get(),
                Registration.FIELD_MARKER.get(), Registration.LEVEL_MARKER.get(), Registration.DOOR_MARKER.get(),
                Registration.DISPLAY_BASE.get(), Registration.CAMERA.get(), Registration.INPUT_ITEM_NODE.get(),
                Registration.OUTPUT_ITEM_NODE.get(), Registration.SENSOR_ITEM_NODE.get(), Registration.ROUND_ROBIN_NODE.get());
        for (Block block : blocks) {
            ResourceLocation id = block.builtInRegistryHolder().key().location();
            JsonObject variants = json(id.getNamespace(), "blockstates/" + id.getPath()).getAsJsonObject("variants");
            helper.assertTrue(variants.size() == block.getStateDefinition().getPossibleStates().size(),
                    "Every block state must have a checked model variant: " + id);
            for (var entry : variants.entrySet()) {
                BlockState state = block.defaultBlockState();
                if (!entry.getKey().isEmpty()) {
                    for (String assignment : entry.getKey().split(",")) {
                        String[] pair = assignment.split("=");
                        state = withProperty(state, block.getStateDefinition().getProperty(pair[0]), pair[1]);
                    }
                }
                JsonObject variant = entry.getValue().getAsJsonObject();
                ResourceLocation model = new ResourceLocation(variant.get("model").getAsString());
                VoxelShape expected = modelShape(json(model.getNamespace(), "models/" + model.getPath()), variant);
                VoxelShape actual = state.getShape(helper.getLevel(), BlockPos.ZERO);
                helper.assertTrue(!expected.isEmpty() && !Shapes.joinIsNotEmpty(actual, expected, BooleanOp.NOT_SAME),
                        "Selection must match the model for " + state + ": " + actual.toAabbs() + " vs " + expected.toAabbs());
            }
        }
        helper.succeed();
    }

    private static <T extends Comparable<T>> BlockState withProperty(BlockState state, Property<T> property, String value) {
        return state.setValue(property, property.getValue(value).orElseThrow());
    }

    private static VoxelShape modelShape(JsonObject model, JsonObject variant) throws IOException {
        VoxelShape shape = Shapes.empty();
        if (model.has("elements")) {
            for (var element : model.getAsJsonArray("elements")) {
                shape = Shapes.or(shape, Shapes.create(rotatedBounds(element.getAsJsonObject(), variant)));
            }
        } else if (model.has("children")) {
            for (var child : model.getAsJsonObject("children").entrySet()) {
                shape = Shapes.or(shape, modelShape(child.getValue().getAsJsonObject(), variant));
            }
        } else {
            ResourceLocation parent = new ResourceLocation(model.get("parent").getAsString());
            shape = modelShape(json(parent.getNamespace(), "models/" + parent.getPath()), variant);
        }
        return shape;
    }

    private static AABB rotatedBounds(JsonObject element, JsonObject variant) {
        if (element.has("rotation")) {
            throw new IllegalArgumentException("Mounted-device test expects axis-aligned model elements");
        }
        JsonArray from = element.getAsJsonArray("from");
        JsonArray to = element.getAsJsonArray("to");
        double rx = variant.has("x") ? Math.toRadians(-variant.get("x").getAsInt()) : 0;
        double ry = variant.has("y") ? Math.toRadians(-variant.get("y").getAsInt()) : 0;
        AABB bounds = null;
        for (int corner = 0; corner < 8; corner++) {
            double x = ((corner & 1) == 0 ? from : to).get(0).getAsDouble() / 16 - .5;
            double y = ((corner & 2) == 0 ? from : to).get(1).getAsDouble() / 16 - .5;
            double z = ((corner & 4) == 0 ? from : to).get(2).getAsDouble() / 16 - .5;
            double rotatedY = y * Math.cos(rx) - z * Math.sin(rx);
            double rotatedZ = y * Math.sin(rx) + z * Math.cos(rx);
            Vec3 point = new Vec3(snap(x * Math.cos(ry) + rotatedZ * Math.sin(ry) + .5), snap(rotatedY + .5),
                    snap(-x * Math.sin(ry) + rotatedZ * Math.cos(ry) + .5));
            AABB cornerBounds = new AABB(point, point);
            bounds = bounds == null ? cornerBounds : bounds.minmax(cornerBounds);
        }
        return bounds;
    }

    private static double snap(double coordinate) {
        // Remove roundoff from right-angle blockstate rotations before comparing voxel shapes.
        return Math.rint(coordinate * 1e12) / 1e12;
    }

    private static JsonObject json(String namespace, String path) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(MountedDeviceShapeTests.class.getResourceAsStream(
                "/assets/" + namespace + "/" + path + ".json"), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
