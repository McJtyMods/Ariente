package mcjty.ariente.entities.soldier;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoldierEntity extends CreatureEntity implements IArmRaisable, IForcefieldImmunity, ISoldier {

    private static final DataParameter<Boolean> ARMS_RAISED = EntityDataManager.createKey(SoldierEntity.class, DataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/soldier");

    // If this entity is controlled by a city then this will be set
    protected ChunkPos cityCenter;
    protected SoldierBehaviourType behaviourType = SoldierBehaviourType.SOLDIER_FIGHTER;


    public SoldierEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public static SoldierEntity create(World world, ChunkPos cityCenter, SoldierBehaviourType behaviourType) {
        SoldierEntity entity = new SoldierEntity(Registration.SOLDIER.get(), world);
        entity.cityCenter = cityCenter;
        entity.behaviourType = behaviourType;
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote && isAlive() && cityCenter != null) {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
            ICityAI cityAI = aiSystem.getCityAI(cityCenter);
            if (cityAI != null && !cityAI.isDead(world)) {
                feedPowerIfNeeded(EquipmentSlotType.HEAD);
                feedPowerIfNeeded(EquipmentSlotType.FEET);
                feedPowerIfNeeded(EquipmentSlotType.CHEST);
                feedPowerIfNeeded(EquipmentSlotType.LEGS);
            } else {
                cityCenter = null;
            }
        }
    }

    private void feedPowerIfNeeded(EquipmentSlotType slot) {
        ItemStack stack = getItemStackFromSlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof PowerSuit)) {
            return;
        }
        CompoundNBT compound = stack.getTag();
        if (compound == null) {
            return;
        }
        int negarite = compound.getInt("negarite");
        if (negarite < 2) {
            compound.putInt("negarite", 2);
        }
        int posirite = compound.getInt("posirite");
        if (posirite < 2) {
            compound.putInt("posirite", 2);
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
    public boolean preventDespawn() {
        return behaviourType == SoldierBehaviourType.SOLDIER_GUARD;
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.getDataManager().register(ARMS_RAISED, Boolean.valueOf(false));
//        LootTable lootTableFromLocation = worldObj.getLootTableManager().getLootTableFromLocation(LOOT);
//        System.out.println("lootTableFromLocation = " + lootTableFromLocation);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.32D);
        if (isMaster()) {
            this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150.0D);
            this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
            this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        } else {
            this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
            this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
        }
    }

    public SoldierBehaviourType getBehaviourType() {
        return behaviourType;
    }

    @Override
    public void setArmsRaised(boolean armsRaised) {
        this.getDataManager().set(ARMS_RAISED, Boolean.valueOf(armsRaised));
    }

    public boolean isArmsRaised() {
        return this.getDataManager().get(ARMS_RAISED).booleanValue();
    }

    @Override
    protected void dropLoot(DamageSource damageSourceIn, boolean p_213354_2_) {
        super.dropLoot(damageSourceIn, p_213354_2_);
        if (attackingPlayer != null) {
            if (Ariente.setup.arienteWorld) {
                double chance = ArienteWorldCompat.getArienteWorld().getSoldierCityKeyChance();
                if (cityCenter != null && rand.nextFloat() < chance) {
                    ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
                    ICityAI cityAI = aiSystem.getCityAI(cityCenter);
                    if (cityAI != null) {
                        ItemStack stack = new ItemStack(ModBlocks.KEY_CARD.get());
                        float r = rand.nextFloat();
                        if (r < .4f) {
                            KeyCardItem.addSecurityTag(stack, cityAI.getKeyId());
                        } else if (r < .8f) {
                            KeyCardItem.addSecurityTag(stack, cityAI.getForcefieldId());
                        }
                        KeyCardItem.setDescription(stack, "City: " + cityAI.getCityName());
                        entityDropItem(stack, 0.0f);
                    }
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(2, new EntityAISoldierAttack(this, this, 1.0D, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new EntityAISoldierWander(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, ZombiePigmanEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }


    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        if (cityCenter != null) {
            compound.putInt("cityX", cityCenter.x);
            compound.putInt("cityZ", cityCenter.z);
        }
        compound.putInt("behaviour", behaviourType.ordinal());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains("cityX")) {
            cityCenter = new ChunkPos(compound.getInt("cityX"), compound.getInt("cityZ"));
        }
        behaviourType = SoldierBehaviourType.values()[compound.getInt("behaviour")];
    }

// @todo 1.14
//    @Override
//    @Nullable
//    protected ResourceLocation getLootTable() {
//        return LOOT;
//    }


    // @todo 1.14
//    @Override
//    protected boolean isValidLightLevel() {
//        return super.isValidLightLevel();
//    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 3;
    }

    @Override
    public void setAttackTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setAttackTarget(entitylivingbaseIn);
        if (entitylivingbaseIn instanceof PlayerEntity && cityCenter != null) {
            if (behaviourType == SoldierBehaviourType.SOLDIER_GUARD) {
                alertCity((PlayerEntity) entitylivingbaseIn);
            } else if (this instanceof MasterSoldierEntity) {
                alertCity((PlayerEntity) entitylivingbaseIn);
            }
        }
    }

    private void alertCity(@Nonnull PlayerEntity player) {
        ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(world);
        ICityAI cityAI = aiSystem.getCityAI(cityCenter);
        cityAI.playerSpotted(player);
        aiSystem.saveSystem();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        SoundType soundtype = state.getBlock().getSoundType(state, world, pos, this);

        if (this.world.getBlockState(pos.up()).getBlock() == Blocks.SNOW) {
            soundtype = Blocks.SNOW.getSoundType(null);
            this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        } else if (!state.getBlock().getDefaultState().getMaterial().isLiquid()) {
            this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        } else {
            this.playSound(ModSounds.step, 0.15f, isMaster() ? 0.5f : 1.0f);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return isMaster() ? ModSounds.bossSoldierHurt : ModSounds.soldierHurt;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isMaster() ? ModSounds.bossSoldierDeath : ModSounds.soldierDeath;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

}
