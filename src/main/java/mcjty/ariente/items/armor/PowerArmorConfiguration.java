package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.hologui.gui.HoloGuiSounds;
import mcjty.ariente.gui.ModGuis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerArmorConfiguration {

    public static void openConfigurationGui(EntityPlayer player) {
        World world = player.getEntityWorld();
        BlockPos pos = player.getPosition();
        world.playSound(player, pos, HoloGuiSounds.guiopen, SoundCategory.PLAYERS, 1.0f, 1.0f);

        Ariente.guiHandler.openHoloGui(world, pos, player, ModGuis.GUI_ARMOR, 1f);
    }
}
