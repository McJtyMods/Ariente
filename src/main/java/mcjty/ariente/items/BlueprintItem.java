package mcjty.ariente.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlueprintItem extends GenericItem {

    public BlueprintItem() {
        super("blueprint");
        this.maxStackSize = 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
//        tooltip.add("Security card");
//        Set<String> tags = getSecurityTags(stack);
//        if (tags.isEmpty()) {
//            tooltip.add(TextFormatting.YELLOW + "Security card is empty!");
//        } else {
//            tooltip.add(TextFormatting.YELLOW + "Tags:");
//            for (String tag : tags) {
//                tooltip.add("   " + TextFormatting.GREEN + tag);
//            }
//        }
    }


}