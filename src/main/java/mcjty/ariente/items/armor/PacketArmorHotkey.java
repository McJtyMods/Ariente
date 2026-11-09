package mcjty.ariente.items.armor;

import mcjty.ariente.items.modules.ModuleSupport;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketArmorHotkey implements CustomPacketPayload {

    public static final Type<PacketArmorHotkey> TYPE = new Type<>(Identifier.fromNamespaceAndPath("ariente", "packet_armor_hotkey"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketArmorHotkey> CODEC = StreamCodec.ofMember(PacketArmorHotkey::toBytes, PacketArmorHotkey::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private int index;

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeInt(index);
    }

    public PacketArmorHotkey() {
    }

    public PacketArmorHotkey(RegistryFriendlyByteBuf buf) {
        index = buf.readInt();
    }

    public PacketArmorHotkey(int index) {
        this.index = index;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player playerEntity = (ServerPlayer) ctx.player();
            ModuleSupport.receivedHotkey(playerEntity, index);
        });
    }
}
