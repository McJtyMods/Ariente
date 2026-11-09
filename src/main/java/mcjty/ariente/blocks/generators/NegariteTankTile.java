package mcjty.ariente.blocks.generators;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class NegariteTankTile extends ArienteBlockEntity implements IGuiTile, IAlarmMode {

    public NegariteTankTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.NEGARITE_TANK_TILE.get(), pos, state);
    }

    public static BaseBlock createBlock(BlockBehaviour.Properties properties) {
        return new TankBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(NegariteTankTile::new)
        );
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    public boolean isWorking() {
        BlockPos p = worldPosition.below();
        BlockState state = level.getBlockState(p);
        while (state.getBlock() == mcjty.ariente.setup.Registration.NEGARITE_TANK.get()) {
            p = p.below();
            state = level.getBlockState(p);
        }
        BlockEntity te = level.getBlockEntity(p);
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
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        IPanel panel = registry.panel(0, 0, 8, 8);
        for (int i = 0 ; i < 64 ; i++) {
            switch (i % 3) {
                case 0:
                    panel.add(registry.text(i % 8, i / 8, 1, 1).text("W").color(registry.color(StyledColor.INFORMATION)));
                    break;
                case 1:
                    panel.add(registry.icon(i % 8, i / 8, 1, 1).icon(registry.image(128+64, 128)));
                    break;
                case 2:
                    panel.add(registry.stackIcon(i % 8, i / 8, 1, 1).itemStack(new ItemStack(mcjty.ariente.setup.Registration.NEGARITE_GENERATOR.get())));
                    break;
            }
        }
        return panel;
    }

    @Override
    public void syncToClient() {

    }
}
