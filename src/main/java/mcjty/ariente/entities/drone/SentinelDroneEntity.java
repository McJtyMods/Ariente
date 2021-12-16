package mcjty.ariente.entities.drone;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;

import net.minecraft.entity.ai.controller.MovementController.Action;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class SentinelDroneEntity extends FlyingEntity implements IMob, IForcefieldImmunity, ISentinel {

    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/sentinel_drone");

    private int sentinelId;
    private ChunkPos cityCenter;

    public SentinelDroneEntity(EntityType<? extends FlyingEntity> type, Level worldIn) {
        super(type, worldIn);

// @todo 1.14
//        this.isImmuneToFire = false;

        this.xpReward = 5;
        this.moveControl = new SentinelDroneMoveHelper(this);
    }

    public static SentinelDroneEntity create(Level world, int sentinelId, ChunkPos cityCenter) {
        SentinelDroneEntity entity = new SentinelDroneEntity(Registration.ENTITY_SENTINEL_DRONE.get(), world);
        entity.sentinelId = sentinelId;
        entity.cityCenter = cityCenter;
        return entity;
    }

    @Override
    public boolean isImmuneToForcefield(IForceFieldTile tile) {
        return true;
    }

    // Override this to make it less likely to despawn
    @Override
    public void checkDespawn() {
        Entity entity = this.level.getNearestPlayer(this, -1.0D);

        if (entity != null) {
            double d0 = entity.getX() - this.getX();
            double d1 = entity.getY() - this.getY();
            double d2 = entity.getZ() - this.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            double closest = entity.distanceToSqr(this);
            if (this.removeWhenFarAway(closest) && d3 > 16384.0D) {
                this.remove();
            }

            if (this.noActionTime > 900 && this.random.nextInt(800) == 0 && d3 > 2048.0D && this.removeWhenFarAway(closest)) {
                this.remove();
            } else if (d3 < 2048.0D) {
                this.noActionTime = 0;
            }
        }
    }


    @Override
    public void setTarget(@Nullable LivingEntity entitylivingbaseIn) {
//        super.setAttackTarget(entitylivingbaseIn);
        // This is called by EntityAIFindEntityNearestPlayer when it spots a player.
        // In this case we don't attack but notify the city AI
        if (entitylivingbaseIn instanceof Player && cityCenter != null) {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(level);
            ICityAI cityAI = aiSystem.getCityAI(cityCenter);
            cityAI.playerSpotted((Player) entitylivingbaseIn);
            aiSystem.saveSystem();
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new AICircleCity(this));
        this.goalSelector.addGoal(7, new AILookAround(this));
        this.targetSelector.addGoal(1, new EntityAIScanForPlayer(this));
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
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            return super.hurt(source, amount);
        }
    }

    public static AttributeSupplier.MutableAttribute registerAttributes() {
        AttributeSupplier.MutableAttribute attributes = LivingEntity.createLivingAttributes();
        attributes
            .add(Attributes.MAX_HEALTH, 10.0D)
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
        return ModSounds.sentinelDeath;
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

    static class AILookAround extends Goal {
        private final SentinelDroneEntity parentEntity;

        public AILookAround(SentinelDroneEntity drone) {
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

    static class AICircleCity extends Goal {
        private final SentinelDroneEntity parentEntity;

        public AICircleCity(SentinelDroneEntity drone) {
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
                return;
            }
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(parentEntity.level);
            ICityAI cityAI = aiSystem.getCityAI(parentEntity.cityCenter);
            BlockPos pos = cityAI.requestNewSentinelPosition(parentEntity.level, parentEntity.sentinelId);
            if (pos != null) {
                this.parentEntity.getMoveControl().setWantedPosition(pos.getX(), pos.getY(), pos.getZ(), 2.0D);
            }
        }
    }

    static class SentinelDroneMoveHelper extends MovementController {
        private final SentinelDroneEntity parentEntity;
        private int courseChangeCooldown;

        public SentinelDroneMoveHelper(SentinelDroneEntity drone) {
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
                        Vec3 motion = this.parentEntity.getDeltaMovement();
                        this.parentEntity.setDeltaMovement(motion.add(d0 / d3 * 0.1D, d1 / d3 * 0.1D, d2 / d3 * 0.1D));
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