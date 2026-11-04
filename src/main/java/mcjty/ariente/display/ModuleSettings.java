package mcjty.ariente.display;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/** A configuration session targets a held item or an installed module's stable identity. */
public class ModuleSettings {

    private final Player player;
    private final InteractionHand hand;
    private final UUID id;
    private final DisplayModuleItem item;
    private final DisplayBaseTile owner;

    private ModuleSettings(Player player, InteractionHand hand, ItemStack stack) {
        this.player = player;
        this.hand = hand;
        owner = null;
        item = (DisplayModuleItem) stack.getItem();
        id = stack.getOrCreateTag().hasUUID("ModuleId") ? stack.getTag().getUUID("ModuleId") : new UUID(0, 0);
    }

    private ModuleSettings(DisplayBaseTile owner, InstalledModule module) {
        this.owner = owner;
        id = module.id();
        item = (DisplayModuleItem) module.stack().getItem();
        player = null;
        hand = null;
    }

    public ItemStack stack() {
        if (owner != null) {
            InstalledModule module = owner.findModule(id);
            return owner.isRemoved() || module == null ? ItemStack.EMPTY : module.stack();
        }
        ItemStack stack = player.getItemInHand(hand);
        return stack.getItem() == item && stack.hasTag() && stack.getTag().hasUUID("ModuleId")
                && id.equals(stack.getTag().getUUID("ModuleId")) ? stack : ItemStack.EMPTY;
    }

    public CompoundTag config() {
        return DisplayModuleItem.config(stack());
    }

    public int number(String key, int fallback) {
        return config().contains(key) ? config().getInt(key) : fallback;
    }

    public boolean flag(String key, boolean fallback) {
        return config().contains(key) ? config().getBoolean(key) : fallback;
    }

    private void edit(Player actor, IHoloGuiEntity gui, Consumer<CompoundTag> change) {
        if (stack().isEmpty()) {
            return;
        }
        if (owner != null) {
            if (!owner.configureModule(actor, id, change) && gui != null) {
                gui.getEntity().discard();
            }
        } else if (actor == player) {
            change.accept(config());
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }

    public void setNumber(Player actor, IHoloGuiEntity gui, String key, int value) {
        edit(actor, gui, config -> config.putInt(key, value));
    }

    public void text(Player actor, IHoloGuiEntity gui, String key, String value) {
        edit(actor, gui, config -> config.putString(key, value));
    }

    public void toggle(Player actor, IHoloGuiEntity gui, String key, boolean fallback) {
        boolean value = !flag(key, fallback);
        edit(actor, gui, config -> config.putBoolean(key, value));
    }

    public void addToggle(IGuiComponentRegistry r, IPanel panel, double y, String key, String title, boolean fallback) {
        panel.add(r.textChoice(.3, y, 7.2, .85).name("setting_" + key).scale(.48f)
                .addText(title + ": Off").addText(title + ": On")
                .getter(p -> flag(key, fallback) ? 1 : 0)
                .hitEvent((c, p, e, x, cy) -> toggle(p, e, key, fallback)));
    }

    public void addNumber(IGuiComponentRegistry r, IPanel panel, double y, String key, String title, int fallback, int min, int max) {
        panel.add(r.integerSlider(.3, y, 7.2, .95).name("setting_" + key).label(title).range(min, max)
                .getter((p, e) -> number(key, fallback)).changed((p, e, value) -> setNumber(p, e, key, value)));
    }

    private void switchPage(IHoloGuiEntity gui, boolean color) {
        if (!stack().isEmpty()) {
            if (owner == null) {
                gui.switchGui(guiId(hand, color));
            } else {
                gui.switchTag(installedTag(id, color));
            }
        }
    }

    public void addColorButton(IGuiComponentRegistry r, IPanel panel, double y) {
        panel.add(r.button(.3, y, 4, .8).text("Color...").scale(.6f)
                .swatch(() -> number("Color", 0xb8f4ff))
                .hitEvent((c, p, e, x, cy) -> switchPage(e, true)));
    }

    public static String guiId(InteractionHand hand, boolean color) {
        return "ariente.display." + (color ? "color." : "module.") + hand.name().toLowerCase(Locale.ROOT);
    }

    public static String installedTag(UUID id, boolean color) {
        return (color ? "module-color:" : "module:") + id;
    }

    public static boolean isInstalledTag(String tag) {
        return tag.startsWith("module:") || tag.startsWith("module-color:");
    }

    public static IGuiComponent<?> createInstalled(DisplayBaseTile owner, String tag, IGuiComponentRegistry registry) {
        InstalledModule module;
        try {
            module = owner.findModule(UUID.fromString(tag.substring(tag.indexOf(':') + 1)));
        } catch (IllegalArgumentException exception) {
            module = null;
        }
        if (module == null) {
            return registry.panel(0, 0, 8, 8).add(registry.text(.3, 2, 8, 1).text("This module is no longer installed.").scale(.45f));
        }
        return createPanel(registry, new ModuleSettings(owner, module), tag.startsWith("module-color:"));
    }

    public static void register() {
        for (InteractionHand hand : InteractionHand.values()) {
            Ariente.guiHandler.getGuiRegistry().registerGui(guiId(hand, false), player -> create(player, hand, false));
            Ariente.guiHandler.getGuiRegistry().registerGui(guiId(hand, true), player -> create(player, hand, true));
        }
    }

    private static IGuiComponent<?> create(Player player, InteractionHand hand, boolean color) {
        IGuiComponentRegistry r = Ariente.guiHandler.getComponentRegistry();
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof DisplayModuleItem)) {
            return r.panel(0, 0, 8, 8).add(r.text(.3, 2, 8, 1).text("Hold the module to configure it.").scale(.5f));
        }
        return createPanel(r, new ModuleSettings(player, hand, stack), color);
    }

    private static IGuiComponent<?> createPanel(IGuiComponentRegistry r, ModuleSettings settings, boolean color) {
        IPanel panel = HoloGuiTools.createPanelWithHelp(r, new ResourceLocation(Ariente.MODID, "construction/displays"));
        panel.add(r.text(.3, 0, 8, 1).text(color ? "MODULE COLOR" : settings.item.type().title()).scale(.65f).color(0x76eaff));
        if (color) {
            panel.add(r.button(6.4, .1, 1.6, .65).name("color_close").text("Back").scale(.4f)
                    .hitEvent((c, p, e, x, y) -> settings.switchPage(e, false)));
            panel.add(r.colorPicker(.4, 1, 6.8, 5.4).name("color_picker").color(() -> settings.number("Color", 0xb8f4ff))
                    .selected((p, e, rgb) -> {
                        settings.setNumber(p, e, "Color", rgb);
                        settings.switchPage(e, false);
                    }));
            panel.add(r.text(.3, 7.4, 8, .5).text("Choose a hue, then select a shade.").scale(.38f));
        } else {
            settings.item.type().configure(r, panel, settings);
            panel.add(r.text(.3, 7.4, 8, .5).text(() -> {
                if (settings.stack().isEmpty()) {
                    return settings.owner == null ? "Hold the original module to edit." : "Module returned to your inventory.";
                }
                DisplayModuleType.Size size = settings.item.type().size(settings.stack());
                return "Footprint: " + size.width() + " x " + size.height() + " px | 64 px = 1 block";
            }).scale(.32f));
        }
        return panel;
    }
}
