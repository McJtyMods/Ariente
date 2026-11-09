package mcjty.ariente.blocks.decorative;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import javax.annotation.Nullable;
import mcjty.ariente.api.EnumFacingUpDown;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;

public class SlopeBlock extends BaseBlock {

    private static final Property[] PROPERTIES = {EnumFacingUpDown.FACING};

    public SlopeBlock(BlockBehaviour.Properties properties) {
        super(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .properties(properties.mapColor(MapColor.STONE).strength(2.0f, 4.0f).noOcclusion())
                // @todo 1.18 .harvestLevel(ToolType.PICKAXE, 1)
        );
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
        EnumFacingUpDown facing = state.getValue(EnumFacingUpDown.FACING);
        switch (rot) {
            case NONE:
                return state;
            case CLOCKWISE_90:
                return state.setValue(EnumFacingUpDown.FACING, facing.rotateY());
            case CLOCKWISE_180:
                return state.setValue(EnumFacingUpDown.FACING, facing.rotateY().rotateY());
            case COUNTERCLOCKWISE_90:
                return state.setValue(EnumFacingUpDown.FACING, facing.rotateY().rotateY().rotateY());
        }
        return state;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Direction dir = context.getPlayer().getDirection().getOpposite();
        EnumFacingUpDown updown = EnumFacingUpDown.VALUES[dir.ordinal() - 2 + (context.getClickedFace() == Direction.DOWN ? 4 : 0)];
        return state.setValue(EnumFacingUpDown.FACING, updown);
    }

    @Override
    protected Property<?>[] getProperties() {
        return PROPERTIES;
    }
}
