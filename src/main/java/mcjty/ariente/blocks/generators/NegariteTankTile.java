package mcjty.ariente.blocks.generators;

import mcjty.ariente.api.IAlarmMode;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.components.IPanel;
import mcjty.ariente.blocks.ModBlocks;
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
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        IPanel panel = registry.panel(0, 0, 8, 8);
        for (int i = 0 ; i < 64 ; i++) {
            switch (i % 3) {
                case 0:
                    panel.add(registry.text(i % 8, i / 8, 1, 1).text("W").color(0xffffff));
                    break;
                case 1:
                    panel.add(registry.icon(i % 8, i / 8, 1, 1).icon(registry.image(128+64, 128)));
                    break;
                case 2:
                    panel.add(registry.stackIcon(i % 8, i / 8, 1, 1).itemStack(new ItemStack(ModBlocks.negariteGeneratorBlock)));
                    break;
            }
        }
        return panel;
    }

    @Override
    public void syncToClient() {

    }
}
