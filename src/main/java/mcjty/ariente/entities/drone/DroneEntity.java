package mcjty.ariente.entities.drone;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.entity.monster.IMob;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.util.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.entity.ai.controller.MovementController.Action;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class DroneEntity extends FlyingEntity implements IMob, IForcefieldImmunity, IDrone {

    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/drone");

    // If this drone is controlled by a city then this will be set
    private ChunkPos cityCenter;

    public DroneEntity(EntityType<? extends FlyingEntity> type, Level worldIn) {
        super(type, worldIn);
        // @todo 1.14
//        this.isImmuneToFire = false;
        this.xpReward = 5;
        this.moveControl = new DroneMoveHelper(this);
    }

    public static DroneEntity create(Level world, ChunkPos cityCenter) {
        DroneEntity entity = new DroneEntity(Registration.ENTITY_DRONE.get(), world);
        entity.cityCenter = cityCenter;
        return entity;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new AIDroneFly(this));
        this.goalSelector.addGoal(7, new AILookAround(this));
        this.goalSelector.addGoal(7, new AILaserAttack(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (p_213812_1_) -> {
            return Math.abs(p_213812_1_.getY() - this.getY()) <= 4.0D;
        }));
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
    public boolean isAttacking() {
        return this.entityData.get(ATTACKING).booleanValue();
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, Boolean.valueOf(attacking));
    }

    @Override
    public boolean isImmuneToForcefield(IForceFieldTile tile) {
        return true;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        super.tick();

        if (!this.getCommandSenderWorld().isClientSide && this.getCommandSenderWorld().getDifficulty() == Difficulty.PEACEFUL) {
            this.remove();
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
// @todo 1.14
        //        if (this.isEntityInvulnerable(source)) {
//            return false;
//        } else {
            return super.hurt(source, amount);
//        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, Boolean.valueOf(false));
    }

    public static AttributeSupplier.MutableAttribute registerAttributes() {
        AttributeSupplier.MutableAttribute attributes = LivingEntity.createLivingAttributes();
        attributes
            .add(Attributes.MAX_HEALTH, 30.0D)
            .add(Attributes.FOLLOW_RANGE, 50.0D); // Configurable

        return attributes;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.droneAmbient;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.droneHurt;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.droneDeath;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return LOOT;
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public boolean checkSpawnRules(Level worldIn, SpawnReason spawnReasonIn) {
        boolean b = (this.random.nextInt(100) == 0) && super.checkSpawnRules(worldIn, spawnReasonIn) && this.getCommandSenderWorld().getDifficulty() != Difficulty.PEACEFUL;
        return b;
    }

    @Override
    public boolean checkSpawnObstruction(IWorldReader worldIn) {
        return true;
    }

    /**
     * Will return how many at most can spawn in a chunk at once.
     */
    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    public void setTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setTarget(entitylivingbaseIn);
        if (entitylivingbaseIn instanceof Player && cityCenter != null) {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(level);
            ICityAI cityAI = aiSystem.getCityAI(cityCenter);
            cityAI.playerSpotted((Player) entitylivingbaseIn);
            aiSystem.saveSystem();
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (cityCenter != null) {
            compound.putInt("cityX", cityCenter.x);
            compound.putInt("cityZ", cityCenter.z);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("cityX")) {
            cityCenter = new ChunkPos(compound.getInt("cityX"), compound.getInt("cityZ"));
        }
    }

    // @todo 1.14
//    @Override
//    public float getEyeHeight() {
//        return 0.8F;
//    }

    static class AILaserAttack extends Goal {
        private final DroneEntity drone;
        public int attackTimer;

        public AILaserAttack(DroneEntity drone) {
            this.drone = drone;
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        @Override
        public boolean canUse() {
            return this.drone.getTarget() != null;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        @Override
        public void start() {
            this.attackTimer = 0;
        }

        /**
         * Resets the task
         */
        @Override
        public void stop() {
            this.drone.setAttacking(false);
        }

        /**
         * Updates the task
         */
        @Override
        public void tick() {
            LivingEntity target = this.drone.getTarget();
            double d0 = 64.0D;

            if (target.distanceToSqr(this.drone) < 4096.0D && this.drone.canSee(target)) {
                Level world = this.drone.getCommandSenderWorld();
                ++this.attackTimer;

                if (this.attackTimer == 10) {
//                    world.playEvent(null, 1015, new BlockPos(this.parentEntity), 0);
                }

                if (this.attackTimer == 20) {
                    double d1 = 4.0D;
                    Vec3 vec3d = this.drone.getViewVector(1.0F);

//                    world.playSound(null, target.getPosX() - vec3d.x * 8.0d, target.getPosY() - vec3d.y * 8.0d, target.getPosZ() - vec3d.z * 8.0d, ModSounds.droneShoot, SoundCategory.HOSTILE, 5.0f, 1.0f);

                    double d2 = target.getX() - (this.drone.getX() + vec3d.x * 4.0D);
                    double d3 = target.getBoundingBox().minY + ((target.getBbHeight()+2) / 2.0F) - (0.5D + this.drone.getY() + (this.drone.getBbHeight() / 2.0F));
                    double d4 = target.getZ() - (this.drone.getZ() + vec3d.z * 4.0D);
//                    world.playEvent(null, 1016, new BlockPos(this.parentEntity), 0);
//                    for (int i = 0; i < world.playerEntities.size(); ++i) {
//                        world.playSound(world.playerEntities.get(i), d2, d3, d4, ModSounds.droneShoot, SoundCategory.HOSTILE, 1.0f, 1.0f);
//                        world.playSound(world.playerEntities.get(i), target.getPosX(), target.getPosY(), target.getPosZ(), ModSounds.droneShoot, SoundCategory.HOSTILE, 1.0f, 1.0f);
//                    }

//                    world.playEvent((PlayerEntity)null, 1016, new BlockPos(this.drone), 0);

                    LaserEntity laser = LaserEntity.create(world, this.drone, d2, d3, d4);
                    double laserX = this.drone.getX() + vec3d.x * 2.0D;
                    double laserY = this.drone.getY() + (this.drone.getBbHeight() / 2.0F) + 0.5D;
                    double laserZ = this.drone.getZ() + vec3d.z * 2.0D;
                    laser.setPosRaw(laserX, laserY, laserZ); // @todo 1.15 is this right?

                    double dx = target.getX() - laser.getX();
                    double dy = target.getY() + target.getEyeHeight() - laser.getY() + 0;
                    double dz = target.getZ() - laser.getZ();
                    double dpitch = MathHelper.sqrt(dx * dx + dz * dz);
//                    float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
                    float f1 = (float)(-(MathHelper.atan2(dy, dpitch) * (180D / Math.PI)));
                    laser.setSpawnYawPitch(laser.getSpawnYaw(), f1);

                    world.addFreshEntity(laser);
                    this.attackTimer = -40;
                }
            } else if (this.attackTimer > 0) {
                --this.attackTimer;
            }

            this.drone.setAttacking(this.attackTimer > 10);
        }
    }

    static class AILookAround extends Goal {
        private final DroneEntity parentEntity;

        public AILookAround(DroneEntity drone) {
            this.parentEntity = drone;
            // @todo 1.14
//            this.setMutexFlags(2);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        @Override
        public boolean canUse() {
            return true;
        }

        /**
         * Updates the task
         */
        @Override
        public void tick() {
            if (this.parentEntity.getTarget() == null) {
                this.parentEntity.yRot = -((float) MathHelper.atan2(this.parentEntity.getDeltaMovement().x, this.parentEntity.getDeltaMovement().z)) * (180F / (float) Math.PI);
                this.parentEntity.yBodyRot = this.parentEntity.yRot;
            } else {
                LivingEntity entitylivingbase = this.parentEntity.getTarget();
                double d0 = 64.0D;

                if (entitylivingbase.distanceToSqr(this.parentEntity) < 4096.0D) {
                    double d1 = entitylivingbase.getX() - this.parentEntity.getX();
                    double d2 = entitylivingbase.getZ() - this.parentEntity.getZ();
                    this.parentEntity.yRot = -((float) MathHelper.atan2(d1, d2)) * (180F / (float) Math.PI);
                    this.parentEntity.yBodyRot = this.parentEntity.yRot;
                }
            }
        }
    }

    static class AIDroneFly extends Goal {
        private final DroneEntity parentEntity;

        public AIDroneFly(DroneEntity drone) {
            this.parentEntity = drone;
            // @todo 1.14
//            this.setMutexFlags(1);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        @Override
        public boolean canUse() {
            MovementController controller = this.parentEntity.getMoveControl();

            if (!controller.hasWanted()) {
                return true;
            } else {
                double d0 = controller.getWantedX() - this.parentEntity.getX();
                double d1 = controller.getWantedY() - this.parentEntity.getY();
                double d2 = controller.getWantedZ() - this.parentEntity.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        @Override
        public boolean canContinueToUse() {
            return false;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        @Override
        public void start() {
            if (parentEntity.cityCenter == null) {
                Random random = this.parentEntity.getRandom();
                double d0 = this.parentEntity.getX() + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
                double d1 = this.parentEntity.getY() + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
                double d2 = this.parentEntity.getZ() + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
                this.parentEntity.getMoveControl().setWantedPosition(d0, d1, d2, 1.0D);
            } else {
                // City controls movement
                ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(parentEntity.level);
                ICityAI cityAI = aiSystem.getCityAI(parentEntity.cityCenter);
                BlockPos pos = cityAI.requestNewDronePosition(parentEntity.level, parentEntity.getTarget());
                if (pos != null) {
                    this.parentEntity.getMoveControl().setWantedPosition(pos.getX(), pos.getY(), pos.getZ(), 2.0D);
                }
            }
        }
    }

    static class DroneMoveHelper extends MovementController {
        private final DroneEntity parentEntity;
        private int courseChangeCooldown;

        public DroneMoveHelper(DroneEntity drone) {
            super(drone);
            this.parentEntity = drone;
        }

        @Override
        public void tick() {
            if (this.operation == Action.MOVE_TO) {
                double d0 = this.wantedX - this.parentEntity.getX();
                double d1 = this.wantedY - this.parentEntity.getY();
                double d2 = this.wantedZ - this.parentEntity.getZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.parentEntity.getRandom().nextInt(5) + 2;
                    d3 = Math.sqrt(d3);

                    if (this.isNotColliding(this.wantedX, this.wantedY, this.wantedZ, d3)) {
                        double motionX = this.parentEntity.getDeltaMovement().x;
                        double motionY = this.parentEntity.getDeltaMovement().y;
                        double motionZ = this.parentEntity.getDeltaMovement().z;
                        motionX += d0 / d3 * 0.1D;
                        motionY += d1 / d3 * 0.1D;
                        motionZ += d2 / d3 * 0.1D;
                        parentEntity.setDeltaMovement(motionX, motionY, motionZ);
                    } else {
                        this.operation = Action.WAIT;
                    }
                }
            }
        }

        /**
         * Checks if entity bounding box is not colliding with terrain
         */
        private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
            double d0 = (x - this.parentEntity.getX()) / p_179926_7_;
            double d1 = (y - this.parentEntity.getY()) / p_179926_7_;
            double d2 = (z - this.parentEntity.getZ()) / p_179926_7_;
            AABB axisalignedbb = this.parentEntity.getBoundingBox();

            for (int i = 1; i < p_179926_7_; ++i) {
                axisalignedbb = axisalignedbb.move(d0, d1, d2);

                // @todo 1.14
//                if (!this.parentEntity.getEntityWorld().getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty()) {
//                    return false;
//                }
            }

            return true;
        }
    }
}