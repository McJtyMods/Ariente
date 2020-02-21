package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.Icons;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.nbt.CompoundNBT;
import org.apache.commons.lang3.tuple.Pair;

public class InputItemNodeTile extends AbstractItemNodeTile {

    private ItemStackList inputFilter = ItemStackList.create(FILTER_AMOUNT);

    private boolean inputOredict = false;
    private boolean inputDamage = false;
    private boolean inputNbt = false;

    public InputItemNodeTile() {
        super(Registration.INPUT_ITEM_TILE.get());
    }

    public static BaseNodeBlock createBlock() {
        return new BaseNodeBlock(new BlockBuilder()
                .tileEntitySupplier(InputItemNodeTile::new));
    }

// @todo 1.14
//    @Override
//    public Block getBlockType() {
//        return ModBlocks.inputItemNode;
//    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
//        readBufferFromNBT(tagCompound, "input", inputFilter);
        CompoundNBT info = tagCompound.getCompound("Info");
        if (!info.isEmpty()) {
            inputDamage = info.getBoolean("inDamage");
            inputNbt = info.getBoolean("inNBT");
            inputOredict = info.getBoolean("inOre");
        }
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
//        writeBufferToNBT(tagCompound, "input", inputFilter);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putBoolean("inDamage", inputDamage);
        info.putBoolean("inNBT", inputNbt);
        info.putBoolean("inOre", inputOredict);
    }

    public ItemStackList getInputFilter() {
        return inputFilter;
    }

    public boolean isInputOredict() {
        return inputOredict;
    }

    public boolean isInputDamage() {
        return inputDamage;
    }

    public boolean isInputNbt() {
        return inputNbt;
    }

    private SimpleItemHandler inputHandler = null;

    private SimpleItemHandler getInputHandler() {
        if (inputHandler == null) {
            inputHandler = new SimpleItemHandler(inputFilter);
        }
        return inputHandler;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        final Pair<String, String> pair = getSlotTag(tag);

        if (TAG_HELP.equals(pair.getRight())) {
            return HoloGuiTools.createHelpGui(registry,
                    HelpBuilder.create()
                            .line("This node can be used in")
                            .line("an automation field to input")
                            .line("items"),
                    pair.getLeft() + ":" + TAG_MAIN
            );
        } else {
            return createInputGui(pair, registry);
        }
    }

    private IGuiComponent<?> createInputGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(3.3, -.6, 1, 1).text("Input").color(registry.color(StyledColor.LABEL)))

                .add(registry.iconToggle(0.5, 0.4, 1, 1)
                        .getter(player -> inputNbt)
                        .icon(registry.image(Icons.NBT_OFF))
                        .selected(registry.image(Icons.NBT_ON))
                        .hitEvent((component, player, entity, x, y) -> toggleInputNBT()))
                .add(registry.iconToggle(0.5, 1.4, 1, 1)
                        .getter(player -> inputDamage)
                        .icon(registry.image(Icons.DAM_OFF))
                        .selected(registry.image(Icons.DAM_ON))
                        .hitEvent((component, player, entity, x, y) -> toggleInputDamage()))
                .add(registry.iconToggle(0.5, 2.4, 1, 1)
                        .getter(player -> inputOredict)
                        .icon(registry.image(Icons.ORE_OFF))
                        .selected(registry.image(Icons.ORE_ON))
                        .hitEvent((component, player, entity, x, y) -> toggleInputOre()))

                .add(registry.slots(2.5, 1.2, 6, 2)
                        .name("slots")
                        .fullBright()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> removeFromFilter(player, entity, getInputHandler()))
                        .itemHandler(getInputHandler()))

                .add(registry.playerInventory(4.7)
                        .name("playerSlots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> addToFilter(player, entity, getInputHandler())));
        addFilterChoice(registry, panel, 0, false);
        addFilterChoice(registry, panel, 1, false);
        addFilterChoice(registry, panel, 2, true);
        addFilterChoice(registry, panel, 3, true);
        return panel;
    }

    private void toggleInputNBT() {
        inputNbt = !inputNbt;
        notifyField();
    }

    private void toggleInputDamage() {
        inputDamage = !inputDamage;
        notifyField();
    }

    private void toggleInputOre() {
        inputOredict = !inputOredict;
        notifyField();
    }
}
