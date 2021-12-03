package mcjty.ariente.entities;

import mcjty.ariente.setup.Registration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityArientePearl extends Entity implements IRendersAsItem {

    private double targetX;
    private double targetY;
    private double targetZ;
    private int despawnTimer;
    private boolean shatterOrDrop;

    public EntityArientePearl(EntityType<? extends EntityArientePearl> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Registration.ARIENTE_PEARL.get());
    }

    public static EntityArientePearl create(World worldIn, double x, double y, double z) {
        EntityArientePearl entity = new EntityArientePearl(Registration.ENTITY_PEARL.get(), worldIn);
        entity.despawnTimer = 0;
        entity.setPos(x, y, z);
        return entity;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);

    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 4.0D;

        if (Double.isNaN(d0)) {
            d0 = 4.0D;
        }

        d0 = d0 * 64.0D;
        return distance < d0 * d0;
    }

    public void moveTowards(BlockPos pos) {
        double d0 = pos.getX();
        int i = pos.getY();
        double d1 = pos.getZ();
        double d2 = d0 - this.getX();
        double d3 = d1 - this.getZ();
        float f = MathHelper.sqrt(d2 * d2 + d3 * d3);

        if (f > 12.0F) {
            this.targetX = this.getX() + d2 / f * 12.0D;
            this.targetZ = this.getZ() + d3 / f * 12.0D;
            this.targetY = this.getY() + 8.0D;
        } else {
            this.targetX = d0;
            this.targetY = i;
            this.targetZ = d1;
        }

        this.despawnTimer = 0;
        this.shatterOrDrop = this.random.nextInt(5) > 0;
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        setDeltaMovement(x, y, z);

        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            this.yRot = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
            this.xRot = (float) (MathHelper.atan2(y, f) * (180D / Math.PI));
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }
    }

    @Override
    public void tick() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        super.tick();
        double motionX = this.getDeltaMovement().x;
        double motionY = this.getDeltaMovement().y;
        double motionZ = this.getDeltaMovement().z;
        setPosRaw(getX() + motionX, getY() + motionY, getZ() + motionZ);  // @todo 1.15 is this right?
        float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        this.yRot = (float) (MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));

        for (this.xRot = (float) (MathHelper.atan2(motionY, f) * (180D / Math.PI)); this.xRot - this.xRotO < -180.0F; this.xRotO -= 360.0F) {
            ;
        }

        while (this.xRot - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while (this.yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while (this.yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        this.xRot = this.xRotO + (this.xRot - this.xRotO) * 0.2F;
        this.yRot = this.yRotO + (this.yRot - this.yRotO) * 0.2F;

        if (!this.level.isClientSide) {
            double d0 = this.targetX - this.getX();
            double d1 = this.targetZ - this.getZ();
            float f1 = (float) Math.sqrt(d0 * d0 + d1 * d1);
            float f2 = (float) MathHelper.atan2(d1, d0);
            double d2 = f + (f1 - f) * 0.0025D;

            if (f1 < 1.0F) {
                d2 *= 0.8D;
                motionY *= 0.8D;
            }

            motionX = Math.cos(f2) * d2;
            motionZ = Math.sin(f2) * d2;

            if (this.getY() < this.targetY) {
                motionY += (1.0D - motionY) * 0.015;
            } else {
                motionY += (-1.0D - motionY) * 0.015;
            }
            setDeltaMovement(motionX, motionY, motionZ);
        }

        float f3 = 0.25F;

        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                this.level.addParticle(ParticleTypes.DRIPPING_WATER, this.getX() - motionX * 0.25D, this.getY() - motionY * 0.25D, this.getZ() - motionZ * 0.25D, motionX, motionY, motionZ);
            }
        } else {
            this.level.addParticle(ParticleTypes.PORTAL, this.getX() - motionX * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, this.getY() - motionY * 0.25D - 0.5D, this.getZ() - motionZ * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, motionX, motionY, motionZ);
        }

        if (!this.level.isClientSide) {
            this.setPos(this.getX(), this.getY(), this.getZ());
            ++this.despawnTimer;

            if (this.despawnTimer > 80 && !this.level.isClientSide) {
                this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
                this.remove();

                if (this.shatterOrDrop) {
                    this.level.addFreshEntity(new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), new ItemStack(Registration.ARIENTE_PEARL.get())));
                } else {
                    this.level.levelEvent(2003, new BlockPos(this.blockPosition()), 0);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public int getBrightnessForRender() {
//        return 15728880;
//    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}