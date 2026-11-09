package mcjty.ariente.blocks.generators;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class PosiriteTankTile extends ArienteBlockEntity implements IGuiTile, IAlarmMode {

    public PosiriteTankTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.POSIRITE_TANK_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new TankBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(PosiriteTankTile::new)
        );
    }

    public boolean isWorking() {
        BlockPos p = worldPosition.below();
        BlockState state = level.getBlockState(p);
        while (state.getBlock() == mcjty.ariente.setup.Registration.POSIRITE_TANK.get()) {
            p = p.below();
            state = level.getBlockState(p);
        }
        BlockEntity te = level.getBlockEntity(p);
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
            .add(registry.text(0, 7, 1, 1).text("7").color(0x00ff00));
    }

    @Override
    public void syncToClient() {

    }
}
