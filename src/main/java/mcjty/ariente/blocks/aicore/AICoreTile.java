package mcjty.ariente.blocks.aicore;

import mcjty.ariente.api.IAICoreTile;
import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityAISystem;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class AICoreTile extends GenericTileEntity implements ITickable, IAlarmMode, IAICoreTile {

    private ChunkPos cityCenter;
    private int tickCounter = 10;

    @Override
    public void update() {
        if (!world.isRemote) {
            if (tickCounter > 0) {
                tickCounter--;
                return;
            }
            tickCounter = 10;

            // @todo check if Ariente World is there!
            if (getCityCenter() != null) {
                ICityAISystem cityAISystem = ArienteWorldCompat.getCityAISystem(world);
                ICityAI cityAI = cityAISystem.getCityAI(cityCenter);
                if (cityAI.tick(this)) {
                    cityAISystem.saveSystem();
                }
            }
        }
    }

    @Override
    public boolean isHighAlert() {
        return true;
    }

    private ChunkPos getCityCenter() {
        if (cityCenter == null) {
            cityCenter = BlockPosTools.getChunkCoordFromPos(pos);
            // @todo check if Ariente World is there!
            cityCenter = ArienteWorldCompat.getArienteWorld().getNearestCityCenter(cityCenter);
        }
        return cityCenter;
    }

    @Override
    public void onBlockBreak(World world, BlockPos pos, IBlockState state) {
        super.onBlockBreak(world, pos, state);
        if (!this.world.isRemote) {
            if (getCityCenter() != null) {
                ICityAISystem cityAISystem = ArienteWorldCompat.getCityAISystem(world);
                ICityAI cityAI = cityAISystem.getCityAI(cityCenter);
                cityAI.breakAICore(world, pos);
            }
        }
    }

    @Override
    public BlockPos getCorePos() {
        return getPos();
    }
}
