package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.PlayerInventoryPanel;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.varia.ItemStackList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

public class OutputItemNodeTile extends AbstractItemNodeTile {

    private ItemStackList outputFilter = ItemStackList.create(FILTER_AMOUNT);

    private boolean outputTags = false;
    private boolean outputDamage = false;
    private boolean outputNbt = false;
    private boolean outputBlacklist = false;
    private int outputStackSize = 1;

    public OutputItemNodeTile(BlockPos pos, BlockState state) {
        super(Registration.OUTPUT_ITEM_TILE.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        outputFilter.clear();
        CompoundTag info = tagCompound.getCompound("Info");
        ContainerHelper.loadAllItems(info.getCompound("output"), outputFilter);
        if (!info.isEmpty()) {
            outputDamage = info.getBoolean("outDamage");
            outputNbt = info.getBoolean("outNBT");
            outputTags = info.getBoolean("outOre");
            outputBlacklist = info.getBoolean("outBlacklist");
            outputStackSize = info.getInt("outSS");
        }
    }

    @Override
    public void saveAdditional(CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        CompoundTag info = getOrCreateInfo(tagCompound);
        info.put("output", ContainerHelper.saveAllItems(new CompoundTag(), outputFilter));
        info.putBoolean("outDamage", outputDamage);
        info.putBoolean("outNBT", outputNbt);
        // Keep the legacy key so existing tag filters retain their settings.
        info.putBoolean("outOre", outputTags);
        info.putBoolean("outBlacklist", outputBlacklist);
        info.putInt("outSS", outputStackSize);
    }

    public ItemStackList getOutputFilter() {
        return outputFilter;
    }

    public boolean isOutputTags() {
        return outputTags;
    }

    public boolean isOutputBlacklist() {
        return outputBlacklist;
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
            return GuiHelp.OUTPUT_NODE.createHelpGui(registry,
                    pair.getLeft() + ":" + TAG_MAIN
            );
        } else {
            return createOutputGui(pair, registry);
        }
    }

    private IGuiComponent<?> createOutputGui(final Pair<String, String> pair, IGuiComponentRegistry registry) {
        IPanel panel = GuiHelp.OUTPUT_NODE.createPanel(registry, entity -> entity.switchTag(pair.getLeft() + ":" + TAG_HELP))
                .add(registry.text(.25, -.6, 3, 1).text("Output").color(registry.color(StyledColor.LABEL)))

                .add(registry.text(4.2, 3.65, 2.6, .5).text(() -> "Batch: " + outputStackSize).scale(.35f)
                        .color(registry.color(StyledColor.INFORMATION)))
                .add(registry.button(2.8, 3.65, .6, .5).text("-8").scale(.35f)
                        .hitEvent((component, player, entity, x, y) -> changeOutputStackSize(-8)))
                .add(registry.button(3.5, 3.65, .6, .5).text("-1").scale(.35f)
                        .hitEvent((component, player, entity, x, y) -> changeOutputStackSize(-1)))
                .add(registry.button(7.5, 3.65, .6, .5).text("+1").scale(.35f)
                        .hitEvent((component, player, entity, x, y) -> changeOutputStackSize(1)))
                .add(registry.button(8.2, 3.65, .6, .5).text("+8").scale(.35f)
                        .hitEvent((component, player, entity, x, y) -> changeOutputStackSize(8)))

                .add(registry.slots(2.8, 1.55, 6, 2)
                        .name("slots")
                        .fullBright()
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> removeFromFilter(player, entity, getOutputHandler()))
                        .itemHandler(getOutputHandler()))

                .add(new PlayerInventoryPanel("Double-click to add a filter")
                        .name("playerSlots")
                        .doubleClickEvent((component, player, entity, x, y, stack, index) -> addToFilter(player, entity, getOutputHandler())));

        addFilterConditions(registry, panel);
        addFilterOption(registry, panel, "filter_mode", 1.45, "Whitelist", "Blacklist", this::isOutputBlacklist, this::toggleOutputBlacklist);
        addFilterOption(registry, panel, "filter_nbt", 2.15, "NBT: Off", "NBT: On", this::isOutputNbt, this::toggleOutputNBT);
        addFilterOption(registry, panel, "filter_damage", 2.85, "Damage: Off", "Damage: On", this::isOutputDamage, this::toggleOutputDamage);
        addFilterOption(registry, panel, "filter_tags", 3.55, "Tags: Off", "Tags: On", this::isOutputTags, this::toggleOutputTags);
        return panel;
    }

    private void toggleOutputBlacklist() {
        outputBlacklist = !outputBlacklist;
        notifyField();
    }

    private void toggleOutputNBT() {
        outputNbt = !outputNbt;
        notifyField();
    }

    private void toggleOutputDamage() {
        outputDamage = !outputDamage;
        notifyField();
    }

    private void toggleOutputTags() {
        outputTags = !outputTags;
        notifyField();
    }
}
