package mcjty.ariente.blocks.aicore;

import mcjty.ariente.ai.CityAI;
import mcjty.ariente.ai.CityAISystem;
import mcjty.ariente.blocks.utility.AlarmType;
import mcjty.ariente.cities.CityTools;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.items.ModItems;
import mcjty.ariente.varia.ChunkCoord;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

            if (getCityCenter() != null) {
                CityAISystem cityAISystem = CityAISystem.getCityAISystem(world);
                CityAI cityAI = cityAISystem.getCityAI(cityCenter);
                if (cityAI.tick(this)) {
                    cityAISystem.save();
                }
            }
        }
    }

    private ChunkCoord getCityCenter() {
        if (cityCenter == null) {
            cityCenter = ChunkCoord.getChunkCoordFromPos(pos);
            cityCenter = CityTools.getNearestCityCenter(cityCenter.getChunkX(), cityCenter.getChunkZ());
        }
        return cityCenter;
    }

    @Override
    public void onBlockBreak(World workd, BlockPos pos, IBlockState state) {
        super.onBlockBreak(workd, pos, state);
        if (!world.isRemote) {
            if (getCityCenter() != null) {
                CityAISystem cityAISystem = CityAISystem.getCityAISystem(world);
                CityAI cityAI = cityAISystem.getCityAI(cityCenter);
                if (!cityAI.hasValidCoreExcept(world, pos)) {
                    // There are no other valid AI cores. Spawn an item for the player
                    // with the right security key
                    ItemStack stack = new ItemStack(ModItems.keyCardItem);
                    KeyCardItem.addSecurityTag(stack, cityAI.getStorageKeyId());
                    EntityItem entityitem = new EntityItem(this.world, pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5, stack);
                    entityitem.setDefaultPickupDelay();
                    world.spawnEntity(entityitem);
                    cityAI.setAlarmType(world, AlarmType.DEAD);
                }
            }
        }
    }
}
