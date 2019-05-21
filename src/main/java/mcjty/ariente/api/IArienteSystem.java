package mcjty.ariente.api;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
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

public interface IArienteSystem {

    @Nonnull
    List<? extends ISoldier> getSoldiersWithinAABB(World world, AxisAlignedBB aabb);

    @Nonnull
    List<? extends IFluxLevitatorEntity> getLevitatorsWithinAABB(World world, AxisAlignedBB aabb);

    EntityLivingBase createSoldier(World world, BlockPos pos, EnumFacing facing, @Nullable ChunkPos cityCenter, SoldierBehaviourType type, boolean master);

    Class<? extends EntityLiving> getSoldierClass();
    Class<? extends EntityLiving> getMasterSoldierClass();

    EntityLivingBase createSentinel(World world, int index, @Nullable ChunkPos cityCenter);

    Entity createFluxLevitatorEntity(World world, double x, double y, double z);

    EntityLivingBase createDrone(World world, @Nullable ChunkPos cityCenter);

    void addSecurity(ItemStack keyCard, String tag);

    void setDescription(ItemStack keyCard, String description);

    void fixNetworks(World world, BlockPos pos);

    BlockRailBase.EnumRailDirection getBeamDirection(IBlockState state);

    boolean hasWorkingUpgrade(ItemStack armor, ArmorUpgradeType type);

    ISecuritySystem getSecuritySystem(World world);

    IRedstoneChannels getRedstoneChannels(World world);
}