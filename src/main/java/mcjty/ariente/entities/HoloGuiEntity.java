package mcjty.ariente.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class HoloGuiEntity extends Entity {

    private int timeout;

    public HoloGuiEntity(World worldIn) {
        super(worldIn);
        timeout = 20*10;
        setSize(1, 1);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        timeout--;
        if (timeout <= 0) {
            this.setDead();
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        System.out.println("HoloGuiEntity.processInitialInteract");
        World world = player.getEntityWorld();
        System.out.println("world.isRemote = " + world.isRemote);
        return false;
    }


    @Override
    public boolean hitByEntity(Entity entityIn) {
        System.out.println("HoloGuiEntity.hitByEntity");
        System.out.println("world.isRemote = " + world.isRemote);
        return super.hitByEntity(entityIn);
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.timeout = compound.getInteger("Timeout");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("Timeout", this.timeout);
    }
}
