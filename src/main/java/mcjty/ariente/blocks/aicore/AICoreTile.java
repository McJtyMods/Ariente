package mcjty.ariente.blocks.aicore;

import mcjty.ariente.api.IAICoreTile;
import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityAISystem;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class AICoreTile extends GenericTileEntity implements ITickableTileEntity, IAlarmMode, IAICoreTile {

    private ChunkPos cityCenter;
    private int tickCounter = 10;
    private String cityName = "";

    public AICoreTile(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public void tick() {
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
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
        super.onReplaced(world, pos, state, newstate);
        if (!this.world.isRemote) {
            if (getCityCenter() != null) {
                ICityAISystem cityAISystem = ArienteWorldCompat.getCityAISystem(world);
                ICityAI cityAI = cityAISystem.getCityAI(cityCenter);
                cityAI.breakAICore(world, pos);
            }
        }
    }

    // @todo 1.14
//    @Override
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        probeInfo.text(TextStyleClass.LABEL + "City: " + TextStyleClass.INFO + cityName);
//    }
//
//    @Override
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//        currenttip.add(TextFormatting.GRAY + "City: " + TextFormatting.BLUE + cityName);
//    }

    @Override
    public BlockPos getCorePos() {
        return getPos();
    }

    // @todo 1.14 LOOT TABLES
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        cityName = tagCompound.getString("cityName");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        tagCompound = super.write(tagCompound);
        tagCompound.putString("cityName", cityName);
        return tagCompound;
    }

    @Override
    public void setCityName(String name) {
        cityName = name;
        markDirtyQuick();
    }

    @Override
    public String getCityName() {
        return cityName;
    }
}
