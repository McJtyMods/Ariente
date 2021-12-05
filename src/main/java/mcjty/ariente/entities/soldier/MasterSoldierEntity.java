package mcjty.ariente.entities.soldier;

import mcjty.ariente.api.SoldierBehaviourType;
import mcjty.ariente.setup.Registration;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class MasterSoldierEntity extends SoldierEntity {

    private int noregenCounter = 0;

    public MasterSoldierEntity(EntityType<? extends MonsterEntity> type, World worldIn) {
        super(type, worldIn);
        // @todo 1.14 move to type
        //isImmuneToFire = true;
    }

    public static MasterSoldierEntity create(World world, ChunkPos cityCenter, SoldierBehaviourType behaviourType) {
        MasterSoldierEntity entity = new MasterSoldierEntity(Registration.ENTITY_MASTER_SOLDIER.get(), world);
        entity.cityCenter = cityCenter;
        entity.behaviourType = behaviourType;
        // @todo 1.14 move to type
//        isImmuneToFire = true;
        return entity;
    }

    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        AttributeModifierMap.MutableAttribute attributes = LivingEntity.createLivingAttributes();
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
            EffectInstance effect = getEffect(Effects.REGENERATION);
            if (effect == null || effect.getDuration() <= 0) {
                addEffect(new EffectInstance(Effects.REGENERATION, 30, 3, false, false));
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
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        noregenCounter = compound.getInt("noregen");
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("noregen", noregenCounter);
    }

    @Override
    protected boolean isMaster() {
        return true;
    }
}
