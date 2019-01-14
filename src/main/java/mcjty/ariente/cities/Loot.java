package mcjty.ariente.cities;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class Loot {

    @Nullable private final ResourceLocation id;
    private final int meta;
    private final boolean blueprint;
    private final int maxAmount;
    private final float chance;

    // null id means a random blueprint
    public Loot(@Nullable ResourceLocation id, int meta, boolean blueprint, int maxAmount, float chance) {
        this.id = id;
        this.meta = meta;
        this.blueprint = blueprint;
        this.maxAmount = maxAmount;
        this.chance = chance;
    }

    @Nullable
    public ResourceLocation getId() {
        return id;
    }

    public int getMeta() {
        return meta;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public float getChance() {
        return chance;
    }

    public boolean isBlueprint() {
        return blueprint;
    }
}
