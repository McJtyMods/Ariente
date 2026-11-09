package mcjty.ariente.blocks.defense;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketDamageForcefield implements CustomPacketPayload {

    public static final Type<PacketDamageForcefield> TYPE = new Type<>(Identifier.fromNamespaceAndPath("ariente", "packet_damage_forcefield"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketDamageForcefield> CODEC = StreamCodec.ofMember(PacketDamageForcefield::toBytes, PacketDamageForcefield::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private BlockPos pos;
    private int index;
    private Vec3 intersection;

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(index);
        buf.writeDouble(intersection.x);
        buf.writeDouble(intersection.y);
        buf.writeDouble(intersection.z);
    }

    public PacketDamageForcefield() {
    }

    public PacketDamageForcefield(RegistryFriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        index = buf.readInt();
        intersection = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public PacketDamageForcefield(BlockPos pos, int index, Vec3 intersection) {
        this.pos = pos;
        this.index = index;
        this.intersection = intersection;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ForceFieldRenderer.damageField(pos, index, intersection);
        });
    }
}
