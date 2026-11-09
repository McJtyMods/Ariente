package mcjty.ariente.entities.levitator;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import mcjty.lib.network.PayloadContext;

public class PacketLevitatorInput implements CustomPacketPayload {

    public static final Type<PacketLevitatorInput> TYPE = new Type<>(Identifier.fromNamespaceAndPath("ariente", "packet_levitator_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketLevitatorInput> CODEC = StreamCodec.ofMember(PacketLevitatorInput::toBytes, PacketLevitatorInput::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private final int input;

    public PacketLevitatorInput(int input) {
        this.input = input;
    }

    public PacketLevitatorInput(RegistryFriendlyByteBuf buffer) {
        this(buffer.readByte());
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeByte(input);
    }

    public void handle(PayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null && player.getVehicle() instanceof FluxLevitatorEntity cart) {
                cart.handleInput(player, input);
            }
        });
    }
}
