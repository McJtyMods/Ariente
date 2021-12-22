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
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class PowerCombinerTile extends TickingTileEntity implements IPowerReceiver, IGuiTile, IPowerUser {

    private long usingPower = 0;

    private int powerTransfer = 100;

    public PowerCombinerTile(BlockPos pos, BlockState state) {
        super(Registration.POWER_COMBINER_TILE.get(), pos, state);
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
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickServer() {
        usingPower = 0;
        if (PowerReceiverSupport.consumePower(level, worldPosition, powerTransfer, false)) {
            usingPower += powerTransfer;
            sendPower();
        }
    }

    private int getPowerToTransfer() {
        return (int) (powerTransfer * .98); // @todo configurable cost
    }

    private void sendPower() {

        int power = getPowerToTransfer();

        PowerSystem powerSystem = PowerSystem.getPowerSystem(level);
        int cnt = 0;
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            BlockPos p = worldPosition.relative(facing);
            BlockEntity te = level.getBlockEntity(p);
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
                BlockEntity te = level.getBlockEntity(worldPosition.relative(facing));
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
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        CompoundTag info = tagCompound.getCompound("Info");
        if (info.contains("transfer")) {
            powerTransfer = info.getInt("transfer");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        tagCompound = super.save(tagCompound);
        getOrCreateInfo(tagCompound).putInt("transfer", powerTransfer);
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
