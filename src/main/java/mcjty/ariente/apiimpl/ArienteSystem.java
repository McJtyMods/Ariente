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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ArienteSystem implements IArienteSystem {

    @Override
    public Class<? extends LivingEntity> getSoldierClass() {
        return SoldierEntity.class;
    }

    @Override
    public Class<? extends LivingEntity> getMasterSoldierClass() {
        return MasterSoldierEntity.class;
    }

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
    public LivingEntity createSoldier(World world, BlockPos pos, Direction facing, @Nullable ChunkPos cityCenter, SoldierBehaviourType type, boolean master) {
        return createSoldierInt(world, pos, facing, cityCenter, type, master);
    }

    @Override
    public LivingEntity createSentinel(World world, int index, @Nullable ChunkPos cityCenter) {
        return new SentinelDroneEntity(world, index, cityCenter);
    }

    @Override
    public LivingEntity createDrone(World world, @Nullable ChunkPos cityCenter) {
        return new DroneEntity(world, cityCenter);
    }

    @Override
    public void addSecurity(ItemStack keyCard, String tag) {
        KeyCardItem.addSecurityTag(keyCard, tag);
    }

    @Override
    public void setDescription(ItemStack keyCard, String description) {
        KeyCardItem.setDescription(keyCard, description);
    }

    @Override
    public void fixNetworks(World world, BlockPos pos) {
        PowerSenderSupport.fixNetworks(world, pos);
    }

    @Override
    public RailShape getBeamDirection(BlockState state) {
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

    private SoldierEntity createSoldierInt(World world, BlockPos p, Direction facing, @Nullable ChunkPos center, SoldierBehaviourType behaviourType,
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
        world.addEntity(entity);
        return entity;
    }


}
