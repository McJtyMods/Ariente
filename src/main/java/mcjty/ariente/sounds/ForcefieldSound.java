package mcjty.ariente.sounds;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ForcefieldSound extends TickableSound {

    private final World world;
    private final BlockPos pos;
    private final SoundEvent soundEvent;

    public ForcefieldSound(SoundEvent event, World world, BlockPos pos, float baseVolume, int ticks) {
        super(event, SoundCategory.BLOCKS);
        this.world = world;
        this.pos = pos;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.attenuationType = AttenuationType.LINEAR;
        this.repeat = true;
        this.repeatDelay = 0;
        this.soundEvent = event;
        this.volume = baseVolume;
    }

    public void setVolume(float v) {
        this.volume = v;
    }

    @Override
    public void tick() {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof ISoundProducer)) {
            donePlaying = true;
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
