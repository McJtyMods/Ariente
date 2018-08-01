package mcjty.ariente.blocks.aicore;

import mcjty.ariente.ai.CityAI;
import mcjty.ariente.ai.CityAISystem;
import mcjty.ariente.cities.CityTools;
import mcjty.ariente.varia.ChunkCoord;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.util.ITickable;

public class AICoreTile extends GenericTileEntity implements ITickable {

    private ChunkCoord cityCenter;
    private int tickCounter = 10;

    @Override
    public void update() {
        if (!world.isRemote) {
            if (tickCounter > 0) {
                tickCounter--;
                return;
            }
            tickCounter = 10;

            if (cityCenter == null) {
                cityCenter = ChunkCoord.getChunkCoordFromPos(pos);
                cityCenter = CityTools.getNearestCityCenter(cityCenter.getChunkX(), cityCenter.getChunkZ());
            }

            if (cityCenter != null) {
                CityAI cityAI = CityAISystem.getCityAISystem(world).getCityAI(cityCenter);
                cityAI.tick(this);
            }
        }
    }
}
