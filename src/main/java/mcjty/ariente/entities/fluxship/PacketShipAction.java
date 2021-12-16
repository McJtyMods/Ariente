package mcjty.ariente.entities.fluxship;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketShipAction {

    private FlyAction action;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(action.ordinal());
    }

    public PacketShipAction() {
    }

    public PacketShipAction(FriendlyByteBuf buf) {
        action = FlyAction.values()[buf.readInt()];
    }

    public PacketShipAction(FlyAction action) {
        this.action = action;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Player player = ctx.getSender();
            Entity ridingEntity = player.getVehicle();
            if (ridingEntity instanceof FluxShipEntity) {
                ((FluxShipEntity) ridingEntity).handleAction(action);
            }
        });
        ctx.setPacketHandled(true);
    }
}
