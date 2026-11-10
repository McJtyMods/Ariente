package mcjty.ariente.blocks.utility;

import mcjty.lib.network.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PacketClickStorage implements CustomPacketPayload {

    public static final Type<PacketClickStorage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_click_storage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketClickStorage> CODEC = StreamCodec.ofMember(PacketClickStorage::toBytes, PacketClickStorage::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private BlockPos pos;
    private int index;

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(index);
    }

    public PacketClickStorage() {
    }

    public PacketClickStorage(RegistryFriendlyByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        index = buf.readInt();
    }

    public PacketClickStorage(BlockPos pos, int index) {
        this.pos = pos;
        this.index = index;
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = (ServerPlayer) ctx.player();
            if (player != null) {
                BlockInventoryInteraction.extract(player, pos, index);
            }
        });
    }
}
