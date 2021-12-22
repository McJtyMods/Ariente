package mcjty.ariente.entities.fluxelevator;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.StyledColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static mcjty.hologui.api.Icons.*;

public class FluxElevatorGui {

    public static IGuiComponent<?> createGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        return registry.panel(0, 0, 8, 8)

                .add(registry.iconButton(2, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, -10)))
                .add(registry.iconButton(3, 2, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, -5)))
                .add(registry.iconButton(4, 2, 1, 1).icon(registry.image(FADED_RED_BALL)).hover(registry.image(RED_BALL))
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 0)))
                .add(registry.iconButton(5, 2, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 5)))
                .add(registry.iconButton(6, 2, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, p, holo, x, y) -> changeSpeed(holo, 10)))

                .add(registry.text(1, 4, 4, 1).text("Speed:").color(registry.color(StyledColor.LABEL)))
                .add(registry.number(5, 4, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p,holo) -> getSpeed(holo)))
                ;
    }

    private static int getSpeed(IHoloGuiEntity holo) {
        Entity ridingEntity = holo.getEntity().getVehicle();
        if (ridingEntity instanceof FluxElevatorEntity) {
            FluxElevatorEntity levitator = (FluxElevatorEntity) ridingEntity;
            return levitator.getSpeed();
        } else {
            return 0;
        }
    }

    private static void changeSpeed(IHoloGuiEntity holo, int amount) {
        Entity ridingEntity = holo.getEntity().getVehicle();
        if (ridingEntity instanceof FluxElevatorEntity) {
            FluxElevatorEntity levitator = (FluxElevatorEntity) ridingEntity;
            if (amount != 0) {
                levitator.changeSpeed(levitator.getSpeed() + amount);
            } else {
                levitator.changeSpeed(0);
            }
        }
    }
}
