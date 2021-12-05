package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.entities.EntityArientePearl;
import mcjty.ariente.setup.Registration;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.DimensionType;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.header;

import net.minecraft.item.Item.Properties;

public class ArientePearlItem extends Item implements ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header());

    public ArientePearlItem() {
        super(new Properties().stacksTo(16).tab(Ariente.setup.getTab()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flagIn);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Hand hand = context.getHand();
        PlayerEntity player = context.getPlayer();
        Direction facing = context.getClickedFace();
        BlockState state = world.getBlockState(pos);
        ItemStack itemstack = player.getItemInHand(hand);

        if (player.mayUseItemAt(pos.relative(facing), facing, itemstack) && state.getBlock() == Registration.WARPER.get()) {
            if (world.isClientSide) {
                return ActionResultType.SUCCESS;
            } else {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof WarperTile) {
                    WarperTile warper = (WarperTile) te;
                    int charges = warper.getCharges();
                    if (charges >= UtilityConfiguration.WARPER_MAX_CHARGES.get()) {
                        player.displayClientMessage(new StringTextComponent("Already fully charged!"), false);
                        return ActionResultType.SUCCESS;
                    }
                    warper.setCharges(charges+1);
                    int pct = warper.getChargePercentage();
                    player.displayClientMessage(new StringTextComponent("Charged to " + pct + "%"), false);
                }
                itemstack.shrink(1);

                for (int i = 0; i < 16; ++i) {
                    double dx = (pos.getX() + (5.0F + random.nextFloat() * 6.0F) / 16.0F);
                    double dy = (pos.getY() + 0.8125F);
                    double dz = (pos.getZ() + (5.0F + random.nextFloat() * 6.0F) / 16.0F);
                    world.addParticle(ParticleTypes.SMOKE, dx, dy, dz, 0.0D, 0.0D, 0.0D);
                }

                world.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResultType.SUCCESS;
            }
        } else {
            return ActionResultType.PASS;
        }
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return ActionResultType.PASS;
    }


    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (world.dimension() != World.OVERWORLD) {
            if (world.isClientSide) {
                player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Doesn't work in this dimension!"), false);
            }
            return new ActionResult<>(ActionResultType.FAIL, itemstack);
        }

        RayTraceResult raytraceresult = Item.getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.NONE);

        if (raytraceresult instanceof BlockRayTraceResult && raytraceresult.getType() == RayTraceResult.Type.BLOCK && world.getBlockState(((BlockRayTraceResult) raytraceresult).getBlockPos()).getBlock() == Registration.WARPER.get()) {
            return new ActionResult<>(ActionResultType.PASS, itemstack);
        } else {
            player.startUsingItem(hand);

            if (!world.isClientSide) {
                BlockPos blockpos = ArienteWorldCompat.getArienteWorld().getNearestDungeon(world, player.blockPosition());

                if (blockpos != null) {
                    EntityArientePearl entityendereye = EntityArientePearl.create(world, player.getX(), player.getY() + (player.getBbHeight() / 2.0F), player.getZ());
                    entityendereye.moveTowards(blockpos);
                    world.addFreshEntity(entityendereye);

                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
                    world.levelEvent(null, 1003, player.blockPosition(), 0);

                    if (!player.abilities.instabuild) {
                        itemstack.shrink(1);
                    }

                    return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
                } else {
                    player.displayClientMessage(new StringTextComponent("Can't find any nearby Ariente dungeon!"), false);
                }
            }

            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }
    }

}