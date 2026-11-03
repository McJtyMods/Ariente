package mcjty.ariente.entities.levitator;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketLevitatorInput {

    private final int input;

    public PacketLevitatorInput(int input) {
        this.input = input;
    }

    public PacketLevitatorInput(FriendlyByteBuf buffer) {
        this(buffer.readByte());
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeByte(input);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getVehicle() instanceof FluxLevitatorEntity cart) {
                cart.handleInput(player, input);
            }
        });
        context.setPacketHandled(true);
    }
}
