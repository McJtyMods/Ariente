package mcjty.ariente.display;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sends intent only. The server redoes the ray, occlusion, footprint and overlap checks. */
public record PacketDisplayAction(BlockPos base, InteractionHand hand, boolean leftClick) {

    public PacketDisplayAction(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readEnum(InteractionHand.class), buffer.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(base);
        buffer.writeEnum(hand);
        buffer.writeBoolean(leftClick);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator() || !player.level().hasChunkAt(base)
                    || !(player.level().getBlockEntity(base) instanceof DisplayBaseTile tile)
                    || !player.level().mayInteract(player, base)) {
                return;
            }
            DisplayGeometry.Hit hit = DisplayGeometry.intersect(tile, player, Math.min(8, player.getBlockReach()));
            if (hit == null) {
                return;
            }
            DisplayGeometry.ModuleHit module = DisplayGeometry.moduleAt(hit);
            if (!leftClick && player.isShiftKeyDown()) {
                if (module != null) {
                    module.owner().removeModule(player, module.module());
                }
            } else if (!leftClick && module != null) {
                // Installed modules use the press/release gesture packet, so configuration never opens on press.
                return;
            } else if (!leftClick && player.getItemInHand(hand).getItem() instanceof DisplayModuleItem) {
                tile.install(player, hand, hit);
            } else if (module != null) {
                module.module().type().interact(player, module.module().stack(), module.module().snapshot(),
                        leftClick, module.x(), module.y());
            }
        });
        context.setPacketHandled(true);
    }
}
