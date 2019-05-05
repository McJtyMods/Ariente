package mcjty.ariente.apiimpl;

import mcjty.ariente.api.*;
import mcjty.ariente.blocks.utility.wireless.RedstoneChannels;
import mcjty.ariente.entities.drone.DroneEntity;
import mcjty.ariente.entities.drone.SentinelDroneEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.ariente.security.SecuritySystem;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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
    public EntityLivingBase createSoldier(World world, BlockPos pos, EnumFacing facing, @Nullable ChunkPos cityCenter, SoldierBehaviourType type, boolean master) {
        return createSoldierInt(world, pos, facing, cityCenter, type, master);
    }

    @Override
    public EntityLivingBase createSentinel(World world, int index, @Nullable ChunkPos cityCenter) {
        return new SentinelDroneEntity(world, index, cityCenter);
    }

    @Override
    public EntityLivingBase createDrone(World world, @Nullable ChunkPos cityCenter) {
        return new DroneEntity(world, cityCenter);
    }

    @Override
    public void addSecurity(ItemStack keyCard, String tag) {
        KeyCardItem.addSecurityTag(keyCard, tag);
    }

    @Override
    public void fixNetworks(World world, BlockPos pos) {
        PowerSenderSupport.fixNetworks(world, pos);
    }

    @Override
    public BlockRailBase.EnumRailDirection getBeamDirection(IBlockState state) {
        return FluxLevitatorEntity.getBeamDirection(state);
    }

    @Override
    public boolean hasWorkingUpgrade(ItemStack armor, ArmorUpgradeType type) {
        return ModuleSupport.hasWorkingUpgrade(armor, type);
    }

    @Override
    public ISecuritySystem getSecuritySystem(World world) {
        return SecuritySystem.getSecuritySystem(world);
    }

    @Override
    public IRedstoneChannels getRedstoneChannels(World world) {
        return RedstoneChannels.getChannels(world);
    }

    @Override
    public Entity createFluxLevitatorEntity(World world, double x, double y, double z) {
        return new FluxLevitatorEntity(world, x, y, z);
    }

    private SoldierEntity createSoldierInt(World world, BlockPos p, EnumFacing facing, @Nullable ChunkPos center, SoldierBehaviourType behaviourType,
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
