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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

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
    public List<? extends ISoldier> getSoldiersWithinAABB(Level world, AABB aabb) {
        return world.getEntitiesOfClass(SoldierEntity.class, aabb);
    }

    @Nonnull
    @Override
    public List<? extends IFluxLevitatorEntity> getLevitatorsWithinAABB(Level world, AABB aabb) {
        return world.getEntitiesOfClass(FluxLevitatorEntity.class, aabb);
    }

    @Override
    public LivingEntity createSoldier(Level world, BlockPos pos, Direction facing, @Nullable ChunkPos cityCenter, SoldierBehaviourType type, boolean master) {
        return createSoldierInt(world, pos, facing, cityCenter, type, master);
    }

    @Override
    public LivingEntity createSentinel(Level world, int index, @Nullable ChunkPos cityCenter) {
        return SentinelDroneEntity.create(world, index, cityCenter);
    }

    @Override
    public LivingEntity createDrone(Level world, @Nullable ChunkPos cityCenter) {
        return DroneEntity.create(world, cityCenter);
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
    public void fixNetworks(Level world, BlockPos pos) {
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
    public ISecuritySystem getSecuritySystem(Level world) {
        return SecuritySystem.getSecuritySystem(world);
    }

    @Override
    public IRedstoneChannels getRedstoneChannels(Level world) {
        return RedstoneChannels.getChannels(world);
    }

    @Override
    public Entity createFluxLevitatorEntity(Level world, double x, double y, double z) {
        return FluxLevitatorEntity.create(world, x, y, z);
    }

    private SoldierEntity createSoldierInt(Level world, BlockPos p, Direction facing, @Nullable ChunkPos center, SoldierBehaviourType behaviourType,
                                           boolean master) {
        SoldierEntity entity;
        if (master) {
            entity = MasterSoldierEntity.create(world, center, behaviourType);
        } else {
            entity = SoldierEntity.create(world, center, behaviourType);
        }
        entity.setPos(p.getX()+.5, p.getY(), p.getZ()+.5);
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
        entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), yaw, 0);
        world.addFreshEntity(entity);
        return entity;
    }


}
