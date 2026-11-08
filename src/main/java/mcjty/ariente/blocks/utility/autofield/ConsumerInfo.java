package mcjty.ariente.blocks.utility.autofield;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import mcjty.ariente.multipart.MultipartHelper;
import mcjty.lib.multipart.PartPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ConsumerInfo {

    private final List<WantedItem> wantedItems = new ArrayList<>();
    private final Level world;

    public ConsumerInfo(Level world, Set<PartPos> itemNodes) {
        this.world = world;
        itemNodes.stream().sorted(ProducerInfo.NODE_ORDER).forEach(pos -> {
            if (MultipartHelper.getBlockEntity(world, pos) instanceof InputItemNodeTile node) {
                List<ItemStack> samples = node.getInputFilter().stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
                wantedItems.add(new WantedItem(pos, samples, node.isInputTags(), node.isInputDamage(), node.isInputNbt(), node.isInputBlacklist(), node.getInputCategory()));
            }
        });
    }

    public Stream<PartPos> getWantedStream(ItemStack stack) {
        if (stack.isEmpty()) {
            return Stream.empty();
        }
        return wantedItems.stream().filter(item -> item.category().matches(world, stack))
                .filter(item -> item.blacklist() != item.samples().stream().anyMatch(sample ->
                        ItemFilterMatcher.matches(sample, stack, item.tags(), item.damage(), item.nbt())))
                .map(WantedItem::pos);
    }

    private record WantedItem(PartPos pos, List<ItemStack> samples, boolean tags, boolean damage, boolean nbt, boolean blacklist, ItemFilterCategory category) {
    }
}
