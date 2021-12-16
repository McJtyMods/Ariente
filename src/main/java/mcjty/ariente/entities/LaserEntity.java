package mcjty.ariente.entities;

import com.mojang.datafixers.DataFixer;
import mcjty.ariente.api.IForceFieldTile;
import mcjty.ariente.api.IForcefieldImmunity;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.math.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.network.NetworkHooks;


public class LaserEntity extends Entity implements IForcefieldImmunity {

    private static final EntityDataAccessor<Float> SPAWN_YAW = SynchedEntityData.<Float>defineId(LaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPAWN_PITCH = SynchedEntityData.<Float>defineId(LaserEntity.class, EntityDataSerializers.FLOAT);

    private LivingEntity shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    private double accelerationX;
    private double accelerationY;
    private double accelerationZ;

    private int soundTicker = 0;

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    public LaserEntity(EntityType<? extends LaserEntity> type, Level worldIn) {
        super(type, worldIn);
        // @todo 1.14
//        this.setSize(1.0F, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SPAWN_YAW, 0.0f);
        this.entityData.define(SPAWN_PITCH, 0.0f);
    }

    @Override
    public boolean isImmuneToForcefield(IForceFieldTile tile) {
        // @todo only make immune if this laser belongs to the controller of the forcefield
        return true;
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

    public void setSpawnYawPitch(float spawnYaw, float spawnPitch) {
        this.entityData.set(SPAWN_YAW, spawnYaw);
        this.entityData.set(SPAWN_PITCH, spawnPitch);
    }

    public float getSpawnYaw() {
        return this.entityData.get(SPAWN_YAW);
    }

    public float getSpawnPitch() {
        return this.entityData.get(SPAWN_PITCH);
    }

    public static LaserEntity create(Level worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
        LaserEntity entity = new LaserEntity(Registration.ENTITY_LASER.get(), worldIn);
        entity.moveTo(x, y, z, entity.yRot, entity.xRot);
        entity.setPos(x, y, z);
        double d0 = MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        entity.accelerationX = accelX / d0 * 0.1D;
        entity.accelerationY = accelY / d0 * 0.1D;
        entity.accelerationZ = accelZ / d0 * 0.1D;
        return entity;
    }

    public static LaserEntity create(Level worldIn, LivingEntity shooter, double accelX, double accelY, double accelZ) {
        LaserEntity entity = new LaserEntity(Registration.ENTITY_LASER.get(), worldIn);
        entity.shootingEntity = shooter;
        entity.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.yRot, shooter.xRot);
        entity.setPos(entity.getX(), entity.getY(), entity.getZ());
        entity.setDeltaMovement(0, 0, 0);
        accelX = accelX + entity.random.nextGaussian() * 0.4D;
        accelY = accelY + entity.random.nextGaussian() * 0.4D;
        accelZ = accelZ + entity.random.nextGaussian() * 0.4D;
        double d0 = MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        entity.accelerationX = accelX / d0 * 0.1D;
        entity.accelerationY = accelY / d0 * 0.1D;
        entity.accelerationZ = accelZ / d0 * 0.1D;
        entity.setSpawnYawPitch(shooter.yRot, shooter.xRot);
        return entity;
    }

    @Override
    public void baseTick() {
        super.baseTick();

        soundTicker--;
        if (soundTicker <= 0) {
            level.playSound(null, getX(), getY(), getZ(), ModSounds.laser, SoundSource.HOSTILE, 5.0f, 1.0f);
            soundTicker = 40;
        }
    }

    @Override
    public void tick() {
        if (this.level.isClientSide || (this.shootingEntity == null || this.shootingEntity.isAlive()) && this.level.hasChunkAt(this.blockPosition())) {
            super.tick();


            ++this.ticksInAir;
//            RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);
            RayTraceResult raytraceresult = ProjectileHelper.getHitResult(this, (entity) -> { return entity != this.shootingEntity || this.ticksInAir >= 25; });

            if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                this.onImpact(raytraceresult);
            }

            double motionX = this.getDeltaMovement().x;
            double motionY = this.getDeltaMovement().y;
            double motionZ = this.getDeltaMovement().z;

            // @todo 1.15 is this right? setRawPosition
//            this.posX += motionX;
//            this.posY += motionY;
//            this.posZ += motionZ;
            this.setPosRaw(getX() + motionX, getY() + motionY, getZ() + motionZ);

            ProjectileHelper.rotateTowardsMovement(this, 0.2F);
            float f = this.getMotionFactor();

            if (this.isInWater()) {
                for (int i = 0; i < 4; ++i) {
                    float f1 = 0.25F;
                    this.level.addParticle(ParticleTypes.DRIPPING_WATER, this.getX() - motionX * 0.25D, this.getY() - motionY * 0.25D, this.getZ() - motionZ * 0.25D, motionX, motionY, motionZ);
                }

                f = 0.8F;
            }

            motionX += this.accelerationX;
            motionY += this.accelerationY;
            motionZ += this.accelerationZ;
            motionX *= f;
            motionY *= f;
            motionZ *= f;
            setDeltaMovement(motionX, motionY, motionZ);
//            this.world.spawnParticle(this.getParticleType(), this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), 0.0D, 0.0D, 0.0D);
            this.setPos(this.getX(), this.getY(), this.getZ());
        } else {
            this.remove();
        }
    }

    protected ParticleType getParticleType() {
        return ParticleTypes.SMOKE;
    }

    protected float getMotionFactor() {
        return 0.95F;
    }

    protected void onImpact(RayTraceResult result) {
        if (!this.level.isClientSide && result instanceof EntityRayTraceResult) {
            if (((EntityRayTraceResult) result).getEntity() != null) {
                ((EntityRayTraceResult) result).getEntity().hurt(DamageSource.indirectMagic(this, this.shootingEntity), 10.0F);

//                this.applyEnchantments(this.shootingEntity, result.entityHit);
            }
            this.remove();
        }
    }

    public static void registerFixesFireball(DataFixer fixer, String name) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.put("direction", this.newDoubleList(new double[]{this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z}));
        compound.put("power", this.newDoubleList(new double[]{this.accelerationX, this.accelerationY, this.accelerationZ}));
        compound.putInt("life", this.ticksAlive);
        compound.putFloat("spawnYaw", getSpawnYaw());
        compound.putFloat("spawnPitch", getSpawnPitch());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("power", 9)) {
            ListNBT nbttaglist = compound.getList("power", 6);

            if (nbttaglist.size() == 3) {
                this.accelerationX = nbttaglist.getDouble(0);
                this.accelerationY = nbttaglist.getDouble(1);
                this.accelerationZ = nbttaglist.getDouble(2);
            }
        }

        this.ticksAlive = compound.getInt("life");

        if (compound.contains("direction", 9) && compound.getList("direction", 6).size() == 3) {
            ListNBT lst = compound.getList("direction", 6);
            setDeltaMovement(lst.getDouble(0), lst.getDouble(1), lst.getDouble(2));
        } else {
            this.remove();
        }

        float yaw = compound.getFloat("spawnYaw");
        float pitch = compound.getFloat("spawnPitch");
        setSpawnYawPitch(yaw, pitch);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            this.markHurt();

            if (source.getEntity() != null) {
                Vec3 vec3d = source.getEntity().getLookAngle();

                if (vec3d != null) {
                    setDeltaMovement(vec3d);
                    this.accelerationX = vec3d.x * 0.1D;
                    this.accelerationY = vec3d.y * 0.1D;
                    this.accelerationZ = vec3d.z * 0.1D;
                }

                if (source.getEntity() instanceof LivingEntity) {
                    this.shootingEntity = (LivingEntity) source.getEntity();
                }

                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

// @todo 1.15
//    @Override
//    public int getBrightnessForRender() {
//        return 15728880;
//    }
}