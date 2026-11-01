package mcjty.ariente.gui;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/** Manual destinations and the compact help available without Patchouli. */
public enum GuiHelp {
    CONSTRUCTOR("construction/constructor",
            "Craft using blueprints in",
            "adjacent Blueprint Storage.",
            "",
            "Top grid: your inventory.",
            "Bottom grid: available blueprints.",
            "Hover to see missing ingredients.",
            "Double-click a blueprint to craft.",
            "No flux supply is needed."),
    AUTO_CONSTRUCTOR("construction/auto_constructor",
            "Automatically crafts using",
            "blueprints in adjacent storage.",
            "Supply both fluxes and check",
            "the redstone mode.",
            "",
            "Use Ingredients to transfer items",
            "between you and the machine.",
            "Output slots are on the main page.",
            "Item automation also works."),
    BLUEPRINT_STORAGE("construction/blueprints",
            "Stores six blueprints for adjacent",
            "Constructors and Auto Constructors.",
            "",
            "Top slots: your blueprints.",
            "Bottom slots: stored blueprints.",
            "Double-click to transfer, or select",
            "a slot and use the up/down arrows.",
            "Blueprints are kept when crafting."),
    COMBINER("power/combiner",
            "Combines negarite and posirite",
            "for combined cables/connectors.",
            "Supply both separate flux types.",
            "",
            "Use the slider and arrows to set",
            "the maximum input per tick.",
            "In and Out show the transfer.",
            "Combining loses 2% of each flux."),
    GENERATORS("power/generators",
            "Put matching tanks directly above",
            "the generator, without gaps.",
            "Each tank adds 1000 flux per tick.",
            "Use matching dust as fuel.",
            "",
            "Arrows move 1 or 64 dust between",
            "your inventory and the generator.",
            "Use redstone mode to stop idle",
            "generators from burning fuel."),
    FIELD("automation/first_field",
            "Place connected Field Markers",
            "beside the controller at its base.",
            "Fill the area under node columns.",
            "Set the height to reach the nodes.",
            "",
            "Output Nodes take from inventories;",
            "Input Nodes insert into them.",
            "Set filters and supply both fluxes.",
            "Use the outline to check coverage."),
    INPUT_NODE("automation/filters",
            "In an automation field, this node",
            "inserts items into its inventory.",
            "Double-click player items to filter.",
            "Double-click samples to clear them.",
            "",
            "The ore icon matches material tags",
            "such as planks or ingots/silver.",
            "NBT and damage match separately.",
            "An empty filter accepts nothing."),
    OUTPUT_NODE("automation/filters",
            "In an automation field, this node",
            "takes items out of its inventory.",
            "Double-click player items to filter.",
            "Double-click samples to clear them.",
            "The ore icon matches material tags.",
            "NBT and damage match separately.",
            "An empty filter accepts nothing.",
            "Stack sets the minimum batch size.",
            "Colors set required conditions."),
    SENSOR("automation/sensors",
            "Detects the total item count in",
            "its attached inventory within",
            "an automation field.",
            "",
            "Choose a comparison, count and",
            "output color. Other nodes can",
            "require that colored condition.",
            "The sensor counts all item types."),
    EQUIPMENT("equipment/configuration",
            "Configure armor and weapon modules.",
            "Click a module icon to install it",
            "from your inventory, or remove it.",
            "",
            "The checkbox enables the module.",
            "The last control assigns hotkey",
            "group 1 to 4 for quick toggles.",
            "",
            "Fuel each piece with both dusts.",
            "Keep active usage within its limit."),
    ENERGY_HOLDER("equipment/fuel",
            "Stores negarite and posirite dust.",
            "Arrows transfer dust in or out.",
            "",
            "Automatic mode gathers loose dust",
            "from your inventory over time.",
            "Manual mode leaves collection to you.",
            "",
            "Gear with enabled Autofeed can use",
            "stored dust in either mode. Keep",
            "the holder in your inventory."),
    SHIELD("security/shields",
            "Set the shield radius and",
            "redstone mode. Supply both fluxes.",
            "Larger fields need more power.",
            "",
            "Field Integrity shows strength.",
            "Damage can leave temporary holes.",
            "Leave spare power for repairs.",
            "Changing radius rebuilds the field.",
            "Crossing needs matching immunity."),
    LOCK("security/doors",
            "Controls Door Markers within its",
            "horizontal and vertical ranges.",
            "Use the sliders to adjust them.",
            "",
            "A matching Key Card toggles the",
            "lock and its doors. Unlock before",
            "changing the settings.",
            "Avoid overlapping lock ranges."),
    DOOR("security/doors",
            "Place markers across an opening.",
            "A ceiling limits panel height.",
            "Unlocked doors open for nearby",
            "players and soldiers.",
            "",
            "The icon buttons choose the",
            "door panel's displayed symbol.",
            "Nearby locks control access."),
    WIRELESS_LOCK("security/wireless",
            "Use a Wireless Lock item on a",
            "Wireless Button to copy its channel.",
            "Place the lock near Door Markers.",
            "",
            "The button unlocks the doors while",
            "its channel is on. Adjust the",
            "horizontal and vertical ranges",
            "with the arrows. Keep the devices",
            "in loaded chunks."),
    ELEVATOR("transport/elevators",
            "Set height and keep the shaft clear.",
            "Elevators need no flux supply.",
            "Stand in the beam: Jump goes up,",
            "Sneak goes down.",
            "",
            "Place Level Markers at landings,",
            "within two blocks horizontally.",
            "With multiple floors, Jump/Sneak",
            "select the next floor. You can",
            "also click its floor button."),
    LEVEL_MARKER("transport/elevators",
            "Marks an elevator landing height.",
            "Place within two blocks horizontally",
            "and inside the elevator's height.",
            "Keep the landing clear.",
            "",
            "Multiple floors enable floor",
            "selection in the elevator beam.",
            "Floor naming is not available."),
    LEVITATOR("transport/levitators",
            "Place on a continuous Flux Beam",
            "route, then right-click to board.",
            "",
            "Set speed with the slider/arrows.",
            "Negative speed reverses travel.",
            "The red button stops the cart.",
            "Sneak to dismount.",
            "No generator connection is needed.");

    private final ResourceLocation entry;
    private final String[] lines;

    GuiHelp(String entry, String... lines) {
        this.entry = new ResourceLocation(Ariente.MODID, entry);
        this.lines = lines;
    }

    public IPanel createPanel(IGuiComponentRegistry registry) {
        return HoloGuiTools.createPanelWithHelp(registry, entry);
    }

    public IPanel createPanel(IGuiComponentRegistry registry, Consumer<IHoloGuiEntity> switchHelp) {
        return HoloGuiTools.createPanelWithHelp(registry, entry, switchHelp);
    }

    public IGuiComponent<?> createHelpGui(IGuiComponentRegistry registry) {
        return HoloGuiTools.createHelpGui(registry, help());
    }

    public IGuiComponent<?> createHelpGui(IGuiComponentRegistry registry, String tagBack) {
        return HoloGuiTools.createHelpGui(registry, help(), tagBack);
    }

    public IGuiComponent<?> createHelpGui(IGuiComponentRegistry registry, Consumer<IHoloGuiEntity> switchBack) {
        return HoloGuiTools.createHelpGui(registry, help(), switchBack);
    }

    private HelpBuilder help() {
        HelpBuilder help = HelpBuilder.create();
        for (String line : lines) {
            help.line(line);
        }
        return help;
    }
}
