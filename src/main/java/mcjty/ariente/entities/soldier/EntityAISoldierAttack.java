package mcjty.ariente.entities.soldier;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class EntityAISoldierAttack extends MeleeAttackGoal {
    private final IArmRaisable raisable;
    private int raiseArmTicks;

    public EntityAISoldierAttack(CreatureEntity zombieIn, IArmRaisable raisable, double speedIn, boolean longMemoryIn) {
        super(zombieIn, speedIn, longMemoryIn);
        this.raisable = raisable;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void start() {
        super.start();
        this.raiseArmTicks = 0;
    }

    /**
     * Resets the task
     */
    @Override
    public void stop() {
        super.stop();
        this.raisable.setArmsRaised(false);
    }

    /**
     * Updates the task
     */
    @Override
    public void tick() {
        super.tick();
        ++this.raiseArmTicks;

        if (this.raiseArmTicks >= 5 && this.getTicksUntilNextAttack() < 10) {
            this.raisable.setArmsRaised(true);
        } else {
            this.raisable.setArmsRaised(false);
        }
    }
}