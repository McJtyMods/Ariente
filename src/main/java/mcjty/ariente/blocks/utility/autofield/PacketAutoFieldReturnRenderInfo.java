package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.McJtyLib;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class PacketAutoFieldReturnRenderInfo {

    private BlockPos pos;
    private AutoFieldRenderInfo renderInfo;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        renderInfo.toBytes(buf);
    }

    public PacketAutoFieldReturnRenderInfo() {
    }

    public PacketAutoFieldReturnRenderInfo(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        renderInfo = new AutoFieldRenderInfo();
        renderInfo.fromBytes(buf);
    }

    public PacketAutoFieldReturnRenderInfo(BlockPos pos, AutoFieldRenderInfo renderInfo) {
        this.pos = pos;
        this.renderInfo = renderInfo;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            BlockEntity te = McJtyLib.proxy.getClientWorld().getBlockEntity(pos);
            if (te instanceof AutoFieldTile) {
                ((AutoFieldTile) te).clientRenderInfoReceived(renderInfo);
            }
        });
        ctx.setPacketHandled(true);
    }
}
