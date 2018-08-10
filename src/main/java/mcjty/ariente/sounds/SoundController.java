package mcjty.ariente.sounds;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class SoundController {

    private static final Map<Pair<Integer, BlockPos>, ForcefieldSound> forcefieldSounds = Maps.newHashMap();

    public static void stopForcefieldSounds(World worldObj, BlockPos pos) {
        Pair<Integer, BlockPos> g = fromPosition(worldObj, pos);
        if (forcefieldSounds.containsKey(g)) {
            MovingSound movingSound = forcefieldSounds.get(g);
            Minecraft.getMinecraft().getSoundHandler().stopSound(movingSound);
            forcefieldSounds.remove(g);
        }
    }

    public static void playForcefieldSound(World worldObj, BlockPos pos, SoundEvent soundType, float baseVolume, int ticks) {
        ForcefieldSound sound = new ForcefieldSound(soundType, worldObj, pos, baseVolume, ticks);
        stopForcefieldSounds(worldObj, pos);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        Pair<Integer, BlockPos> g = Pair.of(worldObj.provider.getDimension(), pos);
        forcefieldSounds.put(g, sound);
    }

    public static boolean isForcefieldSoundPlayingAt(SoundEvent event, World world, BlockPos pos){
        ForcefieldSound s = getForcefieldSoundAt(world, pos);
        return s != null && s.isSoundType(event);
    }

    public static ForcefieldSound getForcefieldSoundAt(World world, BlockPos pos){
        return forcefieldSounds.get(fromPosition(world, pos));
    }


    private static Pair<Integer, BlockPos> fromPosition(World world, BlockPos producerPos){
        return Pair.of(world.provider.getDimension(), producerPos);
    }

}
