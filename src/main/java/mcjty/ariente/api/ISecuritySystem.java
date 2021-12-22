package mcjty.ariente.api;

import net.minecraft.world.level.Level;

public interface ISecuritySystem {
    String generateKeyId(Level world);
}
