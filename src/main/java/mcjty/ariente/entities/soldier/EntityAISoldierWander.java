package mcjty.ariente.entities.soldier;

import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityAISystem;
import mcjty.ariente.api.SoldierBehaviourType;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class EntityAISoldierWander extends Goal {
    protected final SoldierEntity entity;
    protected double x;
    protected double y;
    protected double z;
    protected final double speed;
    protected int executionChance;
    protected boolean mustUpdate;

    public EntityAISoldierWander(SoldierEntity creatureIn, double speedIn) {
        this(creatureIn, speedIn, 120);
    }

    public EntityAISoldierWander(SoldierEntity creatureIn, double speedIn, int chance) {
        this.entity = creatureIn;
        this.speed = speedIn;
        this.executionChance = chance;
        // @todo 1.14
//        this.setMutexBits(1);
    }

    @Override
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
        } else if (entity.getCityCenter() == null) {
            return RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
        } else {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(entity.world);
            ICityAI cityAI = aiSystem.getCityAI(entity.getCityCenter());
            BlockPos pos = cityAI.requestNewSoldierPosition(entity.world, entity.getAttackTarget());
            if (pos != null) {
                return new Vec3d(pos);
            } else {
                return RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.entity.getNavigator().noPath();
    }

    @Override
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