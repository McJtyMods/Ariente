package mcjty.ariente.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Map;
import mcjty.ariente.bindings.Binding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ship bindings share movement keys with vanilla; both mappings must receive each input. */
@Mixin(KeyMapping.class)
abstract class KeyMappingMixin {

    @Shadow @Final private static Map<String, KeyMapping> ALL;

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void ariente$sharedHeldKey(InputConstants.Key key, boolean pressed, CallbackInfo ci) {
        if (Binding.isBoundTo(key)) {
            ALL.values().stream()
                    .filter(mapping -> KeyBindingHelper.getBoundKeyOf(mapping).equals(key))
                    .forEach(mapping -> mapping.setDown(pressed));
            ci.cancel();
        }
    }

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void ariente$sharedKeyClick(InputConstants.Key key, CallbackInfo ci) {
        if (Binding.isBoundTo(key)) {
            ALL.values().stream()
                    .filter(mapping -> KeyBindingHelper.getBoundKeyOf(mapping).equals(key))
                    .forEach(mapping -> {
                        var access = (KeyMappingAccessor) mapping;
                        access.ariente$setClickCount(access.ariente$getClickCount() + 1);
                    });
            ci.cancel();
        }
    }
}
