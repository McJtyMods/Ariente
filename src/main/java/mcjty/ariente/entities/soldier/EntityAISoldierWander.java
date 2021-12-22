package mcjty.ariente.entities.soldier;

import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityAISystem;
import mcjty.ariente.api.SoldierBehaviourType;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

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
    public boolean canUse() {
        if (!this.mustUpdate) {
            if (this.entity.getNoActionTime() >= 100) {
                return false;
            }

            if (this.entity.getRandom().nextInt(this.executionChance) != 0) {
                return false;
            }
        }

        Vec3 vec3d = this.getPosition();

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
    protected Vec3 getPosition() {
        if (entity.getBehaviourType() == SoldierBehaviourType.SOLDIER_GUARD) {
            return null;
        } else if (entity.getCityCenter() == null) {
            return LandRandomPos.getPos(this.entity, 10, 7);
        } else {
            ICityAISystem aiSystem = ArienteWorldCompat.getCityAISystem(entity.level);
            ICityAI cityAI = aiSystem.getCityAI(entity.getCityCenter());
            BlockPos pos = cityAI.requestNewSoldierPosition(entity.level, entity.getTarget());
            if (pos != null) {
                return new Vec3(pos.getX(), pos.getY(), pos.getZ());
            } else {
                return LandRandomPos.getPos(this.entity, 10, 7);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.x, this.y, this.z, this.speed);
    }

    public void makeUpdate() {
        this.mustUpdate = true;
    }

    public void setExecutionChance(int newchance) {
        this.executionChance = newchance;
    }
}