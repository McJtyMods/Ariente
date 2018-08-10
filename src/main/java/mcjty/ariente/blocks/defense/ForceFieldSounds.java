package mcjty.ariente.blocks.defense;

import mcjty.ariente.sounds.ForcefieldSound;
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
        if (minDistance < 5 * 5) {
            ForcefieldSound soundAt = SoundController.getForcefieldSoundAt(world, pos);
            minDistance = Math.sqrt(minDistance);

            float volume = (float) ((5f - minDistance) * 2.0f / 5.0f);
            if (soundAt != null) {
                soundAt.setVolume(volume);
            } else {
                SoundController.playForcefieldSound(world, pos, ModSounds.forcefield, volume, 20);
            }
        } else {
            SoundController.stopForcefieldSounds(world, pos);
        }
    }
}
