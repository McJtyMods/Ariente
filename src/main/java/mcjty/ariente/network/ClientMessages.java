package mcjty.ariente.network;

import mcjty.ariente.blocks.defense.PacketDamageForcefield;
import mcjty.ariente.blocks.storagecube.PacketStorageCubeData;
import mcjty.ariente.blocks.utility.autofield.PacketAutoFieldReturnRenderInfo;
import mcjty.ariente.display.PacketDisplayData;
import mcjty.ariente.display.PacketDisplayGestureResult;
import mcjty.ariente.items.armor.PacketPersonalForcefield;
import mcjty.lib.network.PayloadContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientMessages {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                PacketStorageCubeData.TYPE,
                (packet, context) ->
                        packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(
                PacketDisplayGestureResult.TYPE,
                (packet, context) ->
                        packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(
                PacketDisplayData.TYPE,
                (packet, context) ->
                        packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(
                PacketDamageForcefield.TYPE,
                (packet, context) ->
                        packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(
                PacketAutoFieldReturnRenderInfo.TYPE,
                (packet, context) ->
                        packet.handle(new PayloadContext(context.player(), context.client())));
        ClientPlayNetworking.registerGlobalReceiver(
                PacketPersonalForcefield.TYPE,
                (packet, context) ->
                        packet.handle(new PayloadContext(context.player(), context.client())));
    }
}
