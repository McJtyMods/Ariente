package mcjty.ariente.blocks.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketClickStorage {
    private BlockPos pos;
    private int index;


    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(index);
    }

    public PacketClickStorage() {
    }

    public PacketClickStorage(FriendlyByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        index = buf.readInt();
    }

    public PacketClickStorage(BlockPos pos, int index) {
        this.pos = pos;
        this.index = index;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Player playerEntity = ctx.getSender();
            BlockEntity te = playerEntity.getCommandSenderWorld().getBlockEntity(pos);
            if (te instanceof StorageTile) {
                StorageTile storageTile = (StorageTile) te;
                storageTile.giveToPlayer(index, playerEntity);
            }
        });
        ctx.setPacketHandled(true);
    }
}
