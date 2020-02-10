package mcjty.ariente.blocks.generators;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.IAlarmMode;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

public class NegariteTankTile extends GenericTileEntity implements IGuiTile, IAlarmMode {

    public static final BooleanProperty UPPER = BooleanProperty.create("upper");
    public static final BooleanProperty LOWER = BooleanProperty.create("lower");

    public NegariteTankTile() {
        super(Registration.NEGARITE_TANK_TILE.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.negarite_tank")
                .tileEntitySupplier(NegariteTankTile::new)
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

    @Override
    public ActionResultType onBlockActivated(BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        Ariente.guiHandler.openHoloGui(world, pos, player);
        return ActionResultType.SUCCESS;
    }

    public boolean isWorking() {
        BlockPos p = pos.down();
        BlockState state = world.getBlockState(p);
        while (state.getBlock() == Registration.NEGARITE_TANK.get()) {
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

    // @todo 1.14
//    @Override
//    public BlockState getActualState(BlockState state) {
//        return state.withProperty(UPPER, world.getBlockState(pos.up()).getBlock() == ModBlocks.negariteTankBlock)
//                .withProperty(LOWER, world.getBlockState(pos.down()).getBlock() == ModBlocks.negariteTankBlock);
//    }

    // @todo 1.14
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
////        Boolean working = isWorking();
////        if (working) {
////            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
////        }
//    }
//
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
////        if (isWorking()) {
////            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
////        }
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
