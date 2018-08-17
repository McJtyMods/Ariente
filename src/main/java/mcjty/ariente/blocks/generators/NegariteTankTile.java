package mcjty.ariente.blocks.generators;

import mcjty.ariente.ai.IAlarmMode;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class NegariteTankTile extends GenericTileEntity implements IGuiTile, IAlarmMode {

    public static final PropertyBool UPPER = PropertyBool.create("upper");
    public static final PropertyBool LOWER = PropertyBool.create("lower");

    public boolean isWorking() {
        BlockPos p = pos.down();
        IBlockState state = world.getBlockState(p);
        while (state.getBlock() == ModBlocks.negariteTankBlock) {
            p = p.down();
            state = world.getBlockState(p);
        }
        TileEntity te = world.getTileEntity(p);
        if (te instanceof NegariteGeneratorTile) {
            return ((NegariteGeneratorTile)te).isWorking();
        }
        return false;
    }

    @Override
    public boolean isHighAlert() {
        return false;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        return state.withProperty(UPPER, world.getBlockState(pos.up()).getBlock() == ModBlocks.negariteTankBlock)
                .withProperty(LOWER, world.getBlockState(pos.down()).getBlock() == ModBlocks.negariteTankBlock);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        Boolean working = isWorking();
//        if (working) {
//            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }


    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (isWorking()) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    public IGuiComponent createGui(HoloGuiEntity entity, String tag) {
        return new HoloPanel(0, 0, 8, 8)
            .add(new HoloText(0, 0, 1, 1, "0", 0xffffff))
            .add(new HoloText(1, 0, 1, 1, "1", 0xffffff))
            .add(new HoloText(2, 0, 1, 1, "2", 0xffffff))
            .add(new HoloText(3, 0, 1, 1, "3", 0xffffff))
            .add(new HoloText(4, 0, 1, 1, "4", 0xffffff))
            .add(new HoloText(5, 0, 1, 1, "5", 0xffffff))
            .add(new HoloText(6, 0, 1, 1, "6", 0xffffff))
            .add(new HoloText(7, 0, 1, 1, "7", 0xffffff))
            .add(new HoloText(0, 1, 1, 1, "1", 0x00ff00))
            .add(new HoloText(0, 2, 1, 1, "2", 0x00ff00))
            .add(new HoloText(0, 3, 1, 1, "3", 0x00ff00))
            .add(new HoloText(0, 4, 1, 1, "4", 0x00ff00))
            .add(new HoloText(0, 5, 1, 1, "5", 0x00ff00))
            .add(new HoloText(0, 6, 1, 1, "6", 0x00ff00))
            .add(new HoloText(0, 7, 1, 1, "7", 0x00ff00))
            .add(new HoloText(7, 7, 1, 1, "X", 0xff0000));
    }

    @Override
    public void syncToClient() {

    }
}
