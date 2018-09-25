package mcjty.ariente.sounds;

import mcjty.ariente.entities.FluxLevitatorEntity;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MovingSoundLevitator extends MovingSound {
    private final FluxLevitatorEntity levitator;
    private float distance = 0.0F;

    public MovingSoundLevitator(FluxLevitatorEntity levitatorIn) {
        super(ModSounds.levitator, SoundCategory.NEUTRAL);
        this.levitator = levitatorIn;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    @Override
    public void update() {
        if (this.levitator.isDead) {
            this.donePlaying = true;
        } else {
            this.xPosF = (float) this.levitator.posX;
            this.yPosF = (float) this.levitator.posY;
            this.zPosF = (float) this.levitator.posZ;
//            float f = Math.abs(this.levitator.getSpeed()) / 50.0f;
            float f = MathHelper.sqrt(this.levitator.motionX * this.levitator.motionX + this.levitator.motionZ * this.levitator.motionZ);

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