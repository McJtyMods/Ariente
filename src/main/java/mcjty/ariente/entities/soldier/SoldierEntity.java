package mcjty.ariente.entities.soldier;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.items.armor.PowerSuit;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoldierEntity extends EntityMob implements IArmRaisable, IForcefieldImmunity, ISoldier {

    private static final DataParameter<Boolean> ARMS_RAISED = EntityDataManager.createKey(SoldierEntity.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/soldier");

    // If this entity is controlled by a city then this will be set
    private ChunkPos cityCenter;
    private SoldierBehaviourType behaviourType = SoldierBehaviourType.SOLDIER_FIGHTER;


    public SoldierEntity(World worldIn) {
        super(worldIn);
        if (isMaster()) {
            setSize(0.7F, 2.7F);
        } else {
            setSize(0.6F, 1.95F);
        }
    }

    public SoldierEntity(World world, ChunkPos cityCenter, SoldierBehaviourType behaviourType) {
        this(world);
        this.cityCenter = cityCenter;
        this.behaviourType = behaviourType;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && !isDead && cityCenter != null) {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
            ICityAI cityAI = aiSystem.getCityAI(cityCenter);
            if (cityAI != null && !cityAI.isDead(world)) {
                feedPowerIfNeeded(EntityEquipmentSlot.HEAD);
                feedPowerIfNeeded(EntityEquipmentSlot.FEET);
                feedPowerIfNeeded(EntityEquipmentSlot.CHEST);
                feedPowerIfNeeded(EntityEquipmentSlot.LEGS);
            } else {
                cityCenter = null;
            }
        }
    }

    private void feedPowerIfNeeded(EntityEquipmentSlot slot) {
        ItemStack stack = getItemStackFromSlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof PowerSuit)) {
            return;
        }
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            return;
        }
        int negarite = compound.getInteger("negarite");
        if (negarite < 2) {
            compound.setInteger("negarite", 2);
        }
        int posirite = compound.getInteger("posirite");
        if (posirite < 2) {
            compound.setInteger("posirite", 2);
        }
    }

    protected boolean isMaster() {
        return false;
    }

    @Override
    public boolean isImmuneToForcefield(IForceFieldTile tile) {
        // @todo Need to implement forcefield immunity cards
        return true;
    }

    public ChunkPos getCityCenter() {
        return cityCenter;
    }

    // Guards don't despawn. Spawning and despawning is managed by the cities
    @Override
    protected boolean canDespawn() {
        return behaviourType != SoldierBehaviourType.SOLDIER_GUARD;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(ARMS_RAISED, Boolean.valueOf(false));
//        LootTable lootTableFromLocation = worldObj.getLootTableManager().getLootTableFromLocation(LOOT);
//        System.out.println("lootTableFromLocation = " + lootTableFromLocation);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.32D);
        if (isMaster()) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
        }
    }

    public SoldierBehaviourType getBehaviourType() {
        return behaviourType;
    }

    @Override
    public void setArmsRaised(boolean armsRaised) {
        this.getDataManager().set(ARMS_RAISED, Boolean.valueOf(armsRaised));
    }

    @SideOnly(Side.CLIENT)
    public boolean isArmsRaised() {
        return this.getDataManager().get(ARMS_RAISED).booleanValue();
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        super.dropLoot(wasRecentlyHit, lootingModifier, source);
        if (attackingPlayer != null) {
            if (Ariente.setup.arienteWorld) {
                double chance = ArienteWorldCompat.getArienteWorld().getSoldierCityKeyChance();
                if (cityCenter != null && rand.nextFloat() < chance) {
                    ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
                    ICityAI cityAI = aiSystem.getCityAI(cityCenter);
                    if (cityAI != null) {
                        ItemStack stack = new ItemStack(ModItems.keyCardItem);
                        float r = rand.nextFloat();
                        if (r < .4f) {
                            KeyCardItem.addSecurityTag(stack, cityAI.getKeyId());
                        } else if (r < .8f) {
                            KeyCardItem.addSecurityTag(stack, cityAI.getForcefieldId());
                        }
                        entityDropItem(stack, 0.0f);
                    }
                }
            }
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAISoldierAttack(this, this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAISoldierWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }

    private void applyEntityAI() {
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[]{EntityPigZombie.class}));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (cityCenter != null) {
            compound.setInteger("cityX", cityCenter.x);
            compound.setInteger("cityZ", cityCenter.z);
        }
        compound.setInteger("behaviour", behaviourType.ordinal());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("cityX")) {
            cityCenter = new ChunkPos(compound.getInteger("cityX"), compound.getInteger("cityZ"));
        }
        behaviourType = SoldierBehaviourType.values()[compound.getInteger("behaviour")];
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }


    @Override
    protected boolean isValidLightLevel() {
        return super.isValidLightLevel();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 3;
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        super.setAttackTarget(entitylivingbaseIn);
        if (entitylivingbaseIn instanceof EntityPlayer && cityCenter != null) {
            if (behaviourType == SoldierBehaviourType.SOLDIER_GUARD) {
                alertCity((EntityPlayer) entitylivingbaseIn);
            } else if (this instanceof MasterSoldierEntity) {
                alertCity((EntityPlayer) entitylivingbaseIn);
            }
        }
    }

    private void alertCity(@Nonnull EntityPlayer player) {
        ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
        ICityAI cityAI = aiSystem.getCityAI(cityCenter);
        cityAI.playerSpotted(player);
        aiSystem.saveSystem();
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        super.playStepSound(pos, blockIn);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

}
