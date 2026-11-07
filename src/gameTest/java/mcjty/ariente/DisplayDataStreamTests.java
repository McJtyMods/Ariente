package mcjty.ariente;

import io.netty.buffer.Unpooled;
import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayDataAssembly;
import mcjty.ariente.display.DisplayDataCodec;
import mcjty.ariente.display.InstalledModule;
import mcjty.ariente.display.PacketDisplayData;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class DisplayDataStreamTests {

    private static PacketDisplayData fragment(byte[] bytes, int offset, int version) {
        return new PacketDisplayData(BlockPos.ZERO, 27, 0, version, bytes.length, offset,
                Arrays.copyOfRange(bytes, offset, Math.min(bytes.length, offset + DisplayDataCodec.FRAGMENT_BYTES)));
    }

    @GameTest(template = "empty")
    public static void snapshotsLargerThanOnePacketArriveAtomically(GameTestHelper helper) {
        CompoundTag source = new CompoundTag();
        source.putByteArray("LargeScene", new byte[1200 * 1024]);
        source.putInt("Range", 100);
        byte[] bytes = DisplayDataCodec.encode(source);
        helper.assertTrue(bytes.length > 1048576, "The regression must exceed the custom payload limit");
        InstalledModule client = new InstalledModule(new ItemStack(Registration.DISPLAY_CAMERA.get()), 0, 0);
        CompoundTag old = new CompoundTag();
        old.putString("Status", "Previous complete image");
        client.updateSnapshot(old);
        DisplayDataAssembly assembly = new DisplayDataAssembly(fragment(bytes, 0, 12), 0);
        for (int offset = 0; offset < bytes.length; offset += DisplayDataCodec.FRAGMENT_BYTES) {
            PacketDisplayData packet = fragment(bytes, offset, 12);
            FriendlyByteBuf wire = new FriendlyByteBuf(Unpooled.buffer());
            try {
                packet.toBytes(wire);
                helper.assertTrue(wire.readableBytes() < 33 * 1024, "Every fragment has a strict encoded size bound");
                new ClientboundCustomPayloadPacket(new ResourceLocation(Ariente.MODID, "main"), wire);
                PacketDisplayData decoded = new PacketDisplayData(wire);
                byte[] complete = assembly.accept(decoded);
                if (complete == null) {
                    helper.assertTrue(client.snapshot().equals(old), "Incomplete images leave the last complete view visible");
                } else {
                    client.receiveSnapshot(decoded.snapshotVersion(), DisplayDataCodec.decode(complete));
                }
            } finally {
                wire.release();
            }
        }
        helper.assertTrue(client.snapshot().equals(source), "A snapshot above 1 MiB arrives without losing data");
        client.receiveSnapshot(11, old);
        client.receiveSnapshot(12, old);
        helper.assertTrue(client.snapshot().equals(source), "Old and duplicate transfers cannot overwrite a newer image");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void duplicateAndMissingFragmentsCannotCompleteAnImage(GameTestHelper helper) {
        CompoundTag source = new CompoundTag();
        source.putByteArray("Scene", new byte[DisplayDataCodec.FRAGMENT_BYTES * 2]);
        byte[] bytes = DisplayDataCodec.encode(source);
        PacketDisplayData first = fragment(bytes, 0, 1);
        DisplayDataAssembly assembly = new DisplayDataAssembly(first, 10);
        helper.assertTrue(assembly.accept(first) == null && assembly.accept(first) == null, "Duplicate fragments do not count twice");
        helper.assertTrue(assembly.accept(fragment(bytes, DisplayDataCodec.FRAGMENT_BYTES * 2, 1)) == null,
                "Skipping a fragment cannot display an incomplete image");
        helper.assertTrue(assembly.accept(fragment(bytes, DisplayDataCodec.FRAGMENT_BYTES, 2)) == null,
                "Fragments from another capture cannot be mixed into this image");
        assembly.accept(fragment(bytes, DisplayDataCodec.FRAGMENT_BYTES, 1));
        byte[] complete = assembly.accept(fragment(bytes, DisplayDataCodec.FRAGMENT_BYTES * 2, 1));
        helper.assertTrue(complete != null && DisplayDataCodec.decode(complete).equals(source), "The correct ordered fragments still complete");
        helper.assertTrue(!assembly.expired(200) && assembly.expired(211), "Abandoned transfers have a bounded lifetime");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oversizedAndMalformedSnapshotsAreBounded(GameTestHelper helper) {
        for (int size : new int[]{0, DisplayDataCodec.MAX_SNAPSHOT_BYTES + 1}) {
            boolean rejected = false;
            try {
                new PacketDisplayData(BlockPos.ZERO, 0, 0, 0, size, 0, new byte[1]);
            } catch (IllegalArgumentException e) {
                rejected = true;
            }
            helper.assertTrue(rejected, "Invalid total sizes are rejected before assembly allocation");
        }
        CompoundTag enormous = new CompoundTag();
        enormous.putByteArray("Scene", new byte[DisplayDataCodec.MAX_SNAPSHOT_BYTES]);
        InstalledModule module = new InstalledModule(new ItemStack(Registration.DISPLAY_CAMERA.get()), 0, 0);
        module.updateSnapshot(enormous);
        helper.assertTrue(DisplayDataCodec.decode(module.encodedSnapshot()).contains("Status"),
                "Oversized data becomes a small status instead of crashing the server or sending an oversized packet");
        boolean rejected = false;
        try {
            DisplayDataCodec.decode(new byte[]{0});
        } catch (RuntimeException e) {
            rejected = true;
        }
        helper.assertTrue(rejected, "Malformed NBT cannot become a displayed image");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void initialLayoutsNeverContainCameraSnapshots(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.DISPLAY_BASE.get().defaultBlockState());
        DisplayBaseTile base = (DisplayBaseTile) helper.getLevel().getBlockEntity(pos);
        CompoundTag layout = new CompoundTag();
        layout.putInt("Height", 10);
        ListTag modules = new ListTag();
        for (int index = 0; index < 10; index++) {
            modules.add(new InstalledModule(new ItemStack(Registration.DISPLAY_CAMERA.get()), 0, index * 60).save(false));
        }
        layout.put("Modules", modules);
        base.load(layout);
        CompoundTag scene = new CompoundTag();
        scene.putByteArray("Scene", new byte[1200 * 1024]);
        base.modules().forEach(module -> module.updateSnapshot(scene));
        CompoundTag initial = base.getUpdateTag();
        FriendlyByteBuf wire = new FriendlyByteBuf(Unpooled.buffer());
        try {
            wire.writeNbt(initial);
            helper.assertTrue(wire.readableBytes() < 16 * 1024, "Even a display holding many large images has a small initial layout");
            CompoundTag decoded = wire.readNbt();
            DisplayBaseTile client = new DisplayBaseTile(pos, base.getBlockState());
            client.loadClientDataFromNBT(decoded);
            helper.assertTrue(client.modules().size() == 10 && client.modules().stream().allMatch(module -> module.snapshot().isEmpty()),
                    "Initial synchronization works with the normal NBT quota and no embedded camera data");
            client.applyReadings(client.layoutVersion(), 4, 1, scene);
            helper.assertTrue(client.modules().get(4).snapshot().equals(scene), "Separate feeds can populate the initialized layout");
        } finally {
            wire.release();
        }
        helper.assertTrue(CameraCapture.MAX_BLOCKS == 16384, "The expanded surface budget is enabled");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 140)
    public static void retriesRequestOnlyTheMissingModules(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile client = new DisplayBaseTile(pos, Registration.DISPLAY_BASE.get().defaultBlockState());
        client.setLevel(helper.getLevel());
        CompoundTag layout = new CompoundTag();
        ListTag modules = new ListTag();
        for (int index = 0; index < 10; index++) {
            modules.add(new InstalledModule(new ItemStack(Registration.DISPLAY_CAMERA.get()), 0, index * 60).save(false));
        }
        layout.put("Modules", modules);
        client.loadClientDataFromNBT(layout);
        var player = helper.makeMockSurvivalPlayer();
        player.setPos(pos.getCenter());
        helper.assertTrue(client.requestReadings(player) == 1023, "Initial synchronization requests all ten feeds");
        helper.assertTrue(client.requestReadings(player) == 0, "Retries are throttled");
        for (int index = 0; index < 6; index++) {
            client.applyReadings(client.layoutVersion(), index, 1, new CompoundTag());
        }
        helper.runAfterDelay(101, () -> {
            helper.assertTrue(client.requestReadings(player) == (15 << 6),
                    "Retrying only the missing feeds prevents completed feeds from starving them in a bounded queue");
            helper.succeed();
        });
    }
}
