package mcjty.ariente.display;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Sends intent only. The server redoes the ray, occlusion, footprint and overlap checks. */
public record PacketDisplayAction(BlockPos base, InteractionHand hand, boolean leftClick) implements CustomPacketPayload {

    public static final Type<PacketDisplayAction> TYPE = new Type<>(Identifier.fromNamespaceAndPath("ariente", "packet_display_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketDisplayAction> CODEC = StreamCodec.ofMember(PacketDisplayAction::toBytes, PacketDisplayAction::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public PacketDisplayAction(RegistryFriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readEnum(InteractionHand.class), buffer.readBoolean());
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(base);
        buffer.writeEnum(hand);
        buffer.writeBoolean(leftClick);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null || player.isSpectator() || !player.level().hasChunkAt(base)
                    || !(player.level().getBlockEntity(base) instanceof DisplayBaseTile tile)
                    || !player.level().mayInteract(player, base)) {
                return;
            }
            DisplayGeometry.Hit hit = DisplayGeometry.intersect(tile, player, Math.min(8, player.blockInteractionRange()));
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
    }
}
