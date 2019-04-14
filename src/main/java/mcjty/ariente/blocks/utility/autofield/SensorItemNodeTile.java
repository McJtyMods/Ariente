package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class SensorItemNodeTile extends AbstractNodeTile {

    public static IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        NodeOrientation orientation = getOrientationFromPlacement(facing, hitX, hitY, hitZ);
        // Since this is a multipart we can use state that isn't convertable to metadata
        return ModBlocks.sensorItemNode.getDefaultState().withProperty(ORIENTATION, orientation);
    }


    @Override
    public Block getBlockType() {
        return ModBlocks.sensorItemNode;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        final Pair<String, String> pair = getSlotTag(tag);

        if (TAG_HELP.equals(pair.getRight())) {
            return HoloGuiTools.createHelpGui(registry,
                    HelpBuilder.create()
                            .line("This node can be used in")
                            .line("an automation field to detect")
                            .line("items"),
                    pair.getLeft() + ":" + TAG_MAIN
            );
        } else {
            return createSensorGui(pair, registry);
        }
    }

    private IGuiComponent<?> createSensorGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(2.3, -.2, 1, 1).text("Sensor").color(0xaaccff))

//                .add(registry.slots(2.5, 1.2, 6, 2)
//                        .name("slots")
//                        .fullBright()
//                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> removeFromFilter(player, entity, getInputHandler()))
//                        .itemHandler(getInputHandler()))
//
//                .add(registry.playerInventory(4.7)
//                        .name("playerSlots")
//                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> addToFilter(player, entity, getInputHandler())))
                ;
    }
}
