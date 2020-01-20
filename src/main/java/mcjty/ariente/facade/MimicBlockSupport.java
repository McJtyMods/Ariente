package mcjty.ariente.facade;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class MimicBlockSupport {

    @Nullable
    private BlockState mimicBlock = null;

    @Nullable
    public BlockState getMimicBlock() {
        return mimicBlock;
    }

    public void setMimicBlock(@Nullable BlockState mimicBlock) {
        this.mimicBlock = mimicBlock;
    }


    public void readFromNBT(CompoundNBT tagCompound) {
        if (tagCompound.contains("regName")) {
            String regName = tagCompound.getString("regName");
            // @todo 1.14 meta
//            int meta = tagCompound.getInt("meta");
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(regName));
            if (value == null) {
                mimicBlock = Blocks.COBBLESTONE.getDefaultState();
            } else {
                // @todo 1.14 meta
                mimicBlock = value.getDefaultState();
            }
        } else {
            mimicBlock = null;
        }
    }

    public void writeToNBT(CompoundNBT tagCompound) {
        if (mimicBlock != null) {
            tagCompound.putString("regName", mimicBlock.getBlock().getRegistryName().toString());
            // @todo 1.14 meta
//            tagCompound.putInt("meta", mimicBlock.getBlock().getMetaFromState(mimicBlock));
        }
    }
}
