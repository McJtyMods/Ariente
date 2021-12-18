package mcjty.ariente.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ForcefieldSound extends AbstractTickableSoundInstance {

    private final Level world;
    private final BlockPos pos;
    private final SoundEvent soundEvent;

    public ForcefieldSound(SoundEvent event, Level world, BlockPos pos, float baseVolume, int ticks) {
        super(event, SoundSource.BLOCKS);
        this.world = world;
        this.pos = pos;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.attenuation = Attenuation.LINEAR;
        this.looping = true;
        this.delay = 0;
        this.soundEvent = event;
        this.volume = baseVolume;
    }

    public void setVolume(float v) {
        this.volume = v;
    }

    @Override
    public void tick() {
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof ISoundProducer)) {
            stop();
            return;
        }
//        if (ticksRemaining != -1) {
//            ticksRemaining--;
//            if (ticksRemaining <= 0) {
//                donePlaying = true;
//            }
//        }
    }

    protected boolean isSoundType(SoundEvent event){
        return soundEvent == event;
    }

}
