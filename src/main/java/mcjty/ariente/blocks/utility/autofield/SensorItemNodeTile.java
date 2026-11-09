package mcjty.ariente.blocks.utility.autofield;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.Item;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.StepButton;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IIconChoice;
import mcjty.hologui.api.components.IPanel;
import mcjty.hologui.api.components.ITextChoice;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.multipart.PartPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

public class SensorItemNodeTile extends AbstractNodeTile {

    private DyeColor[] outputColor = new DyeColor[] { DyeColor.WHITE };
    private int operator = 0;
    private int amount = 0;

    private static String[] operators = new String[] { "<",  "<=", ">", ">=", "=", "<>" };

    public SensorItemNodeTile(BlockPos pos, BlockState state) {
        super(mcjty.ariente.setup.Registration.SENSOR_ITEM_TILE.get(), pos, state);
    }

    public static BaseNodeBlock createBlock(BlockBehaviour.Properties properties) {
        return new BaseNodeBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .tileEntitySupplier(SensorItemNodeTile::new));
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
            for (int i = 0 ; i < h.size() ; i++) {
                cnt += ItemUtil.getStack(h, i).getCount();
                Boolean earlyCheck = checkOperatorEarly(cnt);
                if (earlyCheck != null) {
                    return earlyCheck;
                }
            }
            return checkOperator(cnt);
        }).orElse(false);
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        CompoundTag info = tagCompound.getCompoundOrEmpty("Info");
        if (!info.isEmpty()) {
            if (info.contains("outColor")) {
                outputColor[0] = DyeColor.values()[info.getIntOr("outColor", 0)];
            }
            operator = info.getIntOr("op", 0);
            amount = info.getIntOr("amount", 0);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.saveAdditional(tagCompound, provider);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.putInt("outColor", outputColor[0].ordinal());
        info.putInt("op", operator);
        info.putInt("amount", amount);
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        final Pair<String, String> pair = getSlotTag(tag);

        if (TAG_HELP.equals(pair.getRight())) {
            return GuiHelp.SENSOR.createHelpGui(registry,
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
        amount = (int) Math.max(1L, Math.min(Integer.MAX_VALUE, (long) amount + d));
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

        ITextChoice operatorChoice = registry.textChoice(0, 2.4, 7.2, .8).scale(.55f)
                .getter(player -> operator)
                .hitEvent((component, player, entity, x, y) -> changeOperator());
        for (String op : operators) {
            operatorChoice.addText(op);
        }

        IPanel panel = GuiHelp.SENSOR.createPanel(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(.25, -.6, 3, 1).text("Sensor").color(registry.color(StyledColor.LABEL)))
                .add(registry.text(0, 7, 1, 1).text("Output").color(registry.color(StyledColor.LABEL)))
                .add(outColor)
                .add(operatorChoice)

                .add(new StepButton(0, 4, .6, .6, -10, this::changeAmount).name("sensor_decrease_10"))
                .add(registry.integerSlider(.8, 3.6, 7.2, 1).name("sensor_amount").label("Item threshold")
                        .range(1, Integer.MAX_VALUE).logarithmic(true).getter((p, e) -> amount)
                        .changed((p, e, value) -> changeAmount(value - amount)))
                .add(new StepButton(8.2, 4, .6, .6, 10, this::changeAmount).name("sensor_increase_10"))
                ;

        addFilterConditions(registry, panel);

        return panel;
    }
}
