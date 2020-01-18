package mcjty.ariente.sounds;

import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

public class FluxLevitatorSounds {

    public static void playMovingSoundClient(FluxLevitatorEntity entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundLevitator(entity));
    }

    public static void playMovingSoundClientInside(PlayerEntity player, FluxLevitatorEntity entity) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundLevitatorRiding(player, entity));
    }
}
