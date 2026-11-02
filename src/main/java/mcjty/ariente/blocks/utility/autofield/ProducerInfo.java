package mcjty.ariente.blocks.utility.autofield;

import mcjty.ariente.multipart.MultipartHelper;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProducerInfo {

    private final Map<PartPos, Producer> producers = new LinkedHashMap<>();
    public static final Comparator<PartPos> NODE_ORDER = Comparator.comparingLong((PartPos p) -> p.pos().asLong()).thenComparingInt(p -> p.slot().ordinal());

    public ProducerInfo(Level world, Set<PartPos> itemNodes, Set<PartPos> modifierNodes) {
        itemNodes.stream().sorted(NODE_ORDER).forEach(pos -> {
            if (MultipartHelper.getBlockEntity(world, pos) instanceof OutputItemNodeTile node) {
                Producer producer = new Producer(node.isOutputTags(), node.isOutputDamage(), node.isOutputNbt(), node.isOutputBlacklist(),
                        node.getOutputStackSize(), modifierNodes.contains(pos) ? pos.slot().getBackSlot() : null);
                for (ItemStack stack : node.getOutputFilter()) {
                    if (!stack.isEmpty()) {
                        producer.addItem(new ProvidedItem(stack));
                    }
                }
                if (node.isOutputBlacklist() || !producer.getProvidedItems().isEmpty()) {
                    producers.put(pos, producer);
                }
            }
        });
    }

    public Map<PartPos, Producer> getProducers() {
        return producers;
    }

    public static class Producer {

        private final boolean matchTags;
        private final boolean matchDamage;
        private final boolean matchNbt;
        private final boolean blacklist;
        private final PartSlot roundRobinSlot;
        private final int minStackSize;
        private final List<ProvidedItem> providedItems = new ArrayList<>();

        public Producer(boolean matchTags, boolean matchDamage, boolean matchNbt, boolean blacklist, int minStackSize, @Nullable PartSlot roundRobinSlot) {
            this.matchTags = matchTags;
            this.matchDamage = matchDamage;
            this.matchNbt = matchNbt;
            this.blacklist = blacklist;
            this.minStackSize = Math.max(1, Math.min(64, minStackSize));
            this.roundRobinSlot = roundRobinSlot;
        }

        public void addItem(ProvidedItem item) {
            providedItems.add(item);
        }

        public boolean isMatchTags() {
            return matchTags;
        }

        public boolean isMatchDamage() {
            return matchDamage;
        }

        public boolean isMatchNbt() {
            return matchNbt;
        }

        public int getMinStackSize() {
            return minStackSize;
        }

        @Nullable
        public PartSlot getRoundRobinSlot() {
            return roundRobinSlot;
        }

        public List<ProvidedItem> getProvidedItems() {
            return providedItems;
        }

        public boolean provides(ItemStack stack) {
            return !stack.isEmpty() && blacklist != (getProvidedItem(stack) != null);
        }

        @Nullable
        public ProvidedItem getProvidedItem(ItemStack stack) {
            for (ProvidedItem item : providedItems) {
                if (ItemFilterMatcher.matches(item.getStack(), stack, matchTags, matchDamage, matchNbt)) {
                    return item;
                }
            }
            return null;
        }
    }

    public static class ProvidedItem {

        private final ItemStack stack;

        public ProvidedItem(ItemStack stack) {
            this.stack = stack.copy();
        }

        public ItemStack getStack() {
            return stack;
        }
    }
}
