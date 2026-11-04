package mcjty.ariente.entities;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.fluxship.FluxShipLaser;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.FriendlyByteBuf;
import mcjty.ariente.api.IForceFieldTile;
import mcjty.ariente.api.IForcefieldImmunity;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.items.modules.WeaponModuleSupport;
import mcjty.ariente.items.LanceAim;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.joml.Vector3f;

public class LaserEntity extends Projectile implements IForcefieldImmunity, IEntityAdditionalSpawnData {

    public static final ResourceKey<DamageType> SHIP_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(Ariente.MODID, "ship_laser"));

    private static final EntityDataAccessor<Float> SPAWN_YAW = SynchedEntityData.<Float>defineId(LaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPAWN_PITCH = SynchedEntityData.<Float>defineId(LaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ACCELERATION_X = SynchedEntityData.defineId(LaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ACCELERATION_Y = SynchedEntityData.defineId(LaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ACCELERATION_Z = SynchedEntityData.defineId(LaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> SHIP_VELOCITY = SynchedEntityData.defineId(LaserEntity.class, EntityDataSerializers.VECTOR3);

    private LivingEntity shootingEntity;
    private int ticksAlive;
    private int ticksInAir;
    private float damage = 10.0F;
    private boolean lanceShot;
    private boolean shipShot;
    private boolean inhibit;
    private int fireAspect;
    private int looting;
    private Vec3 launchPosition;

    private int soundTicker = 0;

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public LaserEntity(EntityType<? extends LaserEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (launchPosition == null) {
            launchPosition = position();
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        Vec3 start = launchPosition == null ? position() : launchPosition;
        buffer.writeDouble(start.x);
        buffer.writeDouble(start.y);
        buffer.writeDouble(start.z);
        buffer.writeBoolean(shipShot);
        Vec3 velocity = getDeltaMovement();
        buffer.writeDouble(velocity.x);
        buffer.writeDouble(velocity.y);
        buffer.writeDouble(velocity.z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        launchPosition = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        shipShot = buffer.readBoolean();
        // Vanilla spawn packets clamp velocity to 3.9 blocks/tick; the cannon must outrun the ship.
        Vec3 velocity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        entityData.set(SHIP_VELOCITY, velocity.toVector3f());
        setDeltaMovement(velocity);
    }

    public double getBeamLength(Vec3 head) {
        // A newly fired trail must not extend behind the muzzle or through the camera.
        return launchPosition == null ? 2 : Math.min(2, head.distanceTo(launchPosition));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SPAWN_YAW, 0.0f);
        this.entityData.define(SPAWN_PITCH, 0.0f);
        entityData.define(ACCELERATION_X, 0.0F);
        entityData.define(ACCELERATION_Y, 0.0F);
        entityData.define(ACCELERATION_Z, 0.0F);
        entityData.define(SHIP_VELOCITY, new Vector3f());
    }

    @Override
    public void setDeltaMovement(Vec3 velocity) {
        super.setDeltaMovement(velocity);
        if (shipShot && !level().isClientSide) {
            entityData.set(SHIP_VELOCITY, velocity.toVector3f());
        }
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        // Pairing and later velocity packets also clamp each component. Use the full synced vector.
        if (shipShot) {
            Vector3f velocity = entityData.get(SHIP_VELOCITY);
            super.lerpMotion(velocity.x, velocity.y, velocity.z);
        } else {
            super.lerpMotion(x, y, z);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(SHIP_VELOCITY) && shipShot && level().isClientSide) {
            Vector3f velocity = entityData.get(SHIP_VELOCITY);
            setDeltaMovement(velocity.x, velocity.y, velocity.z);
        }
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

    public Vec3 getAcceleration() {
        return new Vec3(entityData.get(ACCELERATION_X), entityData.get(ACCELERATION_Y), entityData.get(ACCELERATION_Z));
    }

    private void setAcceleration(Vec3 acceleration) {
        entityData.set(ACCELERATION_X, (float) acceleration.x);
        entityData.set(ACCELERATION_Y, (float) acceleration.y);
        entityData.set(ACCELERATION_Z, (float) acceleration.z);
    }

    private void setLaunchRotation() {
        Vec3 direction = getBeamDirection();
        setSpawnYawPitch((float) (Mth.atan2(-direction.x, direction.z) * Mth.RAD_TO_DEG),
                (float) (-Mth.atan2(direction.y, direction.horizontalDistance()) * Mth.RAD_TO_DEG));
        setRot(getSpawnYaw(), getSpawnPitch());
    }

    public Vec3 getBeamDirection() {
        Vec3 direction = getDeltaMovement();
        if (direction.lengthSqr() < 1.0E-7D) {
            direction = getAcceleration();
        }
        return direction.lengthSqr() < 1.0E-7D ? getLookAngle() : direction.normalize();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        // The visible trail extends behind the projectile's collision box.
        return getBoundingBox().inflate(2.1D);
    }

    public static LaserEntity create(Level worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
        LaserEntity entity = new LaserEntity(Registration.ENTITY_LASER.get(), worldIn);
        entity.moveTo(x, y, z, entity.getYRot(), entity.getXRot());
        entity.setPos(x, y, z);
        entity.setAcceleration(new Vec3(accelX, accelY, accelZ).normalize().scale(0.1D));
        entity.setLaunchRotation();
        return entity;
    }

    public static LaserEntity create(Level worldIn, LivingEntity shooter, double accelX, double accelY, double accelZ) {
        LaserEntity entity = new LaserEntity(Registration.ENTITY_LASER.get(), worldIn);
        entity.shootingEntity = shooter;
        entity.setOwner(shooter);
        entity.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
        entity.setPos(entity.getX(), entity.getY(), entity.getZ());
        entity.setDeltaMovement(0, 0, 0);
        accelX = accelX + entity.random.nextGaussian() * 0.4D;
        accelY = accelY + entity.random.nextGaussian() * 0.4D;
        accelZ = accelZ + entity.random.nextGaussian() * 0.4D;
        entity.setAcceleration(new Vec3(accelX, accelY, accelZ).normalize().scale(0.1D));
        entity.setLaunchRotation();
        return entity;
    }

    public static LaserEntity createLanceShot(Level level, LivingEntity shooter, Vec3 direction, float damage) {
        LaserEntity entity = new LaserEntity(Registration.ENTITY_LASER.get(), level);
        LanceAim shot = LanceAim.calculate(shooter, direction);
        Vec3 aim = shot.direction();
        entity.shootingEntity = shooter;
        entity.setOwner(shooter);
        entity.lanceShot = true;
        entity.damage = damage;
        ItemStack weapon = shooter.getMainHandItem();
        entity.inhibit = ModuleSupport.hasWorkingUpgrade(weapon, ArmorUpgradeType.INHIBIT);
        entity.fireAspect = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, weapon);
        entity.looting = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, weapon);
        entity.setPos(shot.muzzle());
        entity.setDeltaMovement(aim.scale(1.8D));
        entity.setAcceleration(aim.scale(0.12D));
        entity.setLaunchRotation();
        return entity;
    }

    public boolean isLanceShot() {
        return lanceShot;
    }

    public static LaserEntity createShipShot(Level level, LivingEntity pilot, Vec3 muzzle, Vec3 direction, float damage) {
        LaserEntity entity = new LaserEntity(Registration.ENTITY_LASER.get(), level);
        entity.shootingEntity = pilot;
        entity.setOwner(pilot);
        entity.shipShot = true;
        entity.damage = damage;
        entity.setPos(muzzle);
        entity.setDeltaMovement(direction.normalize().scale(FluxShipLaser.SHOT_SPEED));
        entity.setAcceleration(Vec3.ZERO);
        entity.setLaunchRotation();
        return entity;
    }

    public boolean isShipShot() {
        return shipShot;
    }

    public int getLootingLevel() {
        return looting;
    }

    @Override
    public void baseTick() {
        super.baseTick();

        soundTicker--;
        if (soundTicker <= 0) {
            level().playSound(null, getX(), getY(), getZ(), ModSounds.laser.get(), SoundSource.HOSTILE, 5.0f, 1.0f);
            soundTicker = 40;
        }
    }

    @Override
    public void tick() {
        if (!level().isClientSide && (lanceShot || shipShot)) {
            if (++ticksAlive > (shipShot ? FluxShipLaser.SHOT_LIFETIME : 100)) {
                discard();
                return;
            }
            if (getOwner() instanceof LivingEntity owner) {
                shootingEntity = owner;
            }
        }
        if (this.level().isClientSide || (this.shootingEntity == null || this.shootingEntity.isAlive()) && this.level().hasChunkAt(this.blockPosition())) {
            super.tick();

            ++this.ticksInAir;
            HitResult raytraceresult = ProjectileUtil.getHitResultOnMoveVector(this,
                    entity -> shipShot ? canHitEntity(entity) && !(entity instanceof LaserEntity) && !(entity instanceof IHoloGuiEntity)
                            : lanceShot ? canHitEntity(entity) : entity != this.shootingEntity || this.ticksInAir >= 25);

            if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                this.onImpact(raytraceresult);
            }

            double motionX = this.getDeltaMovement().x;
            double motionY = this.getDeltaMovement().y;
            double motionZ = this.getDeltaMovement().z;

            // @todo 1.15 is this right? setRawPosition
            this.setPos(getX() + motionX, getY() + motionY, getZ() + motionZ);

            ProjectileUtil.rotateTowardsMovement(this, 0.2F);
            float f = this.getMotionFactor();

            if (this.isInWater()) {
                for (int i = 0; i < 4; ++i) {
                    float f1 = 0.25F;
                    this.level().addParticle(ParticleTypes.DRIPPING_WATER, this.getX() - motionX * 0.25D, this.getY() - motionY * 0.25D, this.getZ() - motionZ * 0.25D, motionX, motionY, motionZ);
                }

                f = 0.8F;
            }

            Vec3 acceleration = getAcceleration();
            motionX += acceleration.x;
            motionY += acceleration.y;
            motionZ += acceleration.z;
            motionX *= f;
            motionY *= f;
            motionZ *= f;
            setDeltaMovement(motionX, motionY, motionZ);
            this.setPos(this.getX(), this.getY(), this.getZ());
        } else {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    protected float getMotionFactor() {
        return shipShot ? 1 : 0.95F;
    }

    protected void onImpact(HitResult result) {
        if (!level().isClientSide && result instanceof EntityHitResult hit) {
            Entity target = hit.getEntity();
            int previousFireTicks = target.getRemainingFireTicks();
            if (lanceShot && fireAspect > 0) {
                // Ignite before damage so a killing shot can also produce cooked drops.
                target.setSecondsOnFire(fireAspect * 4);
            }
            DamageSource source = shipShot ? new DamageSource(level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(SHIP_DAMAGE), this, shootingEntity) : damageSources().indirectMagic(this, shootingEntity);
            boolean damaged = target.hurt(source, damage);
            if (!damaged && lanceShot && fireAspect > 0) {
                target.setRemainingFireTicks(previousFireTicks);
            }
            if (lanceShot && inhibit && target instanceof LivingEntity living) {
                WeaponModuleSupport.inhibit(living);
            }
            this.remove(RemovalReason.DISCARDED);
        } else if (!level().isClientSide && (lanceShot || shipShot) && result.getType() == HitResult.Type.BLOCK) {
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("direction", this.newDoubleList(new double[]{this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z}));
        Vec3 acceleration = getAcceleration();
        compound.put("power", this.newDoubleList(new double[]{acceleration.x, acceleration.y, acceleration.z}));
        compound.putInt("life", this.ticksAlive);
        compound.putFloat("spawnYaw", getSpawnYaw());
        compound.putFloat("spawnPitch", getSpawnPitch());
        compound.putFloat("damage", damage);
        compound.putBoolean("lanceShot", lanceShot);
        compound.putBoolean("shipShot", shipShot);
        compound.putBoolean("lanceInhibit", inhibit);
        compound.putInt("lanceFire", fireAspect);
        compound.putInt("lanceLooting", looting);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        damage = compound.contains("damage") ? compound.getFloat("damage") : 10.0F;
        lanceShot = compound.getBoolean("lanceShot");
        shipShot = compound.getBoolean("shipShot");
        inhibit = compound.getBoolean("lanceInhibit");
        fireAspect = compound.getInt("lanceFire");
        looting = compound.getInt("lanceLooting");
        if (compound.contains("power", 9)) {
            ListTag nbttaglist = compound.getList("power", 6);

            if (nbttaglist.size() == 3) {
                setAcceleration(new Vec3(nbttaglist.getDouble(0), nbttaglist.getDouble(1), nbttaglist.getDouble(2)));
            }
        }

        this.ticksAlive = compound.getInt("life");

        if (compound.contains("direction", 9) && compound.getList("direction", 6).size() == 3) {
            ListTag lst = compound.getList("direction", 6);
            setDeltaMovement(lst.getDouble(0), lst.getDouble(1), lst.getDouble(2));
        } else {
            this.remove(RemovalReason.DISCARDED);
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
                    setAcceleration(vec3d.normalize().scale(0.1D));
                }

                if (source.getEntity() instanceof LivingEntity) {
                    this.shootingEntity = (LivingEntity) source.getEntity();
                    setOwner(shootingEntity);
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public float getBrightness() {
        return 1.0F;
    }

}
