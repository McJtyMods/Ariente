package mcjty.ariente.varia;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class Messages {

    private Messages() {}

    public static void player(Player player, Component message, boolean overlay) {
        if (overlay) player.displayClientMessage(message, true);
        else player.sendSystemMessage(message);
    }
}
