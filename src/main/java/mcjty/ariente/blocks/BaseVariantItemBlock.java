package mcjty.ariente.blocks;

import mcjty.lib.blocks.DamageMetadataItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class BaseVariantItemBlock extends DamageMetadataItemBlock {

    public BaseVariantItemBlock(Block block) {
        super(block);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return ((BaseVariantBlock)this.block).getUnlocalizedName(stack.getMetadata());
    }
}
