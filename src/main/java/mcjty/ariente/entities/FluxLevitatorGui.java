package mcjty.ariente.entities;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import static mcjty.hologui.api.Icons.*;

public class FluxLevitatorGui {

    public static IGuiComponent<?> createGui(EntityPlayer player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        return registry.panel(0, 0, 8, 8)

                .add(registry.iconButton(2, 2, 1, 1).icon(GRAY_DOUBLE_ARROW_LEFT).hover(WHITE_DOUBLE_ARROW_LEFT)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, -10)))
                .add(registry.iconButton(3, 2, 1, 1).icon(GRAY_ARROW_LEFT).hover(WHITE_ARROW_LEFT)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, -5)))
                .add(registry.iconButton(4, 2, 1, 1).icon(FADED_RED_BALL).hover(RED_BALL)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 0)))
                .add(registry.iconButton(5, 2, 1, 1).icon(GRAY_ARROW_RIGHT).hover(WHITE_ARROW_RIGHT)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 5)))
                .add(registry.iconButton(6, 2, 1, 1).icon(GRAY_DOUBLE_ARROW_RIGHT).hover(WHITE_DOUBLE_ARROW_RIGHT)
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 10)))

                .add(registry.text(1, 4, 4, 1).text("Speed:").color(0xffffff))
                .add(registry.number(5, 4, 1, 1).color(0x00ff00).getter((p,holo) -> getSpeed(holo)))
                ;
    }

    private static int getSpeed(IHoloGuiEntity holo) {
        Entity ridingEntity = holo.getEntity().getRidingEntity();
        if (ridingEntity instanceof FluxLevitatorEntity) {
            FluxLevitatorEntity levitator = (FluxLevitatorEntity) ridingEntity;
            boolean front = holo.getEntity().getUniqueID().equals(levitator.getHoloFrontUUID());
            return front ? levitator.getSpeed() : -levitator.getSpeed();
        } else {
            return 0;
        }
    }

    private static void changeSpeed(IHoloGuiEntity holo, int amount) {
        Entity ridingEntity = holo.getEntity().getRidingEntity();
        if (ridingEntity instanceof FluxLevitatorEntity) {
            FluxLevitatorEntity levitator = (FluxLevitatorEntity) ridingEntity;
            if (amount != 0) {
                boolean front = holo.getEntity().getUniqueID().equals(levitator.getHoloFrontUUID());
                if (!front) {
                    amount = -amount;
                }
                levitator.changeSpeed(levitator.getSpeed() + amount);
            } else {
                levitator.changeSpeed(0);
            }
        }
    }
}
