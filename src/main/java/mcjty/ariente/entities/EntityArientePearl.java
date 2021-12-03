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
        entity.setPosition(x, y, z);
        return entity;
    }

    @Override
    protected void registerData() {

    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);

    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getBoundingBox().getAverageEdgeLength() * 4.0D;

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
        double d2 = d0 - this.getPosX();
        double d3 = d1 - this.getPosZ();
        float f = MathHelper.sqrt(d2 * d2 + d3 * d3);

        if (f > 12.0F) {
            this.targetX = this.getPosX() + d2 / f * 12.0D;
            this.targetZ = this.getPosZ() + d3 / f * 12.0D;
            this.targetY = this.getPosY() + 8.0D;
        } else {
            this.targetX = d0;
            this.targetY = i;
            this.targetZ = d1;
        }

        this.despawnTimer = 0;
        this.shatterOrDrop = this.rand.nextInt(5) > 0;
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        setMotion(x, y, z);

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
            this.rotationPitch = (float) (MathHelper.atan2(y, f) * (180D / Math.PI));
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }
    }

    @Override
    public void tick() {
        this.lastTickPosX = this.getPosX();
        this.lastTickPosY = this.getPosY();
        this.lastTickPosZ = this.getPosZ();
        super.tick();
        double motionX = this.getMotion().x;
        double motionY = this.getMotion().y;
        double motionZ = this.getMotion().z;
        setRawPosition(getPosX() + motionX, getPosY() + motionY, getPosZ() + motionZ);  // @todo 1.15 is this right?
        float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        this.rotationYaw = (float) (MathHelper.atan2(motionX, motionZ) * (180D / Math.PI));

        for (this.rotationPitch = (float) (MathHelper.atan2(motionY, f) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

        if (!this.world.isRemote) {
            double d0 = this.targetX - this.getPosX();
            double d1 = this.targetZ - this.getPosZ();
            float f1 = (float) Math.sqrt(d0 * d0 + d1 * d1);
            float f2 = (float) MathHelper.atan2(d1, d0);
            double d2 = f + (f1 - f) * 0.0025D;

            if (f1 < 1.0F) {
                d2 *= 0.8D;
                motionY *= 0.8D;
            }

            motionX = Math.cos(f2) * d2;
            motionZ = Math.sin(f2) * d2;

            if (this.getPosY() < this.targetY) {
                motionY += (1.0D - motionY) * 0.015;
            } else {
                motionY += (-1.0D - motionY) * 0.015;
            }
            setMotion(motionX, motionY, motionZ);
        }

        float f3 = 0.25F;

        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                this.world.addParticle(ParticleTypes.DRIPPING_WATER, this.getPosX() - motionX * 0.25D, this.getPosY() - motionY * 0.25D, this.getPosZ() - motionZ * 0.25D, motionX, motionY, motionZ);
            }
        } else {
            this.world.addParticle(ParticleTypes.PORTAL, this.getPosX() - motionX * 0.25D + this.rand.nextDouble() * 0.6D - 0.3D, this.getPosY() - motionY * 0.25D - 0.5D, this.getPosZ() - motionZ * 0.25D + this.rand.nextDouble() * 0.6D - 0.3D, motionX, motionY, motionZ);
        }

        if (!this.world.isRemote) {
            this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
            ++this.despawnTimer;

            if (this.despawnTimer > 80 && !this.world.isRemote) {
                this.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
                this.remove();

                if (this.shatterOrDrop) {
                    this.world.addEntity(new ItemEntity(this.world, this.getPosX(), this.getPosY(), this.getPosZ(), new ItemStack(Registration.ARIENTE_PEARL.get())));
                } else {
                    this.world.playEvent(2003, new BlockPos(this.getPosition()), 0);
                }
            }
        }
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
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
    public boolean canBeAttackedWithItem() {
        return false;
    }
}