package mcjty.ariente.blocks.storagecube;

import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketStorageCubeAction(BlockPos pos, int entityId, Action action, int source, int destination,
                                      int amount, long revision) implements CustomPacketPayload {

    public static final Type<PacketStorageCubeAction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_storage_cube_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketStorageCubeAction> CODEC = StreamCodec.ofMember(PacketStorageCubeAction::toBytes, PacketStorageCubeAction::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action { SUBSCRIBE, CLOSE, DISMISS, TRANSFER, QUICK_MOVE, SORT_NAME, SORT_COUNT }

    public PacketStorageCubeAction(BlockPos pos, int entityId, Action action) {
        this(pos, entityId, action, -1, -1, 0, 0);
    }

    public PacketStorageCubeAction(RegistryFriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt(), buffer.readEnum(Action.class), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readVarLong());
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeVarInt(entityId);
        buffer.writeEnum(action);
        buffer.writeVarInt(source);
        buffer.writeVarInt(destination);
        buffer.writeVarInt(amount);
        buffer.writeVarLong(revision);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
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
    }
}
