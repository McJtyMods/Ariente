package mcjty.ariente.items.armor;

import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketPersonalForcefield {

    private final int entityId;
    private final long cooldownUntil;
    private final long lastHit;

    public PacketPersonalForcefield(int entityId, long cooldownUntil, long lastHit) {
        this.entityId = entityId;
        this.cooldownUntil = cooldownUntil;
        this.lastHit = lastHit;
    }

    public PacketPersonalForcefield(FriendlyByteBuf buf) {
        entityId = buf.readVarInt();
        cooldownUntil = buf.readLong();
        lastHit = buf.readLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeLong(cooldownUntil);
        buf.writeLong(lastHit);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ForceFieldRenderer.updatePersonalField(entityId, cooldownUntil, lastHit)));
        ctx.setPacketHandled(true);
    }
}
