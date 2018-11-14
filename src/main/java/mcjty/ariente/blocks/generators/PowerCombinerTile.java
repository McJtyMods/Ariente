package mcjty.ariente.blocks.generators;

import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.power.PowerSystem;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.tileentity.GenericTileEntity;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static mcjty.hologui.api.Icons.*;

public class PowerCombinerTile extends GenericTileEntity implements ITickable, IPowerReceiver, IGuiTile {

    private long usingPower = 0;

    private int powerTransfer = 100;

    @Override
    public void update() {
        if (!world.isRemote) {
            usingPower = 0;
            long desiredPower = powerTransfer;
            if (!PowerReceiverSupport.consumePower(world, pos, desiredPower)) {
            } else {
                usingPower += desiredPower;
                sendPower(desiredPower);
            }
        }
    }

    private void sendPower(long power) {
        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);
        for (EnumFacing value : EnumFacing.VALUES) {
//            powerSystem.addPower(powerBlobSupport.getCableId(), POWERGEN * cnt, PowerType.NEGARITE);

        }
    }


    public int getPowerTransfer() {
        return powerTransfer;
    }

    public void setPowerTransfer(int powerTransfer) {
        this.powerTransfer = powerTransfer;
        markDirtyClient();
    }

    private void changeTransfer(int dy) {
        powerTransfer += dy;
        if (powerTransfer < 0) {
            powerTransfer = 0;
        } else if (powerTransfer > 20000) { // @todo configurable
            powerTransfer = 20000;
        }
        markDirtyClient();
    }


    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        powerTransfer = tagCompound.getInteger("transfer");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("transfer", powerTransfer);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound = super.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        probeInfo.text(TextStyleClass.LABEL + "Using: " + TextStyleClass.INFO + usingPower + " flux");
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
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 2, 1, 1).text("Transfer max").color(0xaaccff))
                .add(registry.number(3, 4, 1, 1).color(0xffffff).getter((p,h) -> getPowerTransfer()))

                .add(registry.iconButton(1, 4, 1, 1).icon(GRAY_DOUBLE_ARROW_LEFT).hover(WHITE_DOUBLE_ARROW_LEFT)
                        .hitEvent((component, player, entity1, x, y) -> changeTransfer(-50)))
                .add(registry.iconButton(2, 4, 1, 1).icon(GRAY_ARROW_LEFT).hover(WHITE_ARROW_LEFT)
                        .hitEvent((component, player, entity1, x, y) -> changeTransfer(-1)))
                .add(registry.iconButton(5, 4, 1, 1).icon(GRAY_ARROW_RIGHT).hover(WHITE_ARROW_RIGHT)
                        .hitEvent((component, player, entity1, x, y) -> changeTransfer(1)))
                .add(registry.iconButton(6, 4, 1, 1).icon(GRAY_DOUBLE_ARROW_RIGHT).hover(WHITE_DOUBLE_ARROW_RIGHT)
                        .hitEvent((component, player, entity1, x, y) -> changeTransfer(50)))
                ;
    }

    @Override
    public void syncToClient() {

    }
}
