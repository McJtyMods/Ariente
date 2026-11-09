package mcjty.ariente;

import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.blocks.utility.camera.CameraTile;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.CameraView;
import mcjty.ariente.display.CameraViewClipper;
import mcjty.ariente.display.CameraPerspective;
import mcjty.ariente.display.LiveCameraView;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.joml.Vector3f;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.InstalledModule;
import mcjty.ariente.display.ModuleSettings;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class CameraDisplayTests {

    private static CameraTile camera(GameTestHelper helper, BlockPos pos, Direction normal) {
        helper.getLevel().setBlockAndUpdate(pos, mcjty.ariente.setup.Registration.CAMERA.get().defaultBlockState().setValue(BlockStateProperties.FACING, normal));
        return (CameraTile) helper.getLevel().getBlockEntity(pos);
    }

    private static boolean contains(CompoundTag capture, BlockPos offset) {
        return Arrays.stream(capture.getLongArray("Blocks")).anyMatch(block -> CameraCapture.offset(block).equals(offset));
    }

    private static ItemStack linked(GameTestHelper helper, BlockPos pos) {
        ItemStack stack = new ItemStack(mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get());
        Player player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setShiftKeyDown(true);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get().onItemUseFirst(stack, context) == InteractionResult.SUCCESS, "Sneak click links the module");
        helper.assertTrue(!CameraDisplayModule.cameras(stack).isEmpty(), "The link is stored on the module");
        return stack;
    }

    @GameTest(template = "empty")
    public static void sixMountingDirectionsOffsetTheCaptureOriginBehindTheBase(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 6, 3));
        Player player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(mcjty.ariente.setup.Registration.CAMERA.get()));
        for (Direction facing : Direction.values()) {
            CameraTile camera = camera(helper, pos, facing);
            BlockPos front = pos.relative(facing, 2), back = pos.relative(facing.getOpposite(), 2);
            BlockPos excluded = pos.relative(facing.getOpposite(), 3);
            helper.getLevel().setBlockAndUpdate(pos.relative(facing), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(pos.relative(facing.getOpposite()), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(front, Blocks.DIAMOND_BLOCK.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(back, Blocks.GOLD_BLOCK.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(excluded, Blocks.EMERALD_BLOCK.defaultBlockState());
            CompoundTag capture = camera.capture();
            helper.assertTrue(contains(capture, front.subtract(pos)) && contains(capture, back.subtract(pos)) && !contains(capture, excluded.subtract(pos)),
                    "Capture starts two blocks behind the base for " + facing + ": state=" + camera.getBlockState() + ", capture=" + capture.getInt("Facing"));
            BlockPlaceContext placement = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, player.getMainHandItem(),
                    new BlockHitResult(Vec3.atCenterOf(pos), facing, pos, false));
            helper.assertTrue(mcjty.ariente.setup.Registration.CAMERA.get().getStateForPlacement(placement).getValue(BlockStateProperties.FACING) == facing,
                    "Placement follows the clicked face for " + facing);
            var shape = CameraTile.shape(facing);
            boolean positive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            helper.assertTrue(shape.min(facing.getAxis()) == (positive ? 0 : 7 / 16.0)
                    && shape.max(facing.getAxis()) == (positive ? 9 / 16.0 : 1), "Collision shape follows " + facing);
            helper.getLevel().setBlockAndUpdate(front, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(back, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(excluded, Blocks.AIR.defaultBlockState());
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void obstaclesDoNotHideConnectedSurfaces(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 6, 3));
        camera(helper, pos, Direction.UP);
        helper.getLevel().setBlockAndUpdate(pos.above(2), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(4), Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(CameraCapture.DEFAULT_RANGE + 1), Blocks.GOLD_BLOCK.defaultBlockState());
        CompoundTag blocked = CameraCapture.scan(helper.getLevel(), pos, Direction.UP);
        helper.assertTrue(contains(blocked, new BlockPos(0, 2, 0)) && contains(blocked, new BlockPos(0, 4, 0)), "The camera goes around obstacles to capture the surfaces behind them");
        helper.getLevel().setBlockAndUpdate(pos.above(2), Blocks.GLASS.defaultBlockState());
        CompoundTag glass = CameraCapture.scan(helper.getLevel(), pos, Direction.UP);
        helper.assertTrue(contains(glass, new BlockPos(0, 2, 0)) && contains(glass, new BlockPos(0, 4, 0)), "Glass and the scene beyond it are captured");
        helper.assertTrue(!contains(glass, new BlockPos(0, CameraCapture.DEFAULT_RANGE + 1, 0)), "Capture has a hard range limit");
        helper.assertTrue(glass.getLongArray("Blocks").length <= CameraCapture.MAX_BLOCKS, "Capture size is bounded");
        helper.getLevel().setBlockAndUpdate(pos.above(2), Blocks.OAK_TRAPDOOR.defaultBlockState());
        helper.assertTrue(contains(CameraCapture.scan(helper.getLevel(), pos, Direction.UP), new BlockPos(0, 4, 0)),
                "A freestanding partial block can also be scanned around");
        for (int y = 0; y < 3; y++) {
            helper.getLevel().setBlockAndUpdate(pos.offset(3, y, 2), Blocks.STONE.defaultBlockState());
        }
        helper.getLevel().setBlockAndUpdate(pos.offset(3, 3, 2), Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.assertTrue(contains(CameraCapture.scan(helper.getLevel(), pos, Direction.UP), new BlockPos(3, 3, 2)),
                "Exposed pillar surfaces are captured even when neighboring blocks obscure them");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void linkedModulesShareCapturesAndRefreshAfterOneSecond(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 6, 3));
        CameraTile camera = camera(helper, pos, Direction.UP);
        ItemStack first = linked(helper, pos), second = linked(helper, pos);
        CameraDisplayModule type = (CameraDisplayModule) mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get().type();
        CompoundTag initial = type.sample(helper.getLevel(), first, new CompoundTag());
        helper.getLevel().setBlockAndUpdate(pos.above(2), Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.assertTrue(type.sample(helper.getLevel(), second, new CompoundTag()) == initial, "Modules share the same cached capture");
        helper.assertTrue(!contains(initial, new BlockPos(0, 2, 0)), "A capture remains stable within the sampling interval");
        helper.runAfterDelay(21, () -> {
            CompoundTag updated = camera.capture();
            helper.assertTrue(updated != initial && contains(updated, new BlockPos(0, 2, 0)), "Changed terrain appears in the next capture");
            helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            helper.assertTrue(type.sample(helper.getLevel(), first, updated).getString("Status").equals("Target missing"), "Removing the camera invalidates the feed");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void installationIsImmediateAndHoloControlsPreserveTheLink(GameTestHelper helper) {
        BlockPos cameraPos = helper.absolutePos(new BlockPos(3, 6, 3));
        camera(helper, cameraPos, Direction.UP);
        helper.getLevel().setBlockAndUpdate(cameraPos.above(2), Blocks.DIAMOND_BLOCK.defaultBlockState());
        BlockPos basePos = helper.absolutePos(new BlockPos(3, 2, 1));
        for (int i = 0; i < 3; i++) {
            BlockPos pos = basePos.west(i);
            helper.getLevel().setBlockAndUpdate(pos, mcjty.ariente.setup.Registration.DISPLAY_BASE.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
            CompoundTag tag = new CompoundTag();
            tag.putInt("Height", 3);
            ((DisplayBaseTile) helper.getLevel().getBlockEntity(pos)).load(tag);
        }
        DisplayBaseTile base = (DisplayBaseTile) helper.getLevel().getBlockEntity(basePos);
        ItemStack stack = linked(helper, cameraPos);
        DisplayModuleItem.config(stack).putBoolean("AutoRotate", false);
        CompoundTag originalLink = CameraDisplayModule.selectedCamera(stack).copy();
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(Vec3.atCenterOf(basePos));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertTrue(base.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(base, 0, 0, 1)), "The small module fits");
        UUID id = base.modules().get(0).id();
        helper.assertTrue(contains(base.findModule(id).snapshot(), new BlockPos(0, 2, 0)), "Installation immediately captures the scene");
        var gui = base.createGui(ModuleSettings.installedTag(id, false), Ariente.guiHandler.getComponentRegistry());
        var sizeControl = gui.findChild("setting_Size").orElseThrow();
        var previousControl = gui.findChild("camera_previous").orElseThrow();
        var selectedControl = gui.findChild("camera_selected").orElseThrow();
        var nextControl = gui.findChild("camera_next").orElseThrow();
        helper.assertTrue(sizeControl.getX() + sizeControl.getW() < previousControl.getX()
                && previousControl.getX() + previousControl.getW() < selectedControl.getX()
                && selectedControl.getX() + selectedControl.getW() < nextControl.getX()
                && sizeControl.getY() == previousControl.getY() && previousControl.getY() == nextControl.getY(),
                "Size and camera selection fit on one compact row without overlapping");
        for (int size = 1; size <= 2; size++) {
            gui.findChild("setting_Size").orElseThrow().hit(player, null, 1, 1.4);
            helper.assertTrue(base.findModule(id).size().width() == 64 * (size + 1), "The holo GUI changes module size");
        }
        gui.findChild("setting_Yaw").orElseThrow().hit(player, null, .4, 2.6);
        gui.findChild("setting_Tilt").orElseThrow().hit(player, null, .4, 3.9);
        InstalledModule installed = base.findModule(id);
        helper.assertTrue(CameraDisplayModule.yaw(installed.stack()) != 45 && CameraDisplayModule.tilt(installed.stack()) != 30, "Both viewing direction controls work");
        helper.assertTrue(!CameraDisplayModule.autoRotate(installed.stack()) && CameraDisplayModule.rotationSpeed(installed.stack()) == 0,
                "New modules default to stationary views");
        helper.assertTrue(gui.findChild("setting_AutoRotate").isEmpty(), "Rotation speed replaces the old toggle");
        gui.findChild("setting_Radius").orElseThrow().hit(player, null, .4, 4.4);
        gui.findChild("setting_RotationSpeed").orElseThrow().hit(player, null, 7.4, 6.4);
        installed = base.findModule(id);
        helper.assertTrue(CameraDisplayModule.autoRotate(installed.stack()) && CameraDisplayModule.rotationSpeed(installed.stack()) == 1,
                "A positive speed enables rotation");
        helper.assertTrue(CameraDisplayModule.radius(installed.stack()) == 29 && !DisplayModuleItem.config(installed.stack()).contains("AutoRotate"),
                "Radius changes the capture distance, and editing speed removes the legacy toggle");
        helper.assertTrue(CameraDisplayModule.selectedCamera(installed.stack()).equals(originalLink), "Configuration preserves the camera link");
        helper.assertTrue(!installed.save(true).contains("Data"), "Camera images travel separately from the initial layout");
        ItemStack restored = InstalledModule.load(installed.save(false)).stack();
        helper.assertTrue(CameraDisplayModule.autoRotate(restored) && CameraDisplayModule.rotationSpeed(restored) == 1,
                "Automatic rotation and speed survive saving the module");
        helper.assertTrue(BlueprintRecipeRegistry.findRecipe(new ItemStack(mcjty.ariente.setup.Registration.CAMERA.get())) != null
                && BlueprintRecipeRegistry.findRecipe(new ItemStack(mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get())) != null, "Both components have Constructor recipes");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void viewsStayUprightAndRotationUsesDegreesPerSecond(GameTestHelper helper) {
        for (int yaw = 0; yaw < 360; yaw += 45) {
            for (int tilt = 0; tilt < 90; tilt += 15) {
                CameraView view = CameraView.of(yaw, tilt);
                helper.assertTrue(Math.abs(view.screenX(0, 1, 0)) < .0001 && view.screenY(0, 1, 0) < 0,
                        "World up stays vertical and points up at every viewing rotation and tilt");
            }
        }
        ItemStack stack = new ItemStack(mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get());
        helper.assertTrue(CameraDisplayModule.viewYaw(stack, 100) == CameraDisplayModule.yaw(stack), "Manual views remain stationary");
        CompoundTag config = DisplayModuleItem.config(stack);
        config.putBoolean("AutoRotate", true);
        config.putInt("RotationSpeed", 30);
        double start = CameraDisplayModule.viewYaw(stack, 0);
        helper.assertTrue(CameraDisplayModule.viewYaw(stack, 20) == start + 30, "Rotation speed is measured in degrees per second");
        helper.assertTrue(Math.abs(CameraDisplayModule.viewYaw(stack, .5) - start - .75) < .0001, "Partial ticks animate between game ticks");
        helper.assertTrue(CameraDisplayModule.viewYaw(stack, 240) == start, "A full revolution wraps smoothly");
        config.putInt("RotationSpeed", Integer.MAX_VALUE);
        helper.assertTrue(CameraDisplayModule.rotationSpeed(stack) == 90, "Saved speeds respect the upper limit");
        config.putInt("RotationSpeed", Integer.MIN_VALUE);
        helper.assertTrue(CameraDisplayModule.rotationSpeed(stack) == 0, "Zero is the minimum rotation speed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unloadedLinksDoNotLoadChunks(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 6, 3));
        camera(helper, pos, Direction.UP);
        ItemStack stack = linked(helper, pos);
        BlockPos unloaded = new BlockPos(3_000_000, 100, 3_000_000);
        helper.assertTrue(!helper.getLevel().hasChunkAt(unloaded), "The remote chunk starts unloaded");
        CameraDisplayModule.selectedCamera(stack).putLong("Pos", unloaded.asLong());
        CompoundTag result = mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get().type().sample(helper.getLevel(), stack, new CompoundTag());
        helper.assertTrue(result.getString("Status").equals("Target unloaded") && !helper.getLevel().hasChunkAt(unloaded), "Sampling reports offline and keeps the chunk unloaded");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fiftyBlockRangeAndPackedCoordinates(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 250, 3));
        camera(helper, pos, Direction.UP);
        for (int y = 1; y < 50; y++) {
            helper.getLevel().setBlockAndUpdate(pos.above(y), Blocks.AIR.defaultBlockState());
        }
        int reach = 50 - CameraCapture.ORIGIN_DISTANCE;
        helper.getLevel().setBlockAndUpdate(pos.above(reach), Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(reach + 1), Blocks.GOLD_BLOCK.defaultBlockState());
        CompoundTag capture = CameraCapture.scan(helper.getLevel(), pos, Direction.UP, 50);
        helper.assertTrue(contains(capture, new BlockPos(0, reach, 0)), "Terrain fifty blocks from the shifted origin is captured: camera=" + pos
                + ", target=" + helper.getLevel().getBlockState(pos.above(reach)) + ", blocks=" + capture.getLongArray("Blocks").length);
        helper.assertTrue(!contains(capture, new BlockPos(0, reach + 1, 0)), "Terrain beyond the radius from the shifted origin remains out of range");
        for (int sign : new int[]{-1, 1}) {
            BlockPos offset = new BlockPos(50 * sign, -49 * sign, 48 * sign);
            long packed = CameraCapture.pack(offset, CameraCapture.MAX_BLOCKS - 1);
            helper.assertTrue(CameraCapture.offset(packed).equals(offset) && CameraCapture.paletteId(packed) == CameraCapture.MAX_BLOCKS - 1,
                    "Large positive and negative offsets preserve the complete palette ID");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fourLinksPreserveLegacyLinksAndRejectAFifth(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 6, 3));
        for (int i = 0; i < 5; i++) {
            camera(helper, pos.east(i * 2), Direction.UP);
        }
        ItemStack stack = linked(helper, pos);
        CompoundTag first = CameraDisplayModule.selectedCamera(stack).copy();
        DisplayModuleItem.config(stack).remove("Cameras");
        DisplayModuleItem.config(stack).put("Link", first.copy());
        helper.assertTrue(CameraDisplayModule.cameras(stack).size() == 1 && CameraDisplayModule.selectedCamera(stack).equals(first),
                "An existing single-camera module keeps its camera");
        Player player = helper.makeMockSurvivalPlayer();
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        for (int i = 1; i < 5; i++) {
            BlockPos target = pos.east(i * 2);
            mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get().onItemUseFirst(stack, new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false)));
        }
        helper.assertTrue(CameraDisplayModule.cameras(stack).size() == 4 && CameraDisplayModule.cameraIndex(stack) == 3,
                "Four cameras are retained and a fifth cannot replace one");
        helper.assertTrue(CameraDisplayModule.cameras(stack).get(0).equals(first), "Adding cameras migrates the original link");
        mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get().onItemUseFirst(stack, new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false)));
        helper.assertTrue(CameraDisplayModule.cameras(stack).size() == 4 && CameraDisplayModule.cameraIndex(stack) == 0,
                "Relinking a camera selects it without consuming another slot, regardless of the clicked side");
        ItemStack restored = ItemStack.of(stack.save(new CompoundTag()));
        helper.assertTrue(CameraDisplayModule.cameras(restored).equals(CameraDisplayModule.cameras(stack)), "All four links survive saving the item");
        mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(CameraDisplayModule.cameras(stack).isEmpty() && !DisplayModuleItem.config(stack).contains("Link"), "Sneak use in air clears all camera links");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void previousAndNextSwitchImmediatelyAndSampleOnlyTheSelectedCamera(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 6, 3));
        CountingCamera[] cameras = new CountingCamera[4];
        CameraDisplayModule type = (CameraDisplayModule) mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get().type();
        ItemStack stack = new ItemStack(mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get());
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.east(i * 2);
            camera(helper, target, Direction.UP);
            cameras[i] = new CountingCamera(target, i);
            helper.getLevel().setBlockEntity(cameras[i]);
            CompoundTag link = new CompoundTag();
            link.putString("Dimension", helper.getLevel().dimension().location().toString());
            link.putLong("Pos", target.asLong());
            link.putString("Block", "ariente:camera");
            type.link(stack, link, mcjty.ariente.setup.Registration.CAMERA.get().getName());
        }
        BlockPos basePos = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.getLevel().setBlockAndUpdate(basePos, mcjty.ariente.setup.Registration.DISPLAY_BASE.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        DisplayBaseTile base = (DisplayBaseTile) helper.getLevel().getBlockEntity(basePos);
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(Vec3.atCenterOf(basePos));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertTrue(base.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(base, 0, 0, 1)), "The linked module installs");
        UUID id = base.modules().get(0).id();
        helper.assertTrue(base.findModule(id).snapshot().getInt("Source") == 3 && cameras[3].captures == 1,
                "Installation samples the selected camera");
        helper.assertTrue(cameras[0].captures == 0 && cameras[1].captures == 0 && cameras[2].captures == 0, "Unselected cameras are not sampled");
        var gui = base.createGui(ModuleSettings.installedTag(id, false), Ariente.guiHandler.getComponentRegistry());
        gui.findChild("camera_next").orElseThrow().hit(player, null, 7.2, 1.3);
        helper.assertTrue(CameraDisplayModule.cameraIndex(base.findModule(id).stack()) == 0 && base.findModule(id).snapshot().getInt("Source") == 0,
                "Next wraps from the fourth camera to the first and updates the feed immediately");
        gui.findChild("camera_previous").orElseThrow().hit(player, null, 4.2, 1.3);
        helper.assertTrue(CameraDisplayModule.cameraIndex(base.findModule(id).stack()) == 3 && base.findModule(id).snapshot().getInt("Source") == 3,
                "Previous wraps from the first camera to the fourth and updates immediately");
        helper.assertTrue(cameras[1].captures == 0 && cameras[2].captures == 0, "Switching leaves the other cameras idle");
        gui.findChild("setting_Radius").orElseThrow().hit(player, null, 7.4, 4.4);
        helper.assertTrue(base.findModule(id).snapshot().getInt("Range") == 31, "Changing radius updates the installed view immediately");
        int captures = cameras[3].captures;
        CompoundTag snapshot = base.findModule(id).snapshot();
        gui.findChild("setting_Zoom").orElseThrow().hit(player, null, 7.4, 5.4);
        helper.assertTrue(CameraDisplayModule.zoom(base.findModule(id).stack()) == 110
                && CameraDisplayModule.radius(base.findModule(id).stack()) == 31
                && base.findModule(id).snapshot() == snapshot && cameras[3].captures == captures,
                "Visual zoom changes magnification while preserving the radius and cached capture");
        helper.assertTrue(!CameraDisplayModule.perspective(base.findModule(id).stack()), "Existing modules default to orthographic projection");
        var projection = gui.findChild("setting_Perspective").orElseThrow();
        helper.assertTrue(projection.getY() + projection.getH() < gui.findChild("setting_Size").orElseThrow().getY(),
                "The projection selector fits in the header without using another slider row");
        projection.hit(player, null, 6, .4);
        helper.assertTrue(CameraDisplayModule.perspective(base.findModule(id).stack())
                && base.findModule(id).snapshot() == snapshot && cameras[3].captures == captures,
                "Perspective changes projection without recapturing or changing the radius");
        helper.assertTrue(CameraDisplayModule.perspective(InstalledModule.load(base.findModule(id).save(false)).stack()),
                "Perspective mode survives saving the configured module");
        projection.hit(player, null, 6, .4);
        helper.assertTrue(!CameraDisplayModule.perspective(base.findModule(id).stack()), "The projection selector returns to orthographic mode");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void radiusZoomCachingAndLegacyRotation(GameTestHelper helper) {
        ItemStack stack = new ItemStack(mcjty.ariente.setup.Registration.DISPLAY_CAMERA.get());
        CompoundTag config = DisplayModuleItem.config(stack);
        helper.assertTrue(CameraDisplayModule.radius(stack) == 30 && CameraDisplayModule.zoom(stack) == 100 && CameraDisplayModule.rotationSpeed(stack) == 0, "New modules use a 30-block radius, 100% zoom and no rotation");
        config.putBoolean("AutoRotate", true);
        helper.assertTrue(CameraDisplayModule.rotationSpeed(stack) == 15, "Existing rotating modules retain their speed");
        config.putBoolean("AutoRotate", false);
        config.putInt("RotationSpeed", 45);
        helper.assertTrue(CameraDisplayModule.rotationSpeed(stack) == 0, "The old disabled toggle remains disabled until speed is edited");
        config.putInt("Zoom", Integer.MIN_VALUE);
        helper.assertTrue(CameraDisplayModule.radius(stack) == 8, "Legacy capture distance has an eight-block minimum");
        config.putInt("Zoom", Integer.MAX_VALUE);
        helper.assertTrue(CameraDisplayModule.radius(stack) == 100 && CameraDisplayModule.zoom(stack) == 100, "Legacy Zoom remains a capture radius and does not magnify the view");
        config.putInt("Radius", 37);
        config.putInt("ViewZoom", 250);
        helper.assertTrue(CameraDisplayModule.radius(stack) == 37 && CameraDisplayModule.zoom(stack) == 250,
                "The renamed radius supersedes the legacy key and stays independent of visual zoom");
        config.putInt("ViewZoom", Integer.MAX_VALUE);
        helper.assertTrue(CameraDisplayModule.zoom(stack) == 800, "Visual zoom is bounded to eight times magnification");
        config.putInt("ViewZoom", Integer.MIN_VALUE);
        helper.assertTrue(CameraDisplayModule.zoom(stack) == 100, "The default zoom fits the whole captured scene");
        BlockPos pos = helper.absolutePos(new BlockPos(3, 220, 3));
        CameraTile camera = camera(helper, pos, Direction.UP);
        helper.getLevel().setBlockAndUpdate(pos.above(100 - CameraCapture.ORIGIN_DISTANCE), Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(101 - CameraCapture.ORIGIN_DISTANCE), Blocks.GOLD_BLOCK.defaultBlockState());
        CompoundTag close = camera.capture(8), far = camera.capture(100);
        helper.assertTrue(close == camera.capture(8) && far == camera.capture(100) && close != far, "Captures are shared independently for each capture radius");
        helper.assertTrue(!contains(close, new BlockPos(0, 100 - CameraCapture.ORIGIN_DISTANCE, 0)) && contains(far, new BlockPos(0, 100 - CameraCapture.ORIGIN_DISTANCE, 0)), "A hundred-block radius is measured from the shifted origin");
        helper.assertTrue(!contains(far, new BlockPos(0, 101 - CameraCapture.ORIGIN_DISTANCE, 0)), "Terrain beyond the chosen radius stays outside the capture");
        BlockPos corner = new BlockPos(-100, 99, -98);
        long packed = CameraCapture.pack(corner, CameraCapture.MAX_BLOCKS - 1);
        helper.assertTrue(CameraCapture.offset(packed).equals(corner) && CameraCapture.paletteId(packed) == CameraCapture.MAX_BLOCKS - 1,
                "The larger coordinate encoding preserves coordinates and palette IDs");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aBuriedCaptureOriginDoesNotExposeSealedCaves(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 180, 3));
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-5, -6, -5), pos.offset(5, -1, 5))) {
            helper.getLevel().setBlock(block, Blocks.STONE.defaultBlockState(), 2);
        }
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-2, -4, -2), pos.offset(2, -2, 2))) {
            helper.getLevel().setBlock(block, Blocks.AIR.defaultBlockState(), 2);
        }
        camera(helper, pos, Direction.UP);
        helper.getLevel().setBlockAndUpdate(pos.offset(3, -2, 0), Blocks.GOLD_BLOCK.defaultBlockState());
        CompoundTag data = CameraCapture.scan(helper.getLevel(), pos, Direction.UP, 8);
        helper.assertTrue(contains(data, new BlockPos(0, -1, 0)), "The floor in front of the buried origin is visible");
        helper.assertTrue(!contains(data, new BlockPos(3, -2, 0)), "Starting the radius below ground must not seed visibility inside a sealed cave");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void magnifiedFacesAreCroppedWithoutStretchingTheirTextures(GameTestHelper helper) {
        CameraViewClipper clipper = new CameraViewClipper();
        clipper.begin();
        clipper.vertex(-10, 0, 0, 0, 0);
        clipper.vertex(10, 0, 1, 0, 0xffffff);
        clipper.vertex(10, 10, 1, 1, 0xffffff);
        clipper.vertex(-10, 10, 0, 1, 0);
        int count = clipper.clip(0, 0, 10, 10);
        helper.assertTrue(count == 4, "A face crossing the left boundary is cropped to a quad");
        float[] vertices = clipper.vertices();
        for (int index = 0; index < count; index++) {
            int offset = index * CameraViewClipper.STRIDE;
            helper.assertTrue(CameraViewClipper.outcode(vertices[offset], vertices[offset + 1], 0, 0, 10, 10) == 0,
                    "Zoomed geometry stays inside the module viewport");
            if (vertices[offset] == 0) {
                helper.assertTrue(Math.abs(vertices[offset + 2] - .5) < .001 && Math.abs(vertices[offset + 4] - 127.5) < .001,
                        "New edge vertices interpolate texture coordinates and colors instead of stretching the full texture");
            }
        }
        clipper.begin();
        clipper.vertex(-20, 0, 0, 0, 0);
        clipper.vertex(-10, 0, 1, 0, 0);
        clipper.vertex(-10, 10, 1, 1, 0);
        clipper.vertex(-20, 10, 0, 1, 0);
        helper.assertTrue(clipper.clip(0, 0, 10, 10) == 0, "Faces outside the magnified viewport are omitted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void perspectiveHasDepthAndZoomWithoutMovingTheCapture(GameTestHelper helper) {
        for (int yaw = 0; yaw < 360; yaw += 45) {
            for (int tilt : new int[]{0, 30, 90}) {
                CameraView basis = CameraView.of(yaw, tilt);
                CameraPerspective camera = CameraPerspective.of(Vec3.ZERO, 12, basis, 180, 132, 100);
                Matrix4f transform = new Matrix4f(camera.projection()).mul(camera.view());
                Vec3 right = basis.right();
                Vec3 near = basis.direction().scale(5), far = basis.direction().scale(-5);
                Vector4f a = project(transform, near.add(right)), b = project(transform, far.add(right));
                helper.assertTrue(a.w > 0 && b.w > 0 && a.x / a.w > b.x / b.w,
                        "Equal-size details nearer the eye appear larger in perspective at every rotation and tilt");
                helper.assertTrue(a.z / a.w < b.z / b.w, "The depth buffer puts nearer surfaces in front");
                CameraPerspective zoomed = CameraPerspective.of(Vec3.ZERO, 12, basis, 180, 132, 800);
                Vector4f enlarged = project(new Matrix4f(zoomed.projection()).mul(zoomed.view()), near.add(right));
                helper.assertTrue(camera.eye().equals(zoomed.eye()) && Math.abs(enlarged.x / enlarged.w / (a.x / a.w) - 8) < .001,
                        "Visual zoom changes the lens while preserving the orbit and capture");
                Vector4f behind = project(transform, camera.eye().add(basis.direction()));
                helper.assertTrue(behind.w < 0, "Geometry behind the perspective eye lies outside the GPU clip volume");
            }
        }
        helper.succeed();
    }

    private static Vector4f project(Matrix4f transform, Vec3 point) {
        return transform.transform(new Vector4f((float) point.x, (float) point.y, (float) point.z, 1));
    }

    @GameTest(template = "empty")
    public static void cameraModePersistsAndLiveSnapshotsContainOnlyTheSource(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 6, 3));
        CameraTile camera = camera(helper, pos, Direction.SOUTH);
        helper.assertTrue(!camera.isLive(), "Existing cameras default to the holographic capture mode");
        var gui = camera.createGui("", Ariente.guiHandler.getComponentRegistry());
        var player = helper.makeMockSurvivalPlayer();
        gui.findChild("camera_mode").orElseThrow().hit(player, null, 1, 1.6);
        helper.assertTrue(camera.isLive(), "The camera hologui enables a live feed");
        CompoundTag data = camera.capture(30);
        helper.assertTrue(data.getBoolean("Live") && data.getLong("Camera") == pos.asLong()
                && data.getInt("Facing") == Direction.SOUTH.get3DDataValue()
                && data.getString("Dimension").equals(helper.getLevel().dimension().location().toString())
                && !data.contains("Blocks") && !data.contains("Palette"), "Live modules receive source metadata instead of captured geometry");
        CameraTile restored = new CameraTile(pos, camera.getBlockState());
        restored.load(camera.saveWithoutMetadata());
        helper.assertTrue(restored.isLive(), "The camera mode survives chunk saving");
        CompoundTag update = camera.getUpdateTag();
        CameraTile client = new CameraTile(pos, camera.getBlockState());
        client.loadClientDataFromNBT(update);
        helper.assertTrue(client.isLive(), "The camera mode is available to nearby clients");
        gui.findChild("camera_mode").orElseThrow().hit(player, null, 1, 1.6);
        helper.assertTrue(!camera.isLive() && !camera.capture(30).getBoolean("Live"), "Switching back restores the existing block capture");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void liveViewStartsOutsideTheLensAndFacesAwayFromEveryBase(GameTestHelper helper) {
        BlockPos pos = new BlockPos(10, 70, -10);
        for (Direction facing : Direction.values()) {
            Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
            LiveCameraView neutral = LiveCameraView.of(pos, facing, 0, 0);
            helper.assertTrue(neutral.forward().distanceToSqr(normal) < .00001
                    && Math.abs(neutral.eye().subtract(pos.getCenter()).dot(normal) - .125) < .00001,
                    "Neutral live views face away from the mounting plate and start outside its glass for " + facing);
            for (int pan : new int[]{-180, -90, 0, 90, 180}) {
                for (int tilt : new int[]{-90, -45, 0, 45, 90}) {
                    LiveCameraView view = LiveCameraView.of(pos, facing, pan, tilt);
                    helper.assertTrue(Float.isFinite(view.yaw()) && Float.isFinite(view.pitch()) && Math.abs(view.forward().length() - 1) < .00001,
                            "Pan and tilt remain well-defined on walls, floors and ceilings");
                    Vector3f projected = view.rotation().transformDirection(new Vector3f((float) view.forward().x, (float) view.forward().y, (float) view.forward().z));
                    helper.assertTrue(Math.abs(projected.x) < .00001 && Math.abs(projected.y) < .00001 && Math.abs(projected.z + 1) < .00001,
                            "The world renderer looks along the requested camera direction, rather than backwards");
                }
            }
            helper.assertTrue(LiveCameraView.of(pos, facing, 90, 0).forward().dot(normal) < .00001
                    && LiveCameraView.of(pos, facing, 0, 90).forward().dot(normal) < .00001, "Both controls can turn the view away from its default direction");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void liveModulesUseNeutralIndependentPanAndTiltControls(GameTestHelper helper) {
        BlockPos source = helper.absolutePos(new BlockPos(3, 6, 3));
        CameraTile camera = camera(helper, source, Direction.NORTH);
        camera.setLive(true);
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, mcjty.ariente.setup.Registration.DISPLAY_BASE.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
        var base = (DisplayBaseTile) helper.getLevel().getBlockEntity(pos);
        var player = helper.makeMockSurvivalPlayer();
        player.setPos(pos.getCenter());
        player.setItemInHand(InteractionHand.MAIN_HAND, linked(helper, source));
        base.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(base, 0, 0, 1));
        var module = base.modules().get(0);
        var gui = base.createGui(ModuleSettings.installedTag(module.id(), false), Ariente.guiHandler.getComponentRegistry());
        helper.assertTrue(CameraDisplayModule.livePan(module.stack()) == 0 && CameraDisplayModule.liveTilt(module.stack()) == 0
                && gui.findChild("setting_Perspective").isEmpty() && gui.findChild("setting_Yaw").isEmpty(),
                "Live controls start neutral and replace holographic orbit controls");
        CompoundTag snapshot = module.snapshot();
        gui.findChild("setting_LivePan").orElseThrow().hit(player, null, 7.4, 2.4);
        gui.findChild("setting_LiveTilt").orElseThrow().hit(player, null, 7.4, 3.4);
        module = base.findModule(module.id());
        helper.assertTrue(CameraDisplayModule.livePan(module.stack()) == 1 && CameraDisplayModule.liveTilt(module.stack()) == 1
                && module.snapshot() == snapshot && CameraDisplayModule.yaw(module.stack()) == 45 && CameraDisplayModule.tilt(module.stack()) == 30,
                "Live view adjustments leave capture data and holographic settings intact");
        camera.setLive(false);
        module.refresh(helper.getLevel());
        helper.assertTrue(gui.findChild("setting_Yaw").isPresent() && gui.findChild("setting_LivePan").isEmpty(),
                "The existing controls return when a selected camera switches back to holographic mode");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fluidInteriorsAreSkippedAndBudgetEdgesStayContinuous(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = new BlockPos(1000, 200, 1000);
        for (int x = (pos.getX() - 100) >> 4; x <= (pos.getX() + 100) >> 4; x++) {
            for (int z = (pos.getZ() - 100) >> 4; z <= (pos.getZ() + 100) >> 4; z++) {
                level.setChunkForced(x, z, true);
            }
        }
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-100, -4, -100), pos.offset(100, -4, 100))) {
            level.setBlock(block, Blocks.STONE.defaultBlockState(), 2);
        }
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-10, -3, -10), pos.offset(10, -1, 10))) {
            level.setBlock(block, Blocks.WATER.defaultBlockState(), 2);
        }
        camera(helper, pos, Direction.DOWN);
        CompoundTag lake = CameraCapture.scan(level, pos, Direction.DOWN, 30);
        helper.assertTrue(contains(lake, new BlockPos(0, -1, 0)) && !contains(lake, new BlockPos(0, -2, 0)), "Only the water surface consumes capture budget");
        helper.assertTrue(contains(lake, new BlockPos(0, -4, 0)), "Underwater terrain stays in the scene");
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-10, -3, -10), pos.offset(10, -1, 10))) {
            level.setBlock(block, Blocks.AIR.defaultBlockState(), 2);
        }
        // Let newly generated fixture chunks become accessible to the capture's non-loading chunk lookup.
        helper.runAfterDelay(2, () -> {
            try {
                CompoundTag wide = CameraCapture.scan(level, pos, Direction.DOWN, 100);
                int cutoff = wide.getInt("SurfaceRadiusSquared");
                helper.assertTrue(cutoff < 10000 && wide.getLongArray("Blocks").length <= CameraCapture.MAX_BLOCKS,
                        "Dense terrain crops its outer radius to the capture budget: radiusSquared=" + cutoff + ", blocks=" + wide.getLongArray("Blocks").length);
                Set<BlockPos> included = new HashSet<>();
                for (long entry : wide.getLongArray("Blocks")) {
                    included.add(CameraCapture.offset(entry));
                }
                for (int x = -100; x <= 100; x++) {
                    for (int z = -100; z <= 100; z++) {
                        if (x * x + z * z + (4 + CameraCapture.ORIGIN_DISTANCE) * (4 + CameraCapture.ORIGIN_DISTANCE) <= cutoff) {
                            helper.assertTrue(included.contains(new BlockPos(x, -4, z)), "The terrain inside a cropped view must have no holes");
                        }
                    }
                }
                helper.succeed();
            } finally {
                for (int x = (pos.getX() - 100) >> 4; x <= (pos.getX() + 100) >> 4; x++) {
                    for (int z = (pos.getZ() - 100) >> 4; z <= (pos.getZ() + 100) >> 4; z++) {
                        level.setChunkForced(x, z, false);
                    }
                }
            }
        });
    }

    private static boolean hasMarker(CompoundTag capture, float x, float z) {
        var markers = capture.getList("Entities", Tag.TAG_COMPOUND);
        for (int i = 0; i < markers.size(); i++) {
            CompoundTag marker = markers.getCompound(i);
            if (Math.abs(marker.getFloat("X") - x) < .2 && Math.abs(marker.getFloat("Z") - z) < .2) {
                return true;
            }
        }
        return false;
    }

    @GameTest(template = "empty")
    public static void connectedSpaceHidesSealedCavesAndTheirCreatures(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 180, 3));
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-9, -9, -12), pos.offset(9, 8, 3))) {
            level.setBlock(block, Blocks.AIR.defaultBlockState(), 2);
        }
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-8, -8, -10), pos.offset(8, -1, 2))) {
            level.setBlock(block, Blocks.STONE.defaultBlockState(), 2);
        }
        for (BlockPos block : BlockPos.betweenClosed(pos.offset(-2, -4, -7), pos.offset(2, -2, -3))) {
            level.setBlock(block, Blocks.AIR.defaultBlockState(), 2);
        }
        camera(helper, pos, Direction.NORTH);
        level.setBlockAndUpdate(pos.north(2), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos.north(4), Blocks.DIAMOND_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos.offset(0, -5, -5), Blocks.GOLD_BLOCK.defaultBlockState());
        var outside = EntityType.PIG.create(level);
        outside.setNoAi(true);
        outside.moveTo(pos.getX() + 2.5, pos.getY(), pos.getZ() - 3.5, 0, 0);
        level.addFreshEntity(outside);
        var hidden = EntityType.PIG.create(level);
        hidden.setNoAi(true);
        hidden.moveTo(pos.getX() + 1.5, pos.getY() - 4, pos.getZ() - 4.5, 0, 0);
        level.addFreshEntity(hidden);
        helper.runAfterDelay(2, () -> {
            CompoundTag sealed = CameraCapture.scan(level, pos, Direction.NORTH, 12);
            helper.assertTrue(contains(sealed, new BlockPos(0, 0, -4)), "The camera sees around the pillar");
            helper.assertTrue(!contains(sealed, new BlockPos(0, -5, -5)), "A sealed cave's floor and buried ore remain hidden");
            helper.assertTrue(hasMarker(sealed, 2, -4) && !hasMarker(sealed, 1, -5), "Markers obey the same connected-space rule as terrain: " + sealed.getList("Entities", Tag.TAG_COMPOUND));
            for (int y = -4; y <= -1; y++) {
                level.setBlockAndUpdate(pos.offset(3, y, -5), Blocks.AIR.defaultBlockState());
            }
            CompoundTag opened = CameraCapture.scan(level, pos, Direction.NORTH, 12);
            helper.assertTrue(contains(opened, new BlockPos(0, -5, -5)) && hasMarker(opened, 1, -5), "An open passage connects the cave to the captured space");
            BlockPos hatch = pos.offset(3, -1, -5);
            level.setBlockAndUpdate(hatch, Blocks.OAK_TRAPDOOR.defaultBlockState());
            helper.assertTrue(!contains(CameraCapture.scan(level, pos, Direction.NORTH, 12), new BlockPos(0, -5, -5)), "A closed hatch seals the cave without leaking through its partial collision shape");
            level.setBlockAndUpdate(hatch, Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(BlockStateProperties.OPEN, true));
            helper.assertTrue(contains(CameraCapture.scan(level, pos, Direction.NORTH, 12), new BlockPos(0, -5, -5)), "Opening the hatch restores the connection");
            level.setBlockAndUpdate(hatch, Blocks.GLASS.defaultBlockState());
            helper.assertTrue(contains(CameraCapture.scan(level, pos, Direction.NORTH, 12), new BlockPos(0, -5, -5)), "A transparent window also exposes the space beyond it");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void connectedScanKeepsNeighboringChunksUnloaded(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4_000_000, 200, 4_000_000);
        CameraTile camera = camera(helper, pos, Direction.UP);
        BlockPos unloaded = pos.east(48);
        helper.assertTrue(!helper.getLevel().hasChunkAt(unloaded), "The neighboring terrain starts unloaded");
        camera.capture();
        helper.assertTrue(!helper.getLevel().hasChunkAt(unloaded), "The connected-space scan never loads neighboring chunks");
        helper.succeed();
    }

    private static class CountingCamera extends CameraTile {

        private final int source;
        private int captures;

        private CountingCamera(BlockPos pos, int source) {
            super(pos, mcjty.ariente.setup.Registration.CAMERA.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
            this.source = source;
        }

        @Override
        public CompoundTag capture(int range) {
            captures++;
            CompoundTag result = new CompoundTag();
            result.putInt("Source", source);
            result.putInt("Range", range);
            return result;
        }
    }
}
