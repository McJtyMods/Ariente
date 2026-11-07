package mcjty.ariente.display;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class InstalledModule {

    private UUID id = UUID.randomUUID();
    private final ItemStack stack;
    private final DisplayModuleType.Size size;
    private final int x;
    private final int y;
    private int snapshotVersion;
    private CompoundTag snapshot = new CompoundTag();
    private int receivedSnapshotVersion = -1;
    private byte[] encodedSnapshot;

    public InstalledModule(ItemStack stack, int x, int y) {
        this.stack = stack.copyWithCount(1);
        size = ((DisplayModuleItem) this.stack.getItem()).type().size(this.stack);
        this.x = x;
        this.y = y;
    }

    public UUID id() {
        return id;
    }

    public InstalledModule reconfigured(ItemStack configured) {
        InstalledModule replacement = new InstalledModule(configured, x, y);
        replacement.id = id;
        replacement.snapshot = snapshot;
        replacement.snapshotVersion = snapshotVersion;
        replacement.encodedSnapshot = encodedSnapshot;
        return replacement;
    }

    public InstalledModule moved(int x, int y) {
        InstalledModule replacement = new InstalledModule(stack, x, y);
        replacement.id = id;
        replacement.snapshot = snapshot;
        replacement.snapshotVersion = snapshotVersion;
        replacement.encodedSnapshot = encodedSnapshot;
        return replacement;
    }

    public ItemStack stack() {
        return stack;
    }

    public DisplayModuleType type() {
        return ((DisplayModuleItem) stack.getItem()).type();
    }

    public DisplayModuleType.Size size() {
        return size;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public CompoundTag snapshot() {
        return snapshot;
    }

    public boolean refresh(Level level) {
        CompoundTag next = type().sample(level, stack, snapshot);
        if (next.equals(snapshot)) {
            return false;
        }
        updateSnapshot(next);
        return true;
    }

    public void updateSnapshot(CompoundTag snapshot) {
        this.snapshot = snapshot;
        snapshotVersion++;
        encodedSnapshot = null;
    }

    public int snapshotVersion() {
        return snapshotVersion;
    }

    public int receivedSnapshotVersion() {
        return receivedSnapshotVersion;
    }

    public void receiveSnapshot(int version, CompoundTag data) {
        if (version > receivedSnapshotVersion) {
            receivedSnapshotVersion = version;
            updateSnapshot(data);
        }
    }

    public void retainSnapshot(InstalledModule previous) {
        snapshot = previous.snapshot;
        snapshotVersion = previous.snapshotVersion;
    }

    public byte[] encodedSnapshot() {
        if (encodedSnapshot == null) {
            try {
                encodedSnapshot = DisplayDataCodec.encode(snapshot);
            } catch (RuntimeException e) {
                CompoundTag status = new CompoundTag();
                status.putString("Status", "Display snapshot exceeds size limit");
                encodedSnapshot = DisplayDataCodec.encode(status);
            }
        }
        return encodedSnapshot;
    }

    public boolean contains(int px, int py) {
        return px >= x && px < x + size().width() && py >= y && py < y + size().height();
    }

    public CompoundTag save(boolean client) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.put("Item", stack.save(new CompoundTag()));
        tag.putInt("X", x);
        tag.putInt("Y", y);
        return tag;
    }

    public static InstalledModule load(CompoundTag tag) {
        ItemStack stack = ItemStack.of(tag.getCompound("Item"));
        if (!(stack.getItem() instanceof DisplayModuleItem) || tag.getInt("X") < 0 || tag.getInt("X") >= 64
                || tag.getInt("Y") < 0 || tag.getInt("Y") >= 636) {
            return null;
        }
        InstalledModule module = new InstalledModule(stack, tag.getInt("X"), tag.getInt("Y"));
        if (tag.hasUUID("Id")) {
            module.id = tag.getUUID("Id");
        }
        module.snapshot = tag.getCompound("Data");
        return module;
    }
}
