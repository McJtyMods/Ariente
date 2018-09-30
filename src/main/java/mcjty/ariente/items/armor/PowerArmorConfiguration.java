package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.ModGuis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PowerArmorConfiguration {

    public static void openConfigurationGui(EntityPlayer player) {
        World world = player.getEntityWorld();
        BlockPos pos = player.getPosition();
        Ariente.guiHandler.openHoloGui(world, pos, player, ModGuis.GUI_ARMOR, 1f);
    }
}
