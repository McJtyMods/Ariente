package mcjty.ariente.entities.drone;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.Team;

import java.util.List;

public class EntityAIScanForPlayer extends Goal {

    private final LivingEntity entityLiving;
    private final Predicate<Entity> predicate;
    //    private final EntityAINearestAttackableTarget.Sorter sorter;
    private LivingEntity entityTarget;

    public EntityAIScanForPlayer(LivingEntity entityLivingIn) {
        this.entityLiving = entityLivingIn;

        this.predicate = entity -> {
            if (!(entity instanceof PlayerEntity)) {
                return false;
            } else if (((PlayerEntity) entity).abilities.disableDamage) {
                return false;
            } else {
                double d0 = EntityAIScanForPlayer.this.maxTargetRange();

                if (entity.isSneaking()) {
                    d0 *= 0.8;
                }

                if (entity.isInvisible()) {
                    // @todo 1.14
//                    float f = ((PlayerEntity) entity).getArmorVisibility();
//
//                    if (f < 0.1F) {
//                        f = 0.1F;
//                    }
//
//                    d0 *= (double) (0.7F * f);
                }

                // @todo 1.14
//                return (double) entity.getDistance(EntityAIScanForPlayer.this.entityLiving) > d0 ? false : EntityAITarget.isSuitableTarget(EntityAIScanForPlayer.this.entityLiving, (LivingEntity) entity, false, true);
                return false;
            }
        };
        // @todo 1.14
//        this.sorter = new EntityAINearestAttackableTarget.Sorter(entityLivingIn);
    }

    @Override
    public boolean shouldExecute() {
        double range = this.maxTargetRange();
        List<PlayerEntity> list = this.entityLiving.world.<PlayerEntity>getEntitiesWithinAABB(PlayerEntity.class, this.entityLiving.getBoundingBox().grow(range, range, range), this.predicate);
        // @todo 1.14
//        Collections.sort(list, this.sorter);

        if (list.isEmpty()) {
            return false;
        } else {
            this.entityTarget = list.get(0);
            return true;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        LivingEntity entitylivingbase = this.entityLiving.getRevengeTarget();

        if (entitylivingbase == null) {
            return false;
        } else if (!entitylivingbase.isAlive()) {
            return false;
        } else if (entitylivingbase instanceof PlayerEntity && ((PlayerEntity) entitylivingbase).abilities.disableDamage) {
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
                    return !(entitylivingbase instanceof ServerPlayerEntity) || !((ServerPlayerEntity) entitylivingbase).interactionManager.isCreative();
                }
            }
        }
    }

    @Override
    public void startExecuting() {
        this.entityLiving.setRevengeTarget(this.entityTarget);
        super.startExecuting();
    }

    @Override
    public void resetTask() {
        this.entityLiving.setRevengeTarget(null);
        super.startExecuting();
    }

    protected double maxTargetRange() {
        ModifiableAttributeInstance iattributeinstance = this.entityLiving.getAttribute(Attributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getValue();
    }
}