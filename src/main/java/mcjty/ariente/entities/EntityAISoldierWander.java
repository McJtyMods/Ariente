package mcjty.ariente.entities;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class EntityAISoldierWander extends EntityAIBase {
    protected final EntitySoldier entity;
    protected double x;
    protected double y;
    protected double z;
    protected final double speed;
    protected int executionChance;
    protected boolean mustUpdate;

    public EntityAISoldierWander(EntitySoldier creatureIn, double speedIn) {
        this(creatureIn, speedIn, 120);
    }

    public EntityAISoldierWander(EntitySoldier creatureIn, double speedIn, int chance) {
        this.entity = creatureIn;
        this.speed = speedIn;
        this.executionChance = chance;
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (!this.mustUpdate) {
            if (this.entity.getIdleTime() >= 100) {
                return false;
            }

            if (this.entity.getRNG().nextInt(this.executionChance) != 0) {
                return false;
            }
        }

        Vec3d vec3d = this.getPosition();

        if (vec3d == null) {
            return false;
        } else {
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            this.mustUpdate = false;
            return true;
        }
    }

    @Nullable
    protected Vec3d getPosition() {
        if (entity.getBehaviourType() == SoldierBehaviourType.SOLDIER_GUARD) {
            return null;
        }
        return RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
    }

    public boolean shouldContinueExecuting() {
        return !this.entity.getNavigator().noPath();
    }

    public void startExecuting() {
        this.entity.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, this.speed);
    }

    public void makeUpdate() {
        this.mustUpdate = true;
    }

    public void setExecutionChance(int newchance) {
        this.executionChance = newchance;
    }
}