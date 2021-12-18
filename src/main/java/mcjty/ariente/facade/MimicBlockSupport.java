package mcjty.ariente.facade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
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


    public void readFromNBT(CompoundTag tagCompound) {
        if (tagCompound.contains("regName")) {
            String regName = tagCompound.getString("regName");
            // @todo 1.14 meta
//            int meta = tagCompound.getInt("meta");
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(regName));
            if (value == null) {
                mimicBlock = Blocks.COBBLESTONE.defaultBlockState();
            } else {
                // @todo 1.14 meta
                mimicBlock = value.defaultBlockState();
            }
        } else {
            mimicBlock = null;
        }
    }

    public void writeToNBT(CompoundTag tagCompound) {
        if (mimicBlock != null) {
            tagCompound.putString("regName", mimicBlock.getBlock().getRegistryName().toString());
            // @todo 1.14 meta
//            tagCompound.putInt("meta", mimicBlock.getBlock().getMetaFromState(mimicBlock));
        }
    }
}
