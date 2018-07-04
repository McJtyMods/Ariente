package mcjty.ariente.network;

import io.netty.buffer.ByteBuf;
import mcjty.ariente.blocks.utility.StorageTile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    public PacketClickStorage(BlockPos pos, int index) {
        this.pos = pos;
        this.index = index;
    }

    public static class Handler implements IMessageHandler<PacketClickStorage, IMessage> {
        @Override
        public IMessage onMessage(PacketClickStorage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketClickStorage message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            TileEntity te = playerEntity.getEntityWorld().getTileEntity(message.pos);
            if (te instanceof StorageTile) {
                StorageTile storageTile = (StorageTile) te;
                storageTile.giveToPlayer(message.index, playerEntity);
            }
        }
    }
}
