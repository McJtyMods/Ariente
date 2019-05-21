package mcjty.ariente.blocks.aicore;

import mcjty.ariente.api.IAICoreTile;
import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityAISystem;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.BlockPosTools;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class AICoreTile extends GenericTileEntity implements ITickable, IAlarmMode, IAICoreTile {

    private ChunkPos cityCenter;
    private int tickCounter = 10;
    private String cityName = "";

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
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        probeInfo.text(TextStyleClass.LABEL + "City: " + TextStyleClass.INFO + cityName);
    }

    @Override
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
        currenttip.add(TextFormatting.GRAY + "City: " + TextFormatting.BLUE + cityName);
    }

    @Override
    public BlockPos getCorePos() {
        return getPos();
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        cityName = tagCompound.getString("cityName");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setString("cityName", cityName);
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
