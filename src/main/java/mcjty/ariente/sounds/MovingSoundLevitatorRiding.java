package mcjty.ariente.sounds;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class MovingSoundLevitatorRiding extends AbstractTickableSoundInstance {

    private final Player player;
    private final FluxLevitatorEntity levitator;

    public MovingSoundLevitatorRiding(Player playerRiding, FluxLevitatorEntity levitator) {
        super(ModSounds.levitator.get(), SoundSource.NEUTRAL, RandomSource.create());
        this.player = playerRiding;
        this.levitator = levitator;
        this.attenuation = Attenuation.NONE;
        this.relative = true;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
    }

    @Override
    public boolean canPlaySound() {
        return player == Minecraft.getInstance().player && !levitator.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (canPlaySound() && this.levitator.isAlive() && this.player.getVehicle() == this.levitator) {
            float f = Math.abs(this.levitator.getSpeed()) / 50.0f;
            float targetVolume = Mth.clamp(f, 0.0F, 1.0F) * 0.75F;
            this.volume = Mth.approach(this.volume, targetVolume, 0.075F);
        } else {
            stop();
        }
    }
}
