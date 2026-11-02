package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.PlayerInventoryPanel;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

public class InputItemNodeTile extends AbstractItemNodeTile {

    private ItemStackList inputFilter = ItemStackList.create(FILTER_AMOUNT);

    private boolean inputTags = false;
    private boolean inputDamage = false;
    private boolean inputNbt = false;
    private boolean inputBlacklist = false;
    private ItemFilterCategory inputCategory = ItemFilterCategory.ALL;

    public InputItemNodeTile(BlockPos pos, BlockState state) {
        super(Registration.INPUT_ITEM_TILE.get(), pos, state);
    }

    public static BaseNodeBlock createBlock() {
        return new BaseNodeBlock(new BlockBuilder()
                .tileEntitySupplier(InputItemNodeTile::new));
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        inputFilter.clear();
        CompoundTag info = tagCompound.getCompound("Info");
        inputCategory = ItemFilterCategory.fromId(info.getString("inCategory"));
        ContainerHelper.loadAllItems(info.getCompound("input"), inputFilter);
        if (!info.isEmpty()) {
            inputDamage = info.getBoolean("inDamage");
            inputNbt = info.getBoolean("inNBT");
            inputTags = info.getBoolean("inOre");
            inputBlacklist = info.getBoolean("inBlacklist");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.put("input", ContainerHelper.saveAllItems(new CompoundTag(), inputFilter));
        info.putBoolean("inDamage", inputDamage);
        info.putBoolean("inNBT", inputNbt);
        // Keep the legacy key so existing tag filters retain their settings.
        info.putBoolean("inOre", inputTags);
        info.putBoolean("inBlacklist", inputBlacklist);
        info.putString("inCategory", inputCategory.id());
    }

    public ItemStackList getInputFilter() {
        return inputFilter;
    }

    public boolean isInputTags() {
        return inputTags;
    }

    public ItemFilterCategory getInputCategory() {
        return inputCategory;
    }

    public void setInputCategory(ItemFilterCategory category) {
        inputCategory = category;
        notifyField();
    }

    public boolean isInputBlacklist() {
        return inputBlacklist;
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
            return GuiHelp.INPUT_NODE.createHelpGui(registry,
                    pair.getLeft() + ":" + TAG_MAIN
            );
        } else {
            return createInputGui(pair, registry);
        }
    }

    private IGuiComponent<?> createInputGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        IPanel panel = GuiHelp.INPUT_NODE.createPanel(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(.25, -.6, 3, 1).text("Input").color(registry.color(StyledColor.LABEL)))

                .add(registry.slots(2.8, 1.55, 6, 2)
                        .name("slots")
                        .fullBright()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> removeFromFilter(player, entity, getInputHandler()))
                        .itemHandler(getInputHandler()))

                .add(new PlayerInventoryPanel("Double-click to add a filter")
                        .name("playerSlots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> addToFilter(player, entity, getInputHandler())));
        addFilterConditions(registry, panel);
        addFilterOption(registry, panel, "filter_mode", 1.45, "Whitelist", "Blacklist", this::isInputBlacklist, this::toggleInputBlacklist);
        addFilterOption(registry, panel, "filter_nbt", 2.15, "NBT: Off", "NBT: On", this::isInputNbt, this::toggleInputNBT);
        addFilterOption(registry, panel, "filter_damage", 2.85, "Damage: Off", "Damage: On", this::isInputDamage, this::toggleInputDamage);
        addFilterOption(registry, panel, "filter_tags", 3.55, "Tags: Off", "Tags: On", this::isInputTags, this::toggleInputTags);
        var categoryChoice = registry.textChoice(2.8, 3.55, 6.1, .6).name("filter_category").scale(.32f)
                .getter(player -> inputCategory.ordinal())
                .hitEvent((component, player, entity, x, y) -> setInputCategory(
                        ItemFilterCategory.values()[(inputCategory.ordinal() + 1) % ItemFilterCategory.values().length]));
        for (ItemFilterCategory category : ItemFilterCategory.values()) {
            categoryChoice.addText("Category: " + category.title());
        }
        panel.add(categoryChoice);
        return panel;
    }

    private void toggleInputBlacklist() {
        inputBlacklist = !inputBlacklist;
        notifyField();
    }

    private void toggleInputNBT() {
        inputNbt = !inputNbt;
        notifyField();
    }

    private void toggleInputDamage() {
        inputDamage = !inputDamage;
        notifyField();
    }

    private void toggleInputTags() {
        inputTags = !inputTags;
        notifyField();
    }
}
