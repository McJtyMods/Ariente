package mcjty.ariente.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorModel;
import mcjty.ariente.sounds.FluxLevitatorSounds;
import mcjty.ariente.sounds.MovingSoundLevitator;
import mcjty.ariente.sounds.MovingSoundLevitatorRiding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Geometry and sound regressions checked by the opt-in FluxCheck client run. */
public class FluxCartModelAndSoundChecks {

    public static void verify(FluxLevitatorEntity mountedCart, Minecraft mc) {
        checkGeometry();
        checkSoundAsset(mc);
        checkExteriorSound(mc);
        checkInteriorSound(mountedCart, mc);
        Ariente.setup.getLogger().info("FLUX CART MODEL AND SOUND CHECKS PASSED");
    }

    private static void checkGeometry() {
        BufferBuilder builder = new BufferBuilder(16384);
        builder.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.NEW_ENTITY);
        new FluxLevitatorModel<>().renderToBuffer(new PoseStack(), builder, 0, 0, 1, 1, 1, 1);
        var rendered = builder.end();
        List<Vec3> vertices = new ArrayList<>();
        try {
            var bytes = rendered.vertexBuffer();
            int stride = rendered.drawState().format().getVertexSize();
            for (int i = 0; i < rendered.drawState().vertexCount(); i++) {
                int offset = i * stride;
                vertices.add(new Vec3(bytes.getFloat(offset), bytes.getFloat(offset + 4), bytes.getFloat(offset + 8)));
            }
        } finally {
            rendered.release();
        }
        require(vertices.size() == 240, "The cart must retain all ten original model parts");
        // Original model dimensions in pixels, before the renderer's axis flip.
        // These corners ensure both broad surfaces lie flat around an open cabin.
        checkCorners(vertices, 21, 3, 5, 9.6, "floor");
        checkCorners(vertices, 18.9, -17, -15, 8.8, "roof");
        for (int side : new int[]{-1, 1}) {
            for (float x : new float[]{19, 21}) {
                require(hasVertex(vertices, side * x, -6, -8)
                                && hasVertex(vertices, side * x, 3, 8),
                        "End panels must face each other around the cabin");
            }
        }
        require(vertices.stream().allMatch(v -> Math.abs(v.x) <= 21.001 / 16
                        && Math.abs(v.z) <= 9.601 / 16 && v.y >= -17.001 / 16 && v.y <= 5.001 / 16),
                "No panel or support may project beyond the original cart bounds");
    }

    private static void checkCorners(List<Vec3> vertices, double x, double minY, double maxY, double z, String part) {
        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                require(hasVertex(vertices, sx * x, minY, sz * z) && hasVertex(vertices, sx * x, maxY, sz * z),
                        "The " + part + " must have its original horizontal dimensions");
            }
        }
    }

    private static boolean hasVertex(List<Vec3> vertices, double x, double y, double z) {
        Vec3 expected = new Vec3(x / 16, y / 16, z / 16);
        return vertices.stream().anyMatch(v -> v.distanceToSqr(expected) < 0.00000001);
    }

    private static void checkSoundAsset(Minecraft mc) {
        var resource = mc.getResourceManager().getResource(new ResourceLocation("ariente", "sounds/levitator.ogg")).orElseThrow();
        try (var input = resource.open(); var audio = new OggAudioStream(input)) {
            require(audio.getFormat().getChannels() == 1,
                    "The cart sound must be mono so OpenAL applies positional attenuation to distant carts");
        } catch (Exception e) {
            throw new IllegalStateException("Could not decode the cart sound", e);
        }
    }

    private static void checkExteriorSound(Minecraft mc) {
        var cart = FluxLevitatorEntity.create(mc.level, mc.player.getX() + 64, mc.player.getY() - 40, mc.player.getZ());
        var sound = new MovingSoundLevitator(cart);
        sound.resolve(mc.getSoundManager());
        require(sound.getVolume() == 0 && sound.canStartSilent(), "Newly tracked carts must start silently and stay tickable");
        require(sound.getX() == cart.getX() && sound.getY() == cart.getY() && sound.getZ() == cart.getZ(),
                "Sound must start at the cart, never at the world origin");
        require(sound.getAttenuation() == Attenuation.LINEAR && !sound.isRelative()
                        && sound.getSound().getAttenuationDistance() == 16,
                "Exterior cart sound must attenuate to silence at 16 blocks, including vertically");
        cart.changeSpeed(50);
        sound.tick();
        require(sound.getVolume() > 0 && sound.getVolume() < .35F, "Starting carts must fade in");
        for (int i = 0; i < 12; i++) {
            sound.tick();
        }
        require(Math.abs(sound.getVolume() - .35F) < .00001, "Moving carts must reach normal exterior volume");
        cart.changeSpeed(0);
        sound.tick();
        require(sound.getVolume() > 0 && sound.getVolume() < .35F, "Stopping carts must fade out");
        for (int i = 0; i < 12; i++) {
            sound.tick();
        }
        require(sound.getVolume() == 0 && !sound.isStopped(), "Stationary carts must stay silent without losing their loop");
        cart.changeSpeed(-50);
        cart.setPos(cart.getX() - 5, cart.getY() + 2, cart.getZ() + 3);
        sound.tick();
        require(sound.getVolume() > 0 && sound.getX() == cart.getX() && sound.getY() == cart.getY()
                        && sound.getZ() == cart.getZ(), "Reversing carts must restart sound at their current position");
        cart.setSilent(true);
        require(!sound.canPlaySound(), "Silent carts must not produce exterior sound");
        cart.discard();
        sound.tick();
        require(sound.isStopped(), "Untracked carts must stop their sound");
    }

    private static void checkInteriorSound(FluxLevitatorEntity cart, Minecraft mc) {
        var sound = new MovingSoundLevitatorRiding(mc.player, cart);
        sound.resolve(mc.getSoundManager());
        require(sound.getVolume() == 0 && sound.canStartSilent(), "Mounting a stopped cart must not produce a sound burst");
        require(sound.isRelative() && sound.getAttenuation() == Attenuation.NONE,
                "The local rider must hear the interior sound at their own position");
        int oldSpeed = cart.getSpeed();
        try {
            cart.changeSpeed(50);
            sound.tick();
            require(sound.getVolume() > 0 && sound.getVolume() < .75F, "Interior sound must fade in");
            cart.changeSpeed(0);
            for (int i = 0; i < 12; i++) {
                sound.tick();
            }
            require(sound.getVolume() == 0 && !sound.isStopped(), "Stopped interior sound must remain available for restarting");
            cart.setSilent(true);
            require(!sound.canPlaySound(), "Silent carts must not produce interior sound");
        } finally {
            cart.setSilent(false);
            cart.changeSpeed(oldSpeed);
        }
        var remote = new RemotePlayer(mc.level, new GameProfile(UUID.randomUUID(), "FluxSoundCheck"));
        var remoteSound = new MovingSoundLevitatorRiding(remote, cart);
        require(!remoteSound.canPlaySound(), "Other players must not produce a global interior sound");
        remoteSound.tick();
        require(remoteSound.isStopped(), "Interior loops must stop when their player is not the local rider");
        int[] played = {0};
        SoundEventListener listener = (instance, event) -> {
            if (instance instanceof MovingSoundLevitatorRiding) {
                played[0]++;
            }
        };
        mc.getSoundManager().addListener(listener);
        try {
            FluxLevitatorSounds.playMovingSoundClientInside(remote, cart);
            require(played[0] == 0, "Remote mounts must not queue an interior loop at all");
        } finally {
            mc.getSoundManager().removeListener(listener);
        }
        var unmounted = new MovingSoundLevitatorRiding(mc.player,
                FluxLevitatorEntity.create(mc.level, cart.getX(), cart.getY(), cart.getZ()));
        unmounted.tick();
        require(unmounted.isStopped(), "Dismounting must stop interior sound");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
