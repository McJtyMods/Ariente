package mcjty.ariente.entities.fluxship;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketShipAction {

    private FlyAction action;

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(action.ordinal());
    }

    public PacketShipAction() {
    }

    public PacketShipAction(PacketBuffer buf) {
        action = FlyAction.values()[buf.readInt()];
    }

    public PacketShipAction(FlyAction action) {
        this.action = action;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            Entity ridingEntity = player.getRidingEntity();
            if (ridingEntity instanceof FluxShipEntity) {
                ((FluxShipEntity) ridingEntity).handleAction(action);
            }
        });
        ctx.setPacketHandled(true);
    }
}
