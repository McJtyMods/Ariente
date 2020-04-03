package mcjty.ariente.blocks.generators;

import mcjty.ariente.Ariente;
import mcjty.ariente.cables.CableColor;
import mcjty.ariente.cables.ConnectorTileEntity;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.power.*;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class PowerCombinerTile extends GenericTileEntity implements ITickableTileEntity, IPowerReceiver, IGuiTile, IPowerUser {

    private long usingPower = 0;

    private int powerTransfer = 100;

    public PowerCombinerTile() {
        super(Registration.POWER_COMBINER_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(PowerCombinerTile::new)
            );
        //        powerCombinerBlock = builderFactory.<PowerCombinerTile> builder("power_combiner")
//                .tileEntityClass(PowerCombinerTile.class)
//                .rotationType(BaseBlock.RotationType.ROTATION)
//                .flags(RENDER_SOLID, RENDER_CUTOUT)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .build();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGui(world, pos, player);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void tick() {
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

        int power = getPowerToTransfer();

        PowerSystem powerSystem = PowerSystem.getPowerSystem(world);
        int cnt = 0;
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
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
            for (Direction facing : OrientationTools.DIRECTION_VALUES) {
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        if (info.contains("transfer")) {
            powerTransfer = info.getInt("transfer");
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        tagCompound = super.write(tagCompound);
        getOrCreateInfo(tagCompound).putInt("transfer", powerTransfer);
        return tagCompound;
    }

    @Override
    public long getUsingPower() {
        return usingPower;
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
                .add(registry.text(0, 1, 1, 1).text("Transfer").color(registry.color(StyledColor.LABEL)))
                .add(registry.text(0, 2.7, 1, 1).text("In").color(registry.color(StyledColor.LABEL)).scale(.7f))
                .add(registry.number(2, 2.5, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p,h) -> getPowerTransfer()))
                .add(registry.text(0, 3.7, 1, 1).text("Out").color(registry.color(StyledColor.LABEL)).scale(.7f))
                .add(registry.number(2, 3.5, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p,h) -> getPowerToTransfer()))

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
