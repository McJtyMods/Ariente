package mcjty.ariente.entities.levitator;

import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.Ariente;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.hologui.api.StyledColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static mcjty.hologui.api.Icons.*;

public class FluxLevitatorGui {

    public static IGuiComponent<?> createGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        return GuiHelp.LEVITATOR.createPanel(registry, entity -> entity.switchGui(ModGuis.GUI_LEVITATOR_HELP))
                .add(registry.text(.3, .6, 8, 1).text("LEVITATOR SPEED").scale(.6f).color(registry.color(StyledColor.LABEL)))
                .add(registry.iconButton(4, 2, 1, 1).icon(registry.image(FADED_RED_BALL)).hover(registry.image(RED_BALL))
                        .hitEvent((c, p, e, x, y) -> setSpeed(e, 0)))
                .add(registry.integerSlider(.3, 4, 7.2, 1).name("transport_speed").label("Speed").range(-80, 80)
                        .getter((p, e) -> getSpeed(e)).changed((p, e, value) -> setSpeed(e, value)));
    }

    public static IGuiComponent<?> createHelpGui(Player player) {
        return GuiHelp.LEVITATOR.createHelpGui(Ariente.guiHandler.getComponentRegistry(),
                entity -> entity.switchGui(ModGuis.GUI_LEVITATOR));
    }

    private static int getSpeed(IHoloGuiEntity holo) {
        Entity ridingEntity = holo.getEntity().getVehicle();
        if (ridingEntity instanceof FluxLevitatorEntity) {
            FluxLevitatorEntity levitator = (FluxLevitatorEntity) ridingEntity;
            boolean front = holo.getEntity().getUUID().equals(levitator.getHoloFrontUUID());
            return front ? levitator.getSpeed() : -levitator.getSpeed();
        } else {
            return 0;
        }
    }

    private static void setSpeed(IHoloGuiEntity holo, int value) {
        Entity ridingEntity = holo.getEntity().getVehicle();
        if (ridingEntity instanceof FluxLevitatorEntity transport) {
            boolean front = holo.getEntity().getUUID().equals(transport.getHoloFrontUUID());
            transport.changeSpeed(front ? value : -value);
        }
    }
}
