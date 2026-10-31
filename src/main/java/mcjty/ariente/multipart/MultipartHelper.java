package mcjty.ariente.multipart;

import mcjty.lib.multipart.PartSlot;
import mcjty.lib.multipart.PartPos;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.minecraft.core.BlockPos;

public class MultipartHelper {

    public static BlockEntity getBlockEntity(BlockGetter access, BlockPos pos, PartSlot slot) {
        if (access instanceof Level level && !level.hasChunkAt(pos)) {
            return null;
        }
        if (access.getBlockEntity(pos) instanceof MultipartTE multipart) {
            MultipartTE.Part part = multipart.getParts().get(slot);
            return part == null ? null : part.getBlockEntity();
        }
        return null;
    }

    public static BlockEntity getBlockEntity(BlockGetter access, PartPos pos) {
        return getBlockEntity(access, pos.pos(), pos.slot());
    }

    public static BlockState getBlockState(BlockGetter access, BlockPos pos, PartSlot slot) {
        if (access instanceof Level level && !level.hasChunkAt(pos)) {
            return null;
        }
        if (access.getBlockEntity(pos) instanceof MultipartTE multipart) {
            MultipartTE.Part part = multipart.getParts().get(slot);
            return part == null ? null : part.getState();
        }
        return null;
    }

    public static Vec3 getPlayerEyes(Player player) {
        return player.getEyePosition();
    }
}
