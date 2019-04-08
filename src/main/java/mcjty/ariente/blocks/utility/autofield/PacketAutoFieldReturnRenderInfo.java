package mcjty.ariente.blocks.utility.autofield;

import io.netty.buffer.ByteBuf;
import mcjty.ariente.Ariente;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketAutoFieldReturnRenderInfo implements IMessage {

    private BlockPos pos;
    private AutoFieldRenderInfo renderInfo;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        renderInfo = new AutoFieldRenderInfo();
        renderInfo.fromBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        renderInfo.toBytes(buf);
    }

    public PacketAutoFieldReturnRenderInfo() {
    }

    public PacketAutoFieldReturnRenderInfo(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketAutoFieldReturnRenderInfo(BlockPos pos, AutoFieldRenderInfo renderInfo) {
        this.pos = pos;
        this.renderInfo = renderInfo;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = Ariente.proxy.getClientWorld().getTileEntity(pos);
            if (te instanceof AutoFieldTile) {
                ((AutoFieldTile) te).clientRenderInfoReceived(renderInfo);
            }
        });
        ctx.setPacketHandled(true);
    }
}
