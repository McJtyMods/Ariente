package mcjty.ariente.multipart;

import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.blocks.utility.autofield.AbstractNodeTile;
import mcjty.lib.compat.theoneprobe.TOPDriver;
import mcjty.lib.compat.theoneprobe.TOPInfoProvider;
import mcjty.lib.compat.waila.WailaInfoProvider;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.CompoundText;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MultipartBlock extends Block implements WailaInfoProvider, TOPInfoProvider, EntityBlock {

    public static final AABB AABB_EMPTY = new AABB(0, 0, 0, 0, 0, 0);
    public static final AABB AABB_CENTER = new AABB(.4, .4, .4, .6, .6, .6);

    public MultipartBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.METAL)
                .strength(2.0f)
                .noOcclusion()
                .sound(SoundType.METAL));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultipartTE(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData, Player player) {
        MultipartTE.Part part = getHitPart(state, world, pos, player.getEyePosition(0), player.getEyePosition(0).add(player.getLookAngle().scale(player.blockInteractionRange())));
        if (part != null) {
            return new ItemStack(part.getState().getBlock().asItem());
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemStack getCloneItemStack(@Nonnull LevelReader worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return combinePartShapes(world, pos, s -> s.getShape(world, pos, context));
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return combinePartShapes(world, pos, s -> s.getCollisionShape(world, pos, context));
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getInteractionShape(@Nonnull BlockState state, @Nonnull BlockGetter world, @Nonnull BlockPos pos) {
        return combinePartShapes(world, pos, s -> s.getVisualShape(world, pos, CollisionContext.empty()));
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getOcclusionShape(@Nonnull BlockState state) {
        return Shapes.empty();
    }

    private VoxelShape combinePartShapes(BlockGetter world, BlockPos pos, Function<BlockState, VoxelShape> shapeGetter) {
        VoxelShape combined = Shapes.empty();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof MultipartTE) {
            MultipartTE multipartTE = (MultipartTE) te;
            for (Map.Entry<PartSlot, MultipartTE.Part> entry : multipartTE.getParts().entrySet()) {
                MultipartTE.Part part = entry.getValue();
                VoxelShape shape = shapeGetter.apply(part.getState());
                if (combined.isEmpty()) {
                    combined = shape;
                } else {
                    combined = Shapes.join(combined, shape, BooleanOp.OR);
                }
            }
        }
        return combined;
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level world, BlockPos pos, @Nonnull Player player, BlockHitResult result) {
        Vec3 hit = result.getLocation();
        Vec3 start = MultipartHelper.getPlayerEyes(player);
        Vec3 end = start.add(hit.subtract(start).scale(3));
        MultipartTE.Part part = getHitPart(state, world, pos, start, end);
        if (part != null) {
            if (part.getBlockEntity() instanceof GenericTileEntity genTileEntity) {
                return genTileEntity.onBlockActivated(part.getState(), player, InteractionHand.MAIN_HAND, result);
            } else {
                return part.getState().useWithoutItem(world, player, result);
            }
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof MultipartTE multipartTE) {
            multipartTE.setChanged();
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    public BlockState getHitState(BlockState blockState, Level world, BlockPos pos, Vec3 start, Vec3 end) {
        MultipartTE.Part part = getHitPart(blockState, world, pos, start, end);
        if (part != null) {
            return part.getState();
        } else {
            return null;
        }
    }

    @Nullable
    public static MultipartTE.Part getHitPart(BlockState blockState, BlockGetter world, BlockPos pos, Vec3 start, Vec3 end) {
        if (!(world.getBlockEntity(pos) instanceof MultipartTE multipart)) {
            return null;
        }
        MultipartTE.Part closest = null;
        double distance = Double.MAX_VALUE;
        // Extend the endpoint slightly: a clone/probe hit is exactly on the surface.
        Vec3 extendedEnd = end.add(end.subtract(start).normalize().scale(.001));
        for (MultipartTE.Part part : multipart.getParts().values()) {
            BlockHitResult hit = part.getState().getShape(world, pos).clip(start, extendedEnd, pos);
            if (hit != null && start.distanceToSqr(hit.getLocation()) < distance) {
                closest = part;
                distance = start.distanceToSqr(hit.getLocation());
            }
        }
        return closest;
    }

    public static ItemStack getPartStack(MultipartTE.Part part) {
        ItemStack stack = new ItemStack(part.getState().getBlock());
        BlockEntity child = part.getBlockEntity();
        if (child != null && !stack.isEmpty()) {
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, child.getLevel().registryAccess());
            output.store(child.saveWithoutMetadata(child.getLevel().registryAccess()));
            BlockItem.setBlockEntityData(stack, child.getType(), output);
        }
        return stack;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, ItemStack tool, boolean willHarvest, FluidState fluid) {
        if (world.isClientSide() || !(world.getBlockEntity(pos) instanceof MultipartTE multipart)) {
            return false;
        }
        Vec3 start = player.getEyePosition();
        MultipartTE.Part part = getHitPart(state, world, pos, start, start.add(player.getLookAngle().scale(player.blockInteractionRange() + 1)));
        if (part == null || part.getBlockEntity() instanceof ILockable lock && lock.isLocked()) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            popResource(world, pos, getPartStack(part));
        }
        multipart.removePart(part.getState());
        if (multipart.getParts().isEmpty()) {
            world.removeBlock(pos, false);
        }
        // The selected part and its drop are already handled. Keep the container's loot out of playerDestroy.
        return false;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MultipartTE multipart) {
            multipart.getParts().values().forEach(part -> drops.add(getPartStack(part)));
        }
        return drops;
    }

    @Override
    public TOPDriver getProbeDriver() {
        return (mode, info, player, level, state, hit) -> {
            MultipartTE.Part part = getHitPart(state, level, hit.getPos(), player.getEyePosition(), hit.getHitVec());
            if (part != null) {
                info.item(new ItemStack(part.getState().getBlock()));
                info.text(part.getState().getBlock().getName());
                if (part.getBlockEntity() instanceof AbstractNodeTile node) {
                    long colors = Arrays.stream(node.getFilters()).filter(Objects::nonNull).count();
                    info.text(CompoundText.createLabelInfo("Color filters: ", colors));
                }
            }
        };
    }

}
