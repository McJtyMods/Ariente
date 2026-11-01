package mcjty.ariente;

import mcjty.ariente.blocks.utility.autofield.AutoFieldTile;
import mcjty.ariente.blocks.utility.autofield.FieldMarkerTile;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.ariente.setup.Registration;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class AutoFieldLifecycleTests {

    @GameTest(template = "empty")
    public static void controllerUnloadAndRemovalHaveDifferentOwnershipRules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above(2);
        AutoFieldTile field = controller(level, pos);
        FieldMarkerTile marker = marker(level, pos.east());
        field.getFieldBox();
        helper.assertTrue(pos.equals(marker.getAutoFieldTile()), "A connected multipart marker must belong to the controller");
        level.setBlockAndUpdate(pos, field.getBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
        helper.assertTrue(pos.equals(marker.getAutoFieldTile()), "Changing controller orientation must preserve marker ownership");

        field.onChunkUnloaded();
        field.setRemoved();
        helper.assertTrue(pos.equals(marker.getAutoFieldTile()), "Unloading a controller must retain its saved marker ownership");
        CompoundTag saved = field.saveWithoutMetadata();
        AutoFieldTile restored = new AutoFieldTile(pos, field.getBlockState());
        restored.load(saved);
        level.setBlockEntity(restored);
        restored.onLoad();
        restored.getFieldBox();
        helper.assertTrue(pos.equals(marker.getAutoFieldTile()), "A restored controller must reclaim its multipart markers");

        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        helper.assertTrue(marker.getAutoFieldTile() == null, "Replacing a controller must release its multipart markers");
        helper.assertTrue(!marker.saveWithoutMetadata().getCompound("Info").contains("autofield"),
                "Released marker ownership must also be removed from saved data");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void newlyLoadedChunkExtendsAnAlreadyCachedField(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkPos testChunk = new ChunkPos(helper.absolutePos(BlockPos.ZERO));
        ChunkPos fieldChunk = new ChunkPos(testChunk.x + 256, testChunk.z);
        BlockPos pos = new BlockPos(fieldChunk.getMinBlockX() + 13, 80, fieldChunk.getMinBlockZ() + 8);
        AutoFieldTile field = controller(level, pos);
        try {
            marker(level, pos.east());
            marker(level, pos.east(2));
            boolean boundaryLoaded = level.hasChunkAt(pos.east(3));
            AABB initial = field.getFieldBox();
            helper.assertTrue(initial != null && initial.maxX == pos.getX() + 3,
                    "The initial field must end at the loaded chunk boundary");
            helper.assertTrue(level.hasChunkAt(pos.east(3)) == boundaryLoaded, "Discovery must not force the neighboring chunk to load");

            // The real chunk-load event is delivered before this marker's saved contents are installed.
            FieldMarkerTile loaded = marker(level, pos.east(3));
            if (boundaryLoaded) {
                // Chunk generation may already have promoted this neighbor. Replay its reload event in that case.
                MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(level.getChunkAt(pos.east(3)), false));
            }
            field.tickServer();
            helper.assertTrue(field.getFieldBox().maxX == pos.getX() + 4,
                    "A newly loaded boundary chunk must refresh an existing field cache");
            helper.assertTrue(pos.equals(loaded.getAutoFieldTile()), "The newly discovered marker must be linked to its controller");
        } finally {
            level.removeBlock(pos, false);
            for (int x = 1; x <= 3; x++) {
                BlockPos markerPos = pos.east(x);
                if (level.hasChunkAt(markerPos)) {
                    level.removeBlock(markerPos, false);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chunkUnloadInvalidatesCachedMarkerTopology(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above(2);
        AutoFieldTile field = controller(level, pos);
        FieldMarkerTile first = marker(level, pos.east());
        marker(level, pos.east(2));
        field.getFieldBox();

        // Simulate a chunk replacing its contents without running player placement/removal hooks.
        MultipartTE container = (MultipartTE) level.getBlockEntity(pos.east(2));
        container.load(new CompoundTag());
        MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(level.getChunkAt(pos.east(2))));
        field.tickServer();
        helper.assertTrue(field.getFieldBox().maxX == pos.getX() + 2,
                "Chunk unload must invalidate cached marker topology before the next transfer tick");
        helper.assertTrue(pos.equals(first.getAutoFieldTile()), "Refreshing topology must retain connected marker ownership");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void disconnectingMarkersReleasesOnlyThisControllersLinks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above(2);
        AutoFieldTile field = controller(level, pos);
        marker(level, pos.east());
        FieldMarkerTile detached = marker(level, pos.east(2));
        field.getFieldBox();
        level.removeBlock(pos.east(), false);
        field.tickServer();
        helper.assertTrue(detached.getAutoFieldTile() == null, "Disconnected markers must release stale controller links");

        FieldMarkerTile other = marker(level, pos.north());
        field.getFieldBox();
        BlockPos otherOwner = pos.north(2);
        controller(level, otherOwner);
        other.setAutoFieldTile(otherOwner);
        level.removeBlock(pos, false);
        helper.assertTrue(otherOwner.equals(other.getAutoFieldTile()), "Controller removal must preserve another controller's marker ownership");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void loadingUnlinkedMarkerClearsPreviousOwnership(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above(2);
        FieldMarkerTile marker = marker(level, pos);
        marker.setAutoFieldTile(pos.west());
        CompoundTag saved = marker.saveWithoutMetadata();
        saved.getCompound("Info").remove("autofield");
        marker.load(saved);
        helper.assertTrue(marker.getAutoFieldTile() == null, "Loading unlinked marker data must clear the previous controller position");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void markerWaitsForOwnerChunkBeforeDiscardingAStaleLink(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(BlockPos.ZERO).above(2);
        BlockPos owner = pos.west(8192);
        FieldMarkerTile marker = marker(level, pos);
        helper.assertTrue(!level.hasChunkAt(owner), "The owner chunk must start unloaded");
        marker.setAutoFieldTile(owner);
        helper.assertTrue(owner.equals(marker.getAutoFieldTile()) && !level.hasChunkAt(owner),
                "Reading ownership must preserve unloaded owners without loading their chunks");

        level.getChunkAt(owner);
        helper.assertTrue(marker.getAutoFieldTile() == null,
                "Once the owner chunk loads, a missing controller must release its stale marker link");
        helper.succeed();
    }

    private static AutoFieldTile controller(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Registration.AUTOMATION_FIELD.get().defaultBlockState());
        AutoFieldTile field = (AutoFieldTile) level.getBlockEntity(pos);
        field.onLoad();
        return field;
    }

    private static FieldMarkerTile marker(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Registration.MULTIPART_BLOCK.get().defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        MultipartTE container = (MultipartTE) level.getBlockEntity(pos);
        FieldMarkerTile marker = new FieldMarkerTile(pos, Registration.FIELD_MARKER.get().defaultBlockState());
        container.addPart(PartSlot.DOWN, marker.getBlockState(), marker);
        return marker;
    }
}
