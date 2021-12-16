package mcjty.ariente.sounds;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.DimensionType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class SoundController {

    private static final Map<Pair<DimensionType, BlockPos>, ForcefieldSound> forcefieldSounds = Maps.newHashMap();

    public static void stopForcefieldSounds(Level worldObj, BlockPos pos) {
        Pair<DimensionType, BlockPos> g = fromPosition(worldObj, pos);
        if (forcefieldSounds.containsKey(g)) {
            TickableSound movingSound = forcefieldSounds.get(g);
            Minecraft.getInstance().getSoundManager().stop(movingSound);
            forcefieldSounds.remove(g);
        }
    }

    public static void playForcefieldSound(Level worldObj, BlockPos pos, SoundEvent soundType, float baseVolume, int ticks) {
        ForcefieldSound sound = new ForcefieldSound(soundType, worldObj, pos, baseVolume, ticks);
        stopForcefieldSounds(worldObj, pos);
        Minecraft.getInstance().getSoundManager().play(sound);
        Pair<DimensionType, BlockPos> g = Pair.of(worldObj.dimensionType(), pos);
        forcefieldSounds.put(g, sound);
    }

    public static boolean isForcefieldSoundPlayingAt(SoundEvent event, Level world, BlockPos pos){
        ForcefieldSound s = getForcefieldSoundAt(world, pos);
        return s != null && s.isSoundType(event);
    }

    public static ForcefieldSound getForcefieldSoundAt(Level world, BlockPos pos){
        return forcefieldSounds.get(fromPosition(world, pos));
    }


    private static Pair<DimensionType, BlockPos> fromPosition(Level world, BlockPos producerPos){
        return Pair.of(world.dimensionType(), producerPos);
    }

}
