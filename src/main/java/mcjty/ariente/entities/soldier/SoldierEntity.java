package mcjty.ariente.entities.soldier;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.*;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoldierEntity extends Monster implements IArmRaisable, IForcefieldImmunity, ISoldier {

    private static final EntityDataAccessor<Boolean> ARMS_RAISED = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.BOOLEAN);
    public static final ResourceLocation LOOT = new ResourceLocation(Ariente.MODID, "entities/soldier");

    // If this entity is controlled by a city then this will be set
    protected ChunkPos cityCenter;
    protected SoldierBehaviourType behaviourType = SoldierBehaviourType.SOLDIER_FIGHTER;


    public SoldierEntity(EntityType<? extends Monster> type, Level worldIn) {
        super(type, worldIn);
    }

    public static SoldierEntity create(Level world, ChunkPos cityCenter, SoldierBehaviourType behaviourType) {
        SoldierEntity entity = new SoldierEntity(Registration.ENTITY_SOLDIER.get(), world);
        entity.cityCenter = cityCenter;
        entity.behaviourType = behaviourType;
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide && isAlive() && cityCenter != null) {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(level);
            ICityAI cityAI = aiSystem.getCityAI(cityCenter);
            if (cityAI != null && !cityAI.isDead(level)) {
                feedPowerIfNeeded(EquipmentSlot.HEAD);
                feedPowerIfNeeded(EquipmentSlot.FEET);
                feedPowerIfNeeded(EquipmentSlot.CHEST);
                feedPowerIfNeeded(EquipmentSlot.LEGS);
            } else {
                cityCenter = null;
            }
        }
    }

    private void feedPowerIfNeeded(EquipmentSlot slot) {
        ItemStack stack = getItemBySlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof PowerSuit)) {
            return;
        }
        CompoundTag compound = stack.getTag();
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
    public boolean requiresCustomPersistence() {
        return behaviourType == SoldierBehaviourType.SOLDIER_GUARD;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(ARMS_RAISED, Boolean.valueOf(false));
//        LootTable lootTableFromLocation = worldObj.getLootTableManager().getLootTableFromLocation(LOOT);
//        System.out.println("lootTableFromLocation = " + lootTableFromLocation);
    }
//            this.getAttributes().registerAttribute(Attributes.FOLLOW_RANGE).setBaseValue(16.0D);

    public static AttributeSupplier.Builder registerAttributes() {
        AttributeSupplier.Builder attributes = LivingEntity.createLivingAttributes();
        attributes
            .add(Attributes.FOLLOW_RANGE, 35.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.32D)
            .add(Attributes.ATTACK_DAMAGE, 4.0D)
            .add(Attributes.ARMOR, 4.0D);

        return attributes;
    }

    public SoldierBehaviourType getBehaviourType() {
        return behaviourType;
    }

    @Override
    public void setArmsRaised(boolean armsRaised) {
        this.getEntityData().set(ARMS_RAISED, Boolean.valueOf(armsRaised));
    }

    public boolean isArmsRaised() {
        return this.getEntityData().get(ARMS_RAISED).booleanValue();
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSourceIn, boolean p_213354_2_) {
        super.dropFromLootTable(damageSourceIn, p_213354_2_);
        if (lastHurtByPlayer != null) {
            if (Ariente.setup.arienteWorld) {
                double chance = ArienteWorldCompat.getArienteWorld().getSoldierCityKeyChance();
                if (cityCenter != null && random.nextFloat() < chance) {
                    ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(level);
                    ICityAI cityAI = aiSystem.getCityAI(cityCenter);
                    if (cityAI != null) {
                        ItemStack stack = new ItemStack(Registration.KEY_CARD.get());
                        float r = random.nextFloat();
                        if (r < .4f) {
                            KeyCardItem.addSecurityTag(stack, cityAI.getKeyId());
                        } else if (r < .8f) {
                            KeyCardItem.addSecurityTag(stack, cityAI.getForcefieldId());
                        }
                        KeyCardItem.setDescription(stack, "City: " + cityAI.getCityName());
                        spawnAtLocation(stack, 0.0f);
                    }
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RandomSwimmingGoal(this, 0.8D, 4));
        this.goalSelector.addGoal(2, new EntityAISoldierAttack(this, this, 1.0D, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new EntityAISoldierWander(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }


    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (cityCenter != null) {
            compound.putInt("cityX", cityCenter.x);
            compound.putInt("cityZ", cityCenter.z);
        }
        compound.putInt("behaviour", behaviourType.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
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
    public int getMaxSpawnClusterSize() {
        return 3;
    }

    @Override
    public void setTarget(@Nullable LivingEntity entitylivingbaseIn) {
        super.setTarget(entitylivingbaseIn);
        if (entitylivingbaseIn instanceof Player && cityCenter != null) {
            if (behaviourType == SoldierBehaviourType.SOLDIER_GUARD) {
                alertCity((Player) entitylivingbaseIn);
            } else if (this instanceof MasterSoldierEntity) {
                alertCity((Player) entitylivingbaseIn);
            }
        }
    }

    private void alertCity(@Nonnull Player player) {
        ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(level);
        ICityAI cityAI = aiSystem.getCityAI(cityCenter);
        cityAI.playerSpotted(player);
        aiSystem.saveSystem();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        SoundType soundtype = state.getBlock().getSoundType(state, level, pos, this);

        if (this.level.getBlockState(pos.above()).getBlock() == Blocks.SNOW) {
            soundtype = Blocks.SNOW.getSoundType(null);
            this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
        } else if (!state.getBlock().defaultBlockState().getMaterial().isLiquid()) {
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
