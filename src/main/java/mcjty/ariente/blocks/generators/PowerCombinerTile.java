package mcjty.ariente.blocks.generators;

import mcjty.ariente.cables.CableColor;
import mcjty.ariente.cables.ConnectorTileEntity;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.power.IPowerReceiver;
import mcjty.ariente.power.PowerReceiverSupport;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
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
            if (PowerReceiverSupport.consumePower(world, pos, powerTransfer, false)) {
                usingPower += powerTransfer;
                sendPower();
            }
        }
    }

    private int getPowerToTransfer() {
        return (int) (powerTransfer * .98); // @todo configurable cost
    }

    private void sendPower() {

        int power = getPowerTransfer();

        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);
        int cnt = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos p = pos.offset(facing);
            TileEntity te = world.getTileEntity(p);
            if (te instanceof ConnectorTileEntity) {
                ConnectorTileEntity connector = (ConnectorTileEntity) te;
                if (connector.getCableColor() == CableColor.COMBINED) {
                    cnt++;
                }
            }
        }

        if (cnt > 0) {
            long pPerConnector = power / cnt;
            long p = pPerConnector + power % cnt;
            for (EnumFacing facing : EnumFacing.VALUES) {
                TileEntity te = world.getTileEntity(pos.offset(facing));
                if (te instanceof ConnectorTileEntity) {
                    ConnectorTileEntity connector = (ConnectorTileEntity) te;
                    if (connector.getCableColor() == CableColor.COMBINED) {
                        powerSystem.addPower(connector.getCableId(), p, PowerType.NEGARITE);
                        powerSystem.addPower(connector.getCableId(), p, PowerType.POSIRITE);
                        p = pPerConnector;
                    }
                }
            }
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
        if (TAG_HELP.equals(tag)) {
            return HoloGuiTools.createHelpGui(registry,
                    HelpBuilder.create()
                            .line("This block can combine negarite")
                            .line("and posirite power and send it")
                            .line("out through a combined cable")
                            .line("Note that there is a small loss")
                            .line("associated with this operation")
                            .nl()
                            .line("Use the controls to change the")
                            .line("maximum transfer rate")
            );
        } else {
            return createMainGui(registry);
        }
    }

    private IGuiComponent<?> createMainGui(IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry)
                .add(registry.text(0, 1, 1, 1).text("Transfer").color(0xaaccff))
                .add(registry.text(0, 2.7, 1, 1).text("In").color(0xaaccff).scale(.7f))
                .add(registry.number(2, 2.5, 1, 1).color(0xffffff).getter((p,h) -> getPowerTransfer()))
                .add(registry.text(0, 3.7, 1, 1).text("Out").color(0xaaccff).scale(.7f))
                .add(registry.number(2, 3.5, 1, 1).color(0xffffff).getter((p,h) -> getPowerToTransfer()))

                .add(registry.iconButton(1, 6, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeTransfer(-50)))
                .add(registry.iconButton(2, 6, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeTransfer(-1)))
                .add(registry.iconButton(5, 6, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeTransfer(1)))
                .add(registry.iconButton(6, 6, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeTransfer(50)))
                ;
    }

    @Override
    public void syncToClient() {

    }
}
