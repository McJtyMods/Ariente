package mcjty.ariente.entities;

import com.mojang.datafixers.DataFixer;
import mcjty.ariente.api.IForceFieldTile;
import mcjty.ariente.api.IForcefieldImmunity;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;


public class LaserEntity extends Entity implements IForcefieldImmunity {

    private static final DataParameter<Float> SPAWN_YAW = EntityDataManager.<Float>createKey(LaserEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> SPAWN_PITCH = EntityDataManager.<Float>createKey(LaserEntity.class, DataSerializers.FLOAT);

    private LivingEntity shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    private double accelerationX;
    private double accelerationY;
    private double accelerationZ;

    private int soundTicker = 0;

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    public LaserEntity(EntityType<? extends LaserEntity> type, World worldIn) {
        super(type, worldIn);
        // @todo 1.14
//        this.setSize(1.0F, 1.0F);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(SPAWN_YAW, 0.0f);
        this.dataManager.register(SPAWN_PITCH, 0.0f);
    }

    @Override
    public boolean isImmuneToForcefield(IForceFieldTile tile) {
        // @todo only make immune if this laser belongs to the controller of the forcefield
        return true;
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

    public void setSpawnYawPitch(float spawnYaw, float spawnPitch) {
        this.dataManager.set(SPAWN_YAW, spawnYaw);
        this.dataManager.set(SPAWN_PITCH, spawnPitch);
    }

    public float getSpawnYaw() {
        return this.dataManager.get(SPAWN_YAW);
    }

    public float getSpawnPitch() {
        return this.dataManager.get(SPAWN_PITCH);
    }

    public static LaserEntity create(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
        LaserEntity entity = new LaserEntity(Registration.ENTITY_LASER.get(), worldIn);
        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
        entity.setPosition(x, y, z);
        double d0 = MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        entity.accelerationX = accelX / d0 * 0.1D;
        entity.accelerationY = accelY / d0 * 0.1D;
        entity.accelerationZ = accelZ / d0 * 0.1D;
        return entity;
    }

    public static LaserEntity create(World worldIn, LivingEntity shooter, double accelX, double accelY, double accelZ) {
        LaserEntity entity = new LaserEntity(Registration.ENTITY_LASER.get(), worldIn);
        entity.shootingEntity = shooter;
        entity.setLocationAndAngles(shooter.getPosX(), shooter.getPosY(), shooter.getPosZ(), shooter.rotationYaw, shooter.rotationPitch);
        entity.setPosition(entity.getPosX(), entity.getPosY(), entity.getPosZ());
        entity.setMotion(0, 0, 0);
        accelX = accelX + entity.rand.nextGaussian() * 0.4D;
        accelY = accelY + entity.rand.nextGaussian() * 0.4D;
        accelZ = accelZ + entity.rand.nextGaussian() * 0.4D;
        double d0 = MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
        entity.accelerationX = accelX / d0 * 0.1D;
        entity.accelerationY = accelY / d0 * 0.1D;
        entity.accelerationZ = accelZ / d0 * 0.1D;
        entity.setSpawnYawPitch(shooter.rotationYaw, shooter.rotationPitch);
        return entity;
    }

    @Override
    public void baseTick() {
        super.baseTick();

        soundTicker--;
        if (soundTicker <= 0) {
            world.playSound(null, getPosX(), getPosY(), getPosZ(), ModSounds.laser, SoundCategory.HOSTILE, 5.0f, 1.0f);
            soundTicker = 40;
        }
    }

    @Override
    public void tick() {
        if (this.world.isRemote || (this.shootingEntity == null || this.shootingEntity.isAlive()) && this.world.isBlockLoaded(new BlockPos(this))) {
            super.tick();


            ++this.ticksInAir;
//            RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);
            RayTraceResult raytraceresult = ProjectileHelper.rayTrace(this, true, this.ticksInAir >= 25, this.shootingEntity, RayTraceContext.BlockMode.COLLIDER);

            if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                this.onImpact(raytraceresult);
            }

            double motionX = this.getMotion().x;
            double motionY = this.getMotion().y;
            double motionZ = this.getMotion().z;

            // @todo 1.15 is this right? setRawPosition
//            this.posX += motionX;
//            this.posY += motionY;
//            this.posZ += motionZ;
            this.setRawPosition(getPosX() + motionX, getPosY() + motionY, getPosZ() + motionZ);

            ProjectileHelper.rotateTowardsMovement(this, 0.2F);
            float f = this.getMotionFactor();

            if (this.isInWater()) {
                for (int i = 0; i < 4; ++i) {
                    float f1 = 0.25F;
                    this.world.addParticle(ParticleTypes.DRIPPING_WATER, this.getPosX() - motionX * 0.25D, this.getPosY() - motionY * 0.25D, this.getPosZ() - motionZ * 0.25D, motionX, motionY, motionZ);
                }

                f = 0.8F;
            }

            motionX += this.accelerationX;
            motionY += this.accelerationY;
            motionZ += this.accelerationZ;
            motionX *= f;
            motionY *= f;
            motionZ *= f;
            setMotion(motionX, motionY, motionZ);
//            this.world.spawnParticle(this.getParticleType(), this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), 0.0D, 0.0D, 0.0D);
            this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
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
        if (!this.world.isRemote && result instanceof EntityRayTraceResult) {
            if (((EntityRayTraceResult) result).getEntity() != null) {
                ((EntityRayTraceResult) result).getEntity().attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.shootingEntity), 10.0F);

//                this.applyEnchantments(this.shootingEntity, result.entityHit);
            }
            this.remove();
        }
    }

    public static void registerFixesFireball(DataFixer fixer, String name) {
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        compound.put("direction", this.newDoubleNBTList(new double[]{this.getMotion().x, this.getMotion().y, this.getMotion().z}));
        compound.put("power", this.newDoubleNBTList(new double[]{this.accelerationX, this.accelerationY, this.accelerationZ}));
        compound.putInt("life", this.ticksAlive);
        compound.putFloat("spawnYaw", getSpawnYaw());
        compound.putFloat("spawnPitch", getSpawnPitch());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
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
            setMotion(lst.getDouble(0), lst.getDouble(1), lst.getDouble(2));
        } else {
            this.remove();
        }

        float yaw = compound.getFloat("spawnYaw");
        float pitch = compound.getFloat("spawnPitch");
        setSpawnYawPitch(yaw, pitch);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public float getCollisionBorderSize() {
        return 1.0F;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            this.markVelocityChanged();

            if (source.getTrueSource() != null) {
                Vec3d vec3d = source.getTrueSource().getLookVec();

                if (vec3d != null) {
                    setMotion(vec3d);
                    this.accelerationX = vec3d.x * 0.1D;
                    this.accelerationY = vec3d.y * 0.1D;
                    this.accelerationZ = vec3d.z * 0.1D;
                }

                if (source.getTrueSource() instanceof LivingEntity) {
                    this.shootingEntity = (LivingEntity) source.getTrueSource();
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