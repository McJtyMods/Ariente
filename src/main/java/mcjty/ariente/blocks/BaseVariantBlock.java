package mcjty.ariente.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

public abstract class BaseVariantBlock<T extends Enum<T> & StringRepresentable> extends BaseBlock {

    public BaseVariantBlock(BlockBehaviour.Properties properties) {
        super(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .properties(properties.mapColor(MapColor.STONE)
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
    public ItemStack getCloneItemStack(LevelReader worldIn, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this, 1); // @todo 1.14 META?, state.get(getProperty()).ordinal());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(getProperty());
    }

}
