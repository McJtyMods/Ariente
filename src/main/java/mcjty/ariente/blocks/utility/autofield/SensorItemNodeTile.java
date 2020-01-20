package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IIconChoice;
import mcjty.hologui.api.components.IPanel;
import mcjty.hologui.api.components.ITextChoice;
import mcjty.lib.multipart.PartPos;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.hologui.api.Icons.*;

public class SensorItemNodeTile extends AbstractNodeTile {

    private DyeColor[] outputColor = new DyeColor[] { DyeColor.WHITE };
    private int operator = 0;
    private int amount = 0;

    private static String[] operators = new String[] { "<",  "<=", ">", ">=", "=", "<>" };

    public static BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        NodeOrientation orientation = getOrientationFromPlacement(facing, hitX, hitY, hitZ);
        // Since this is a multipart we can use state that isn't convertable to metadata
        return ModBlocks.sensorItemNode.get().getDefaultState().with(ORIENTATION, orientation);
    }

    public SensorItemNodeTile(TileEntityType<?> type) {
        super(type);
    }

    // Return true if we already know the operator is succesful. Check false if we already know the operator can never succeed.
    // Return null otherwise
    private Boolean checkOperatorEarly(int cnt) {
        switch (operator) {
            case 0: if (cnt >= amount) { return false; } else { return null; }
            case 1: if (cnt > amount) { return false; } else { return null; }
            case 2: if (cnt <= amount) { return null; } else { return true; }
            case 3: if (cnt < amount) { return null; } else { return true; }
            case 4: if (cnt > amount) { return false; } else { return null; }
            case 5: if (cnt > amount) { return true; } else { return null; }
            default: return null;
        }
    }

    private boolean checkOperator(int cnt) {
        switch (operator) {
            case 0: return cnt < amount;
            case 1: return cnt <= amount;
            case 2: return cnt > amount;
            case 3: return cnt >= amount;
            case 4: return cnt == amount;
            case 5: return cnt != amount;
            default: return false;
        }
    }

    public boolean sense(PartPos sensorPos) {
        return getConnectedItemHandler(sensorPos).map(h -> {
            int cnt = 0;
            for (int i = 0 ; i < h.getSlots() ; i++) {
                cnt += h.getStackInSlot(i).getCount();
                Boolean earlyCheck = checkOperatorEarly(cnt);
                if (earlyCheck != null) {
                    return earlyCheck;
                }
            }
            return checkOperator(cnt);
        }).orElse(false);
    }

    // @todo 1.14
//    @Override
//    public Block getBlockType() {
//        return ModBlocks.sensorItemNode;
//    }

    // @todo 1.14 loot
    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        if (tagCompound.contains("outColor")) {
            outputColor[0] = DyeColor.values()[tagCompound.getInt("outColor")];
        }
        operator = tagCompound.getInt("op");
        amount = tagCompound.getInt("amount");
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.putInt("outColor", outputColor[0].ordinal());
        tagCompound.putInt("op", operator);
        tagCompound.putInt("amount", amount);
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

    public DyeColor getOutputColor() {
        return outputColor[0];
    }

    private IGuiComponent<?> createSensorGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        IIconChoice outColor = registry.iconChoice(4, 7, 1, 1)
                .getter(player -> outputColor[0].ordinal()+1)
                .hitEvent(changeColor(outputColor, 0));
        addColors(registry, outColor, false);

        ITextChoice operatorChoice = registry.textChoice(0, 3.5, 1.3, 1)
                .getter(player -> operator)
                .hitEvent((component, player, entity, x, y) -> changeOperator());
        for (String op : operators) {
            operatorChoice.addText(op);
        }

        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(3.3, -.6, 1, 1).text("Sensor").color(registry.color(StyledColor.LABEL)))
                .add(registry.text(0, 7, 1, 1).text("Output").color(registry.color(StyledColor.LABEL)))
                .add(outColor)
                .add(operatorChoice)

                .add(registry.number(4, 3.4, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> amount))

                .add(registry.iconButton(2, 3.3, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeAmount(-8)))
                .add(registry.iconButton(3, 3.3, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeAmount(-1)))
                .add(registry.iconButton(6.6, 3.3, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeAmount(1)))
                .add(registry.iconButton(7.6, 3.3, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeAmount(8)))
                ;

        addFilterChoice(registry, panel, 0, false);
        addFilterChoice(registry, panel, 1, false);
        addFilterChoice(registry, panel, 2, true);
        addFilterChoice(registry, panel, 3, true);

        return panel;
    }
}
