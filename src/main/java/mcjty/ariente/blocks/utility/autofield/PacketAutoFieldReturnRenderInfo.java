package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.network.PayloadContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PacketAutoFieldReturnRenderInfo implements CustomPacketPayload {

    public static final Type<PacketAutoFieldReturnRenderInfo> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ariente", "packet_auto_field_return_render_info"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketAutoFieldReturnRenderInfo> CODEC = StreamCodec.ofMember(PacketAutoFieldReturnRenderInfo::toBytes, PacketAutoFieldReturnRenderInfo::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private BlockPos pos;
    private AutoFieldRenderInfo renderInfo;

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        renderInfo.toBytes(buf);
    }

    public PacketAutoFieldReturnRenderInfo() {
    }

    public PacketAutoFieldReturnRenderInfo(RegistryFriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        renderInfo = new AutoFieldRenderInfo();
        renderInfo.fromBytes(buf);
    }

    public PacketAutoFieldReturnRenderInfo(BlockPos pos, AutoFieldRenderInfo renderInfo) {
        this.pos = pos;
        this.renderInfo = renderInfo;
    }

    public void handle(PayloadContext ctx) {
        ctx.enqueueWork(() -> {
            BlockEntity te = Minecraft.getInstance().level.getBlockEntity(pos);
            if (te instanceof AutoFieldTile) {
                ((AutoFieldTile) te).clientRenderInfoReceived(renderInfo);
            }
        });
    }
}
