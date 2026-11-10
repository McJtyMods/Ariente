package mcjty.ariente.network;

import mcjty.lib.network.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PacketHitForcefield implements CustomPacketPayload {

    public static final Type<PacketHitForcefield> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_hit_forcefield"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketHitForcefield> CODEC = StreamCodec.ofMember(PacketHitForcefield::toBytes, PacketHitForcefield::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
    }

    public PacketHitForcefield() {
    }

    public PacketHitForcefield(RegistryFriendlyByteBuf buf) {
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player playerEntity = (ServerPlayer) ctx.player();
            // @todo

        });
    }
}
