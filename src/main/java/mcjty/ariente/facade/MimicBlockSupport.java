package mcjty.ariente.facade;

import net.minecraft.core.registries.Registries;
import javax.annotation.Nullable;
import mcjty.lib.varia.Tools;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
        if (tagCompound.contains("mimicState")) {
            mimicBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, tagCompound.getCompoundOrEmpty("mimicState"));
        } else if (tagCompound.contains("regName")) {
            String regName = tagCompound.getStringOr("regName", "");
            Block value = BuiltInRegistries.BLOCK.getValue(Identifier.parse(regName));
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
            tagCompound.put("mimicState", NbtUtils.writeBlockState(mimicBlock));
            tagCompound.putString("regName", Tools.getId(mimicBlock.getBlock()).toString());
        }
    }
}
