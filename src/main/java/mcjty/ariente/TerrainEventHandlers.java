package mcjty.ariente;

import mcjty.ariente.config.WorldgenConfiguration;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TerrainEventHandlers {

    @SubscribeEvent
    public void onCreateDecorate(DecorateBiomeEvent.Decorate event) {
        World world = event.getWorld();
        if (!world.isRemote && world.provider.getDimension() == WorldgenConfiguration.DIMENSION_ID.get()) {
            switch (event.getType()) {
                case CLAY:
                case DEAD_BUSH:
                case ICE:
                case LAKE_LAVA:
                case ROCK:
                case SAND:
                case SAND_PASS2:
                case CUSTOM:
                case FOSSIL:
                case DESERT_WELL:
                case LAKE_WATER:
                case GRASS:
                case TREE:
                    break;

                case PUMPKIN:
                case SHROOM:
                case BIG_SHROOM:
                case CACTUS:
                case REED:
                case LILYPAD:
                case FLOWERS:
                    event.setResult(Event.Result.DENY);
                    break;
            }
        }
    }
}
