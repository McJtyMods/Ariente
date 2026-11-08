package mcjty.ariente.items.armor;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketConfigureArmor implements CustomPacketPayload {

    public static final Type<PacketConfigureArmor> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_configure_armor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketConfigureArmor> CODEC = StreamCodec.ofMember(PacketConfigureArmor::toBytes, PacketConfigureArmor::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
    }

    public PacketConfigureArmor() {
    }

    public PacketConfigureArmor(RegistryFriendlyByteBuf buf) {
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player playerEntity = (ServerPlayer) ctx.player();
            PowerArmorConfiguration.openConfigurationGui(playerEntity);
        });
    }
}
