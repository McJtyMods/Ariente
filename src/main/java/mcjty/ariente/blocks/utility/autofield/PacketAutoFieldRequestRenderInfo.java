package mcjty.ariente.blocks.utility.autofield;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketAutoFieldRequestRenderInfo implements IMessage {
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketAutoFieldRequestRenderInfo() {
    }

    public PacketAutoFieldRequestRenderInfo(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketAutoFieldRequestRenderInfo(BlockPos pos) {
        this.pos = pos;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().world.getTileEntity(pos);
            if (te instanceof AutoFieldTile) {
                ((AutoFieldTile) te).renderInfoRequested();
            }
        });
        ctx.setPacketHandled(true);
    }
}
