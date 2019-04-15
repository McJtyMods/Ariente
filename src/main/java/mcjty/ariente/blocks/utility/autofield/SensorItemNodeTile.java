package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IIconChoice;
import mcjty.hologui.api.components.IPanel;
import mcjty.hologui.api.components.ITextChoice;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.hologui.api.Icons.*;
import static mcjty.hologui.api.Icons.WHITE_DOUBLE_ARROW_RIGHT;

public class SensorItemNodeTile extends AbstractNodeTile {

    private EnumDyeColor[] outputColor = new EnumDyeColor[] { EnumDyeColor.WHITE };
    private int operator = 0;
    private int amount = 0;

    private static String[] operators = new String[] { "<",  "<=", ">", ">=", "=", "<>" };

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
        if (tagCompound.hasKey("outColor")) {
            outputColor[0] = EnumDyeColor.values()[tagCompound.getInteger("outColor")];
        }
        operator = tagCompound.getInteger("op");
        amount = tagCompound.getInteger("amount");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("outColor", outputColor[0].ordinal());
        tagCompound.setInteger("op", operator);
        tagCompound.setInteger("amount", amount);
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

    private void changeOperator() {
        operator++;
        if (operator >= operators.length) {
            operator = 0;
        }
        notifyField();
    }

    private void changeAmount(int d) {
        amount += d;
        if (amount < 1) {
            amount = 1;
        }
        notifyField();
    }



    private IGuiComponent<?> createSensorGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        IIconChoice outColor = registry.iconChoice(4, 7, 1, 1)
                .getter(player -> outputColor[0].ordinal()+1)
                .hitEvent(changeColor(outputColor, 0));
        addColors(registry, outColor);

        ITextChoice operatorChoice = registry.textChoice(0, 3.5, 1.3, 1)
                .getter(player -> operator)
                .hitEvent((component, player, entity, x, y) -> changeOperator());
        for (String op : operators) {
            operatorChoice.addText(op);
        }

        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(3.3, -.6, 1, 1).text("Sensor").color(0xaaccff))
                .add(registry.text(0, 7, 1, 1).text("Output").color(0xaaccff))
                .add(outColor)
                .add(operatorChoice)

                .add(registry.number(4, 3.4, 1, 1).color(0xffffff).getter((p, h) -> amount))

                .add(registry.iconButton(2, 3.3, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeAmount(-8)))
                .add(registry.iconButton(3, 3.3, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeAmount(-1)))
                .add(registry.iconButton(6.6, 3.3, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeAmount(1)))
                .add(registry.iconButton(7.6, 3.3, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeAmount(8)))
                ;

        addFilterChoice(registry, panel, 0);
        addFilterChoice(registry, panel, 1);
        addFilterChoice(registry, panel, 2);
        addFilterChoice(registry, panel, 3);


//                .add(registry.slots(2.5, 1.2, 6, 2)
//                        .name("slots")
//                        .fullBright()
//                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> removeFromFilter(player, entity, getInputHandler()))
//                        .itemHandler(getInputHandler()))
//
//                .add(registry.playerInventory(4.7)
//                        .name("playerSlots")
//                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> addToFilter(player, entity, getInputHandler())))
        return panel;
    }
}
