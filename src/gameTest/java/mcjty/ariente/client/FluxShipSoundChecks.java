package mcjty.ariente.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.audio.OggAudioStream;
import mcjty.ariente.Ariente;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.sounds.FluxShipSounds;
import mcjty.ariente.sounds.MovingSoundFluxShip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Exercise the real engine loop, live pitch updates, channel recovery and positional playback. */
public class FluxShipSoundChecks {

    private static final List<MovingSoundFluxShip> PLAYED = new ArrayList<>();
    private static final SoundEventListener LISTENER = (instance, event) -> {
        if (instance instanceof MovingSoundFluxShip sound) {
            PLAYED.add(sound);
        }
    };
    private static float cruisePitch;
    private static float cruiseVolume;

    public static void begin(FluxShipEntity ship) {
        Minecraft mc = Minecraft.getInstance();
        mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(1.0);
        mc.options.getSoundSourceOptionInstance(SoundSource.NEUTRAL).set(1.0);
        mc.getSoundManager().addListener(LISTENER);
        var resource = mc.getResourceManager().getResource(new ResourceLocation("ariente", "sounds/levitator.ogg")).orElseThrow();
        try (var input = resource.open(); var audio = new OggAudioStream(input)) {
            require(audio.getFormat().getChannels() == 1, "The reused cart sound must support positional attenuation");
        } catch (Exception exception) {
            throw new IllegalStateException("Could not decode the ship's engine sound", exception);
        }
        checkEnvelopeAndLifecycle(mc);
        require(PLAYED.stream().noneMatch(s -> s.getShip() == ship), "A stationary ship must not start an engine loop");
    }

    public static void cruise(FluxShipEntity ship) {
        List<MovingSoundFluxShip> active = active(ship);
        require(active.size() == 1 && active.get(0).isRelative()
                        && active.get(0).getAttenuation() == Attenuation.NONE,
                "The pilot must hear exactly one cockpit loop without positional volume flutter");
        var sound = active.get(0);
        cruisePitch = sound.getPitch();
        cruiseVolume = sound.getVolume();
        require(cruisePitch > 1.05f && cruisePitch <= 1.151f && cruiseVolume > .5f && cruiseVolume <= .651f,
                "Cruising must raise the engine pitch and volume: pitch=" + cruisePitch + ", volume=" + cruiseVolume);
        // Losing a channel must recover without requiring the ship to be reloaded or remounted.
        Minecraft.getInstance().getSoundManager().stop(sound);
    }

    public static void recovered(FluxShipEntity ship) {
        require(PLAYED.stream().filter(s -> s.getShip() == ship).count() >= 2 && active(ship).size() == 1,
                "A lost engine channel must restart exactly once for a ship already in flight");
    }

    public static void braking(FluxShipEntity ship) {
        List<MovingSoundFluxShip> active = active(ship);
        require(active.size() == 1 && active.get(0).getPitch() < cruisePitch - .1f
                        && active.get(0).getVolume() > 0 && active.get(0).getVolume() < cruiseVolume / 2,
                "Braking must smoothly lower the engine pitch and volume");
    }

    public static void stopped(FluxShipEntity ship) {
        require(active(ship).isEmpty(), "Hovering at rest must finish fading and release all ship sound channels");
        Minecraft.getInstance().getSoundManager().removeListener(LISTENER);
        Ariente.setup.getLogger().info("FLUX SHIP SOUND CHECKS PASSED: cart asset, live pitch/volume, positional attenuation, cockpit isolation, duplicate tracking, channel recovery and stop/despawn cleanup");
        PLAYED.clear();
    }

    private static List<MovingSoundFluxShip> active(FluxShipEntity ship) {
        var manager = Minecraft.getInstance().getSoundManager();
        return PLAYED.stream().filter(s -> s.getShip() == ship && manager.isActive(s)).toList();
    }

    private static void checkEnvelopeAndLifecycle(Minecraft mc) {
        TestShip ship = new TestShip(mc);
        try {
            var sound = new MovingSoundFluxShip(ship);
            sound.resolve(mc.getSoundManager());
            require(sound.canStartSilent() && sound.getVolume() == 0 && sound.getPitch() == .85f,
                    "Ship engines must begin quietly with a lower cart pitch");
            require(!sound.isRelative() && sound.getAttenuation() == Attenuation.LINEAR
                            && sound.getSound().getAttenuationDistance() == 16 && sound.getX() == ship.getX(),
                    "Observers must hear a positional engine within the cart asset's 16-block range");
            ship.strength = .25f;
            sound.tick();
            require(sound.getVolume() > 0 && sound.getVolume() <= .031f && sound.getPitch() <= .861f,
                    "Starting the engine must ease volume and pitch upward");
            for (int i = 0; i < 30; i++) {
                sound.tick();
            }
            float slowPitch = sound.getPitch();
            ship.strength = 1;
            sound.tick();
            require(sound.getPitch() > slowPitch && sound.getPitch() <= slowPitch + .0101f,
                    "Speed changes must adjust the existing loop's pitch gradually");
            for (int i = 0; i < 40; i++) {
                sound.tick();
            }
            require(Math.abs(sound.getPitch() - 1.15f) < .001 && Math.abs(sound.getVolume() - .4f) < .001,
                    "Full-speed exterior sound must remain bounded");
            ship.setPos(ship.getX() + 3, ship.getY() + 2, ship.getZ() - 1);
            ship.strength = 0;
            sound.tick();
            require(sound.getVolume() > 0 && sound.getVolume() < .4f && sound.getPitch() > .85f
                            && sound.getX() == ship.getX() && sound.getY() == ship.getY() && sound.getZ() == ship.getZ(),
                    "Slowing down must fade sound at the ship's current position");
            for (int i = 0; i < 20; i++) {
                sound.tick();
            }
            require(sound.isStopped() && sound.getVolume() == 0, "A stopped engine must release its channel");
            var remote = new RemotePlayer(mc.level, new GameProfile(UUID.randomUUID(), "ShipSoundCheck"));
            var remoteInterior = new MovingSoundFluxShip(remote, ship);
            require(!remoteInterior.canPlaySound(), "Remote pilots must never create a global cockpit loop");
            remoteInterior.tick();
            require(remoteInterior.isStopped(), "A cockpit loop with the wrong listener must stop");
            ship.setSilent(true);
            var silent = new MovingSoundFluxShip(ship);
            silent.tick();
            require(!silent.canPlaySound() && silent.isStopped(), "Silent ships must remain silent");
            ship.setSilent(false);
            ship.strength = 1;
            FluxShipSounds.entityJoined(new EntityJoinLevelEvent(ship, mc.level));
            FluxShipSounds.entityJoined(new EntityJoinLevelEvent(ship, mc.level));
            FluxShipSounds.clientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
            FluxShipSounds.clientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
            require(PLAYED.stream().filter(s -> s.getShip() == ship).count() == 1 && active(ship).size() == 1
                            && !active(ship).get(0).isRelative(),
                    "Repeated tracking events must create exactly one exterior loop for an observer");
        } finally {
            ship.discard();
            for (var sound : PLAYED.stream().filter(s -> s.getShip() == ship).toList()) {
                sound.tick();
                require(sound.isStopped(), "Despawned ships must stop their engine immediately");
                mc.getSoundManager().stop(sound);
            }
            FluxShipSounds.clientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
            PLAYED.removeIf(s -> s.getShip() == ship);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static class TestShip extends FluxShipEntity {

        private float strength;

        private TestShip(Minecraft mc) {
            super(Registration.ENTITY_FLUX_SHIP.get(), mc.level);
            setPos(mc.player.getX() + 4, mc.player.getY(), mc.player.getZ());
        }

        @Override
        public float getExhaustStrength(float partialTick) {
            return strength;
        }
    }
}
