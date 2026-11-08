package mcjty.ariente.entities.fluxship;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketShipAction implements CustomPacketPayload {

    public static final Type<PacketShipAction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_ship_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketShipAction> CODEC = StreamCodec.ofMember(PacketShipAction::toBytes, PacketShipAction::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

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

    public PacketShipAction(RegistryFriendlyByteBuf buf) {
        this(buf.readByte(), buf.readByte(), buf.readByte(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readFloat(), buf.readFloat());
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeByte(forward);
        buf.writeByte(turn);
        buf.writeByte(vertical);
        buf.writeBoolean(firing);
        buf.writeBoolean(mouseFlight);
        buf.writeBoolean(mouseSteering);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (player != null && player.getVehicle() instanceof FluxShipEntity ship) {
                ship.handleInput(player, forward, turn, vertical, firing, mouseFlight, mouseSteering, yaw, pitch);
            }
        });
    }
}
