package mcjty.ariente.compat;

import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.gui.ModGuis;
import mcjty.hologui.api.IHoloGuiEntity;
import mcjty.lib.varia.Logging;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class ArienteTOPCompatibility {

    private static boolean registered;

    private ArienteTOPCompatibility() {
    }

    public static void register() {
        if (!registered && ModList.get().isLoaded("theoneprobe")) {
            registered = true;
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", GetTheOneProbe::new);
        }
    }

    static boolean isVehicleHud(Player player, Entity entity) {
        if (entity instanceof IHoloGuiEntity holo && (ModGuis.GUI_FLUX_SHIP.equals(holo.getGuiId())
                || ModGuis.GUI_LEVITATOR.equals(holo.getGuiId()))) {
            return true;
        }
        // The camera can also hit the hull while the rider looks through the panel.
        return entity == player.getVehicle()
                && (entity instanceof FluxShipEntity || entity instanceof FluxLevitatorEntity);
    }

    public static class GetTheOneProbe implements Function<ITheOneProbe, Void> {

        @Override
        public Void apply(ITheOneProbe probe) {
            try {
                // Standalone builds and older installations may still supply the pre-blacklist API.
                // Reflect only this optional addition; the rest of the integration uses the stable API.
                ITheOneProbe.class.getMethod("registerEntityBlacklist", BiPredicate.class)
                        .invoke(probe, (BiPredicate<Player, Entity>) ArienteTOPCompatibility::isVehicleHud);
            } catch (NoSuchMethodException e) {
                Logging.log("Vehicle HUD filtering requires a newer version of The One Probe");
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Cannot register Ariente's entity blacklist with The One Probe", e);
            }
            return null;
        }
    }
}
