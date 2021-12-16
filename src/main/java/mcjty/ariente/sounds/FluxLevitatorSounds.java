package mcjty.ariente.sounds;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class FluxLevitatorSounds {

    public static void playMovingSoundClient(FluxLevitatorEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new MovingSoundLevitator(entity));
    }

    public static void playMovingSoundClientInside(Player player, FluxLevitatorEntity entity) {
        Minecraft.getInstance().getSoundManager().play(new MovingSoundLevitatorRiding(player, entity));
    }
}
