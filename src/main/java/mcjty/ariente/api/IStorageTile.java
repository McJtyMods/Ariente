package mcjty.ariente.api;

import net.minecraft.resources.ResourceLocation;

public interface IStorageTile {

    void setLoot(ResourceLocation id, int slot, boolean doBlueprint, int amount, int meta);
}
