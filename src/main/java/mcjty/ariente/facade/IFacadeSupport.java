package mcjty.ariente.facade;


import net.minecraft.block.BlockState;

public interface IFacadeSupport {
    BlockState getMimicBlock();

    void setMimicBlock(BlockState mimicBlock);
}
