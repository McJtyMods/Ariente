package mcjty.ariente.sounds;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.lang3.tuple.Pair;

public class SoundController {

    private static final Map<Pair<DimensionType, BlockPos>, ForcefieldSound> forcefieldSounds = Maps.newHashMap();

    public static void stopForcefieldSounds(Level worldObj, BlockPos pos) {
        ForcefieldSound sound = getForcefieldSoundAt(worldObj, pos);
        if (sound != null) {
            // Retain the loop until it has faded, so a quick re-entry can reuse its channel.
            sound.setVolume(0);
        }
    }

    public static void playForcefieldSound(Level worldObj, BlockPos pos, SoundEvent soundType, float baseVolume, int ticks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getSoundSourceVolume(SoundSource.MASTER) <= 0 || mc.options.getSoundSourceVolume(SoundSource.BLOCKS) <= 0) {
            return;
        }
        ForcefieldSound sound = new ForcefieldSound(soundType, worldObj, pos, baseVolume, ticks);
        ForcefieldSound previous = forcefieldSounds.put(fromPosition(worldObj, pos), sound);
        if (previous != null) {
            mc.getSoundManager().stop(previous);
        }
        mc.getSoundManager().play(sound);
    }

    public static boolean isForcefieldSoundPlayingAt(SoundEvent event, Level world, BlockPos pos) {
        ForcefieldSound s = getForcefieldSoundAt(world, pos);
        return s != null && s.isSoundType(event);
    }

    public static ForcefieldSound getForcefieldSoundAt(Level world, BlockPos pos) {
        Pair<DimensionType, BlockPos> key = fromPosition(world, pos);
        ForcefieldSound sound = forcefieldSounds.get(key);
        if (sound != null && (sound.isStopped() || !sound.isForLevel(world) || !Minecraft.getInstance().getSoundManager().isActive(sound))) {
            Minecraft.getInstance().getSoundManager().stop(sound);
            forcefieldSounds.remove(key);
            return null;
        }
        return sound;
    }

    private static Pair<DimensionType, BlockPos> fromPosition(Level world, BlockPos producerPos) {
        return Pair.of(world.dimensionType(), producerPos);
    }
}
