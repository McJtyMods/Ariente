package mcjty.ariente.entities.fluxship;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketShipAction {

    private final int forward;
    private final int turn;
    private final int vertical;
    private final boolean firing;

    public PacketShipAction(int forward, int turn, int vertical) {
        this(forward, turn, vertical, false);
    }

    public PacketShipAction(int forward, int turn, int vertical, boolean firing) {
        this.forward = forward;
        this.turn = turn;
        this.vertical = vertical;
        this.firing = firing;
    }

    public PacketShipAction(FriendlyByteBuf buf) {
        this(buf.readByte(), buf.readByte(), buf.readByte(), buf.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(forward);
        buf.writeByte(turn);
        buf.writeByte(vertical);
        buf.writeBoolean(firing);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getVehicle() instanceof FluxShipEntity ship) {
                ship.handleInput(player, forward, turn, vertical, firing);
            }
        });
        context.setPacketHandled(true);
    }
}
