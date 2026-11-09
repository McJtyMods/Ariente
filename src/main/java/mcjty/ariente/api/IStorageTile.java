package mcjty.ariente.api;

import net.minecraft.resources.Identifier;

public interface IStorageTile {

    void setLoot(Identifier id, int slot, boolean doBlueprint, int amount, int meta);
}
