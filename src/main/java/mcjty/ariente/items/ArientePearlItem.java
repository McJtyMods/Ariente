package mcjty.ariente.items;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.entities.EntityArientePearl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ArientePearlItem extends GenericItem {

    public ArientePearlItem() {
        super("ariente_pearl");
        this.maxStackSize = 16;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TextFormatting.GOLD + "With this item you can");
        tooltip.add(TextFormatting.GOLD + "locate the mystical Ariente");
        tooltip.add(TextFormatting.GOLD + "dungeons and charge them");
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
        IBlockState state = world.getBlockState(pos);
        ItemStack itemstack = player.getHeldItem(hand);

        if (player.canPlayerEdit(pos.offset(facing), facing, itemstack) && state.getBlock() == ModBlocks.warperBlock) {
            if (world.isRemote) {
                return EnumActionResult.SUCCESS;
            } else {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof WarperTile) {
                    WarperTile warper = (WarperTile) te;
                    int charges = warper.getCharges();
                    if (charges >= UtilityConfiguration.WARPER_MAX_CHARGES.get()) {
                        player.sendStatusMessage(new TextComponentString("Already fully charged!"), false);
                        return EnumActionResult.SUCCESS;
                    }
                    warper.setCharges(charges+1);
                    int pct = warper.getChargePercentage();
                    player.sendStatusMessage(new TextComponentString("Charged to " + pct + "%"), false);
                }
                itemstack.shrink(1);

                for (int i = 0; i < 16; ++i) {
                    double dx = (pos.getX() + (5.0F + itemRand.nextFloat() * 6.0F) / 16.0F);
                    double dy = (pos.getY() + 0.8125F);
                    double dz = (pos.getZ() + (5.0F + itemRand.nextFloat() * 6.0F) / 16.0F);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, dx, dy, dz, 0.0D, 0.0D, 0.0D);
                }

                world.playSound(null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return EnumActionResult.SUCCESS;
            }
        } else {
            return EnumActionResult.PASS;
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.PASS;
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (world.provider.getDimension() != 0) {
            if (world.isRemote) {
                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Doesn't work in this dimension!"), false);
            }
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
        }

        RayTraceResult raytraceresult = this.rayTrace(world, player, false);

        if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK && world.getBlockState(raytraceresult.getBlockPos()).getBlock() == ModBlocks.warperBlock) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        } else {
            player.setActiveHand(hand);

            if (!world.isRemote) {
                BlockPos blockpos = ArienteWorldCompat.getArienteWorld().getNearestDungeon(world, new BlockPos(player));

                if (blockpos != null) {
                    EntityArientePearl entityendereye = new EntityArientePearl(world, player.posX, player.posY + (player.height / 2.0F), player.posZ);
                    entityendereye.moveTowards(blockpos);
                    world.spawnEntity(entityendereye);

                    world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                    world.playEvent(null, 1003, new BlockPos(player), 0);

                    if (!player.capabilities.isCreativeMode) {
                        itemstack.shrink(1);
                    }

                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                } else {
                    player.sendStatusMessage(new TextComponentString("Can't find any nearby Ariente dungeon!"), false);
                }
            }

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }
    }

}