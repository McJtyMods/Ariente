package mcjty.ariente.sounds;

import mcjty.ariente.entities.FluxLevitatorEntity;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MovingSoundLevitatorRiding extends MovingSound {
    private final EntityPlayer player;
    private final FluxLevitatorEntity levitator;

    public MovingSoundLevitatorRiding(EntityPlayer playerRiding, FluxLevitatorEntity levitator) {
        super(ModSounds.levitator, SoundCategory.NEUTRAL);
        this.player = playerRiding;
        this.levitator = levitator;
        this.attenuationType = AttenuationType.NONE;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    @Override
    public void update() {
        if (!this.levitator.isDead && this.player.isRiding() && this.player.getRidingEntity() == this.levitator) {
            float f = Math.abs(this.levitator.getSpeed()) / 50.0f;
//            float f = MathHelper.sqrt(this.levitator.motionX * this.levitator.motionX + this.levitator.motionZ * this.levitator.motionZ);

            if (f >= 0.01D) {
                this.volume = 0.0F + MathHelper.clamp(f, 0.0F, 1.0F) * 0.75F;
            } else {
                this.volume = 0.0F;
            }
        } else {
            this.donePlaying = true;
        }
    }
}