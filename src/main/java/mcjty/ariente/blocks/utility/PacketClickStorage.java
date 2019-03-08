package mcjty.ariente.blocks.utility;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketClickStorage implements IMessage {
    private BlockPos pos;
    private int index;


    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(index);
    }

    public PacketClickStorage() {
    }

    public PacketClickStorage(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketClickStorage(BlockPos pos, int index) {
        this.pos = pos;
        this.index = index;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP playerEntity = ctx.getSender();
            TileEntity te = playerEntity.getEntityWorld().getTileEntity(pos);
            if (te instanceof StorageTile) {
                StorageTile storageTile = (StorageTile) te;
                storageTile.giveToPlayer(index, playerEntity);
            }
        });
        ctx.setPacketHandled(true);
    }
}
