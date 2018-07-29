package mcjty.ariente.sounds;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ArienteSound extends MovingSound {

    private final World world;
    private final BlockPos pos;
    private final SoundEvent sound;

    private int ticksRemaining;

    public ArienteSound(SoundEvent event, World world, BlockPos pos, float baseVolume, int ticks) {
        super(event, SoundCategory.BLOCKS);
        this.world = world;
        this.pos = pos;
        this.xPosF = pos.getX();
        this.yPosF = pos.getY();
        this.zPosF = pos.getZ();
        this.attenuationType = AttenuationType.LINEAR;
        this.repeat = true;
        this.repeatDelay = 0;
        this.sound = event;
        this.volume = baseVolume;
        ticksRemaining = ticks;
    }

    public void setVolume(float v) {
        this.volume = v;
    }

    @Override
    public void update() {
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
        return sound == event;
    }

}
