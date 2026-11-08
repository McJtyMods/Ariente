package mcjty.ariente.entities.levitator;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketLevitatorInput implements CustomPacketPayload {

    public static final Type<PacketLevitatorInput> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_levitator_input"));
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

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null && player.getVehicle() instanceof FluxLevitatorEntity cart) {
                cart.handleInput(player, input);
            }
        });
    }
}
