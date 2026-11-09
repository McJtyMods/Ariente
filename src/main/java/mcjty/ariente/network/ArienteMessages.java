package mcjty.ariente.network;

import mcjty.lib.network.PayloadContext;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import mcjty.lib.network.ClientNetworking;
import javax.annotation.Nonnull;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.defense.PacketDamageForcefield;
import mcjty.ariente.blocks.storagecube.PacketStorageCubeAction;
import mcjty.ariente.blocks.storagecube.PacketStorageCubeData;
import mcjty.ariente.blocks.utility.PacketClickStorage;
import mcjty.ariente.blocks.utility.autofield.PacketAutoFieldRequestRenderInfo;
import mcjty.ariente.blocks.utility.autofield.PacketAutoFieldReturnRenderInfo;
import mcjty.ariente.display.PacketDisplayAction;
import mcjty.ariente.display.PacketDisplayData;
import mcjty.ariente.display.PacketDisplayGesture;
import mcjty.ariente.display.PacketDisplayGestureResult;
import mcjty.ariente.display.PacketRequestDisplayData;
import mcjty.ariente.entities.fluxship.PacketShipAction;
import mcjty.ariente.entities.levitator.PacketLevitatorInput;
import mcjty.ariente.items.armor.PacketArmorHotkey;
import mcjty.ariente.items.armor.PacketConfigureArmor;
import mcjty.ariente.items.armor.PacketPersonalForcefield;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import mcjty.ariente.network.Packets;

public class ArienteMessages {

    public static void registerMessages() {
        PayloadTypeRegistry.serverboundPlay().register(PacketStorageCubeAction.TYPE, PacketStorageCubeAction.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketStorageCubeAction.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.clientboundPlay().register(PacketStorageCubeData.TYPE, PacketStorageCubeData.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketDisplayAction.TYPE, PacketDisplayAction.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketDisplayAction.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketDisplayGesture.TYPE, PacketDisplayGesture.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketDisplayGesture.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.clientboundPlay().register(PacketDisplayGestureResult.TYPE, PacketDisplayGestureResult.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketClickStorage.TYPE, PacketClickStorage.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketClickStorage.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketHitForcefield.TYPE, PacketHitForcefield.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketHitForcefield.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketConfigureArmor.TYPE, PacketConfigureArmor.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketConfigureArmor.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketArmorHotkey.TYPE, PacketArmorHotkey.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketArmorHotkey.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketShipAction.TYPE, PacketShipAction.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketShipAction.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.serverboundPlay().register(PacketAutoFieldRequestRenderInfo.TYPE, PacketAutoFieldRequestRenderInfo.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketAutoFieldRequestRenderInfo.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.clientboundPlay().register(PacketDisplayData.TYPE, PacketDisplayData.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketRequestDisplayData.TYPE, PacketRequestDisplayData.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketRequestDisplayData.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
        PayloadTypeRegistry.clientboundPlay().register(PacketDamageForcefield.TYPE, PacketDamageForcefield.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketAutoFieldReturnRenderInfo.TYPE, PacketAutoFieldReturnRenderInfo.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketPersonalForcefield.TYPE, PacketPersonalForcefield.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketLevitatorInput.TYPE, PacketLevitatorInput.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketLevitatorInput.TYPE, (packet, context) -> packet.handle(new PayloadContext(context.player(), context.server())));
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        ClientNetworking.sendToServer(PacketSendServerCommand.create(Ariente.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        ClientNetworking.sendToServer(PacketSendServerCommand.create(Ariente.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        Packets.sendToPlayer((ServerPlayer) player, PacketSendClientCommand.create(Ariente.MODID, command, argumentBuilder.build()));
    }

    public static void sendToClient(Player player, String command) {
        Packets.sendToPlayer((ServerPlayer) player, PacketSendClientCommand.create(Ariente.MODID, command, TypedMap.EMPTY));
    }
}
