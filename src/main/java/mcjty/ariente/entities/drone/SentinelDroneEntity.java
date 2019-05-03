package mcjty.ariente.entities.drone;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityAISystem;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.blocks.defense.IForcefieldImmunity;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.sounds.ModSounds;
import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class SentinelDroneEntity extends EntityFlying implements IMob, IForcefieldImmunity {

    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/sentinel_drone");

    private int sentinelId;
    private ChunkCoord cityCenter;

    public SentinelDroneEntity(World worldIn) {
        super(worldIn);
        this.setSize(1.3F, 1.3F);
        this.isImmuneToFire = false;
        this.experienceValue = 5;
        this.moveHelper = new SentinelDroneMoveHelper(this);
    }

    public SentinelDroneEntity(World world, int sentinelId, ChunkCoord cityCenter) {
        this(world);
        this.sentinelId = sentinelId;
        this.cityCenter = cityCenter;
    }

    @Override
    public boolean isImmuneToForcefield(ForceFieldTile tile) {
        return true;
    }

    // Override this to make it less likely to despawn
    @Override
    protected void despawnEntity() {
        Entity entity = this.world.getClosestPlayerToEntity(this, -1.0D);

        if (entity != null) {
            double d0 = entity.posX - this.posX;
            double d1 = entity.posY - this.posY;
            double d2 = entity.posZ - this.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;

            if (this.canDespawn() && d3 > 16384.0D) {
                this.setDead();
            }

            if (this.idleTime > 900 && this.rand.nextInt(800) == 0 && d3 > 2048.0D && this.canDespawn()) {
                this.setDead();
            } else if (d3 < 2048.0D) {
                this.idleTime = 0;
            }
        }
    }


    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
//        super.setAttackTarget(entitylivingbaseIn);
        // This is called by EntityAIFindEntityNearestPlayer when it spots a player.
        // In this case we don't attack but notify the city AI
        if (entitylivingbaseIn instanceof EntityPlayer && cityCenter != null) {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
            ICityAI cityAI = aiSystem.getCityAI(cityCenter);
            cityAI.playerSpotted((EntityPlayer) entitylivingbaseIn);
            aiSystem.saveSystem();
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(5, new AICircleCity(this));
        this.tasks.addTask(7, new AILookAround(this));
        this.targetTasks.addTask(1, new EntityAIScanForPlayer(this));
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
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50); // Configurable
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

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (cityCenter != null) {
            compound.setInteger("cityX", cityCenter.getChunkX());
            compound.setInteger("cityZ", cityCenter.getChunkZ());
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("cityX")) {
            cityCenter = new ChunkCoord(compound.getInteger("cityX"), compound.getInteger("cityZ"));
        }
    }

    @Override
    public float getEyeHeight() {
        return 0.8F;
    }

    static class AILookAround extends EntityAIBase {
        private final SentinelDroneEntity parentEntity;

        public AILookAround(SentinelDroneEntity drone) {
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
                EntityLivingBase entitylivingbase = this.parentEntity.getAttackTarget();
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

    static class AICircleCity extends EntityAIBase {
        private final SentinelDroneEntity parentEntity;

        public AICircleCity(SentinelDroneEntity drone) {
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

    static class SentinelDroneMoveHelper extends EntityMoveHelper {
        private final SentinelDroneEntity parentEntity;
        private int courseChangeCooldown;

        public SentinelDroneMoveHelper(SentinelDroneEntity drone) {
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