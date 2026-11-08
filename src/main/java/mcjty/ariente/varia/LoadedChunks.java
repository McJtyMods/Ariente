package mcjty.ariente.varia;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public final class LoadedChunks {

    private LoadedChunks() {
    }

    /** Unlike Level.getBlockEntity, this never requests or waits for a server chunk. */
    @Nullable
    public static BlockEntity getBlockEntity(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return null;
        }
        if (level instanceof ServerLevel server) {
            LevelChunk chunk = server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk == null ? null : chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
        }
        return level.hasChunkAt(pos) ? level.getBlockEntity(pos) : null;
    }
}
