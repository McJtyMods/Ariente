package mcjty.ariente.entities.drone;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.EnumSet;

public class EntityAIScanForPlayer extends Goal {

    private final Mob entityLiving;
    private final TargetingConditions targeting = TargetingConditions.forCombat();
    private Player entityTarget;
    private int alertCooldown;

    public EntityAIScanForPlayer(Mob entityLiving) {
        this.entityLiving = entityLiving;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean canSpot(Player player) {
        if (player.isCreative() || player.isSpectator() || player.getAbilities().invulnerable) {
            return false;
        }
        // Vanilla targeting applies sneaking/invisibility range reductions and line of sight.
        return targeting.range(maxTargetRange()).test(entityLiving, player);
    }

    @Override
    public boolean canUse() {
        double range = maxTargetRange();
        entityTarget = entityLiving.level().getEntitiesOfClass(Player.class,
                        entityLiving.getBoundingBox().inflate(range), this::canSpot).stream()
                .min(Comparator.comparingDouble(entityLiving::distanceToSqr))
                .orElse(null);
        return entityTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        return entityTarget != null && canSpot(entityTarget);
    }

    @Override
    public void start() {
        // SentinelDroneEntity reports this target to the city instead of attacking it.
        entityLiving.setTarget(entityTarget);
        alertCooldown = adjustedTickDelay(20);
    }

    @Override
    public void tick() {
        if (--alertCooldown <= 0) {
            entityLiving.setTarget(entityTarget);
            alertCooldown = adjustedTickDelay(20);
        }
    }

    @Override
    public void stop() {
        entityTarget = null;
        entityLiving.setTarget(null);
    }

    protected double maxTargetRange() {
        AttributeInstance attribute = entityLiving.getAttribute(Attributes.FOLLOW_RANGE);
        return attribute == null ? 16 : attribute.getValue();
    }
}
