package mcjty.ariente.display;

import java.util.UUID;
import mcjty.ariente.display.client.DisplayModuleDrag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import mcjty.lib.network.PayloadContext;

public record PacketDisplayGestureResult(UUID ticket, BlockPos target, int x, int y) implements CustomPacketPayload {

    public static final Type<PacketDisplayGestureResult> TYPE = new Type<>(Identifier.fromNamespaceAndPath("ariente", "packet_display_gesture_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketDisplayGestureResult> CODEC = StreamCodec.ofMember(PacketDisplayGestureResult::toBytes, PacketDisplayGestureResult::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public PacketDisplayGestureResult(RegistryFriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readBoolean() ? buffer.readBlockPos() : null, buffer.readVarInt(), buffer.readVarInt());
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(ticket);
        buffer.writeBoolean(target != null);
        if (target != null) {
            buffer.writeBlockPos(target);
        }
        buffer.writeVarInt(x);
        buffer.writeVarInt(y);
    }

    public void handle(PayloadContext context) {
        context.enqueueWork(() -> DisplayModuleDrag.complete(ticket, target, x, y));
    }
}
