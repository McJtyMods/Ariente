package mcjty.ariente.entities.soldier;

import mcjty.ariente.api.SoldierBehaviourType;
import mcjty.ariente.setup.Registration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class MasterSoldierEntity extends SoldierEntity {

    private int noregenCounter = 0;

    public MasterSoldierEntity(EntityType<? extends Monster> type, Level worldIn) {
        super(type, worldIn);
        // @todo 1.14 move to type
        //isImmuneToFire = true;
    }

    public static MasterSoldierEntity create(Level world, ChunkPos cityCenter, SoldierBehaviourType behaviourType) {
        MasterSoldierEntity entity = new MasterSoldierEntity(Registration.ENTITY_MASTER_SOLDIER.get(), world);
        entity.cityCenter = cityCenter;
        entity.behaviourType = behaviourType;
        // @todo 1.14 move to type
//        isImmuneToFire = true;
        return entity;
    }

    public static AttributeSupplier.Builder registerAttributes() {
        AttributeSupplier.Builder attributes = LivingEntity.createLivingAttributes();
        attributes
            .add(Attributes.FOLLOW_RANGE, 35.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.32D)
            .add(Attributes.MAX_HEALTH, 150.0D)
            .add(Attributes.ATTACK_DAMAGE, 8.0D)
            .add(Attributes.ARMOR, 6.0D);

        return attributes;
    }

    @Override
    public void tick() {
        super.tick();

        if (noregenCounter > 0) {
            noregenCounter--;
        } else {
            MobEffectInstance effect = getEffect(MobEffects.REGENERATION);
            if (effect == null || effect.getDuration() <= 0) {
                addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 3, false, false));
            }
        }
    }

    public void setNoregenCounter(int c) {
        this.noregenCounter = c;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        boolean rc = super.isInvulnerableTo(source);
        if (rc) {
            return rc;
        }
        if (source.isFire()) {
            return true;
        }
        if (source.isMagic()) {
            return true;
        }
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        noregenCounter = compound.getInt("noregen");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("noregen", noregenCounter);
    }

    @Override
    protected boolean isMaster() {
        return true;
    }
}
