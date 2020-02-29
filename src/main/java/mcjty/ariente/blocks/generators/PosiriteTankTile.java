package mcjty.ariente.blocks.generators;

import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import static mcjty.ariente.blocks.BlockProperties.LOWER;
import static mcjty.ariente.blocks.BlockProperties.UPPER;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class PosiriteTankTile extends GenericTileEntity implements IGuiTile, IAlarmMode {

    public PosiriteTankTile() {
        super(Registration.POSIRITE_TANK_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.posirite_tank")
                .topDriver(DRIVER)
                .tileEntitySupplier(PosiriteTankTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }

            @Override
            protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
                super.fillStateContainer(builder);
                builder.add(UPPER).add(LOWER);
            }
        };
    }


    public boolean isWorking() {
        BlockPos p = pos.down();
        BlockState state = world.getBlockState(p);
        while (state.getBlock() == Registration.POSIRITE_TANK.get()) {
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

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.withProperty(UPPER, world.getBlockState(pos.up()).getBlock() == ModBlocks.posiriteTankBlock.get())
//                .withProperty(LOWER, world.getBlockState(pos.down()).getBlock() == ModBlocks.posiriteTankBlock.get());
//    }

    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }

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
