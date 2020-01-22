package mcjty.ariente.entities.drone;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SentinelDroneEntity extends FlyingEntity implements IMob, IForcefieldImmunity, ISentinel {

    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/sentinel_drone");

    private int sentinelId;
    private ChunkPos cityCenter;

    public SentinelDroneEntity(EntityType<? extends FlyingEntity> type, World worldIn) {
        super(type, worldIn);

// @todo 1.14
//        this.isImmuneToFire = false;

        this.experienceValue = 5;
        this.moveController = new SentinelDroneMoveHelper(this);
    }

    public static SentinelDroneEntity create(World world, int sentinelId, ChunkPos cityCenter) {
        SentinelDroneEntity entity = new SentinelDroneEntity(Registration.SENTINEL_DRONE.get(), world);
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
    protected void checkDespawn() {
        Entity entity = this.world.getClosestPlayer(this, -1.0D);

        if (entity != null) {
            double d0 = entity.posX - this.posX;
            double d1 = entity.posY - this.posY;
            double d2 = entity.posZ - this.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            double closest = entity.getDistanceSq(this);
            if (this.canDespawn(closest) && d3 > 16384.0D) {
                this.remove();
            }

            if (this.idleTime > 900 && this.rand.nextInt(800) == 0 && d3 > 2048.0D && this.canDespawn(closest)) {
                this.remove();
            } else if (d3 < 2048.0D) {
                this.idleTime = 0;
            }
        }
    }


    @Override
    public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn) {
//        super.setAttackTarget(entitylivingbaseIn);
        // This is called by EntityAIFindEntityNearestPlayer when it spots a player.
        // In this case we don't attack but notify the city AI
        if (entitylivingbaseIn instanceof PlayerEntity && cityCenter != null) {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
            ICityAI cityAI = aiSystem.getCityAI(cityCenter);
            cityAI.playerSpotted((PlayerEntity) entitylivingbaseIn);
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

        if (!this.getEntityWorld().isRemote && this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL) {
            this.remove();
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50); // Configurable
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
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
    protected ResourceLocation getLootTable() {
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
    public boolean canSpawn(IWorld worldIn, SpawnReason spawnReasonIn) {
        boolean b = (this.rand.nextInt(100) == 0) && super.canSpawn(worldIn, spawnReasonIn) && this.getEntityWorld().getDifficulty() != Difficulty.PEACEFUL;
        return b;
    }

    @Override
    public boolean isNotColliding(IWorldReader worldIn) {
        return true;
    }

    /**
     * Will return how many at most can spawn in a chunk at once.
     */
    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        if (cityCenter != null) {
            compound.putInt("cityX", cityCenter.x);
            compound.putInt("cityZ", cityCenter.z);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
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
        public boolean shouldExecute() {
            return true;
        }

        /**
         * Updates the task
         */
        @Override
        public void tick() {
            if (this.parentEntity.getAttackTarget() == null) {
                this.parentEntity.rotationYaw = -((float) MathHelper.atan2(this.parentEntity.getMotion().x, this.parentEntity.getMotion().z)) * (180F / (float) Math.PI);
                this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
            } else {
                LivingEntity entitylivingbase = this.parentEntity.getAttackTarget();
                double d0 = 64.0D;

                if (entitylivingbase.getDistanceSq(this.parentEntity) < 4096.0D) {
                    double d1 = entitylivingbase.posX - this.parentEntity.posX;
                    double d2 = entitylivingbase.posZ - this.parentEntity.posZ;
                    this.parentEntity.rotationYaw = -((float) MathHelper.atan2(d1, d2)) * (180F / (float) Math.PI);
                    this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
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
        public boolean shouldExecute() {
            MovementController controller = this.parentEntity.getMoveHelper();

            if (!controller.isUpdating()) {
                return true;
            } else {
                double d0 = controller.getX() - this.parentEntity.posX;
                double d1 = controller.getY() - this.parentEntity.posY;
                double d2 = controller.getZ() - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        @Override
        public void startExecuting() {
            if (parentEntity.cityCenter == null) {
                return;
            }
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(parentEntity.world);
            ICityAI cityAI = aiSystem.getCityAI(parentEntity.cityCenter);
            BlockPos pos = cityAI.requestNewSentinelPosition(parentEntity.world, parentEntity.sentinelId);
            if (pos != null) {
                this.parentEntity.getMoveHelper().setMoveTo(pos.getX(), pos.getY(), pos.getZ(), 2.0D);
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
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - this.parentEntity.posX;
                double d1 = this.posY - this.parentEntity.posY;
                double d2 = this.posZ - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
                    d3 = Math.sqrt(d3);

                    if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
                        Vec3d motion = this.parentEntity.getMotion();
                        this.parentEntity.setMotion(motion.add(d0 / d3 * 0.1D, d1 / d3 * 0.1D, d2 / d3 * 0.1D));
                    } else {
                        this.action = Action.WAIT;
                    }
                }
            }
        }

        /**
         * Checks if entity bounding box is not colliding with terrain
         */
        private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
            double d0 = (x - this.parentEntity.posX) / p_179926_7_;
            double d1 = (y - this.parentEntity.posY) / p_179926_7_;
            double d2 = (z - this.parentEntity.posZ) / p_179926_7_;
            AxisAlignedBB axisalignedbb = this.parentEntity.getBoundingBox();

            for (int i = 1; i < p_179926_7_; ++i) {
                axisalignedbb = axisalignedbb.offset(d0, d1, d2);

                // @todo 1.14
//                if (!this.parentEntity.getEntityWorld().getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty()) {
//                    return false;
//                }
            }

            return true;
        }
    }
}