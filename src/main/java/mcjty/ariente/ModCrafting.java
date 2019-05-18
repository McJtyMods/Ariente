package mcjty.ariente;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.items.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModCrafting {
    public static void init() {
        GameRegistry.addSmelting(ModBlocks.lithiumore, new ItemStack(ModItems.lithiumIngot, 1), 0.0f);
        GameRegistry.addSmelting(ModBlocks.manganeseore, new ItemStack(ModItems.manganeseIngot, 1), 0.0f);
        GameRegistry.addSmelting(ModBlocks.platinumore, new ItemStack(ModItems.platinumIngot, 1), 0.0f);
        GameRegistry.addSmelting(ModBlocks.silverore, new ItemStack(ModItems.silverIngot, 1), 0.0f);
        GameRegistry.addSmelting(ModBlocks.negarite, new ItemStack(ModItems.negariteDust, 4), 0.0f);
        GameRegistry.addSmelting(ModBlocks.posirite, new ItemStack(ModItems.posiriteDust, 4), 0.0f);
    }
}
