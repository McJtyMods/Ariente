package mcjty.ariente.sounds;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class MovingSoundLevitator extends AbstractTickableSoundInstance {

    private final FluxLevitatorEntity levitator;

    public MovingSoundLevitator(FluxLevitatorEntity levitatorIn) {
        super(ModSounds.levitator.get(), SoundSource.NEUTRAL, RandomSource.create());
        this.levitator = levitatorIn;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.x = levitatorIn.getX();
        this.y = levitatorIn.getY();
        this.z = levitatorIn.getZ();
    }

    @Override
    public boolean canPlaySound() {
        return !levitator.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.levitator.isAlive()) {
            stop();
        } else {
            this.x = this.levitator.getX();
            this.y = this.levitator.getY();
            this.z = this.levitator.getZ();
            float f = Math.abs(this.levitator.getSpeed()) / 50.0f;
            float targetVolume = Mth.clamp(f, 0.0F, 0.5F) * 0.7F;
            this.volume = Mth.approach(this.volume, targetVolume, 0.035F);
        }
    }
}
