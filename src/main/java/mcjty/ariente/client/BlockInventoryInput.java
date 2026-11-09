package mcjty.ariente.client;

import mcjty.lib.network.ClientNetworking;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.BlockInventoryInteraction;
import mcjty.ariente.blocks.utility.PacketClickStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;

/** Creative slot clicks consume the attack gesture before Minecraft can break a block. */
public class BlockInventoryInput {

    private static boolean consumingAttack;

    public static void attack(InputGesture event) {
        if (!event.isAttack()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (consumingAttack) {
            event.setCanceled(true);
            event.setSwingHand(false);
            return;
        }
        if (mc.player == null || mc.level == null || mc.screen != null || !mc.player.isCreative()
                || !(mc.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        int slot = BlockInventoryInteraction.getSlot(mc.level, hit);
        if (slot >= 0) {
            consumingAttack = true;
            event.setCanceled(true);
            event.setSwingHand(false);
            ClientNetworking.sendToServer(new PacketClickStorage(hit.getBlockPos(), slot));
        }
    }

    public static void tick() {
        if (true) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null || mc.screen != null || !mc.options.keyAttack.isDown()) {
                consumingAttack = false;
            }
        }
    }
}
