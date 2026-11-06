package mcjty.ariente.entities.fluxship;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.CompactNumber;
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
                .add(registry.text(.3, .2, 7.4, .6).text("FLUX SHIP").scale(.5f).color(LABEL_COLOR))
                .add(registry.text(.3, 1.0, 3.2, .6).text("Speed km/h").scale(.35f).color(LABEL_COLOR))
                .add(new CompactNumber(.3, 1.6, 3.2, .8).name("ship_speed")
                        .color(VALUE_COLOR)
                        .getter((p, holo) -> holo.getEntity().getVehicle() instanceof FluxShipEntity ship
                                ? Math.round(ship.getSpeed() * 3.6f) : 0))
                .add(registry.text(4.8, 1.0, 2.9, .6).text("Altitude (Y)").scale(.35f).color(LABEL_COLOR))
                .add(new CompactNumber(4.8, 1.6, 2.9, .8).name("ship_altitude")
                        .color(VALUE_COLOR)
                        .getter((p, holo) -> holo.getEntity().getVehicle() instanceof FluxShipEntity ship
                                ? Mth.floor(ship.getY()) : 0))
                .add(registry.text(.3, 2.7, 7.4, .6).name("ship_fire").text(FluxShipClient::firingKey).scale(.35f).color(LABEL_COLOR))
                .add(registry.text(.3, 5.7, 7.4, .5).text(() -> FluxShipClient.movementKeys() + "   " + FluxShipClient.turnKeys()).scale(.3f).color(LABEL_COLOR))
                .add(registry.text(.3, 6.25, 7.4, .5).text(FluxShipClient::flightHint).scale(.26f).color(LABEL_COLOR))
                .add(registry.text(.3, 6.8, 7.4, .5).text(() -> FluxShipClient.dismountKey()
                        + " | Release controls to hover").scale(.25f).color(LABEL_COLOR));
    }
}
