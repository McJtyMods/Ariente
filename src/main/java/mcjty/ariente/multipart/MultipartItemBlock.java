package mcjty.ariente.multipart;

import mcjty.ariente.setup.Registration;
import mcjty.lib.multipart.IPartBlock;
import mcjty.lib.multipart.PartSlot;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MultipartItemBlock extends BlockItem {

    public MultipartItemBlock(Block block) {
        this(block, new Properties());
    }

    public MultipartItemBlock(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MultipartTE) {
            // Keep the original hit coordinates when adding a second part to a container.
            BlockPlaceContext inside = new BlockPlaceContext(context) {
                @Override
                public BlockPos getClickedPos() {
                    return context.getClickedPos();
                }
            };
            BlockState state = getBlock().getStateForPlacement(inside);
            PartSlot slot = ((IPartBlock) getBlock()).getSlotFromState(context.getLevel(), context.getClickedPos(), state);
            MultipartTE tile = (MultipartTE) context.getLevel().getBlockEntity(context.getClickedPos());
            if (!tile.getParts().containsKey(slot)) {
                return place(inside);
            }
        }
        return place(new BlockPlaceContext(context));
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null || stack.isEmpty() || !player.mayUseItemAt(pos, context.getClickedFace(), stack)
                || !level.isInWorldBounds(pos)) {
            return InteractionResult.FAIL;
        }
        BlockState state = getBlock().getStateForPlacement(context);
        if (state == null || !state.canSurvive(level, pos)) {
            return InteractionResult.FAIL;
        }
        PartSlot slot = ((IPartBlock) getBlock()).getSlotFromState(level, pos, state);
        BlockEntity existing = level.getBlockEntity(pos);
        if (existing instanceof MultipartTE multipart) {
            if (multipart.getParts().containsKey(slot)) {
                return InteractionResult.FAIL;
            }
        } else if (!level.getBlockState(pos).canBeReplaced(context)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            if (!(existing instanceof MultipartTE)) {
                if (!level.setBlockAndUpdate(pos, Registration.MULTIPART_BLOCK.get().defaultBlockState())) {
                    return InteractionResult.FAIL;
                }
                existing = level.getBlockEntity(pos);
            }
            if (!(existing instanceof MultipartTE multipart)) {
                return InteractionResult.FAIL;
            }
            BlockEntity child = state.getBlock() instanceof EntityBlock block ? block.newBlockEntity(pos, state) : null;
            if (child != null) {
                child.setLevel(level);
                CompoundTag saved = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
                if (saved != null) {
                    child.loadWithComponents(saved.copy(), level.registryAccess());
                }
            }
            multipart.addPart(slot, state, child);
            if (child instanceof GenericTileEntity generic) {
                generic.setOwner(player);
                generic.onBlockPlacedBy(level, pos, state, player, stack);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, stack);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        SoundType sound = state.getSoundType();
        level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1) / 2, sound.getPitch() * .8F);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
