package mcjty.ariente.sounds;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.math.MathHelper;



import net.minecraft.client.audio.ISound.AttenuationType;

public class MovingSoundLevitatorRiding extends TickableSound {
    private final Player player;
    private final FluxLevitatorEntity levitator;

    public MovingSoundLevitatorRiding(Player playerRiding, FluxLevitatorEntity levitator) {
        super(ModSounds.levitator, SoundSource.NEUTRAL);
        this.player = playerRiding;
        this.levitator = levitator;
        this.attenuation = AttenuationType.NONE;
        this.looping = true;
        this.delay = 0;
    }

    @Override
    public void tick() {
        if (this.levitator.isAlive() && this.player.isPassenger() && this.player.getVehicle() == this.levitator) {
            float f = Math.abs(this.levitator.getSpeed()) / 50.0f;
//            float f = MathHelper.sqrt(this.levitator.motionX * this.levitator.motionX + this.levitator.motionZ * this.levitator.motionZ);

            if (f >= 0.01D) {
                this.volume = 0.0F + MathHelper.clamp(f, 0.0F, 1.0F) * 0.75F;
            } else {
                this.volume = 0.0F;
            }
        } else {
            stop();
        }
    }
}