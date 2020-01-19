package mcjty.ariente.entities;

import mcjty.ariente.items.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

public class EntityArientePearl extends Entity {

    private double targetX;
    private double targetY;
    private double targetZ;
    private int despawnTimer;
    private boolean shatterOrDrop;

    public EntityArientePearl(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        // @todo 1.14
//        this.setSize(0.25F, 0.25F);
    }

    public EntityArientePearl(EntityType<?> entityTypeIn, World worldIn, double x, double y, double z) {
        super(entityTypeIn, worldIn);
        this.despawnTimer = 0;
        // @todo 1.14
//        this.setSize(0.25F, 0.25F);
        this.setPosition(x, y, z);
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
        double d2 = d0 - this.posX;
        double d3 = d1 - this.posZ;
        float f = MathHelper.sqrt(d2 * d2 + d3 * d3);

        if (f > 12.0F) {
            this.targetX = this.posX + d2 / f * 12.0D;
            this.targetZ = this.posZ + d3 / f * 12.0D;
            this.targetY = this.posY + 8.0D;
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
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.tick();
        double motionX = this.getMotion().x;
        double motionY = this.getMotion().y;
        double motionZ = this.getMotion().z;
        this.posX += motionX;
        this.posY += motionY;
        this.posZ += motionZ;
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
            double d0 = this.targetX - this.posX;
            double d1 = this.targetZ - this.posZ;
            float f1 = (float) Math.sqrt(d0 * d0 + d1 * d1);
            float f2 = (float) MathHelper.atan2(d1, d0);
            double d2 = f + (f1 - f) * 0.0025D;

            if (f1 < 1.0F) {
                d2 *= 0.8D;
                motionY *= 0.8D;
            }

            motionX = Math.cos(f2) * d2;
            motionZ = Math.sin(f2) * d2;

            if (this.posY < this.targetY) {
                motionY += (1.0D - motionY) * 0.015;
            } else {
                motionY += (-1.0D - motionY) * 0.015;
            }
            setMotion(motionX, motionY, motionZ);
        }

        float f3 = 0.25F;

        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                this.world.addParticle(ParticleTypes.DRIPPING_WATER, this.posX - motionX * 0.25D, this.posY - motionY * 0.25D, this.posZ - motionZ * 0.25D, motionX, motionY, motionZ);
            }
        } else {
            this.world.addParticle(ParticleTypes.PORTAL, this.posX - motionX * 0.25D + this.rand.nextDouble() * 0.6D - 0.3D, this.posY - motionY * 0.25D - 0.5D, this.posZ - motionZ * 0.25D + this.rand.nextDouble() * 0.6D - 0.3D, motionX, motionY, motionZ);
        }

        if (!this.world.isRemote) {
            this.setPosition(this.posX, this.posY, this.posZ);
            ++this.despawnTimer;

            if (this.despawnTimer > 80 && !this.world.isRemote) {
                this.playSound(SoundEvents.ENTITY_ENDER_EYE_DEATH, 1.0F, 1.0F);
                this.remove();

                if (this.shatterOrDrop) {
                    this.world.addEntity(new ItemEntity(this.world, this.posX, this.posY, this.posZ, new ItemStack(ModItems.arientePearlItem)));
                } else {
                    this.world.playEvent(2003, new BlockPos(this), 0);
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