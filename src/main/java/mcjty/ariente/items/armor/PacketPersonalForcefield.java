package mcjty.ariente.items.armor;

import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.lib.network.PayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class PacketPersonalForcefield implements CustomPacketPayload {

    public static final Type<PacketPersonalForcefield> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_personal_forcefield"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPersonalForcefield> CODEC = StreamCodec.ofMember(PacketPersonalForcefield::toBytes, PacketPersonalForcefield::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private final int entityId;
    private final long cooldownUntil;
    private final long lastHit;

    public PacketPersonalForcefield(int entityId, long cooldownUntil, long lastHit) {
        this.entityId = entityId;
        this.cooldownUntil = cooldownUntil;
        this.lastHit = lastHit;
    }

    public PacketPersonalForcefield(RegistryFriendlyByteBuf buf) {
        entityId = buf.readVarInt();
        cooldownUntil = buf.readLong();
        lastHit = buf.readLong();
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeLong(cooldownUntil);
        buf.writeLong(lastHit);
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> ForceFieldRenderer.updatePersonalField(entityId, cooldownUntil, lastHit));
    }
}
