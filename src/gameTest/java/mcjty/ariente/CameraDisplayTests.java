package mcjty.ariente;

import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.blocks.utility.camera.CameraTile;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.CameraView;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.InstalledModule;
import mcjty.ariente.display.ModuleSettings;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
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
        helper.getLevel().setBlockAndUpdate(pos, Registration.CAMERA.get().defaultBlockState().setValue(BlockStateProperties.FACING, normal));
        return (CameraTile) helper.getLevel().getBlockEntity(pos);
    }

    private static boolean contains(CompoundTag capture, BlockPos offset) {
        return Arrays.stream(capture.getLongArray("Blocks")).anyMatch(block -> CameraCapture.offset(block).equals(offset));
    }

    private static ItemStack linked(GameTestHelper helper, BlockPos pos) {
        ItemStack stack = new ItemStack(Registration.DISPLAY_CAMERA.get());
        Player player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setShiftKeyDown(true);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(Registration.DISPLAY_CAMERA.get().onItemUseFirst(stack, context) == InteractionResult.SUCCESS, "Sneak click links the module");
        helper.assertTrue(!CameraDisplayModule.cameras(stack).isEmpty(), "The link is stored on the module");
        return stack;
    }

    @GameTest(template = "empty")
    public static void sixMountingDirectionsCaptureOnlyTheFrontHemisphere(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 6, 3));
        Player player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Registration.CAMERA.get()));
        for (Direction facing : Direction.values()) {
            CameraTile camera = camera(helper, pos, facing);
            BlockPos front = pos.relative(facing, 2), back = pos.relative(facing.getOpposite(), 2);
            helper.getLevel().setBlockAndUpdate(pos.relative(facing), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(pos.relative(facing.getOpposite()), Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(front, Blocks.DIAMOND_BLOCK.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(back, Blocks.GOLD_BLOCK.defaultBlockState());
            CompoundTag capture = camera.capture();
            helper.assertTrue(contains(capture, front.subtract(pos)) && !contains(capture, back.subtract(pos)),
                    "Only the front is captured for " + facing + ": state=" + camera.getBlockState() + ", capture=" + capture.getInt("Facing"));
            BlockPlaceContext placement = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, player.getMainHandItem(),
                    new BlockHitResult(Vec3.atCenterOf(pos), facing, pos, false));
            helper.assertTrue(Registration.CAMERA.get().getStateForPlacement(placement).getValue(BlockStateProperties.FACING) == facing,
                    "Placement follows the clicked face for " + facing);
            var shape = CameraTile.shape(facing);
            boolean positive = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            helper.assertTrue(shape.min(facing.getAxis()) == (positive ? 0 : 7 / 16.0)
                    && shape.max(facing.getAxis()) == (positive ? 9 / 16.0 : 1), "Collision shape follows " + facing);
            helper.getLevel().setBlockAndUpdate(front, Blocks.AIR.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(back, Blocks.AIR.defaultBlockState());
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
        CameraDisplayModule type = (CameraDisplayModule) Registration.DISPLAY_CAMERA.get().type();
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
            helper.getLevel().setBlockAndUpdate(pos, Registration.DISPLAY_BASE.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
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
        gui.findChild("setting_Zoom").orElseThrow().hit(player, null, .4, 5);
        gui.findChild("setting_RotationSpeed").orElseThrow().hit(player, null, 7.4, 6);
        installed = base.findModule(id);
        helper.assertTrue(CameraDisplayModule.autoRotate(installed.stack()) && CameraDisplayModule.rotationSpeed(installed.stack()) == 1,
                "A positive speed enables rotation");
        helper.assertTrue(CameraDisplayModule.zoom(installed.stack()) == 29 && !DisplayModuleItem.config(installed.stack()).contains("AutoRotate"),
                "Zoom changes the capture distance, and editing speed removes the legacy toggle");
        helper.assertTrue(CameraDisplayModule.selectedCamera(installed.stack()).equals(originalLink), "Configuration preserves the camera link");
        helper.assertTrue(!installed.save(true).contains("Data"), "Camera images travel separately from the initial layout");
        ItemStack restored = InstalledModule.load(installed.save(false)).stack();
        helper.assertTrue(CameraDisplayModule.autoRotate(restored) && CameraDisplayModule.rotationSpeed(restored) == 1,
                "Automatic rotation and speed survive saving the module");
        helper.assertTrue(BlueprintRecipeRegistry.findRecipe(new ItemStack(Registration.CAMERA.get())) != null
                && BlueprintRecipeRegistry.findRecipe(new ItemStack(Registration.DISPLAY_CAMERA.get())) != null, "Both components have Constructor recipes");
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
        ItemStack stack = new ItemStack(Registration.DISPLAY_CAMERA.get());
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
        CompoundTag result = Registration.DISPLAY_CAMERA.get().type().sample(helper.getLevel(), stack, new CompoundTag());
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
        helper.getLevel().setBlockAndUpdate(pos.above(50), Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(51), Blocks.GOLD_BLOCK.defaultBlockState());
        CompoundTag capture = CameraCapture.scan(helper.getLevel(), pos, Direction.UP, 50);
        helper.assertTrue(contains(capture, new BlockPos(0, 50, 0)), "Terrain fifty blocks away is captured: origin=" + pos
                + ", target=" + helper.getLevel().getBlockState(pos.above(50)) + ", blocks=" + capture.getLongArray("Blocks").length);
        helper.assertTrue(!contains(capture, new BlockPos(0, 51, 0)), "Terrain beyond fifty blocks remains out of range");
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
            Registration.DISPLAY_CAMERA.get().onItemUseFirst(stack, new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false)));
        }
        helper.assertTrue(CameraDisplayModule.cameras(stack).size() == 4 && CameraDisplayModule.cameraIndex(stack) == 3,
                "Four cameras are retained and a fifth cannot replace one");
        helper.assertTrue(CameraDisplayModule.cameras(stack).get(0).equals(first), "Adding cameras migrates the original link");
        Registration.DISPLAY_CAMERA.get().onItemUseFirst(stack, new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false)));
        helper.assertTrue(CameraDisplayModule.cameras(stack).size() == 4 && CameraDisplayModule.cameraIndex(stack) == 0,
                "Relinking a camera selects it without consuming another slot, regardless of the clicked side");
        ItemStack restored = ItemStack.of(stack.save(new CompoundTag()));
        helper.assertTrue(CameraDisplayModule.cameras(restored).equals(CameraDisplayModule.cameras(stack)), "All four links survive saving the item");
        Registration.DISPLAY_CAMERA.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(CameraDisplayModule.cameras(stack).isEmpty() && !DisplayModuleItem.config(stack).contains("Link"), "Sneak use in air clears all camera links");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void previousAndNextSwitchImmediatelyAndSampleOnlyTheSelectedCamera(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 6, 3));
        CountingCamera[] cameras = new CountingCamera[4];
        CameraDisplayModule type = (CameraDisplayModule) Registration.DISPLAY_CAMERA.get().type();
        ItemStack stack = new ItemStack(Registration.DISPLAY_CAMERA.get());
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.east(i * 2);
            camera(helper, target, Direction.UP);
            cameras[i] = new CountingCamera(target, i);
            helper.getLevel().setBlockEntity(cameras[i]);
            CompoundTag link = new CompoundTag();
            link.putString("Dimension", helper.getLevel().dimension().location().toString());
            link.putLong("Pos", target.asLong());
            link.putString("Block", "ariente:camera");
            type.link(stack, link, Registration.CAMERA.get().getName());
        }
        BlockPos basePos = helper.absolutePos(new BlockPos(3, 2, 1));
        helper.getLevel().setBlockAndUpdate(basePos, Registration.DISPLAY_BASE.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
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
        gui.findChild("camera_next").orElseThrow().hit(player, null, 6, 7);
        helper.assertTrue(CameraDisplayModule.cameraIndex(base.findModule(id).stack()) == 0 && base.findModule(id).snapshot().getInt("Source") == 0,
                "Next wraps from the fourth camera to the first and updates the feed immediately");
        gui.findChild("camera_previous").orElseThrow().hit(player, null, 1, 7);
        helper.assertTrue(CameraDisplayModule.cameraIndex(base.findModule(id).stack()) == 3 && base.findModule(id).snapshot().getInt("Source") == 3,
                "Previous wraps from the first camera to the fourth and updates immediately");
        helper.assertTrue(cameras[1].captures == 0 && cameras[2].captures == 0, "Switching leaves the other cameras idle");
        gui.findChild("setting_Zoom").orElseThrow().hit(player, null, 7.4, 5);
        helper.assertTrue(base.findModule(id).snapshot().getInt("Range") == 31, "Changing zoom updates the installed view immediately");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void zoomRangeCachingAndLegacyRotation(GameTestHelper helper) {
        ItemStack stack = new ItemStack(Registration.DISPLAY_CAMERA.get());
        CompoundTag config = DisplayModuleItem.config(stack);
        helper.assertTrue(CameraDisplayModule.zoom(stack) == 30 && CameraDisplayModule.rotationSpeed(stack) == 0, "New modules use a 30-block zoom and no rotation");
        config.putBoolean("AutoRotate", true);
        helper.assertTrue(CameraDisplayModule.rotationSpeed(stack) == 15, "Existing rotating modules retain their speed");
        config.putBoolean("AutoRotate", false);
        config.putInt("RotationSpeed", 45);
        helper.assertTrue(CameraDisplayModule.rotationSpeed(stack) == 0, "The old disabled toggle remains disabled until speed is edited");
        config.putInt("Zoom", Integer.MIN_VALUE);
        helper.assertTrue(CameraDisplayModule.zoom(stack) == 8, "Zoom has an eight-block minimum");
        config.putInt("Zoom", Integer.MAX_VALUE);
        helper.assertTrue(CameraDisplayModule.zoom(stack) == 100, "Zoom has a hundred-block maximum");
        BlockPos pos = helper.absolutePos(new BlockPos(3, 220, 3));
        CameraTile camera = camera(helper, pos, Direction.UP);
        helper.getLevel().setBlockAndUpdate(pos.above(100), Blocks.DIAMOND_BLOCK.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(101), Blocks.GOLD_BLOCK.defaultBlockState());
        CompoundTag close = camera.capture(8), far = camera.capture(100);
        helper.assertTrue(close == camera.capture(8) && far == camera.capture(100) && close != far, "Captures are shared independently for each zoom level");
        helper.assertTrue(!contains(close, new BlockPos(0, 100, 0)) && contains(far, new BlockPos(0, 100, 0)), "A hundred-block zoom reaches terrain at that distance");
        helper.assertTrue(!contains(far, new BlockPos(0, 101, 0)), "Terrain beyond the chosen zoom stays outside the capture");
        BlockPos corner = new BlockPos(-100, 99, -98);
        long packed = CameraCapture.pack(corner, CameraCapture.MAX_BLOCKS - 1);
        helper.assertTrue(CameraCapture.offset(packed).equals(corner) && CameraCapture.paletteId(packed) == CameraCapture.MAX_BLOCKS - 1,
                "The larger coordinate encoding preserves coordinates and palette IDs");
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
                        if (x * x + z * z + 16 <= cutoff) {
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
            super(pos, Registration.CAMERA.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
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
