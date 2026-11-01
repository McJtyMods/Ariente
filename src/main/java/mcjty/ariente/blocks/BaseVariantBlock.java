package mcjty.ariente.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.BlockGetter;

public abstract class BaseVariantBlock<T extends Enum<T> & StringRepresentable> extends BaseBlock {

    public BaseVariantBlock() {
        super(new BlockBuilder()
                .properties(Properties.of().mapColor(MapColor.STONE)
                    .strength(2.0f, 4.0f))
                // @todo 1.18 .harvestLevel(ToolType.PICKAXE, 1)
        );
    }

    public abstract EnumProperty<T> getProperty();

    public abstract T[] getValues();

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @SuppressWarnings({"NullableProblems", "deprecation"})
    @Override
    public ItemStack getCloneItemStack(BlockGetter worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(this, 1); // @todo 1.14 META?, state.get(getProperty()).ordinal());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(getProperty());
    }

}
