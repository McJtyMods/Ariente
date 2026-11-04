package mcjty.ariente.sounds;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipSounds {

    private static final Map<FluxShipEntity, EngineLoops> SHIPS = new IdentityHashMap<>();
    private static int ticks;

    @SubscribeEvent
    public static void entityJoined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof FluxShipEntity ship) {
            SHIPS.computeIfAbsent(ship, key -> new EngineLoops());
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.level != null && mc.isPaused()) {
            return;
        }
        ticks++;
        SoundManager manager = mc.getSoundManager();
        boolean enabled = mc.options.getSoundSourceVolume(SoundSource.MASTER) > 0
                && mc.options.getSoundSourceVolume(SoundSource.NEUTRAL) > 0;
        var iterator = SHIPS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            FluxShipEntity ship = entry.getKey();
            EngineLoops loops = entry.getValue();
            if (!ship.isAlive() || ship.level() != mc.level) {
                if (loops.exterior != null) {
                    manager.stop(loops.exterior);
                }
                if (loops.interior != null) {
                    manager.stop(loops.interior);
                }
                iterator.remove();
                continue;
            }
            boolean localPilot = mc.player != null && mc.player.getVehicle() == ship;
            boolean flying = enabled && !ship.isSilent() && ship.getExhaustStrength(1) > .001f;
            loops.exterior = update(loops.exterior, ship, null, flying && !localPilot, manager);
            loops.interior = update(loops.interior, ship, mc.player, flying && localPilot, manager);
        }
    }

    private static MovingSoundFluxShip update(@Nullable MovingSoundFluxShip sound, FluxShipEntity ship,
                                              @Nullable Player pilot, boolean wanted, SoundManager manager) {
        if (wanted) {
            // A lost audio channel or resource reload must not leave an already tracked ship permanently silent.
            if (sound == null || sound.isStopped() || !manager.isActive(sound) && ticks % 20 == 0) {
                sound = new MovingSoundFluxShip(pilot, ship);
                manager.play(sound);
            }
            sound.setActive(true);
        } else if (sound != null) {
            sound.setActive(false);
            if (sound.isStopped() || !manager.isActive(sound)) {
                return null;
            }
        }
        return sound;
    }

    private static class EngineLoops {

        private MovingSoundFluxShip exterior;
        private MovingSoundFluxShip interior;
    }
}
