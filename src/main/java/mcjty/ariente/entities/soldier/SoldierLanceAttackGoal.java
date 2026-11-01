package mcjty.ariente.entities.soldier;

import mcjty.ariente.items.EnergyLanceItem;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;

public class SoldierLanceAttackGoal extends RangedAttackGoal {

    private final SoldierEntity soldier;

    public SoldierLanceAttackGoal(SoldierEntity soldier) {
        super(soldier, 1.0D, EnergyLanceItem.SHOT_INTERVAL, EnergyLanceItem.RANGE);
        this.soldier = soldier;
    }

    @Override
    public boolean canUse() {
        return EnergyLanceItem.canFire(soldier) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return EnergyLanceItem.canFire(soldier) && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        soldier.setAggressive(true);
        soldier.setArmsRaised(true);
    }

    @Override
    public void stop() {
        super.stop();
        soldier.setAggressive(false);
        soldier.setArmsRaised(false);
    }
}
