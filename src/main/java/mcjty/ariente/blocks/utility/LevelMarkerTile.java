package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class LevelMarkerTile extends GenericTileEntity implements IGuiTile {

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        // @todo implement this
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(0, 2, 1, 1).text("Floor name (WIP)").color(registry.color(StyledColor.LABEL)));
    }

    public LevelMarkerTile() {
        super(Registration.LEVEL_MARKER_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.level_marker")
                .topDriver(DRIVER)
                .tileEntitySupplier(LevelMarkerTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        };
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGui(world, pos, player);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void syncToClient() {

    }
}
