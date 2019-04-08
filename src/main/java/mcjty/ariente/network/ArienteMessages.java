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
import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.thirteen.ChannelBuilder;
import mcjty.lib.thirteen.SimpleChannel;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import javax.annotation.Nonnull;

public class ArienteMessages {

    public static SimpleNetworkWrapper INSTANCE;

    public static void registerMessages(String name) {
        SimpleChannel net = ChannelBuilder
                .named(new ResourceLocation(Ariente.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();


        INSTANCE = net.getNetwork();

        // Server side
        net.registerMessageServer(id(), PacketClickStorage.class, PacketClickStorage::toBytes, PacketClickStorage::new, PacketClickStorage::handle);
        net.registerMessageServer(id(), PacketHitForcefield.class, PacketHitForcefield::toBytes, PacketHitForcefield::new, PacketHitForcefield::handle);
        net.registerMessageServer(id(), PacketFullHealth.class, PacketFullHealth::toBytes, PacketFullHealth::new, PacketFullHealth::handle);
        net.registerMessageServer(id(), PacketConfigureArmor.class, PacketConfigureArmor::toBytes, PacketConfigureArmor::new, PacketConfigureArmor::handle);
        net.registerMessageServer(id(), PacketArmorHotkey.class, PacketArmorHotkey::toBytes, PacketArmorHotkey::new, PacketArmorHotkey::handle);
        net.registerMessageServer(id(), PacketShipAction.class, PacketShipAction::toBytes, PacketShipAction::new, PacketShipAction::handle);
        net.registerMessageServer(id(), PacketAutoFieldRequestRenderInfo.class, PacketAutoFieldRequestRenderInfo::toBytes, PacketAutoFieldRequestRenderInfo::new, PacketAutoFieldRequestRenderInfo::handle);

        // Client side
        net.registerMessageClient(id(), PacketDamageForcefield.class, PacketDamageForcefield::toBytes, PacketDamageForcefield::new, PacketDamageForcefield::handle);
        net.registerMessageClient(id(), PacketAutoFieldReturnRenderInfo.class, PacketAutoFieldReturnRenderInfo::toBytes, PacketAutoFieldReturnRenderInfo::new, PacketAutoFieldReturnRenderInfo::handle);
    }

    private static int id() {
        return PacketHandler.nextPacketID();
    }

    public static void sendToServer(String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendToServer(new PacketSendServerCommand(Ariente.MODID, command, argumentBuilder.build()));
    }

    public static void sendToServer(String command) {
        INSTANCE.sendToServer(new PacketSendServerCommand(Ariente.MODID, command, TypedMap.EMPTY));
    }

    public static void sendToClient(EntityPlayer player, String command, @Nonnull TypedMap.Builder argumentBuilder) {
        INSTANCE.sendTo(new PacketSendClientCommand(Ariente.MODID, command, argumentBuilder.build()), (EntityPlayerMP) player);
    }

    public static void sendToClient(EntityPlayer player, String command) {
        INSTANCE.sendTo(new PacketSendClientCommand(Ariente.MODID, command, TypedMap.EMPTY), (EntityPlayerMP) player);
    }
}
