package mcjty.ariente.blocks.utility.autofield;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketAutoFieldRequestRenderInfo {
    private BlockPos pos;

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public PacketAutoFieldRequestRenderInfo() {
    }

    public PacketAutoFieldRequestRenderInfo(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public PacketAutoFieldRequestRenderInfo(BlockPos pos) {
        this.pos = pos;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !player.level().hasChunkAt(pos) || player.distanceToSqr(pos.getCenter()) > 128 * 128) {
                return;
            }
            BlockEntity te = player.level().getBlockEntity(pos);
            if (te instanceof AutoFieldTile) {
                ((AutoFieldTile) te).renderInfoRequested();
            }
        });
        ctx.setPacketHandled(true);
    }
}
