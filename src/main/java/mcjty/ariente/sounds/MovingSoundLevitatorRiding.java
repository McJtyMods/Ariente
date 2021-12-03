package mcjty.ariente.sounds;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;



public class MovingSoundLevitatorRiding extends TickableSound {
    private final PlayerEntity player;
    private final FluxLevitatorEntity levitator;

    public MovingSoundLevitatorRiding(PlayerEntity playerRiding, FluxLevitatorEntity levitator) {
        super(ModSounds.levitator, SoundCategory.NEUTRAL);
        this.player = playerRiding;
        this.levitator = levitator;
        this.attenuationType = AttenuationType.NONE;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    @Override
    public void tick() {
        if (this.levitator.isAlive() && this.player.isPassenger() && this.player.getRidingEntity() == this.levitator) {
            float f = Math.abs(this.levitator.getSpeed()) / 50.0f;
//            float f = MathHelper.sqrt(this.levitator.motionX * this.levitator.motionX + this.levitator.motionZ * this.levitator.motionZ);

            if (f >= 0.01D) {
                this.volume = 0.0F + MathHelper.clamp(f, 0.0F, 1.0F) * 0.75F;
            } else {
                this.volume = 0.0F;
            }
        } else {
            finishPlaying();
        }
    }
}