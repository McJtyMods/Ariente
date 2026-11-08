package mcjty.ariente.facade;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mcjty.ariente.items.ItemData;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import static mcjty.lib.builder.TooltipBuilder.*;

public class FacadeItemBlock extends BlockItem implements ITooltipSettings {

    private TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    gold(stack -> !isMimicing(stack)),
                    parameter("info", FacadeItemBlock::isMimicing, FacadeItemBlock::getMimicingString));

    private static boolean isMimicing(ItemStack stack) {
        return ItemData.has(stack) && ItemData.get(stack).contains("mimic");
    }

    private static String getMimicingString(ItemStack stack) {
        return isMimicing(stack) ? getMimicBlock(stack).getBlock().getName().getString() : "<unset>";
    }

    public FacadeItemBlock(FacadeBlock block) {
        super(block, new Properties()
                );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private FacadeRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    renderer = new FacadeRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }
                return renderer;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }

    public static void setMimicBlock(@Nonnull ItemStack item, BlockState mimicBlock) {
        CompoundTag tagCompound = ItemData.get(item);
        CompoundTag nbt = NbtUtils.writeBlockState(mimicBlock);
        tagCompound.put("mimic", nbt);
        ItemData.set(item, tagCompound);

    }

    public static BlockState getMimicBlock(@Nonnull ItemStack stack) {
        CompoundTag tagCompound = ItemData.get(stack);
        if (tagCompound == null || !tagCompound.contains("mimic")) {
            return Blocks.COBBLESTONE.defaultBlockState();
        } else {
            return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tagCompound.getCompound("mimic"));
        }
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        ItemStack itemstack = context.getItemInHand();

        if (!itemstack.isEmpty()) {

            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof IFacadeSupport) {
                IFacadeSupport facadeSupport = (IFacadeSupport) te;
                if (facadeSupport.getMimicBlock() == null) {
                    if (!world.isClientSide) {
                        facadeSupport.setMimicBlock(getMimicBlock(itemstack));
                    }
                    SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    if (!world.isClientSide && !player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                } else {
                    return InteractionResult.FAIL;
                }
            } else {
                if (!world.isClientSide) {
                    setMimicBlock(itemstack, state);
                }
                if (world.isClientSide) {
                    player.displayClientMessage(Component.literal("Facade is now mimicking ").append(block.getName()), false);
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }
}
