package mcjty.ariente.blocks.decorative;

import mcjty.ariente.api.EnumFacingUpDown;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;

public class SlopeBlock extends BaseBlock {

    private static final Property[] PROPERTIES = {EnumFacingUpDown.FACING};

    public SlopeBlock() {
        super(new BlockBuilder()
                .properties(Properties.create(Material.ROCK).hardnessAndResistance(2.0f, 4.0f))
                .harvestLevel(ToolType.PICKAXE, 1)
        );
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    // @todo 1.14
    //    @Override
//    public boolean isTopSolid(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isFullCube(BlockState state) {
//        return false;
//    }


    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rot) {
        EnumFacingUpDown facing = state.get(EnumFacingUpDown.FACING);
        switch (rot) {
            case NONE:
                return state;
            case CLOCKWISE_90:
                return state.with(EnumFacingUpDown.FACING, facing.rotateY());
            case CLOCKWISE_180:
                return state.with(EnumFacingUpDown.FACING, facing.rotateY().rotateY());
            case COUNTERCLOCKWISE_90:
                return state.with(EnumFacingUpDown.FACING, facing.rotateY().rotateY().rotateY());
        }
        return state;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = super.getStateForPlacement(context);
        Direction dir = context.getPlayer().getHorizontalFacing().getOpposite();
        EnumFacingUpDown updown = EnumFacingUpDown.VALUES[dir.ordinal() - 2 + (context.getFace() == Direction.DOWN ? 4 : 0)];
        return state.with(EnumFacingUpDown.FACING, updown);
    }

    @Override
    protected Property<?>[] getProperties() {
        return PROPERTIES;
    }
}
