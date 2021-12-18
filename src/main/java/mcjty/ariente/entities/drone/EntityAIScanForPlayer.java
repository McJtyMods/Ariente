package mcjty.ariente.entities.drone;

import com.google.common.base.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

import java.util.List;

public class EntityAIScanForPlayer extends Goal {

    private final LivingEntity entityLiving;
    private final Predicate<Entity> predicate;
    //    private final EntityAINearestAttackableTarget.Sorter sorter;
    private LivingEntity entityTarget;

    public EntityAIScanForPlayer(LivingEntity entityLivingIn) {
        this.entityLiving = entityLivingIn;

        this.predicate = entity -> {
            if (!(entity instanceof Player)) {
                return false;
            } else if (((Player) entity).getAbilities().invulnerable) {
                return false;
            } else {
                double d0 = EntityAIScanForPlayer.this.maxTargetRange();

                if (entity.isShiftKeyDown()) {
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
    public boolean canUse() {
        double range = this.maxTargetRange();
        List<Player> list = this.entityLiving.level.<Player>getEntitiesOfClass(Player.class, this.entityLiving.getBoundingBox().inflate(range, range, range), this.predicate);
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
    public boolean canContinueToUse() {
        LivingEntity entitylivingbase = this.entityLiving.getLastHurtByMob();

        if (entitylivingbase == null) {
            return false;
        } else if (!entitylivingbase.isAlive()) {
            return false;
        } else if (entitylivingbase instanceof Player && ((Player) entitylivingbase).getAbilities().invulnerable) {
            return false;
        } else {
            Team team = this.entityLiving.getTeam();
            Team team1 = entitylivingbase.getTeam();

            if (team != null && team1 == team) {
                return false;
            } else {
                double d0 = this.maxTargetRange();

                if (this.entityLiving.distanceToSqr(entitylivingbase) > d0 * d0) {
                    return false;
                } else {
                    return !(entitylivingbase instanceof ServerPlayer) || !((ServerPlayer) entitylivingbase).gameMode.isCreative();
                }
            }
        }
    }

    @Override
    public void start() {
        this.entityLiving.setLastHurtByMob(this.entityTarget);
        super.start();
    }

    @Override
    public void stop() {
        this.entityLiving.setLastHurtByMob(null);
        super.start();
    }

    protected double maxTargetRange() {
        AttributeInstance iattributeinstance = this.entityLiving.getAttribute(Attributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 16.0D : iattributeinstance.getValue();
    }
}