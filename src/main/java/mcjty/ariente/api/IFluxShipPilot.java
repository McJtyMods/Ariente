package mcjty.ariente.api;

import mcjty.ariente.entities.fluxship.FluxShipEntity;

/** Server-side flight control for living pilots supplied by companion mods. */
public interface IFluxShipPilot {

    void controlShip(FluxShipEntity ship);

    default boolean shouldFireShipLaser(FluxShipEntity ship) {
        return false;
    }
}
