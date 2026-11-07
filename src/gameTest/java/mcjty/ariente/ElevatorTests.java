package mcjty.ariente;

import mcjty.ariente.blocks.utility.ElevatorTile;
import mcjty.ariente.blocks.utility.LevelMarkerTile;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.components.ITextEntry;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.hologui.gui.components.HoloText;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import static mcjty.hologui.setup.Registration.HOLOGUI_ENTITY_TYPE;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class ElevatorTests {

    @GameTest(template = "empty")
    public static void floorNamesUseTextSessionsAndSurviveSaveAndSync(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.LEVEL_MARKER.get().defaultBlockState());
        LevelMarkerTile marker = (LevelMarkerTile) helper.getLevel().getBlockEntity(pos);
        marker.setFloorName("");
        Player player = helper.makeMockSurvivalPlayer();
        HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
        holo.setGuiTile(pos);
        ITextEntry entry = (ITextEntry) holo.getGui(player).findChild("floor_name").orElseThrow();
        holo.commitTextEntry(player, "floor_name", "Unopened session");
        helper.assertTrue(marker.getFloorName().isEmpty(), "A name edit requires an active holo text session");
        holo.beginTextEntry(player, "floor_name");
        holo.commitTextEntry(player, "floor_name", "  Reactor\nRoom\u0001\u00a7  ");
        helper.assertTrue(marker.getFloorName().equals("Reactor Room") && entry.getText().equals("Reactor Room"),
                "The shared holo editor commits a clean, single-line floor name");

        LevelMarkerTile restored = new LevelMarkerTile(pos, marker.getBlockState());
        restored.load(marker.saveWithoutMetadata());
        helper.assertTrue(restored.getFloorName().equals("Reactor Room"), "Floor names persist in chunk NBT");
        LevelMarkerTile client = new LevelMarkerTile(pos, marker.getBlockState());
        client.loadClientDataFromNBT(marker.getUpdateTag());
        helper.assertTrue(client.getFloorName().equals("Reactor Room"), "Initial chunk sync includes floor names");
        entry.commit(player, holo, "Observation Deck");
        client.loadClientDataFromNBT(marker.getUpdatePacket().getTag());
        helper.assertTrue(client.getFloorName().equals("Observation Deck"), "Live block entity packets include renamed floors");
        entry.commit(player, holo, "A".repeat(100));
        helper.assertTrue(marker.getFloorName().length() == 32, "Floor names have a bounded length");
        entry.commit(player, holo, "   \n   ");
        helper.assertTrue(marker.getFloorName().isEmpty(), "Clearing the name restores a numbered floor");
        restored.load(new CompoundTag());
        helper.assertTrue(restored.getFloorName().isEmpty(), "Old unnamed markers load without migration");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void longFloorListsScrollIndependentlyAndKeepLandingTargets(GameTestHelper helper) throws ReflectiveOperationException {
        BlockPos base = new BlockPos(400, 40, 400);
        helper.getLevel().setBlockAndUpdate(base, Registration.ELEVATOR.get().defaultBlockState());
        ElevatorTile elevator = (ElevatorTile) helper.getLevel().getBlockEntity(base);
        elevator.setHeight(36);
        for (int i = 0; i < 12; i++) {
            BlockPos pos = base.east().above(i * 3);
            helper.getLevel().setBlockAndUpdate(pos, Registration.LEVEL_MARKER.get().defaultBlockState());
            ((LevelMarkerTile) helper.getLevel().getBlockEntity(pos)).setFloorName("Landing " + (i + 1));
        }
        // An earlier unnamed marker at the same height must not hide a later named one.
        helper.getLevel().setBlockAndUpdate(base.west(), Registration.LEVEL_MARKER.get().defaultBlockState());
        var registry = Ariente.guiHandler.getComponentRegistry();
        IGuiComponent<?> gui = elevator.createGui(ElevatorTile.TAG_ELEVATOR, registry);
        IGuiComponent<?> peer = elevator.createGui(ElevatorTile.TAG_ELEVATOR, registry);
        Player player = helper.makeMockSurvivalPlayer();
        HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
        helper.assertTrue(text(gui, "floor_range").equals("Floors 1-5 / 12")
                        && text(gui, "floor_label_0").equals("1: Landing 1"), "Duplicate markers form one named landing");
        helper.assertTrue(gui.findChild("floor_4").isPresent() && gui.findChild("floor_5").isEmpty(),
                "Large elevators have only five visible floor rows");
        helper.assertTrue(gui.findHoveringWidget(7.1, 6.08).getName().equals("floors_down"),
                "The full down-arrow area remains clickable inside its parent panel");
        for (int i = 0; i < 30; i++) {
            gui.findChild("floors_down").orElseThrow().hitClient(player, holo, 7, 5.5);
        }
        helper.assertTrue(text(gui, "floor_range").equals("Floors 8-12 / 12")
                        && text(gui, "floor_label_4").equals("12: Landing 12"), "Scrolling clamps at the last floor");
        helper.assertTrue(text(peer, "floor_range").equals("Floors 1-5 / 12"), "Another hologram retains its own scroll position");
        gui.findChild("floor_4").orElseThrow().hitClient(player, holo, 1, 5.5);
        Field target = ElevatorTile.class.getDeclaredField("moveToFloor");
        target.setAccessible(true);
        helper.assertTrue(target.get(elevator).equals(base.getY() + 33), "A scrolled row selects its landing height");
        for (int i = 0; i < 8; i++) {
            helper.getLevel().setBlockAndUpdate(base.east().above(i * 3), Blocks.AIR.defaultBlockState());
        }
        helper.getLevel().setBlockAndUpdate(base.west(), Blocks.AIR.defaultBlockState());
        helper.runAfterDelay(1, () -> {
            try {
                helper.assertTrue(text(gui, "floor_range").equals("Floors 1-4 / 4")
                                && text(gui, "floor_label_0").equals("1: Landing 9"), "Open lists refresh and clamp when floors disappear");
                helper.assertTrue(gui.findChild("floor_4").isEmpty() && gui.findChild("floors_down").isEmpty(),
                        "Unused rows and scroll controls cannot be interacted with");
                helper.assertTrue(target.get(elevator).equals(base.getY() + 33), "Removing lower floors cannot redirect a selected trip");
                helper.succeed();
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        });
    }

    @GameTest(template = "empty")
    public static void floorListsRefreshAndSupportNegativeLandingHeights(GameTestHelper helper) throws ReflectiveOperationException {
        BlockPos base = new BlockPos(420, -4, 400);
        for (BlockPos pos : BlockPos.betweenClosed(base.east(), base.east().above(10))) {
            helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        helper.getLevel().setBlockAndUpdate(base, Registration.ELEVATOR.get().defaultBlockState());
        ElevatorTile elevator = (ElevatorTile) helper.getLevel().getBlockEntity(base);
        elevator.setHeight(9);
        IGuiComponent<?> gui = elevator.createGui(ElevatorTile.TAG_ELEVATOR, Ariente.guiHandler.getComponentRegistry());
        helper.assertTrue(gui.findChild("floor_0").isEmpty() && text(gui, "floor_range").equals("No marked floors"),
                "Unmarked elevators show manual travel instructions without floor rows");
        BlockPos markerPos = base.east().above(3);
        helper.getLevel().setBlockAndUpdate(markerPos, Registration.LEVEL_MARKER.get().defaultBlockState());
        helper.runAfterDelay(1, () -> {
            try {
                helper.assertTrue(text(gui, "floor_range").equals("Floors 1-1 / 1") && text(gui, "floor_label_0").equals("1"),
                        "An already-open list discovers new, unnamed floors");
                Player player = helper.makeMockSurvivalPlayer();
                HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
                gui.findChild("floor_0").orElseThrow().hitClient(player, holo, 1, 1.5);
                Field target = ElevatorTile.class.getDeclaredField("moveToFloor");
                target.setAccessible(true);
                helper.assertTrue(target.get(elevator).equals(-1), "Y=-1 is a valid landing rather than an idle sentinel");
                for (int i = 1; i < 7; i++) {
                    helper.getLevel().setBlockAndUpdate(markerPos.above(i), Registration.LEVEL_MARKER.get().defaultBlockState());
                }
                elevator.setHeight(10);
                helper.runAfterDelay(1, () -> {
                    try {
                        gui.findChild("floors_up").orElseThrow().hitClient(player, holo, 7, 1.5);
                        helper.assertTrue(text(gui, "floor_range").equals("Floors 1-5 / 7"), "Scrolling above the first floor clamps safely");
                        ((LevelMarkerTile) helper.getLevel().getBlockEntity(markerPos)).setFloorName("Lobby");
                        helper.assertTrue(text(gui, "floor_label_0").equals("1: Lobby"), "Renames appear without reopening the list");
                        helper.succeed();
                    } catch (ReflectiveOperationException exception) {
                        throw new AssertionError(exception);
                    }
                });
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static String text(IGuiComponent<?> gui, String name) throws ReflectiveOperationException {
        Field supplier = HoloText.class.getDeclaredField("text");
        supplier.setAccessible(true);
        return ((Supplier<String>) supplier.get(gui.findChild(name).orElseThrow())).get();
    }

    @GameTest(template = "empty")
    public static void levelMarkersDoNotTrapHoveringPlayers(GameTestHelper helper) {
        BlockPos shaft = new BlockPos(128, 100, 128);
        for (BlockPos pos : BlockPos.betweenClosed(shaft.offset(-2, -2, -2), shaft.offset(2, 3, 2))) {
            helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x != 0 || z != 0) {
                    helper.getLevel().setBlockAndUpdate(shaft.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                }
            }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.getLevel().setBlockAndUpdate(shaft.relative(direction), Registration.LEVEL_MARKER.get().defaultBlockState());
        }
        Player player = helper.makeMockSurvivalPlayer();
        // The reported save has feet exactly at landing height, offset toward a shaft corner,
        // and OnGround=false. Hovering players cannot use vanilla's grounded step-up behavior.
        for (double offset : new double[]{0.30000001192093, 0.5}) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                Vec3 start = new Vec3(shaft.getX() + offset, shaft.getY(), shaft.getZ() + offset);
                Vec3 movement = new Vec3(direction.getStepX() * 0.7, 0, direction.getStepZ() * 0.7);
                player.setPos(start);
                player.setOnGround(false);
                player.setDeltaMovement(Vec3.ZERO);
                player.move(MoverType.SELF, movement);
                helper.assertTrue(player.position().distanceTo(start.add(movement)) < 0.00001,
                        "A hovering player must walk through the level marker toward " + direction + " from offset " + offset);
            }
        }
        BlockPos marker = shaft.east();
        var state = helper.getLevel().getBlockState(marker);
        helper.assertTrue(state.getCollisionShape(helper.getLevel(), marker).isEmpty(), "Level markers must have no movement collision");
        helper.assertTrue(!state.getShape(helper.getLevel(), marker).isEmpty()
                        && state.getShape(helper.getLevel(), marker).bounds().maxY == 1.0 / 16,
                "Level markers must keep their thin selection shape for interaction");
        player.setPos(marker.getX() + 0.5, marker.getY() + 1, marker.getZ() + 0.5);
        player.setOnGround(false);
        player.move(MoverType.SELF, new Vec3(0, -1.5, 0));
        helper.assertTrue(player.getY() == marker.getY(), "Players must land on the floor beneath a marker, not on the marker");
        helper.succeed();
    }
}
