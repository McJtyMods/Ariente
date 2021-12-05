package mcjty.ariente.sounds;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class MovingSoundLevitator extends TickableSound {
    private final FluxLevitatorEntity levitator;
    private float distance = 0.0F;

    public MovingSoundLevitator(FluxLevitatorEntity levitatorIn) {
        super(ModSounds.levitator, SoundCategory.NEUTRAL);
        this.levitator = levitatorIn;
        this.looping = true;
        this.delay = 0;
    }

    @Override
    public void tick() {
        if (!this.levitator.isAlive()) {
            stop();
        } else {
            this.x = (float) this.levitator.getX();
            this.y = (float) this.levitator.getY();
            this.z = (float) this.levitator.getZ();
            float f = Math.abs(this.levitator.getSpeed()) / 50.0f;
//            float f = MathHelper.sqrt(this.levitator.motionX * this.levitator.motionX + this.levitator.motionZ * this.levitator.motionZ);

            if (f >= 0.01D) {
                this.distance = MathHelper.clamp(this.distance + 0.0025F, 0.0F, 1.0F);
                this.volume = 0.0F + MathHelper.clamp(f, 0.0F, 0.5F) * 0.7F;
            } else {
                this.distance = 0.0F;
                this.volume = 0.0F;
            }
        }
    }
}