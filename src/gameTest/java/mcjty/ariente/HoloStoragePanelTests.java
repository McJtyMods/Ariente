package mcjty.ariente;

import com.mojang.authlib.GameProfile;
import mcjty.ariente.blocks.storagecube.PacketStorageCubeData;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class HoloStoragePanelTests {

    private static ServerPlayer player(GameTestHelper helper, BlockPos pos, String name) {
        ServerPlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 2);
        player.connection = new ServerGamePacketListenerImpl(player.server, new Capture(), player);
        player.getInventory().clearContent();
        return player;
    }

    private static HoloGuiEntity open(GameTestHelper helper, BlockPos pos, ServerPlayer player) {
        HoloGuiEntity holo = (HoloGuiEntity) Ariente.guiHandler.openHoloGuiEntity(helper.getLevel(), pos, player, "default", 1);
        holo.setPinned(true);
        return holo;
    }

    @GameTest(template = "empty")
    public static void subscriptionsAreIndependentPerPlayerAndPanel(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.HOLO_STORAGE_3.get().defaultBlockState());
        StorageCubeTile tile = (StorageCubeTile) helper.getLevel().getBlockEntity(pos);
        ServerPlayer first = player(helper, pos, "storage-first");
        ServerPlayer second = player(helper, pos, "storage-second");
        first.getInventory().setItem(9, new ItemStack(Items.APPLE, 7));
        second.getInventory().setItem(9, new ItemStack(Items.GOLD_INGOT, 13));
        HoloGuiEntity firstPanel = open(helper, pos, first);
        HoloGuiEntity secondPanel = open(helper, pos, second);
        Capture firstData = (Capture) first.connection.connection;
        Capture secondData = (Capture) second.connection.connection;
        tile.subscribe(first, firstPanel.getId());
        tile.subscribe(first, secondPanel.getId());
        tile.subscribe(second, secondPanel.getId());
        helper.assertTrue(firstData.updates.size() == 2 && secondData.updates.size() == 1,
                "Each visible panel gets an independent initial snapshot; received=" + firstData.updates.size() + "/" + secondData.updates.size()
                        + "; usable=" + tile.canUse(first, firstPanel.getId()) + "/" + tile.canUse(second, secondPanel.getId()));
        tile.getItems().setStackInSlot(0, new ItemStack(Items.DIAMOND, 10));
        first.getInventory().setItem(9, new ItemStack(Items.APPLE, 8));
        second.getInventory().setItem(9, new ItemStack(Items.GOLD_INGOT, 14));
        tile.refresh(first, firstPanel.getId());
        checkUpdate(helper, firstData.last(), firstPanel.getId(), Items.APPLE, 8);
        tile.refresh(first, secondPanel.getId());
        checkUpdate(helper, firstData.last(), secondPanel.getId(), Items.APPLE, 8);
        tile.refresh(second, secondPanel.getId());
        checkUpdate(helper, secondData.last(), secondPanel.getId(), Items.GOLD_INGOT, 14);
        tile.subscribe(first, firstPanel.getId());
        tile.subscribe(first, secondPanel.getId());
        helper.assertTrue(firstData.updates.size() == 4, "Keepalives retain both snapshots without resending full inventories");

        tile.unsubscribe(first, firstPanel.getId());
        tile.getItems().setStackInSlot(0, new ItemStack(Items.DIAMOND, 11));
        tile.refresh(first, firstPanel.getId());
        helper.assertTrue(firstData.updates.size() == 4, "Closing one panel ends only its own subscription");
        tile.refresh(first, secondPanel.getId());
        tile.refresh(second, secondPanel.getId());
        helper.assertTrue(firstData.last().changes().size() == 1 && secondData.last().changes().size() == 1,
                "The remaining panels keep receiving incremental changes for both players");
        helper.assertTrue(firstData.last().changes().get(0).stack().getCount() == 11,
                "Changes continue after another panel closes");
        firstPanel.discard();
        secondPanel.discard();
        helper.succeed();
    }

    private static void checkUpdate(GameTestHelper helper, PacketStorageCubeData data, int panel, Item item, int count) {
        helper.assertTrue(data.entityId() == panel && data.changes().size() == 2,
                "Each panel keeps its own delta snapshot after another panel subscribes");
        var playerSlot = data.changes().stream().filter(entry -> entry.slot() == data.size() + 9).findFirst().orElseThrow();
        helper.assertTrue(playerSlot.stack().is(item) && playerSlot.stack().getCount() == count,
                "Each viewer receives their own player inventory");
    }

    @GameTest(template = "empty")
    public static void lockingClosesPanelsAndRejectsPendingActions(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.HOLO_STORAGE_1.get().defaultBlockState());
        StorageCubeTile tile = (StorageCubeTile) helper.getLevel().getBlockEntity(pos);
        tile.getItems().setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));
        ServerPlayer player = player(helper, pos, "storage-lock");
        HoloGuiEntity panel = open(helper, pos, player);
        Capture data = (Capture) player.connection.connection;
        tile.subscribe(player, panel.getId());
        long revision = data.last().revision();
        int updates = data.updates.size();
        tile.setLocked(true);
        helper.assertTrue(!panel.isAlive() && !tile.canUse(player, panel.getId()), "Locking closes an active panel");
        tile.transfer(player, panel.getId(), revision, 0, tile.getItems().getSlots(), 5, true);
        tile.sort(player, panel.getId(), revision, true);
        tile.subscribe(player, panel.getId());
        tile.refresh(player, panel.getId());
        helper.assertTrue(tile.getItems().getStackInSlot(0).getCount() == 5 && player.getInventory().isEmpty(),
                "A pending transfer cannot take items after locking");
        helper.assertTrue(data.updates.size() == updates, "Locked cubes do not send inventory snapshots");
        tile.setLocked(false);
        HoloGuiEntity reopened = open(helper, pos, player);
        tile.subscribe(player, reopened.getId());
        tile.transfer(player, reopened.getId(), data.last().revision(), 0, tile.getItems().getSlots(), 5, true);
        helper.assertTrue(tile.getItems().getStackInSlot(0).isEmpty() && player.getInventory().countItem(Items.DIAMOND) == 5,
                "Unlocking restores transfers through a newly opened panel");
        reopened.discard();
        helper.succeed();
    }

    private static class Capture extends Connection {

        private final List<PacketStorageCubeData> updates = new ArrayList<>();

        private Capture() {
            super(PacketFlow.CLIENTBOUND);
        }

        @Override
        public void send(Packet<?> packet) {
            if (packet instanceof ClientboundCustomPayloadPacket payload && payload.getIdentifier().getNamespace().equals(Ariente.MODID)) {
                FriendlyByteBuf data = new FriendlyByteBuf(payload.getData().copy());
                try {
                    data.readVarInt(); // Channel message discriminator; this fixture sends only storage updates.
                    updates.add(new PacketStorageCubeData(data));
                } finally {
                    data.release();
                }
            }
        }

        private PacketStorageCubeData last() {
            return updates.get(updates.size() - 1);
        }
    }
}
