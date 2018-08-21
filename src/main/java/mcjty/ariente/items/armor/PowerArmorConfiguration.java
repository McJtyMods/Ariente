package mcjty.ariente.items.armor;

import mcjty.ariente.gui.HoloGuiHandler;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerArmorConfiguration {

    public static void openConfigurationGui(EntityPlayer player) {
        World world = player.getEntityWorld();
        BlockPos pos = player.getPosition();
        world.playSound(player, pos, ModSounds.guiopen, SoundCategory.PLAYERS, 1.0f, 1.0f);

        HoloGuiHandler.openHoloGui(world, pos, player, ModGuis.GUI_ARMOR, 1f);
    }
}
