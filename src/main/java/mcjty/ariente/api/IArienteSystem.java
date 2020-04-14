package mcjty.ariente.api;

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

public interface IArienteSystem {

    @Nonnull
    List<? extends ISoldier> getSoldiersWithinAABB(World world, AxisAlignedBB aabb);

    @Nonnull
    List<? extends IFluxLevitatorEntity> getLevitatorsWithinAABB(World world, AxisAlignedBB aabb);

    LivingEntity createSoldier(World world, BlockPos pos, Direction facing, @Nullable ChunkPos cityCenter, SoldierBehaviourType type, boolean master);

    Class<? extends LivingEntity> getSoldierClass();
    Class<? extends LivingEntity> getMasterSoldierClass();

    LivingEntity createSentinel(World world, int index, @Nullable ChunkPos cityCenter);

    Entity createFluxLevitatorEntity(World world, double x, double y, double z);

    LivingEntity createDrone(World world, @Nullable ChunkPos cityCenter);

    void addSecurity(ItemStack keyCard, String tag);

    void setDescription(ItemStack keyCard, String description);

    void fixNetworks(World world, BlockPos pos);

    RailShape getBeamDirection(BlockState state);

    boolean hasWorkingUpgrade(ItemStack armor, ArmorUpgradeType type);

    ISecuritySystem getSecuritySystem(World world);

    IRedstoneChannels getRedstoneChannels(World world);
}