package mcjty.ariente.apiimpl;

import mcjty.ariente.api.IArienteSystem;
import mcjty.ariente.api.IFluxLevitatorEntity;
import mcjty.ariente.api.ISoldier;
import mcjty.ariente.api.SoldierBehaviourType;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.lib.varia.ChunkCoord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ArienteSystem implements IArienteSystem {

    @Nonnull
    @Override
    public List<? extends ISoldier> getSoldiersWithinAABB(World world, AxisAlignedBB aabb) {
        return world.getEntitiesWithinAABB(SoldierEntity.class, aabb);
    }

    @Nonnull
    @Override
    public List<? extends IFluxLevitatorEntity> getLevitatorsWithinAABB(World world, AxisAlignedBB aabb) {
        return world.getEntitiesWithinAABB(FluxLevitatorEntity.class, aabb);
    }

    @Override
    public EntityLivingBase createSoldier(World world, BlockPos pos, EnumFacing facing, @Nullable ChunkCoord cityCenter, SoldierBehaviourType type, boolean master) {
        return createSoldierInt(world, pos, facing, cityCenter, type, master);
    }

    private SoldierEntity createSoldierInt(World world, BlockPos p, EnumFacing facing, @Nullable ChunkCoord center, SoldierBehaviourType behaviourType,
                                           boolean master) {
        SoldierEntity entity;
        if (master) {
            entity = new MasterSoldierEntity(world, center, behaviourType);
        } else {
            entity = new SoldierEntity(world, center, behaviourType);
        }
        entity.setPosition(p.getX()+.5, p.getY(), p.getZ()+.5);
        float yaw = 0;
        switch (facing) {
            case NORTH:
                yaw = 0;
                break;
            case SOUTH:
                yaw = 90;
                break;
            case WEST:
                yaw = 180;
                break;
            case EAST:
                yaw = 270;
                break;
            default:
                break;
        }
        entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, yaw, 0);
        world.spawnEntity(entity);
        return entity;
    }


}
