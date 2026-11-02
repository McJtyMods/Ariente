package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.BlockInventoryInteraction;
import mcjty.ariente.blocks.utility.PacketClickStorage;
import mcjty.ariente.network.ArienteMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Creative slot clicks consume the attack gesture before Minecraft can break a block. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class BlockInventoryInput {

    private static boolean consumingAttack;

    @SubscribeEvent
    public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
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
            ArienteMessages.INSTANCE.sendToServer(new PacketClickStorage(hit.getBlockPos(), slot));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null || mc.screen != null || !mc.options.keyAttack.isDown()) {
                consumingAttack = false;
            }
        }
    }
}
