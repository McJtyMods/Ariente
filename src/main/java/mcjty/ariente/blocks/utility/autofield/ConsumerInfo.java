package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.multipart.MultipartHelper;
import mcjty.lib.multipart.PartPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ConsumerInfo {

    private final List<WantedItem> wantedItems = new ArrayList<>();

    public ConsumerInfo(Level world, Set<PartPos> itemNodes) {
        itemNodes.stream().sorted(ProducerInfo.NODE_ORDER).forEach(pos -> {
            if (MultipartHelper.getBlockEntity(world, pos) instanceof InputItemNodeTile node) {
                for (ItemStack stack : node.getInputFilter()) {
                    if (!stack.isEmpty()) {
                        wantedItems.add(new WantedItem(pos, stack.copy(), node.isInputOredict(), node.isInputDamage(), node.isInputNbt()));
                    }
                }
            }
        });
    }

    public Stream<PartPos> getWantedStream(ItemStack stack) {
        return wantedItems.stream().filter(item -> ItemFilterMatcher.matches(item.stack(), stack,
                        item.tags(), item.damage(), item.nbt()))
                .map(WantedItem::pos).distinct();
    }

    private record WantedItem(PartPos pos, ItemStack stack, boolean tags, boolean damage, boolean nbt) {
    }
}
