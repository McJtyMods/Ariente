package mcjty.ariente.display;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

/** Explicit byte and allocation bounds apply to each snapshot, independently of packet or display size. */
public class DisplayDataCodec {

    public static final int FRAGMENT_BYTES = 32 * 1024;
    public static final int MAX_SNAPSHOT_BYTES = 2 * 1024 * 1024;
    public static final int NBT_QUOTA = 8 * 1024 * 1024;

    public static byte[] encode(CompoundTag snapshot) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer(1024, MAX_SNAPSHOT_BYTES));
        try {
            buffer.writeNbt(snapshot);
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally {
            buffer.release();
        }
    }

    public static CompoundTag decode(byte[] bytes) {
        if (bytes.length < 1 || bytes.length > MAX_SNAPSHOT_BYTES) {
            throw new IllegalArgumentException("Invalid display snapshot size");
        }
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        try {
            CompoundTag result = (CompoundTag) buffer.readNbt(NbtAccounter.create(NBT_QUOTA));
            if (result == null || buffer.isReadable()) {
                throw new IllegalArgumentException("Invalid display snapshot data");
            }
            return result;
        } finally {
            buffer.release();
        }
    }
}
