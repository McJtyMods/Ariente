package mcjty.ariente.blocks.storagecube;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PacketStorageCubeAction(BlockPos pos, int entityId, Action action, int source, int destination,
                                      int amount, long revision) {

    public enum Action { SUBSCRIBE, CLOSE, DISMISS, TRANSFER, QUICK_MOVE, SORT_NAME, SORT_COUNT }

    public PacketStorageCubeAction(BlockPos pos, int entityId, Action action) {
        this(pos, entityId, action, -1, -1, 0, 0);
    }

    public PacketStorageCubeAction(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt(), buffer.readEnum(Action.class), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarLong());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(entityId);
        buffer.writeEnum(action);
        buffer.writeVarInt(source);
        buffer.writeVarInt(destination);
        buffer.writeVarInt(amount);
        buffer.writeVarLong(revision);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.level().hasChunkAt(pos)
                    || !(player.level().getBlockEntity(pos) instanceof StorageCubeTile tile)) {
                return;
            }
            if (action == Action.CLOSE) {
                tile.unsubscribe(player, entityId);
            } else if (tile.canUse(player, entityId)) {
                if (action == Action.DISMISS) {
                    tile.unsubscribe(player, entityId);
                    ((HoloGuiEntity) player.level().getEntity(entityId)).discard();
                    return;
                }
                if (action == Action.SUBSCRIBE) {
                    tile.subscribe(player, entityId);
                } else if (action == Action.TRANSFER || action == Action.QUICK_MOVE) {
                    tile.transfer(player, entityId, revision, source, destination, amount, action == Action.QUICK_MOVE);
                    tile.refresh(player, entityId);
                } else if (action == Action.SORT_NAME || action == Action.SORT_COUNT) {
                    tile.sort(player, entityId, revision, action == Action.SORT_COUNT);
                    tile.refresh(player, entityId);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
