package mcjty.ariente.blocks.defense;

import mcjty.ariente.sounds.ArienteSound;
import mcjty.ariente.sounds.ModSounds;
import mcjty.ariente.sounds.SoundController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class ForceFieldSounds {

    public static void doSounds(ForceFieldTile forcefield) {
        BlockPos pos = forcefield.getPos();
        WorldClient world = Minecraft.getMinecraft().world;
        EntityPlayer player = Minecraft.getMinecraft().player;
        double minDistance = 100000000000.0;
        PanelInfo minInfo = null;
        if (forcefield.entityNearField(player)) {
            for (PanelInfo info : forcefield.getPanelInfo()) {
                if (info != null) {
                    if (info.getLife() > 0) {
                        double sqdist = info.getSquaredDistance(player.getPositionVector(), forcefield.getScaleDouble());
                        if (sqdist < minDistance) {
                            minDistance = sqdist;
                            minInfo = info;
                        }
                    } else {
                        // Sound while building up? @todo
                    }
                }
            }
        }
        if (minDistance < 10 * 10) {
            ArienteSound soundAt = SoundController.getSoundAt(world, pos);
            minDistance = Math.sqrt(minDistance);
            float volume = (float) ((10f - minDistance) * 10.0f / 10.0f);
            if (soundAt != null) {
                soundAt.setVolume(volume);
            } else {
                SoundController.playSound(world, pos, ModSounds.forcefield, volume, 20);
            }
        } else {
            SoundController.stopSound(world, pos);
        }
    }
}
