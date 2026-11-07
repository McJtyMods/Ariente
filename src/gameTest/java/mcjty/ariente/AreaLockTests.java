package mcjty.ariente;

import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.blocks.utility.ILockTarget;
import mcjty.ariente.blocks.utility.LockTile;
import mcjty.ariente.blocks.utility.wireless.WirelessLockTile;
import mcjty.ariente.security.AreaLocks;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Comparator;
import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class AreaLockTests {

    private static final TicketType<ChunkPos> TICKET = TicketType.create("area_lock_test", Comparator.comparingLong(ChunkPos::toLong));

    @GameTest(template = "empty")
    public static void savedTargetsReplayTogglesRangeChangesAndRemoval(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = fixture(helper);
        BlockPos controllerPos = pos.west(3);
        for (Block block : targets()) {
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            level.setBlockAndUpdate(controllerPos, Registration.LOCK.get().defaultBlockState());
            LockTile lock = (LockTile) level.getBlockEntity(controllerPos);
            lock.setHorizontalRange(5);
            CompoundTag saved = detach(level, pos);
            lock.setLocked(true);
            BlockEntity target = restore(level, pos, saved);
            helper.assertTrue(((ILockTarget) target).isLocked(), "A returning target receives a missed lock: " + block);

            saved = detach(level, pos);
            lock.setLocked(false);
            target = restore(level, pos, saved);
            helper.assertTrue(!((ILockTarget) target).isLocked(), "A returning target receives a missed unlock: " + block);

            lock.setLocked(true);
            saved = detach(level, pos);
            lock.setHorizontalRange(1);
            target = restore(level, pos, saved);
            helper.assertTrue(!((ILockTarget) target).isLocked(), "Shrinking the range releases an absent target: " + block);

            lock.setHorizontalRange(5);
            saved = detach(level, pos);
            level.removeBlock(controllerPos, false);
            target = restore(level, pos, saved);
            helper.assertTrue(!((ILockTarget) target).isLocked(), "Removing the controller releases an absent target: " + block);
            level.removeBlock(pos, false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void constantWirelessLocksAndOverlappingControllersRespectAuthority(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = fixture(helper);
        BlockPos first = pos.west(3);
        BlockPos second = pos.east(3);
        level.setBlockAndUpdate(pos, Registration.HOLO_STORAGE_1.get().defaultBlockState());
        CompoundTag saved = detach(level, pos);
        level.setBlockAndUpdate(first, Registration.WIRELESS_LOCK.get().defaultBlockState());
        WirelessLockTile wireless = (WirelessLockTile) level.getBlockEntity(first);
        wireless.setLocked(true);
        BlockEntity target = restore(level, pos, saved);
        wireless.setLocked(true);
        helper.assertTrue(((ILockTarget) target).isLocked(), "An unchanged wireless state still applies when a target returns");
        level.setBlockAndUpdate(second, Registration.LOCK.get().defaultBlockState());
        LockTile lock = (LockTile) level.getBlockEntity(second);
        lock.setLocked(true);
        wireless.setLocked(false);
        helper.assertTrue(((ILockTarget) target).isLocked(), "One controller cannot release another authorized locked controller");
        level.removeBlock(first, false);
        helper.assertTrue(((ILockTarget) target).isLocked(), "Removing one controller preserves the other controller's protection");
        ((StorageCubeTile) target).setKeyId("independent-city-key");
        level.removeBlock(second, false);
        helper.assertTrue(((ILockTarget) target).isLocked(), "An old controller cannot unlock a target rekeyed to another authority");
        level.removeBlock(pos, false);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1000)
    public static void returningChunkReceivesLockWithoutLoadingNeighbors(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = fixture(helper);
        BlockPos pos = new BlockPos((origin.getX() & ~15) + 16, origin.getY(), origin.getZ());
        BlockPos controllerPos = pos.west(3);
        ChunkPos controllerChunk = new ChunkPos(controllerPos);
        ChunkPos targetChunk = new ChunkPos(pos);
        // A level-33 ticket retains only the controller's full chunk, not its neighbor.
        level.getChunkSource().addRegionTicket(TICKET, controllerChunk, 0, controllerChunk);
        level.getChunkSource().addRegionTicket(TICKET, targetChunk, 0, targetChunk);
        level.setBlockAndUpdate(controllerPos, Registration.LOCK.get().defaultBlockState());
        level.setBlockAndUpdate(pos, Registration.HOLO_STORAGE_1.get().defaultBlockState());
        LockTile lock = (LockTile) level.getBlockEntity(controllerPos);
        lock.setHorizontalRange(5);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(level.getChunkSource().getChunkNow(targetChunk.x, targetChunk.z) != null
                                && level.getChunkSource().getChunkNow(controllerChunk.x, controllerChunk.z) != null,
                        "Both fixture chunks must first become fully loaded"))
                .thenIdle(2)
                .thenExecute(() -> level.getChunkSource().removeRegionTicket(TICKET, targetChunk, 0, targetChunk))
                .thenWaitUntil(() -> helper.assertTrue(level.getChunkSource().getChunkNow(targetChunk.x, targetChunk.z) == null,
                        "The target chunk must become unavailable"))
                .thenExecute(() -> {
                    lock.setLocked(true);
                    helper.assertTrue(level.getChunkSource().getChunkNow(targetChunk.x, targetChunk.z) == null,
                            "Changing a lock must not reload its target chunk");
                    level.getChunkSource().addRegionTicket(TICKET, targetChunk, 0, targetChunk);
                    level.getChunk(targetChunk.x, targetChunk.z);
                })
                .thenWaitUntil(() -> helper.assertTrue(level.getChunkSource().getChunkNow(targetChunk.x, targetChunk.z) != null,
                        "The returning target chunk must finish promotion"))
                .thenIdle(3)
                .thenExecute(() -> {
                    helper.assertTrue(((ILockTarget) level.getBlockEntity(pos)).isLocked(),
                            "A returning chunk must receive the missed controller transition: controller=" + lock.isLocked()
                                    + ", same=" + (level.getBlockEntity(controllerPos) == lock)
                                    + ", registry=" + AreaLocks.get(level).save(new CompoundTag()));
                    level.removeBlock(controllerPos, false);
                    level.removeBlock(pos, false);
                    level.getChunkSource().removeRegionTicket(TICKET, controllerChunk, 0, controllerChunk);
                    level.getChunkSource().removeRegionTicket(TICKET, targetChunk, 0, targetChunk);
                })
                .thenSucceed();
    }

    private static BlockPos fixture(GameTestHelper helper) {
        BlockPos test = helper.absolutePos(BlockPos.ZERO);
        return new BlockPos(100000 + test.getX() * 64, 200, 100000 + test.getZ() * 64);
    }

    private static CompoundTag detach(ServerLevel level, BlockPos pos) {
        CompoundTag saved = level.getBlockEntity(pos).saveWithFullMetadata();
        level.removeBlockEntity(pos);
        return saved;
    }

    private static BlockEntity restore(ServerLevel level, BlockPos pos, CompoundTag saved) {
        BlockState state = level.getBlockState(pos);
        BlockEntity target = BlockEntity.loadStatic(pos, state, saved);
        level.setBlockEntity(target);
        AreaLocks.get(level).reconcile(target);
        return target;
    }

    private static List<Block> targets() {
        return List.of(Registration.STORAGE.get(), Registration.STORAGE_3X3.get(), Registration.HOLO_STORAGE_1.get(),
                Registration.HOLO_STORAGE_2.get(), Registration.HOLO_STORAGE_3.get(), Registration.DOOR_MARKER.get());
    }
}
