package mcjty.ariente.entities.drone;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityMoveHelper;
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
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class DroneEntity extends EntityFlying implements IMob, IForcefieldImmunity, IDrone {

    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(DroneEntity.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/drone");

    // If this drone is controlled by a city then this will be set
    private ChunkPos cityCenter;

    public DroneEntity(World worldIn) {
        super(worldIn);
        this.setSize(2.0F, 2.0F);
        this.isImmuneToFire = false;
        this.experienceValue = 5;
        this.moveHelper = new DroneMoveHelper(this);
    }

    public DroneEntity(World world, ChunkPos cityCenter) {
        this(world);
        this.cityCenter = cityCenter;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(5, new AIDroneFly(this));
        this.tasks.addTask(7, new AILookAround(this));
        this.tasks.addTask(7, new AILaserAttack(this));
        this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
    }

    @SideOnly(Side.CLIENT)
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
    public void onUpdate() {
        super.onUpdate();

        if (!this.getEntityWorld().isRemote && this.getEntityWorld().getDifficulty() == EnumDifficulty.PEACEFUL) {
            this.setDead();
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, Boolean.valueOf(false));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);  // Configurable
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
    @Nullable
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

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    @Override
    public boolean getCanSpawnHere() {
        boolean b = (this.rand.nextInt(100) == 0) && super.getCanSpawnHere() && this.getEntityWorld().getDifficulty() != EnumDifficulty.PEACEFUL;
        return b;
    }

    @Override
    public boolean isNotColliding() {
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
    public void writeEntityToNBT(CompoundNBT compound) {
        super.writeEntityToNBT(compound);
        if (cityCenter != null) {
            compound.setInteger("cityX", cityCenter.x);
            compound.setInteger("cityZ", cityCenter.z);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(CompoundNBT compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("cityX")) {
            cityCenter = new ChunkPos(compound.getInteger("cityX"), compound.getInteger("cityZ"));
        }
    }

    @Override
    public float getEyeHeight() {
        return 0.8F;
    }

    static class AILaserAttack extends EntityAIBase {
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
        public void updateTask() {
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

//                    world.playSound(null, target.posX - vec3d.x * 8.0d, target.posY - vec3d.y * 8.0d, target.posZ - vec3d.z * 8.0d, ModSounds.droneShoot, SoundCategory.HOSTILE, 5.0f, 1.0f);

                    double d2 = target.posX - (this.drone.posX + vec3d.x * 4.0D);
                    double d3 = target.getEntityBoundingBox().minY + ((target.height+2) / 2.0F) - (0.5D + this.drone.posY + (this.drone.height / 2.0F));
                    double d4 = target.posZ - (this.drone.posZ + vec3d.z * 4.0D);
//                    world.playEvent(null, 1016, new BlockPos(this.parentEntity), 0);
//                    for (int i = 0; i < world.playerEntities.size(); ++i) {
//                        world.playSound(world.playerEntities.get(i), d2, d3, d4, ModSounds.droneShoot, SoundCategory.HOSTILE, 1.0f, 1.0f);
//                        world.playSound(world.playerEntities.get(i), target.posX, target.posY, target.posZ, ModSounds.droneShoot, SoundCategory.HOSTILE, 1.0f, 1.0f);
//                    }

//                    world.playEvent((PlayerEntity)null, 1016, new BlockPos(this.drone), 0);

                    LaserEntity laser = new LaserEntity(world, this.drone, d2, d3, d4);
                    laser.posX = this.drone.posX + vec3d.x * 2.0D;
                    laser.posY = this.drone.posY + (this.drone.height / 2.0F) + 0.5D;
                    laser.posZ = this.drone.posZ + vec3d.z * 2.0D;

                    double dx = target.posX - laser.posX;
                    double dy = target.posY + target.getEyeHeight() - laser.posY + 0;
                    double dz = target.posZ - laser.posZ;
                    double dpitch = MathHelper.sqrt(dx * dx + dz * dz);
//                    float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
                    float f1 = (float)(-(MathHelper.atan2(dy, dpitch) * (180D / Math.PI)));
                    laser.setSpawnYawPitch(laser.getSpawnYaw(), f1);

                    world.spawnEntity(laser);
                    this.attackTimer = -40;
                }
            } else if (this.attackTimer > 0) {
                --this.attackTimer;
            }

            this.drone.setAttacking(this.attackTimer > 10);
        }
    }

    static class AILookAround extends EntityAIBase {
        private final DroneEntity parentEntity;

        public AILookAround(DroneEntity drone) {
            this.parentEntity = drone;
            this.setMutexBits(2);
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
        public void updateTask() {
            if (this.parentEntity.getAttackTarget() == null) {
                this.parentEntity.rotationYaw = -((float) MathHelper.atan2(this.parentEntity.motionX, this.parentEntity.motionZ)) * (180F / (float) Math.PI);
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

    static class AIDroneFly extends EntityAIBase {
        private final DroneEntity parentEntity;

        public AIDroneFly(DroneEntity drone) {
            this.parentEntity = drone;
            this.setMutexBits(1);
        }

        /**
         * Returns whether the EntityAIBase should begin execution.
         */
        @Override
        public boolean shouldExecute() {
            EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();

            if (!entitymovehelper.isUpdating()) {
                return true;
            } else {
                double d0 = entitymovehelper.getX() - this.parentEntity.posX;
                double d1 = entitymovehelper.getY() - this.parentEntity.posY;
                double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
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
                double d0 = this.parentEntity.posX + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
                double d1 = this.parentEntity.posY + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
                double d2 = this.parentEntity.posZ + ((random.nextFloat() * 2.0F - 1.0F) * 32.0F);
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

    static class DroneMoveHelper extends EntityMoveHelper {
        private final DroneEntity parentEntity;
        private int courseChangeCooldown;

        public DroneMoveHelper(DroneEntity drone) {
            super(drone);
            this.parentEntity = drone;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - this.parentEntity.posX;
                double d1 = this.posY - this.parentEntity.posY;
                double d2 = this.posZ - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
                    d3 = Math.sqrt(d3);

                    if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
                        this.parentEntity.motionX += d0 / d3 * 0.1D;
                        this.parentEntity.motionY += d1 / d3 * 0.1D;
                        this.parentEntity.motionZ += d2 / d3 * 0.1D;
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
            AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();

            for (int i = 1; i < p_179926_7_; ++i) {
                axisalignedbb = axisalignedbb.offset(d0, d1, d2);

                if (!this.parentEntity.getEntityWorld().getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty()) {
                    return false;
                }
            }

            return true;
        }
    }
}