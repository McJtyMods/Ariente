package mcjty.ariente.network;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ArienteMessages {

    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("ariente").versioned("1");
        registrar.playToServer(PacketStorageCubeAction.TYPE, PacketStorageCubeAction.CODEC, PacketStorageCubeAction::handle);
        registrar.playToClient(PacketStorageCubeData.TYPE, PacketStorageCubeData.CODEC, PacketStorageCubeData::handle);
        registrar.playToServer(PacketDisplayAction.TYPE, PacketDisplayAction.CODEC, PacketDisplayAction::handle);
        registrar.playToServer(PacketDisplayGesture.TYPE, PacketDisplayGesture.CODEC, PacketDisplayGesture::handle);
        registrar.playToClient(PacketDisplayGestureResult.TYPE, PacketDisplayGestureResult.CODEC, PacketDisplayGestureResult::handle);
        registrar.playToServer(PacketClickStorage.TYPE, PacketClickStorage.CODEC, PacketClickStorage::handle);
        registrar.playToServer(PacketHitForcefield.TYPE, PacketHitForcefield.CODEC, PacketHitForcefield::handle);
        registrar.playToServer(PacketConfigureArmor.TYPE, PacketConfigureArmor.CODEC, PacketConfigureArmor::handle);
        registrar.playToServer(PacketArmorHotkey.TYPE, PacketArmorHotkey.CODEC, PacketArmorHotkey::handle);
        registrar.playToServer(PacketShipAction.TYPE, PacketShipAction.CODEC, PacketShipAction::handle);
        registrar.playToServer(PacketAutoFieldRequestRenderInfo.TYPE, PacketAutoFieldRequestRenderInfo.CODEC, PacketAutoFieldRequestRenderInfo::handle);
        registrar.playToClient(PacketDisplayData.TYPE, PacketDisplayData.CODEC, PacketDisplayData::handle);
        registrar.playToServer(PacketRequestDisplayData.TYPE, PacketRequestDisplayData.CODEC, PacketRequestDisplayData::handle);
        registrar.playToClient(PacketDamageForcefield.TYPE, PacketDamageForcefield.CODEC, PacketDamageForcefield::handle);
        registrar.playToClient(PacketAutoFieldReturnRenderInfo.TYPE, PacketAutoFieldReturnRenderInfo.CODEC, PacketAutoFieldReturnRenderInfo::handle);
        registrar.playToClient(PacketPersonalForcefield.TYPE, PacketPersonalForcefield.CODEC, PacketPersonalForcefield::handle);
        registrar.playToServer(PacketLevitatorInput.TYPE, PacketLevitatorInput.CODEC, PacketLevitatorInput::handle);
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        ClientPacketDistributor.sendToServer(PacketSendServerCommand.create(Ariente.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        ClientPacketDistributor.sendToServer(PacketSendServerCommand.create(Ariente.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, PacketSendClientCommand.create(Ariente.MODID, command, argumentBuilder.build()));
    }

    public static void sendToClient(Player player, String command) {
        PacketDistributor.sendToPlayer((ServerPlayer) player, PacketSendClientCommand.create(Ariente.MODID, command, TypedMap.EMPTY));
    }
}
