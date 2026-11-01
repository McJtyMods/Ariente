package mcjty.ariente.display;

import mcjty.ariente.network.ArienteMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Drag positions stay local; only press, release, cancellation and infrequent keepalives are sent. */
public record PacketDisplayGesture(BlockPos source, UUID module, UUID ticket, Action action, float yaw, float pitch, boolean dragged, boolean allowDrop) {

    public PacketDisplayGesture(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readUUID(), buffer.readUUID(), buffer.readEnum(Action.class), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(source);
        buffer.writeUUID(module);
        buffer.writeUUID(ticket);
        buffer.writeEnum(action);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(dragged);
        buffer.writeBoolean(allowDrop);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (action == Action.CANCEL) {
                DisplayGesture.cancel(player, ticket);
                return;
            }
            if (!Float.isFinite(yaw) || !Float.isFinite(pitch) || pitch < -90 || pitch > 90) {
                return;
            }
            Vec3 look = Vec3.directionFromRotation(pitch, Mth.wrapDegrees(yaw));
            if (action == Action.BEGIN) {
                if (!DisplayGesture.begin(player, source, module, ticket, look)) {
                    reply(player, null);
                }
            } else if (action == Action.KEEP_ALIVE) {
                DisplayGesture.keepAlive(player, source, module, ticket);
            } else {
                reply(player, DisplayGesture.finish(player, source, module, ticket, look, dragged, allowDrop));
            }
        });
        context.setPacketHandled(true);
    }

    private void reply(ServerPlayer player, DisplayGeometry.DragPosition moved) {
        ArienteMessages.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketDisplayGestureResult(ticket, moved == null ? null : moved.target().getBlockPos(),
                        moved == null ? 0 : moved.x(), moved == null ? 0 : moved.y()));
    }

    public enum Action {
        BEGIN, RELEASE, CANCEL, KEEP_ALIVE
    }
}
