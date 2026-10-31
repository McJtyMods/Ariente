package mcjty.ariente.items.armor;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.ModGuis;
import mcjty.hologui.api.IHoloGuiEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class PowerArmorConfiguration {

    // Store UUIDs so the cache does not keep players or their worlds alive through GUI entities.
    private static final Map<Player, UUID> OPEN_GUIS = new WeakHashMap<>();

    public static void openConfigurationGui(Player player) {
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        UUID guiId = OPEN_GUIS.get(player);
        Entity entity = guiId == null ? null : level.getEntity(guiId);
        if (entity instanceof IHoloGuiEntity gui && !entity.isRemoved()) {
            if (ModGuis.GUI_ARMOR.equals(gui.getGuiId())) {
                entity.discard();
                OPEN_GUIS.remove(player);
            } else {
                gui.switchGui(ModGuis.GUI_ARMOR);
                gui.setTimeout(20 * 4);
            }
            return;
        }
        IHoloGuiEntity gui = Ariente.guiHandler.openHoloGui(player, ModGuis.GUI_ARMOR, 1f);
        OPEN_GUIS.put(player, gui.getEntity().getUUID());
    }
}
