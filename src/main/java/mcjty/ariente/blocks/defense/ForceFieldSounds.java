package mcjty.ariente.blocks.defense;

import mcjty.ariente.sounds.ArienteSound;
import mcjty.ariente.sounds.ModSounds;
import mcjty.ariente.sounds.SoundController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
                        double sqdist = info.getSquaredDistance(player.getPositionVector(), forcefield.getScale());
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
        if (minDistance < 30 * 30) {
            Vec3d closestPoint = minInfo.getClosestPoint(player.getPositionVector(), forcefield.getScale());
            System.out.println("closestPoint = " + closestPoint);
            ArienteSound soundAt = SoundController.getSoundAt(world, pos);
            if (soundAt != null) {
//                soundAt.update();
                // Update volume!
            } else {
                SoundController.playSound(world, pos, ModSounds.forcefield, 10.0f, 20);
            }
        } else {
            SoundController.stopSound(world, pos);
        }
    }
}
