package mcjty.ariente.display;

/** Fragments are ordered by the connection. An incomplete or superseded image is never displayed. */
public class DisplayDataAssembly {

    private final int version;
    private final byte[] bytes;
    private final long startedAt;
    private int received;

    public DisplayDataAssembly(PacketDisplayData first, long tick) {
        version = first.snapshotVersion();
        bytes = new byte[first.totalBytes()];
        startedAt = tick;
    }

    public int version() {
        return version;
    }

    public int size() {
        return bytes.length;
    }

    public boolean expired(long tick) {
        return tick - startedAt > 200;
    }

    public byte[] accept(PacketDisplayData packet) {
        if (packet.snapshotVersion() != version || packet.totalBytes() != bytes.length || packet.offset() != received) {
            return null;
        }
        System.arraycopy(packet.data(), 0, bytes, received, packet.data().length);
        received += packet.data().length;
        return received == bytes.length ? bytes : null;
    }
}
