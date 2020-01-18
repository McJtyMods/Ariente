package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.McJtyLib;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public class PacketAutoFieldReturnRenderInfo {

    private BlockPos pos;
    private AutoFieldRenderInfo renderInfo;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        renderInfo.toBytes(buf);
    }

    public PacketAutoFieldReturnRenderInfo() {
    }

    public PacketAutoFieldReturnRenderInfo(PacketBuffer buf) {
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
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(pos);
            if (te instanceof AutoFieldTile) {
                ((AutoFieldTile) te).clientRenderInfoReceived(renderInfo);
            }
        });
        ctx.setPacketHandled(true);
    }
}
