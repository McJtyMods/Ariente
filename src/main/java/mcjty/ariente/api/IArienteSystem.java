package mcjty.ariente.api;

import mcjty.lib.varia.ChunkCoord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IArienteSystem {

    @Nonnull
    List<? extends ISoldier> getSoldiersWithinAABB(World world, AxisAlignedBB aabb);

    @Nonnull
    List<? extends IFluxLevitatorEntity> getLevitatorsWithinAABB(World world, AxisAlignedBB aabb);

    EntityLivingBase createSoldier(World world, BlockPos pos, EnumFacing facing, @Nullable ChunkCoord cityCenter, SoldierBehaviourType type, boolean master);
}
