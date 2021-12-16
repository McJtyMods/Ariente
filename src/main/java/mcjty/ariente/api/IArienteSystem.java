package mcjty.ariente.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IArienteSystem {

    @Nonnull
    List<? extends ISoldier> getSoldiersWithinAABB(Level world, AABB aabb);

    @Nonnull
    List<? extends IFluxLevitatorEntity> getLevitatorsWithinAABB(Level world, AABB aabb);

    LivingEntity createSoldier(Level world, BlockPos pos, Direction facing, @Nullable ChunkPos cityCenter, SoldierBehaviourType type, boolean master);

    Class<? extends LivingEntity> getSoldierClass();
    Class<? extends LivingEntity> getMasterSoldierClass();

    LivingEntity createSentinel(Level world, int index, @Nullable ChunkPos cityCenter);

    Entity createFluxLevitatorEntity(Level world, double x, double y, double z);

    LivingEntity createDrone(Level world, @Nullable ChunkPos cityCenter);

    void addSecurity(ItemStack keyCard, String tag);

    void setDescription(ItemStack keyCard, String description);

    void fixNetworks(Level world, BlockPos pos);

    RailShape getBeamDirection(BlockState state);

    boolean hasWorkingUpgrade(ItemStack armor, ArmorUpgradeType type);

    ISecuritySystem getSecuritySystem(Level world);

    IRedstoneChannels getRedstoneChannels(Level world);
}