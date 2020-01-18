package mcjty.ariente.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICityAI {

    void breakAICore(World world, BlockPos pos);

    /**
     * Return true if we potentially have to save the city system state.
     * Parameter should be an AICoreTile
     */
    boolean tick(TileEntity tile);

    boolean isDead(World world);

    BlockPos requestNewSoldierPosition(World world, LivingEntity currentTarget);

    BlockPos requestNewDronePosition(World world, LivingEntity currentTarget);

    BlockPos requestNewSentinelPosition(World world, int sentinelId);

    void playerSpotted(PlayerEntity player);

    void alertCity(PlayerEntity player);

    String getKeyId();

    String getStorageKeyId();

    String getForcefieldId();

    String getCityName();

    void fillLoot(IStorageTile te);
}
