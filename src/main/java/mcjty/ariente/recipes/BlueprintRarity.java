package mcjty.ariente.recipes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public enum BlueprintRarity {
    COMMON(BlueprintRecipeRegistry.COMMON, "common", ChatFormatting.WHITE),
    UNCOMMON(BlueprintRecipeRegistry.UNCOMMON, "uncommon", ChatFormatting.YELLOW),
    RARE(BlueprintRecipeRegistry.RARE, "rare", ChatFormatting.AQUA),
    EXTREME(BlueprintRecipeRegistry.EXTREME, "extreme", ChatFormatting.LIGHT_PURPLE);

    private final float weight;
    private final String name;
    private final ChatFormatting color;

    BlueprintRarity(float weight, String name, ChatFormatting color) {
        this.weight = weight;
        this.name = name;
        this.color = color;
    }

    public Component displayName() {
        return Component.translatable("rarity.ariente.blueprint." + name).withStyle(color);
    }

    @Nullable
    public static BlueprintRarity of(ConstructorRecipe recipe) {
        for (BlueprintRarity rarity : values()) {
            if (Float.compare(recipe.getChance(), rarity.weight) == 0) {
                return rarity;
            }
        }
        return null;
    }
}
