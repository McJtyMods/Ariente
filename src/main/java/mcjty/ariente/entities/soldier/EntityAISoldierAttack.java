package mcjty.ariente.entities.soldier;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;

public class EntityAISoldierAttack extends EntityAIAttackMelee {
    private final IArmRaisable raisable;
    private int raiseArmTicks;

    public EntityAISoldierAttack(EntityCreature zombieIn, IArmRaisable raisable, double speedIn, boolean longMemoryIn) {
        super(zombieIn, speedIn, longMemoryIn);
        this.raisable = raisable;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        super.startExecuting();
        this.raiseArmTicks = 0;
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask() {
        super.resetTask();
        this.raisable.setArmsRaised(false);
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        super.updateTask();
        ++this.raiseArmTicks;

        if (this.raiseArmTicks >= 5 && this.attackTick < 10) {
            this.raisable.setArmsRaised(true);
        } else {
            this.raisable.setArmsRaised(false);
        }
    }
}