package mcjty.ariente.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;

public interface ICityAI {

    void breakAICore(Level world, BlockPos pos);

    /**
     * Return true if we potentially have to save the city system state.
     * Parameter should be an AICoreTile
     */
    boolean tick(BlockEntity tile);

    boolean isDead(Level world);

    BlockPos requestNewSoldierPosition(Level world, LivingEntity currentTarget);

    BlockPos requestNewDronePosition(Level world, LivingEntity currentTarget);

    BlockPos requestNewSentinelPosition(Level world, int sentinelId);

    void playerSpotted(Player player);

    void alertCity(Player player);

    String getKeyId();

    String getStorageKeyId();

    String getForcefieldId();

    String getCityName();

    void fillLoot(IStorageTile te);
}
