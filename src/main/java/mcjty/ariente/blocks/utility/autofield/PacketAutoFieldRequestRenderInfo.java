package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.network.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PacketAutoFieldRequestRenderInfo implements CustomPacketPayload {

    public static final Type<PacketAutoFieldRequestRenderInfo> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_auto_field_request_render_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketAutoFieldRequestRenderInfo> CODEC = StreamCodec.ofMember(PacketAutoFieldRequestRenderInfo::toBytes, PacketAutoFieldRequestRenderInfo::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private BlockPos pos;

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public PacketAutoFieldRequestRenderInfo() {
    }

    public PacketAutoFieldRequestRenderInfo(RegistryFriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public PacketAutoFieldRequestRenderInfo(BlockPos pos) {
        this.pos = pos;
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null || !player.level().hasChunkAt(pos) || player.distanceToSqr(pos.getCenter()) > 128 * 128) {
                return;
            }
            BlockEntity te = player.level().getBlockEntity(pos);
            if (te instanceof AutoFieldTile) {
                ((AutoFieldTile) te).renderInfoRequested();
            }
        });
    }
}
