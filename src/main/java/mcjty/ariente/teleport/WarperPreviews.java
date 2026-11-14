package mcjty.ariente.teleport;

import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.items.TeleportationCardItem;
import mcjty.ariente.network.ArienteMessages;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import mcjty.ariente.items.ItemData;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** One transient scene per player. Cards carry addresses and names, never terrain NBT. */
public class WarperPreviews {

    private static final Map<ServerPlayer, Preview> PREVIEWS = new WeakHashMap<>();

    public static void refreshLoadedNames(ServerPlayer player, ItemStack stack) {
        for (int i = 0; i < Math.min(TeleportationCardItem.MAX_DESTINATIONS, TeleportationCardItem.destinations(stack).size()); i++) {
            WarperDestination destination = TeleportationCardItem.destination(stack, i);
            ServerLevel level = destination == null ? null : player.level().getServer().getLevel(destination.dimension());
            if (level != null && level.getChunkSource().getChunkNow(destination.pos().getX() >> 4, destination.pos().getZ() >> 4) != null
                    && level.getBlockEntity(destination.pos()) instanceof WarperTile warper) {
                TeleportationCardItem.refreshName(stack, i, warper);
            }
        }
    }

    public static void select(ServerPlayer player, ItemStack stack, int index) {
        WarperDestination destination = TeleportationCardItem.destination(stack, index);
        if (destination == null) {
            return;
        }
        long now = player.level().getServer().overworld().getGameTime();
        Preview previous = PREVIEWS.get(player);
        if (previous != null && now - previous.tick < 10) {
            return;
        }
        ServerLevel level = destination.resolve(player.level().getServer());
        CompoundTag scene = new CompoundTag();
        if (level == null) {
            scene.putString("Status", "Destination Warper missing");
        } else {
            TeleportationCardItem.refreshName(stack, index, (WarperTile) level.getBlockEntity(destination.pos()));
            scene = capture(level, destination);
        }
        ItemData.update(stack, data -> data.putInt("Selected", index));
        TeleportationCardItem.sync(player);
        Preview preview = new Preview(TeleportationCardItem.id(stack), destination, scene, now);
        PREVIEWS.put(player, preview);
        PacketDistributor.sendToPlayer(player, new PacketWarperPreview(preview.card, destination.save(), scene));
    }

    public static boolean apply(Player player, ItemStack stack, WarperTile warper) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        WarperDestination destination = TeleportationCardItem.selected(stack);
        if (destination == null || destination.equals(new WarperDestination(warper.getLevel().dimension(), warper.getBlockPos()))) {
            return false;
        }
        ServerLevel level = destination.resolve(serverPlayer.level().getServer());
        if (level == null) {
            return false;
        }
        Preview preview = PREVIEWS.get(serverPlayer);
        CompoundTag scene = preview != null && destination.equals(preview.destination) && preview.card.equals(TeleportationCardItem.id(stack))
                && !preview.scene.contains("Status") ? preview.scene : capture(level, destination);
        warper.setDestination(destination, scene);
        return true;
    }

    private static CompoundTag capture(ServerLevel level, WarperDestination destination) {
        for (int x = (destination.pos().getX() - 16) >> 4; x <= (destination.pos().getX() + 15) >> 4; x++) {
            for (int z = (destination.pos().getZ() - 16) >> 4; z <= (destination.pos().getZ() + 15) >> 4; z++) {
                level.getChunk(x, z);
            }
        }
        return CameraCapture.scanWarper(level, destination.pos());
    }

    private record Preview(UUID card, WarperDestination destination, CompoundTag scene, long tick) {
    }
}
