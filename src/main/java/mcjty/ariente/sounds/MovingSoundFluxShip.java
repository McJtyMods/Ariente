package mcjty.ariente.sounds;

import mcjty.ariente.entities.fluxship.FluxShipEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class MovingSoundFluxShip extends AbstractTickableSoundInstance {

    private final FluxShipEntity ship;
    private final Player pilot;
    private boolean active = true;

    public MovingSoundFluxShip(FluxShipEntity ship) {
        this(null, ship);
    }

    public MovingSoundFluxShip(@Nullable Player pilot, FluxShipEntity ship) {
        super(ModSounds.levitator.get(), SoundSource.NEUTRAL, RandomSource.create());
        this.ship = ship;
        this.pilot = pilot;
        looping = true;
        delay = 0;
        volume = 0;
        pitch = .85f + .3f * ship.getExhaustStrength(1);
        relative = pilot != null;
        attenuation = relative ? Attenuation.NONE : Attenuation.LINEAR;
        if (!relative) {
            updatePosition();
        }
    }

    public FluxShipEntity getShip() {
        return ship;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean canPlaySound() {
        return !ship.isSilent() && (pilot == null || pilot == Minecraft.getInstance().player);
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (!ship.isAlive() || ship.level() != mc.level || !canPlaySound()) {
            stop();
            return;
        }
        if (!relative) {
            updatePosition();
        }
        boolean localPilot = mc.player != null && mc.player.getVehicle() == ship;
        boolean audible = active && (relative ? localPilot : !localPilot);
        // Share the exhaust's smoothed speed fraction so the engine and power strands build together.
        float speed = Mth.clamp(ship.getExhaustStrength(1), 0, 1);
        float targetVolume = audible ? Mth.sqrt(speed) * (relative ? .65f : .4f) : 0;
        volume = Mth.approach(volume, targetVolume, .03f);
        pitch = Mth.approach(pitch, .85f + .3f * speed, .01f);
        if (volume == 0 && targetVolume == 0) {
            stop();
        }
    }

    private void updatePosition() {
        x = ship.getX();
        y = ship.getY();
        z = ship.getZ();
    }
}
