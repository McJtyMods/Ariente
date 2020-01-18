package mcjty.ariente.blocks.utility.autofield;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketAutoFieldRequestRenderInfo {
    private BlockPos pos;

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
    }

    public PacketAutoFieldRequestRenderInfo() {
    }

    public PacketAutoFieldRequestRenderInfo(PacketBuffer buf) {
        pos = buf.readBlockPos();
    }

    public PacketAutoFieldRequestRenderInfo(BlockPos pos) {
        this.pos = pos;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().world.getTileEntity(pos);
            if (te instanceof AutoFieldTile) {
                ((AutoFieldTile) te).renderInfoRequested();
            }
        });
        ctx.setPacketHandled(true);
    }
}
