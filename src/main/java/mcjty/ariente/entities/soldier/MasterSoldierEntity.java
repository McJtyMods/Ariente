package mcjty.ariente.entities.soldier;

import mcjty.ariente.api.SoldierBehaviourType;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class MasterSoldierEntity extends SoldierEntity {

    private int noregenCounter = 0;

    public MasterSoldierEntity(World worldIn) {
        super(worldIn);
        isImmuneToFire = true;
    }

    public MasterSoldierEntity(World world, ChunkPos cityCenter, SoldierBehaviourType behaviourType) {
        super(world, cityCenter, behaviourType);
        isImmuneToFire = true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (noregenCounter > 0) {
            noregenCounter--;
        } else {
            PotionEffect effect = getActivePotionEffect(MobEffects.REGENERATION);
            if (effect == null || effect.getDuration() <= 0) {
                addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 30, 3, false, false));
            }
        }
    }

    public void setNoregenCounter(int c) {
        this.noregenCounter = c;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        boolean rc = super.isEntityInvulnerable(source);
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
    public void readEntityFromNBT(CompoundNBT compound) {
        super.readEntityFromNBT(compound);
        noregenCounter = compound.getInteger("noregen");
    }

    @Override
    public void writeEntityToNBT(CompoundNBT compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("noregen", noregenCounter);
    }

    @Override
    protected boolean isMaster() {
        return true;
    }
}
