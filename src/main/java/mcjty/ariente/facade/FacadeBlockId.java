package mcjty.ariente.facade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class FacadeBlockId {
    private final String registryName;
//    private final int meta;

    public FacadeBlockId(BlockState mimicBlock) {
        Block block = mimicBlock.getBlock();
        this.registryName = block.getRegistryName().toString();
//        this.meta = block.getMetaFromState(mimicBlock);
    }

    public BlockState getBlockState() {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName)).defaultBlockState();
    }
}
