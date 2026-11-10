package mcjty.ariente.mixin.client;

import mcjty.ariente.client.PurifierRecipeSync;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {

    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void ariente$purifierRecipes(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        PurifierRecipeSync.received(((ClientPacketListener) (Object) this).getRecipeManager());
    }
}
