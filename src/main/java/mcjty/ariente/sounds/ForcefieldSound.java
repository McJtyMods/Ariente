package mcjty.ariente.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ForcefieldSound extends AbstractTickableSoundInstance {

    private final Level world;
    private final BlockPos pos;
    private final SoundEvent soundEvent;
    private final float fadeStep;
    private float targetVolume;

    public ForcefieldSound(SoundEvent event, Level world, BlockPos pos, float baseVolume, int ticks) {
        super(event, SoundSource.BLOCKS, RandomSource.create());
        this.world = world;
        this.pos = pos;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.attenuation = Attenuation.LINEAR;
        this.looping = true;
        this.delay = 0;
        this.soundEvent = event;
        this.volume = 0;
        this.targetVolume = baseVolume;
        this.fadeStep = 2f / Math.max(1, ticks);
    }

    public void setVolume(float v) {
        targetVolume = Mth.clamp(v, 0, 2);
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    boolean isForLevel(Level level) {
        return world == level;
    }

    @Override
    public void tick() {
        if (world != Minecraft.getInstance().level) {
            stop();
            return;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (!(te instanceof ISoundProducer)) {
            targetVolume = 0;
        }
        volume = Mth.approach(volume, targetVolume, fadeStep);
        if (volume == 0 && targetVolume == 0) {
            stop();
        }
    }

    protected boolean isSoundType(SoundEvent event) {
        return soundEvent == event;
    }
}
