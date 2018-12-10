package mcjty.ariente.entities.drone;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;

import java.util.Collections;
import java.util.List;

public class EntityAIScanForPlayer extends EntityAIBase {

    private final EntityLiving entityLiving;
    private final Predicate<Entity> predicate;
    private final EntityAINearestAttackableTarget.Sorter sorter;
    private EntityLivingBase entityTarget;

    public EntityAIScanForPlayer(EntityLiving entityLivingIn) {
        this.entityLiving = entityLivingIn;

        this.predicate = entity -> {
            if (!(entity instanceof EntityPlayer)) {
                return false;
            } else if (((EntityPlayer) entity).capabilities.disableDamage) {
                return false;
            } else {
                double d0 = EntityAIScanForPlayer.this.maxTargetRange();

                if (entity.isSneaking()) {
                    d0 *= 0.8;
                }

                if (entity.isInvisible()) {
                    float f = ((EntityPlayer) entity).getArmorVisibility();

                    if (f < 0.1F) {
                        f = 0.1F;
                    }

                    d0 *= (double) (0.7F * f);
                }

                return (double) entity.getDistance(EntityAIScanForPlayer.this.entityLiving) > d0 ? false : EntityAITarget.isSuitableTarget(EntityAIScanForPlayer.this.entityLiving, (EntityLivingBase) entity, false, true);
            }
        };
        this.sorter = new EntityAINearestAttackableTarget.Sorter(entityLivingIn);
    }

    public boolean shouldExecute() {
        double range = this.maxTargetRange();
        List<EntityPlayer> list = this.entityLiving.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, this.entityLiving.getEntityBoundingBox().grow(range, range, range), this.predicate);
        Collections.sort(list, this.sorter);

        if (list.isEmpty()) {
            return false;
        } else {
            this.entityTarget = list.get(0);
            return true;
        }
    }

    public boolean shouldContinueExecuting() {
        EntityLivingBase entitylivingbase = this.entityLiving.getAttackTarget();

        if (entitylivingbase == null) {
            return false;
        } else if (!entitylivingbase.isEntityAlive()) {
            return false;
        } else if (entitylivingbase instanceof EntityPlayer && ((EntityPlayer) entitylivingbase).capabilities.disableDamage) {
            return false;
        } else {
            Team team = this.entityLiving.getTeam();
            Team team1 = entitylivingbase.getTeam();

            if (team != null && team1 == team) {
                return false;
            } else {
                double d0 = this.maxTargetRange();

                if (this.entityLiving.getDistanceSq(entitylivingbase) > d0 * d0) {
                    return false;
                } else {
                    return !(entitylivingbase instanceof EntityPlayerMP) || !((EntityPlayerMP) entitylivingbase).interactionManager.isCreative();
                }
            }
        }
    }

    public void startExecuting() {
        this.entityLiving.setAttackTarget(this.entityTarget);
        super.startExecuting();
    }

    public void resetTask() {
        this.entityLiving.setAttackTarget(null);
        super.startExecuting();
    }

    protected double maxTargetRange() {
        IAttributeInstance iattributeinstance = this.entityLiving.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getAttributeValue();
    }
}