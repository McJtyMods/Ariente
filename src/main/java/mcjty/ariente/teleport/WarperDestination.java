package mcjty.ariente.teleport;

import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** An address, deliberately independent of the lifetime of a particular block entity. */
public record WarperDestination(ResourceKey<Level> dimension, BlockPos pos) {

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dimension", dimension.identifier().toString());
        tag.putLong("Pos", pos.asLong());
        return tag;
    }

    @Nullable
    public static WarperDestination load(CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr("Dimension", ""));
        if (id == null || !tag.contains("Pos")) {
            return null;
        }
        return new WarperDestination(ResourceKey.create(Registries.DIMENSION, id), BlockPos.of(tag.getLongOr("Pos", 0L)));
    }

    /** Only explicit selection, configuration and travel may load the destination chunk. */
    @Nullable
    public ServerLevel resolve(MinecraftServer server) {
        ServerLevel level = server.getLevel(dimension);
        if (level == null || !level.getWorldBorder().isWithinBounds(pos) || level.isOutsideBuildHeight(pos)) {
            return null;
        }
        level.getChunkAt(pos);
        return level.getBlockState(pos).is(Registration.WARPER.get()) ? level : null;
    }

    public String coordinates() {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
}
