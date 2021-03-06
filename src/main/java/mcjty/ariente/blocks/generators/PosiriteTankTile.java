package mcjty.ariente.blocks.generators;

import mcjty.ariente.api.IAlarmMode;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.hologui.api.StyledColor;
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

public class PosiriteTankTile extends GenericTileEntity implements IGuiTile, IAlarmMode {

    public static final PropertyBool UPPER = PropertyBool.create("upper");
    public static final PropertyBool LOWER = PropertyBool.create("lower");

    public boolean isWorking() {
        BlockPos p = pos.down();
        IBlockState state = world.getBlockState(p);
        while (state.getBlock() == ModBlocks.posiriteTankBlock) {
            p = p.down();
            state = world.getBlockState(p);
        }
        TileEntity te = world.getTileEntity(p);
        if (te instanceof PosiriteGeneratorTile) {
            return ((PosiriteGeneratorTile)te).isWorking();
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
        return state.withProperty(UPPER, world.getBlockState(pos.up()).getBlock() == ModBlocks.posiriteTankBlock)
                .withProperty(LOWER, world.getBlockState(pos.down()).getBlock() == ModBlocks.posiriteTankBlock);
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
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
            .add(registry.text(0, 0, 1, 1).text("0").color(registry.color(StyledColor.INFORMATION)))
            .add(registry.text(1, 0, 1, 1).text("1").color(registry.color(StyledColor.INFORMATION)))
            .add(registry.text(2, 0, 1, 1).text("2").color(registry.color(StyledColor.INFORMATION)))
            .add(registry.text(3, 0, 1, 1).text("3").color(registry.color(StyledColor.INFORMATION)))
            .add(registry.text(4, 0, 1, 1).text("4").color(registry.color(StyledColor.INFORMATION)))
            .add(registry.text(5, 0, 1, 1).text("5").color(registry.color(StyledColor.INFORMATION)))
            .add(registry.text(6, 0, 1, 1).text("6").color(registry.color(StyledColor.INFORMATION)))
            .add(registry.text(7, 0, 1, 1).text("7").color(registry.color(StyledColor.INFORMATION)))
            .add(registry.text(0, 1, 1, 1).text("1").color(0x00ff00))
            .add(registry.text(0, 2, 1, 1).text("2").color(0x00ff00))
            .add(registry.text(0, 3, 1, 1).text("3").color(0x00ff00))
            .add(registry.text(0, 4, 1, 1).text("4").color(0x00ff00))
            .add(registry.text(0, 5, 1, 1).text("5").color(0x00ff00))
            .add(registry.text(0, 6, 1, 1).text("6").color(0x00ff00))
            .add(registry.text(0, 7, 1, 1).text("7").color(0x00ff00))
            .add(registry.text(7, 7, 1, 1).text("X").color(0xff0000));
    }

    @Override
    public void syncToClient() {

    }
}
