package mcjty.ariente.blocks.generators;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IAlarmMode;
import mcjty.ariente.blocks.BlockProperties;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;

public class NegariteTankTile extends GenericTileEntity implements IGuiTile, IAlarmMode {

    public NegariteTankTile() {
        super(Registration.NEGARITE_TANK_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(key("message.ariente.shiftmessage"))
                .infoShift(header())
                .topDriver(DRIVER)
                .tileEntitySupplier(NegariteTankTile::new)
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }

            @Override
            protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(BlockProperties.UPPER).add(BlockProperties.LOWER);
            }
        };
    }

    @Override
    public InteractionResult onBlockActivated(BlockState state, Player player, InteractionHand hand, BlockHitResult result) {
        Ariente.guiHandler.openHoloGui(level, worldPosition, player);
        return InteractionResult.SUCCESS;
    }

    public boolean isWorking() {
        BlockPos p = worldPosition.below();
        BlockState state = level.getBlockState(p);
        while (state.getBlock() == Registration.NEGARITE_TANK.get()) {
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

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.with(UPPER, world.getBlockState(pos.up()).getBlock() == ModBlocks.negariteTankBlock)
//                .with(LOWER, world.getBlockState(pos.down()).getBlock() == ModBlocks.negariteTankBlock);
//    }

    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }

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
                    panel.add(registry.stackIcon(i % 8, i / 8, 1, 1).itemStack(new ItemStack(Registration.NEGARITE_GENERATOR.get())));
                    break;
            }
        }
        return panel;
    }

    @Override
    public void syncToClient() {

    }
}
