package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.setup.Registration;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.Icons;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.hologui.api.Icons.*;

public class OutputItemNodeTile extends AbstractItemNodeTile {

    private ItemStackList outputFilter = ItemStackList.create(FILTER_AMOUNT);

    private boolean outputOredict = false;
    private boolean outputDamage = false;
    private boolean outputNbt = false;
    private int outputStackSize = 1;

    public OutputItemNodeTile() {
        super(Registration.OUTPUT_ITEM_TILE.get());
    }

// @todo 1.14
//    @Override
//    public Block getBlockType() {
//        return ModBlocks.outputItemNode.get();
//    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
//        readBufferFromNBT(tagCompound, "output", outputFilter);
        CompoundNBT info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            outputDamage = info.getBoolean("outDamage");
            outputNbt = info.getBoolean("outNBT");
            outputOredict = info.getBoolean("outOre");
            outputStackSize = info.getInt("outSS");
        }
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
//        writeBufferToNBT(tagCompound, "output", outputFilter);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putBoolean("outDamage", outputDamage);
        info.putBoolean("outNBT", outputNbt);
        info.putBoolean("outOre", outputOredict);
        info.putInt("outSS", outputStackSize);
    }

    public ItemStackList getOutputFilter() {
        return outputFilter;
    }

    public boolean isOutputOredict() {
        return outputOredict;
    }

    public boolean isOutputDamage() {
        return outputDamage;
    }

    public boolean isOutputNbt() {
        return outputNbt;
    }

    public int getOutputStackSize() {
        return outputStackSize;
    }

    public void setOutputStackSize(int outputStackSize) {
        this.outputStackSize = outputStackSize;
    }

    private void changeOutputStackSize(int d) {
        outputStackSize += d;
        if (outputStackSize < 1) {
            outputStackSize = 1;
        } else if (outputStackSize > 64) {
            outputStackSize = 64;
        }
        notifyField();
    }

    private SimpleItemHandler outputHandler = null;

    private SimpleItemHandler getOutputHandler() {
        if (outputHandler == null) {
            outputHandler = new SimpleItemHandler(outputFilter);
        }
        return outputHandler;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        final Pair<String, String> pair = getSlotTag(tag);

        if (TAG_HELP.equals(pair.getRight())) {
            return HoloGuiTools.createHelpGui(registry,
                    HelpBuilder.create()
                            .line("This node can be used in")
                            .line("an automation field to transfer")
                            .line("items"),
                    pair.getLeft() + ":" + TAG_MAIN
            );
        } else {
            return createOutputGui(pair, registry);
        }
    }

    private IGuiComponent<?> createOutputGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(3.3, -.6, 1, 1).text("Output").color(registry.color(StyledColor.LABEL)))

                .add(registry.iconToggle(0.5, 0.4, 1, 1)
                        .getter(player -> outputNbt)
                        .hitEvent((component, player, entity, x, y) -> toggleOutputNBT())
                        .icon(registry.image(Icons.NBT_OFF))
                        .selected(registry.image(Icons.NBT_ON)))
                .add(registry.iconToggle(0.5, 1.4, 1, 1)
                        .getter(player -> outputDamage)
                        .hitEvent((component, player, entity, x, y) -> toggleOutputDamage())
                        .icon(registry.image(Icons.DAM_OFF))
                        .selected(registry.image(Icons.DAM_ON)))
                .add(registry.iconToggle(0.5, 2.4, 1, 1)
                        .getter(player -> outputOredict)
                        .hitEvent((component, player, entity, x, y) -> toggleOutputOre())
                        .icon(registry.image(Icons.ORE_OFF))
                        .selected(registry.image(Icons.ORE_ON)))

                .add(registry.number(5, 3.4, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> getOutputStackSize()))

                .add(registry.iconButton(3, 3.3, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeOutputStackSize(-8)))
                .add(registry.iconButton(4, 3.3, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, entity1, x, y) -> changeOutputStackSize(-1)))
                .add(registry.iconButton(6.6, 3.3, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeOutputStackSize(1)))
                .add(registry.iconButton(7.6, 3.3, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, entity1, x, y) -> changeOutputStackSize(8)))

                .add(registry.slots(2.5, 1.2, 6, 2)
                        .name("slots")
                        .fullBright()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> removeFromFilter(player, entity, getOutputHandler()))
                        .itemHandler(getOutputHandler()))

                .add(registry.playerInventory(4.7)
                        .name("playerSlots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> addToFilter(player, entity, getOutputHandler())));

        addFilterChoice(registry, panel, 0, false);
        addFilterChoice(registry, panel, 1, false);
        addFilterChoice(registry, panel, 2, true);
        addFilterChoice(registry, panel, 3, true);
        return panel;
    }

    private void toggleOutputNBT() {
        outputNbt = !outputNbt;
        notifyField();
    }

    private void toggleOutputDamage() {
        outputDamage = !outputDamage;
        notifyField();
    }

    private void toggleOutputOre() {
        outputOredict = !outputOredict;
        notifyField();
    }
}
