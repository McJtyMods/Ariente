package mcjty.ariente.network;

import mcjty.ariente.Ariente;
import mcjty.ariente.bindings.PacketFullHealth;
import mcjty.ariente.blocks.defense.PacketDamageForcefield;
import mcjty.ariente.blocks.utility.PacketClickStorage;
import mcjty.ariente.blocks.utility.autofield.PacketAutoFieldRequestRenderInfo;
import mcjty.ariente.blocks.utility.autofield.PacketAutoFieldReturnRenderInfo;
import mcjty.ariente.entities.fluxship.PacketShipAction;
import mcjty.ariente.items.armor.PacketArmorHotkey;
import mcjty.ariente.items.armor.PacketConfigureArmor;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nonnull;

public class ArienteMessages {

    public static SimpleChannel INSTANCE;

    public static void registerMessages(String name) {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Ariente.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();


        INSTANCE = net;

        // Server side
        net.registerMessage(id(), PacketClickStorage.class, PacketClickStorage::toBytes, PacketClickStorage::new, PacketClickStorage::handle);
        net.registerMessage(id(), PacketHitForcefield.class, PacketHitForcefield::toBytes, PacketHitForcefield::new, PacketHitForcefield::handle);
        net.registerMessage(id(), PacketFullHealth.class, PacketFullHealth::toBytes, PacketFullHealth::new, PacketFullHealth::handle);
        net.registerMessage(id(), PacketConfigureArmor.class, PacketConfigureArmor::toBytes, PacketConfigureArmor::new, PacketConfigureArmor::handle);
        net.registerMessage(id(), PacketArmorHotkey.class, PacketArmorHotkey::toBytes, PacketArmorHotkey::new, PacketArmorHotkey::handle);
        net.registerMessage(id(), PacketShipAction.class, PacketShipAction::toBytes, PacketShipAction::new, PacketShipAction::handle);
        net.registerMessage(id(), PacketAutoFieldRequestRenderInfo.class, PacketAutoFieldRequestRenderInfo::toBytes, PacketAutoFieldRequestRenderInfo::new, PacketAutoFieldRequestRenderInfo::handle);

        // Client side
        net.registerMessage(id(), PacketDamageForcefield.class, PacketDamageForcefield::toBytes, PacketDamageForcefield::new, PacketDamageForcefield::handle);
        net.registerMessage(id(), PacketAutoFieldReturnRenderInfo.class, PacketAutoFieldReturnRenderInfo::toBytes, PacketAutoFieldReturnRenderInfo::new, PacketAutoFieldReturnRenderInfo::handle);
    }

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendToServer(new PacketSendServerCommand(Ariente.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        INSTANCE.sendToServer(new PacketSendServerCommand(Ariente.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(Player player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendTo(new PacketSendClientCommand(Ariente.MODID, command, argumentBuilder.build()), ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToClient(Player player, String command) {
        INSTANCE.sendTo(new PacketSendClientCommand(Ariente.MODID, command, TypedMap.EMPTY), ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
