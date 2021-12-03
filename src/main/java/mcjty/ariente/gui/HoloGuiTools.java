package mcjty.ariente.gui;

import mcjty.hologui.api.*;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

import static mcjty.hologui.api.IGuiTile.TAG_HELP;
import static mcjty.hologui.api.IGuiTile.TAG_MAIN;

public class HoloGuiTools {

    public static Integer countItem(PlayerEntity player, Item item) {
        PlayerInventory inventory = player.inventory;
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

    public static IPanel createPanelWithHelp(IGuiComponentRegistry registry) {
        return createPanelWithHelp(registry, entity -> entity.switchTag(TAG_HELP));
    }

    public static IPanel createPanelWithHelp(IGuiComponentRegistry registry, Consumer<IHoloGuiEntity> switchHelp) {
        return registry.panel(0, 0, 8, 8)
                .add(registry.iconButton(8.2, -0.8, 1, 1)
                        .icon(registry.image(Icons.FADED_QUESTION_MARK))
                        .hover(registry.image(Icons.QUESTION_MARK))
                        .hitEvent((component, p, entity, x1, y1) -> switchHelp.accept(entity)))
                ;
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
