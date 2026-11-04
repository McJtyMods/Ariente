package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipLaser;
import mcjty.ariente.entities.fluxship.FluxShipWeaponRender;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/** Exercises real held-key packets and fast projectile spawn packets in the opt-in client fixture. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipWeaponChecks {

    private static volatile UUID shipId;
    private static final ConcurrentLinkedQueue<Shot> SHOTS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<LaserEntity> CLIENT_PENDING = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger CLIENT_SHOTS = new AtomicInteger();
    private static final AtomicInteger SOUNDS = new AtomicInteger();
    private static int releasedCount;

    private record Shot(int tick, double directionError, double muzzleError, double speed) {
    }

    @SubscribeEvent
    public static void projectile(EntityJoinLevelEvent event) {
        if (!Boolean.getBoolean("ariente.shipClientCheck") || shipId == null
                || !(event.getEntity() instanceof LaserEntity laser)) {
            return;
        }
        if (event.getLevel().isClientSide) {
            // Forge adds the entity before it applies the additional spawn data.
            CLIENT_PENDING.add(laser);
        } else if (!event.loadedFromDisk() && laser.isShipShot() && laser.getOwner() != null
                && laser.getOwner().getVehicle() instanceof FluxShipEntity ship
                && ship.getUUID().equals(shipId)) {
            SHOTS.add(new Shot(ship.tickCount,
                    laser.getBeamDirection().distanceTo(FluxShipLaser.direction(ship, 1)),
                    laser.position().distanceTo(FluxShipLaser.muzzle(ship, 1)), laser.getDeltaMovement().length()));
        }
    }

    @SubscribeEvent
    public static void sound(PlaySoundEvent event) {
        if (Boolean.getBoolean("ariente.shipClientCheck") && shipId != null && event.getSound() != null
                && event.getSound().getLocation().equals(ModSounds.laser.get().getLocation())) {
            SOUNDS.incrementAndGet();
        }
    }

    public static void begin(FluxShipEntity ship) {
        shipId = ship.getUUID();
        Minecraft mc = Minecraft.getInstance();
        require(mc.getModelManager().getModel(FluxShipWeaponRender.MODEL) != mc.getModelManager().getMissingModel(),
                "The mounted cannon must have its own baked front model");
    }

    public static void observeSpawns() {
        LaserEntity laser;
        while ((laser = CLIENT_PENDING.poll()) != null) {
            if (laser.isShipShot()) {
                require(Math.abs(laser.getDeltaMovement().length() - FluxShipLaser.SHOT_SPEED) < .00001,
                        "Real Forge spawn packets must preserve the cannon's full projectile speed");
                CLIENT_SHOTS.incrementAndGet();
            }
        }
    }

    public static void released(FluxShipEntity ship) {
        require(SHOTS.size() >= 5 && SHOTS.size() <= 10, "Holding V must rapidly fire through the real input path: " + SHOTS.size());
        require(CLIENT_SHOTS.get() >= 3 && SOUNDS.get() >= 1,
                "The pilot must receive laser visuals and the existing laser sound: spawns=" + CLIENT_SHOTS + ", sounds=" + SOUNDS);
        releasedCount = SHOTS.size();
        checkSight(ship);
    }

    public static void beforeFreeLook() {
        require(SHOTS.size() == releasedCount, "Releasing V must stop server firing");
    }

    public static void complete(FluxShipEntity ship) {
        require(SHOTS.size() > releasedCount, "The cannon must keep firing in third person while looking sideways");
        int previous = Integer.MIN_VALUE / 2;
        for (Shot shot : SHOTS) {
            require(shot.tick - previous >= FluxShipLaser.SHOT_INTERVAL && shot.directionError < .00001
                            && shot.muzzleError < .00001 && Math.abs(shot.speed - FluxShipLaser.SHOT_SPEED) < .00001,
                    "Every shot must respect the server cooldown and follow the physical hull and muzzle: " + shot);
            previous = shot.tick;
        }
        checkSight(ship);
        Ariente.setup.getLogger().info("FLUX SHIP WEAPON CHECKS PASSED: {} server shots, {} client spawns, {} laser sounds; cooldown, hull aim, muzzle, speed, reticle and model",
                SHOTS.size(), CLIENT_SHOTS.get(), SOUNDS.get());
    }

    private static void checkSight(FluxShipEntity ship) {
        Vec3 eye = Minecraft.getInstance().player.getEyePosition();
        Vec3 sight = FluxShipLaser.sightOnPanel(ship, 1);
        Vec3 target = FluxShipLaser.target(ship, 1);
        require(sight.subtract(eye).normalize().dot(target.subtract(eye).normalize()) > .99999,
                "The hologram reticle must project onto the cannon's target from the actual seated eye");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
