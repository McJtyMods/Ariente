package mcjty.ariente.entities.drone;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
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
import java.util.Random;

public class DroneEntity extends FlyingEntity implements IMob, IForcefieldImmunity, IDrone {

    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(DroneEntity.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/drone");

    // If this drone is controlled by a city then this will be set
    private ChunkPos cityCenter;

    public DroneEntity(EntityType<? extends FlyingEntity> type, World worldIn) {
        super(type, worldIn);
        // @todo 1.14
//        this.isImmuneToFire = false;
        this.experienceValue = 5;
        this.moveController = new DroneMoveHelper(this);
    }

    public static DroneEntity create(World world, ChunkPos cityCenter) {
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
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 10, true, false, (p_213812_1_) -> {
            return Math.abs(p_213812_1_.getPosY() - this.getPosY()) <= 4.0D;
        }));
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING).booleanValue();
    }

    public void setAttacking(boolean attacking) {
        this.dataManager.set(ATTACKING, Boolean.valueOf(attacking));
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

        if (!this.getEntityWorld().isRemote && this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL) {
            this.remove();
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
// @todo 1.14
        //        if (this.isEntityInvulnerable(source)) {
//            return false;
//        } else {
            return super.attackEntityFrom(source, amount);
//        }
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(ATTACKING, Boolean.valueOf(false));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);  // Configurable
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
        return ModSounds.droneDeath;
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

    @Override
    public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setAttackTarget(entitylivingbaseIn);
        if (entitylivingbaseIn instanceof PlayerEntity && cityCenter != null) {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
            ICityAI cityAI = aiSystem.getCityAI(cityCenter);
            cityAI.playerSpotted((PlayerEntity) entitylivingbaseIn);
            aiSystem.saveSystem();
        }
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
        public boolean shouldExecute() {
            return this.drone.getAttackTarget() != null;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        @Override
        public void startExecuting() {
            this.attackTimer = 0;
        }

        /**
         * Resets the task
         */
        @Override
        public void resetTask() {
            this.drone.setAttacking(false);
        }

        /**
         * Updates the task
         */
        @Override
        public void tick() {
            LivingEntity target = this.drone.getAttackTarget();
            double d0 = 64.0D;

            if (target.getDistanceSq(this.drone) < 4096.0D && this.drone.canEntityBeSeen(target)) {
                World world = this.drone.getEntityWorld();
                ++this.attackTimer;

                if (this.attackTimer == 10) {
//                    world.playEvent(null, 1015, new BlockPos(this.parentEntity), 0);
                }

                if (this.attackTimer == 20) {
                    double d1 = 4.0D;
                    Vec3d vec3d = this.drone.getLook(1.0F);

//                    world.playSound(null, target.getPosX() - vec3d.x * 8.0d, target.getPosY() - vec3d.y * 8.0d, target.getPosZ() - vec3d.z * 8.0d, ModSounds.droneShoot, SoundCategory.HOSTILE, 5.0f, 1.0f);

                    double d2 = target.getPosX() - (this.drone.getPosX() + vec3d.x * 4.0D);
                    double d3 = target.getBoundingBox().minY + ((target.getHeight()+2) / 2.0F) - (0.5D + this.drone.getPosY() + (this.drone.getHeight() / 2.0F));
                    double d4 = target.getPosZ() - (this.drone.getPosZ() + vec3d.z * 4.0D);
//                    world.playEvent(null, 1016, new BlockPos(this.parentEntity), 0);
//                    for (int i = 0; i < world.playerEntities.size(); ++i) {
//                        world.playSound(world.playerEntities.get(i), d2, d3, d4, ModSounds.droneShoot, SoundCategory.HOSTILE, 1.0f, 1.0f);
//                        world.playSound(world.playerEntities.get(i), target.getPosX(), target.getPosY(), target.getPosZ(), ModSounds.droneShoot, SoundCategory.HOSTILE, 1.0f, 1.0f);
//                    }

//                    world.playEvent((PlayerEntity)null, 1016, new BlockPos(this.drone), 0);

                    LaserEntity laser = LaserEntity.create(world, this.drone, d2, d3, d4);
                    double laserX = this.drone.getPosX() + vec3d.x * 2.0D;
                    double laserY = this.drone.getPosY() + (this.drone.getHeight() / 2.0F) + 0.5D;
                    double laserZ = this.drone.getPosZ() + vec3d.z * 2.0D;
                    laser.setRawPosition(laserX, laserY, laserZ); // @todo 1.15 is this right?

                    double dx = target.getPosX() - laser.getPosX();
                    double dy = target.getPosY() + target.getEyeHeight() - laser.getPosY() + 0;
                    double dz = target.getPosZ() - laser.getPosZ();
                    double dpitch = MathHelper.sqrt(dx * dx + dz * dz);
//                    float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
                    float f1 = (float)(-(MathHelper.atan2(dy, dpitch) * (180D / Math.PI)));
                    laser.setSpawnYawPitch(laser.getSpawnYaw(), f1);

                    world.addEntity(laser);
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
                    double d1 = entitylivingbase.getPosX() - this.parentEntity.getPosX();
                    double d2 = entitylivingbase.getPosZ() - this.parentEntity.getPosZ();
                    this.parentEntity.rotationYaw = -((float) MathHelper.atan2(d1, d2)) * (180F / (float) Math.PI);
                    this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
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
        public boolean shouldExecute() {
            MovementController controller = this.parentEntity.getMoveHelper();

            if (!controller.isUpdating()) {
                return true;
            } else {
                double d0 = controller.getX() - this.parentEntity.getPosX();
                double d1 = controller.getY() - this.parentEntity.getPosY();
                double d2 = controller.getZ() - this.parentEntity.getPosZ();
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
                Random random = this.parentEntity.getRNG();
                double d0 = this.parentEntity.getPosX() + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
                double d1 = this.parentEntity.getPosY() + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
                double d2 = this.parentEntity.getPosZ() + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
                this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0D);
            } else {
                // City controls movement
                ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(parentEntity.world);
                ICityAI cityAI = aiSystem.getCityAI(parentEntity.cityCenter);
                BlockPos pos = cityAI.requestNewDronePosition(parentEntity.world, parentEntity.getAttackTarget());
                if (pos != null) {
                    this.parentEntity.getMoveHelper().setMoveTo(pos.getX(), pos.getY(), pos.getZ(), 2.0D);
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
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - this.parentEntity.getPosX();
                double d1 = this.posY - this.parentEntity.getPosY();
                double d2 = this.posZ - this.parentEntity.getPosZ();
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
                    d3 = Math.sqrt(d3);

                    if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
                        double motionX = this.parentEntity.getMotion().x;
                        double motionY = this.parentEntity.getMotion().y;
                        double motionZ = this.parentEntity.getMotion().z;
                        motionX += d0 / d3 * 0.1D;
                        motionY += d1 / d3 * 0.1D;
                        motionZ += d2 / d3 * 0.1D;
                        parentEntity.setMotion(motionX, motionY, motionZ);
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
            double d0 = (x - this.parentEntity.getPosX()) / p_179926_7_;
            double d1 = (y - this.parentEntity.getPosY()) / p_179926_7_;
            double d2 = (z - this.parentEntity.getPosZ()) / p_179926_7_;
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