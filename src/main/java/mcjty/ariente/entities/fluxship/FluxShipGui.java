package mcjty.ariente.entities.fluxship;

import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class FluxShipGui {

    private static final IColor LABEL_COLOR = () -> 0xffb8f4ff;
    private static final IColor VALUE_COLOR = () -> 0xffffffff;

    public static IGuiComponent<?> createGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(.3, .4, 7.4, 1).text("FLUX SHIP").scale(.7f).color(LABEL_COLOR))
                .add(registry.text(.3, 1.6, 4.3, 1).text("Speed km/h").scale(.6f).color(LABEL_COLOR))
                .add(registry.number(5, 1.6, 2.4, 1).name("ship_speed")
                        .color(VALUE_COLOR)
                        .getter((p, holo) -> holo.getEntity().getVehicle() instanceof FluxShipEntity ship
                                ? Math.round(ship.getSpeed() * 3.6f) : 0))
                .add(registry.text(.3, 2.6, 4.3, 1).text("Altitude (Y)").scale(.6f).color(LABEL_COLOR))
                .add(registry.number(5, 2.6, 2.4, 1).name("ship_altitude")
                        .color(VALUE_COLOR)
                        .getter((p, holo) -> holo.getEntity().getVehicle() instanceof FluxShipEntity ship
                                ? Mth.floor(ship.getY()) : 0))
                .add(registry.text(.3, 4, 7.4, 1).text("FLIGHT CONTROLS").scale(.5f).color(LABEL_COLOR))
                .add(registry.text(.3, 4.7, 7.4, 1).text(FluxShipClient::movementKeys).scale(.4f).color(LABEL_COLOR))
                .add(registry.text(.3, 5.3, 7.4, 1).text(FluxShipClient::turnKeys).scale(.4f).color(LABEL_COLOR))
                .add(registry.text(.3, 5.9, 7.4, 1).text(FluxShipClient::heightKeys).scale(.4f).color(LABEL_COLOR))
                .add(registry.text(.3, 6.5, 7.4, 1).text(FluxShipClient::alternateHeightKeys).scale(.4f).color(LABEL_COLOR))
                .add(registry.text(.3, 7.1, 7.4, 1).text(FluxShipClient::dismountKey).scale(.4f).color(LABEL_COLOR))
                .add(registry.text(.3, 7.7, 7.4, 1).text("Release controls to hover").scale(.4f).color(LABEL_COLOR));
    }
}
