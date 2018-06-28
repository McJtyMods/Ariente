package mcjty.ariente.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HoloGuiEntity extends Entity {

    private int timeout;

    public HoloGuiEntity(World worldIn) {
        super(worldIn);
        timeout = 20*10;
        setSize(1f, 1f);
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
        setDead();
        System.out.println("HoloGuiEntity.processInitialInteract");
        World world = player.getEntityWorld();
        System.out.println("world.isRemote = " + world.isRemote);
        return false;
    }


    private Vec3d intersect(EntityPlayer player) {
        // Center point of plane: posX, posY, posZ
        // Perpendicular to the plane: getLookVec()
        Vec3d lookVec = getLookVec();
        double xn = lookVec.x;
        double yn = lookVec.y;
        double zn = lookVec.z;

        // Plane: Ax + By + Cz + D = 0
        double D = - (xn * posX + yn * posY + zn * posZ);
        double A = xn;
        double B = yn;
        double C = zn;

        // Line (from player):
        //        x = x1 + at
        //        y = y1 + bt
        //        z = z1 + ct
        double x1 = player.posX;
        double y1 = player.posY + player.eyeHeight;
        double z1 = player.posZ;
        Vec3d playerLookVec = player.getLookVec();
        double a = playerLookVec.x;
        double b = playerLookVec.y;
        double c = playerLookVec.z;

        // Intersection:
        double factor = (A * x1 + B * y1 + C * z1 + D) / (A * a + B * b + C * c);
        double x = x1 - a * factor;
        double y = y1 - b * factor;
        double z = z1 - c * factor;

        // Origin on plane is posX, posY, posZ
        // Point on plane in 2D x direction: cross( (0,1,0), (xn,yn,zn) )
        // Point on plane in 2D y direction: posX, posY-1, posZ
        Vec3d vx = lookVec.crossProduct(new Vec3d(0, 1, 0));    // @todo optimize
        Vec3d vy = new Vec3d(0, -1, 0);
        x -= posX;
        y -= posY;
        z -= posZ;
        double x2d = vx.x * x + vx.y * y + vx.z * z;
        double y2d = vy.x * x + vy.y * y + vy.z * z;

        return new Vec3d(x2d, y2d, 0);
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        System.out.println("HoloGuiEntity.hitByEntity");
        System.out.println("world.isRemote = " + world.isRemote);
        if (entityIn instanceof EntityPlayer) {
            Vec3d vec3d = intersect((EntityPlayer) entityIn);
            System.out.println("pos = " + posX + "," + posY + "," + posZ + ", vec3d = " + vec3d);
        }
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
