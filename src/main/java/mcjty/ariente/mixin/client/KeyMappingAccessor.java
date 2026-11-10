package mcjty.ariente.mixin.client;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {

    @Accessor("clickCount")
    int ariente$getClickCount();

    @Accessor("clickCount")
    void ariente$setClickCount(int count);
}
