package mcjty.ariente.entities;

import mcjty.ariente.varia.ChunkCoord;
import net.minecraft.world.World;

public class MasterSoldierEntity extends SoldierEntity {

    public MasterSoldierEntity(World worldIn) {
        super(worldIn);
    }

    public MasterSoldierEntity(World world, ChunkCoord cityCenter, SoldierBehaviourType behaviourType) {
        super(world, cityCenter, behaviourType);
    }

    @Override
    protected boolean isMaster() {
        return true;
    }
}
