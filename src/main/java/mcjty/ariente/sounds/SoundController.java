package mcjty.ariente.sounds;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.DimensionType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class SoundController {

    private static final Map<Pair<DimensionType, BlockPos>, ForcefieldSound> forcefieldSounds = Maps.newHashMap();

    public static void stopForcefieldSounds(World worldObj, BlockPos pos) {
        Pair<DimensionType, BlockPos> g = fromPosition(worldObj, pos);
        if (forcefieldSounds.containsKey(g)) {
            TickableSound movingSound = forcefieldSounds.get(g);
            Minecraft.getInstance().getSoundHandler().stop(movingSound);
            forcefieldSounds.remove(g);
        }
    }

    public static void playForcefieldSound(World worldObj, BlockPos pos, SoundEvent soundType, float baseVolume, int ticks) {
        ForcefieldSound sound = new ForcefieldSound(soundType, worldObj, pos, baseVolume, ticks);
        stopForcefieldSounds(worldObj, pos);
        Minecraft.getInstance().getSoundHandler().play(sound);
        Pair<DimensionType, BlockPos> g = Pair.of(worldObj.getDimensionType(), pos);
        forcefieldSounds.put(g, sound);
    }

    public static boolean isForcefieldSoundPlayingAt(SoundEvent event, World world, BlockPos pos){
        ForcefieldSound s = getForcefieldSoundAt(world, pos);
        return s != null && s.isSoundType(event);
    }

    public static ForcefieldSound getForcefieldSoundAt(World world, BlockPos pos){
        return forcefieldSounds.get(fromPosition(world, pos));
    }


    private static Pair<DimensionType, BlockPos> fromPosition(World world, BlockPos producerPos){
        return Pair.of(world.getDimensionType(), producerPos);
    }

}
