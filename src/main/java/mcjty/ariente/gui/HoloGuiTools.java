package mcjty.ariente.gui;

import mcjty.hologui.api.*;
import java.util.function.Consumer;
import mcjty.ariente.items.TabletItem;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.compat.patchouli.PatchouliCompatibility;
import mcjty.lib.setup.ModSetup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static mcjty.hologui.api.IGuiTile.TAG_HELP;
import static mcjty.hologui.api.IGuiTile.TAG_MAIN;

public class HoloGuiTools {

    public static Integer countItem(Player player, Item item) {
        Inventory inventory = player.getInventory();
        int size = inventory.getContainerSize();
        int cnt = 0;
        for (int i = 0 ; i < size ; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                cnt += stack.getCount();
            }
        }
        return cnt;
    }

    public static IPanel createPanelWithHelp(IGuiComponentRegistry registry, Identifier entry) {
        return createPanelWithHelp(registry, entry, entity -> entity.switchTag(TAG_HELP));
    }

    public static IPanel createPanelWithHelp(IGuiComponentRegistry registry, Identifier entry, Consumer<IHoloGuiEntity> switchHelp) {
        IPanel panel = registry.panel(0, 0, 8, 8);
        if (ModSetup.patchouli && entry != null) {
            panel.help((component, player, entity, x, y) -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    PatchouliCompatibility.openBookEntry(serverPlayer, TabletItem.MANUAL, entry, 0);
                }
            });
        }
        return panel;
    }

    public static IGuiComponent<?> createHelpGui(IGuiComponentRegistry registry, HelpBuilder helpBuilder) {
        return createHelpGui(registry, helpBuilder, TAG_MAIN);
    }

    public static IGuiComponent<?> createHelpGui(IGuiComponentRegistry registry, HelpBuilder helpBuilder, String tagBack) {
        return createHelpGui(registry, helpBuilder, iHoloGuiEntity -> iHoloGuiEntity.switchTag(tagBack));
    }

    public static IGuiComponent<?> createHelpGui(IGuiComponentRegistry registry, HelpBuilder helpBuilder, Consumer<IHoloGuiEntity> switchBack) {
        IPanel help = registry.panel(0, 0, 8, 8)
                .add(registry.text(0, -.2, 8, 1).text("Help").color(registry.color(StyledColor.LABEL)));

        double y = 1;
        for (HelpBuilder.HelpLine line : helpBuilder.getLines()) {
            help.add(registry.text(0, y, 8, 1).text(line.getText()).color(line.getColor()).scale(.5f));
            y += .5;
        }

        return help
                .add(registry.iconButton(8.1, 7.8, 1, 1)
                        .icon(registry.image(Icons.FADED_NAVIGATE_BACK))
                        .hover(registry.image(Icons.NAVIGATE_BACK))
                        .hitEvent((component, p, entity, x1, y1) -> switchBack.accept(entity)))
                ;
    }
}
