package mcjty.ariente.network;

import net.minecraft.server.level.ServerLevel;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class Packets {

    private Packets() {
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload packet) {
        PlayerLookup.level(level).forEach(player -> sendToPlayer(player, packet));
    }

    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload packet) {
        PlayerLookup.tracking(entity).forEach(player -> sendToPlayer(player, packet));
        if (entity instanceof ServerPlayer player) sendToPlayer(player, packet);
    }

}
