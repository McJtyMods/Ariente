package mcjty.ariente.items;

import mcjty.ariente.cities.CityTools;
import mcjty.ariente.config.WorldgenConfiguration;
import mcjty.lib.varia.TeleportationTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class DirtyDiamondItem extends GenericItem {

    public DirtyDiamondItem() {
        super("dirty_diamond");
        this.maxStackSize = 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(TextFormatting.GOLD + "Temporary item!");
        tooltip.add(TextFormatting.GOLD + "This will be removed");
        tooltip.add(TextFormatting.GOLD + "once a proper teleportation");
        tooltip.add(TextFormatting.GOLD + "mechanism has been made");
        tooltip.add(TextFormatting.GREEN + "Right click to teleport to");
        tooltip.add(TextFormatting.GREEN + "Ariente dimension");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
        if (!worldIn.isRemote) {
            BlockPos nearest = CityTools.getNearestTeleportationSpot(player.getPosition());
            TeleportationTools.teleportToDimension(player, WorldgenConfiguration.DIMENSION_ID, nearest.getX(), nearest.getY(), nearest.getZ());
        }
        return super.onItemRightClick(worldIn, player, handIn);
    }
}