package mcjty.ariente.api;

import mcjty.ariente.blocks.aicore.AICoreTile;
import mcjty.ariente.blocks.utility.StorageTile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICityAI {

    void breakAICore(World world, BlockPos pos);

    // Return true if we potentially have to save the city system state
    boolean tick(AICoreTile tile);

    boolean isDead(World world);

    BlockPos requestNewSoldierPosition(World world, EntityLivingBase currentTarget);

    BlockPos requestNewDronePosition(World world, EntityLivingBase currentTarget);

    BlockPos requestNewSentinelPosition(World world, int sentinelId);

    void playerSpotted(EntityPlayer player);

    void alertCity(EntityPlayer player);

    String getKeyId();

    String getStorageKeyId();

    String getForcefieldId();

    void fillLoot(StorageTile te);
}
