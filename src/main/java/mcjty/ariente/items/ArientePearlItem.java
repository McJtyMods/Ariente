package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.entities.EntityArientePearl;
import mcjty.ariente.setup.Registration;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.ChatFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static mcjty.lib.builder.TooltipBuilder.header;

public class ArientePearlItem extends Item implements ITooltipSettings {
    private static Random random = new Random();
    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header());

    public ArientePearlItem() {
        super(new Properties().stacksTo(16).tab(Ariente.setup.getTab()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flagIn);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        InteractionHand hand = context.getHand();
        Player player = context.getPlayer();
        Direction facing = context.getClickedFace();
        BlockState state = world.getBlockState(pos);
        ItemStack itemstack = player.getItemInHand(hand);

        if (player.mayUseItemAt(pos.relative(facing), facing, itemstack) && state.getBlock() == Registration.WARPER.get()) {
            if (world.isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                BlockEntity te = world.getBlockEntity(pos);
                if (te instanceof WarperTile) {
                    WarperTile warper = (WarperTile) te;
                    int charges = warper.getCharges();
                    if (charges >= UtilityConfiguration.WARPER_MAX_CHARGES.get()) {
                        player.displayClientMessage(new TextComponent("Already fully charged!"), false);
                        return InteractionResult.SUCCESS;
                    }
                    warper.setCharges(charges+1);
                    int pct = warper.getChargePercentage();
                    player.displayClientMessage(new TextComponent("Charged to " + pct + "%"), false);
                }
                itemstack.shrink(1);

                for (int i = 0; i < 16; ++i) {
                    double dx = (pos.getX() + (5.0F + random.nextFloat() * 6.0F) / 16.0F);
                    double dy = (pos.getY() + 0.8125F);
                    double dz = (pos.getZ() + (5.0F + random.nextFloat() * 6.0F) / 16.0F);
                    world.addParticle(ParticleTypes.SMOKE, dx, dy, dz, 0.0D, 0.0D, 0.0D);
                }

                world.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (world.dimension() != Level.OVERWORLD) {
            if (world.isClientSide) {
                player.displayClientMessage(new TextComponent(ChatFormatting.RED + "Doesn't work in this dimension!"), false);
            }
            return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        }

        HitResult raytraceresult = Item.getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);

        if (raytraceresult instanceof BlockHitResult && raytraceresult.getType() == HitResult.Type.BLOCK && world.getBlockState(((BlockHitResult) raytraceresult).getBlockPos()).getBlock() == Registration.WARPER.get()) {
            return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
        } else {
            player.startUsingItem(hand);

            if (!world.isClientSide) {
                BlockPos blockpos = ArienteWorldCompat.getArienteWorld().getNearestDungeon(world, player.blockPosition());

                if (blockpos != null) {
                    EntityArientePearl entityendereye = EntityArientePearl.create(world, player.getX(), player.getY() + (player.getBbHeight() / 2.0F), player.getZ());
                    entityendereye.moveTowards(blockpos);
                    world.addFreshEntity(entityendereye);

                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
                    world.levelEvent(null, 1003, player.blockPosition(), 0);

                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
                } else {
                    player.displayClientMessage(new TextComponent("Can't find any nearby Ariente dungeon!"), false);
                }
            }

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
        }
    }

}