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

    private static final Map<Pair<Integer, BlockPos>, ArienteSound> sounds = Maps.newHashMap();

    public static void stopSound(World worldObj, BlockPos pos) {
        Pair<Integer, BlockPos> g = fromPosition(worldObj, pos);
        if (sounds.containsKey(g)) {
            System.out.println("SoundController.stopSound");
            MovingSound movingSound = sounds.get(g);
            Minecraft.getMinecraft().getSoundHandler().stopSound(movingSound);
            sounds.remove(g);
        }
    }

    public static void playSound(World worldObj, BlockPos pos, SoundEvent soundType, float baseVolume, int ticks) {
        System.out.println("SoundController.playSound");
        ArienteSound sound = new ArienteSound(soundType, worldObj, pos, baseVolume, ticks);
        stopSound(worldObj, pos);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        Pair<Integer, BlockPos> g = Pair.of(worldObj.provider.getDimension(), pos);
        sounds.put(g, sound);
        System.out.println("SoundController.playSound : END");
    }

    public static boolean isSoundTypePlayingAt(SoundEvent event, World world, BlockPos pos){
        ArienteSound s = getSoundAt(world, pos);
        return s != null && s.isSoundType(event);
    }

    public static ArienteSound getSoundAt(World world, BlockPos pos){
        return sounds.get(fromPosition(world, pos));
    }


    private static Pair<Integer, BlockPos> fromPosition(World world, BlockPos producerPos){
        return Pair.of(world.provider.getDimension(), producerPos);
    }

}
