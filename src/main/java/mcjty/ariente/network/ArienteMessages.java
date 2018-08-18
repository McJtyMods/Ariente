package mcjty.ariente.network;

import mcjty.ariente.Ariente;
import mcjty.ariente.bindings.PacketFullHealth;
import mcjty.ariente.blocks.defense.PacketDamageForcefield;
import mcjty.ariente.blocks.utility.PacketClickStorage;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

public class ArienteMessages {
    public static SimpleNetworkWrapper INSTANCE;

    public static void registerNetworkMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;

        // Server side
        net.registerMessage(PacketClickStorage.Handler.class, PacketClickStorage.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketHitForcefield.Handler.class, PacketHitForcefield.class, PacketHandler.nextPacketID(), Side.SERVER);
        net.registerMessage(PacketFullHealth.Handler.class, PacketFullHealth.class, PacketHandler.nextPacketID(), Side.SERVER);

        // Client side
        net.registerMessage(PacketDamageForcefield.Handler.class, PacketDamageForcefield.class, PacketHandler.nextPacketID(), Side.CLIENT);
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
