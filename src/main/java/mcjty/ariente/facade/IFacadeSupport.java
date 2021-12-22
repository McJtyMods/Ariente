package mcjty.ariente.facade;


import net.minecraft.world.level.block.state.BlockState;

public interface IFacadeSupport {
    BlockState getMimicBlock();

    void setMimicBlock(BlockState mimicBlock);
}
