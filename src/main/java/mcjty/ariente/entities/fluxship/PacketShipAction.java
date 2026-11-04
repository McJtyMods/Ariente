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
    private final boolean mouseFlight;
    private final boolean mouseSteering;
    private final float yaw;
    private final float pitch;

    public PacketShipAction(int forward, int turn, int vertical) {
        this(forward, turn, vertical, false);
    }

    public PacketShipAction(int forward, int turn, int vertical, boolean firing) {
        this(forward, turn, vertical, firing, false, false, 0, 0);
    }

    public PacketShipAction(int forward, int turn, int vertical, boolean firing, boolean mouseFlight,
                            boolean mouseSteering, float yaw, float pitch) {
        this.forward = forward;
        this.turn = turn;
        this.vertical = vertical;
        this.firing = firing;
        this.mouseFlight = mouseFlight;
        this.mouseSteering = mouseSteering;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public PacketShipAction(FriendlyByteBuf buf) {
        this(buf.readByte(), buf.readByte(), buf.readByte(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readFloat(), buf.readFloat());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(forward);
        buf.writeByte(turn);
        buf.writeByte(vertical);
        buf.writeBoolean(firing);
        buf.writeBoolean(mouseFlight);
        buf.writeBoolean(mouseSteering);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getVehicle() instanceof FluxShipEntity ship) {
                ship.handleInput(player, forward, turn, vertical, firing, mouseFlight, mouseSteering, yaw, pitch);
            }
        });
        context.setPacketHandled(true);
    }
}
