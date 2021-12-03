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
        AttributeModifierMap.MutableAttribute attributes = LivingEntity.registerAttributes();
        attributes
            .createMutableAttribute(Attributes.FOLLOW_RANGE, 35.0D)
            .createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.32D)
            .createMutableAttribute(Attributes.MAX_HEALTH, 150.0D)
            .createMutableAttribute(Attributes.ATTACK_DAMAGE, 8.0D)
            .createMutableAttribute(Attributes.ARMOR, 6.0D);

        return attributes;
    }

    @Override
    public void tick() {
        super.tick();

        if (noregenCounter > 0) {
            noregenCounter--;
        } else {
            EffectInstance effect = getActivePotionEffect(Effects.REGENERATION);
            if (effect == null || effect.getDuration() <= 0) {
                addPotionEffect(new EffectInstance(Effects.REGENERATION, 30, 3, false, false));
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
        if (source.isFireDamage()) {
            return true;
        }
        if (source.isMagicDamage()) {
            return true;
        }
        return false;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        noregenCounter = compound.getInt("noregen");
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("noregen", noregenCounter);
    }

    @Override
    protected boolean isMaster() {
        return true;
    }
}
