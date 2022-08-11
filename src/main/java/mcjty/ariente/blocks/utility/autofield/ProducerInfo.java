package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.*;

public class ProducerInfo {

    private Map<PartPos, Producer> producers = new HashMap<>();
    public static final int[] EMPTY_OREIDS = new int[0];

    public ProducerInfo(Level world, Set<PartPos> itemNodes, Set<PartPos> modifierNodes) {
        for (PartPos pair : itemNodes) {
            BlockEntity te = MultipartHelper.getTileEntity(world, pair.pos(), pair.slot());
            if (te instanceof OutputItemNodeTile) {
                OutputItemNodeTile outputNode = (OutputItemNodeTile) te;

                boolean outputDamage = outputNode.isOutputDamage();
                boolean outputNbt = outputNode.isOutputNbt();
                boolean outputOredict = outputNode.isOutputOredict();
                for (ItemStack stack : outputNode.getOutputFilter()) {
                    if (!stack.isEmpty()) {
                        // @todo hardcoded round robin modifier
                        boolean roundRobin = modifierNodes.contains(pair);
                        producers.putIfAbsent(pair, new Producer(outputOredict, outputDamage, outputNbt,
                                outputNode.getOutputStackSize(), roundRobin ? pair.slot().getBackSlot() : null));
                        int[] oreIDs;
                        if (outputOredict) {
// @todo 1.14 oredict
                            //                            oreIDs = OreDictionary.getOreIDs(stack);
                            oreIDs = EMPTY_OREIDS;
                        } else {
                            oreIDs = EMPTY_OREIDS;
                        }
                        producers.get(pair).addItem(new ProvidedItem(stack, oreIDs));
                    }
                }
            }
        }
    }

    public Map<PartPos, Producer> getProducers() {
        return producers;
    }

    public static class Producer {
        private final boolean matchOredict;
        private final boolean matchDamage;
        private final boolean matchNbt;
        private final PartSlot roundRobinSlot;
        private final int minStackSize;
        private final List<ProvidedItem> providedItems = new ArrayList<>();
        private final Set<Item> isProvidedItem = new HashSet<>();
        private final Set<Integer> isProvidedOre = new HashSet<>();

        public Producer(boolean matchOredict, boolean matchDamage, boolean matchNbt, int minStackSize, @Nullable PartSlot roundRobinSlot) {
            this.matchOredict = matchOredict;
            this.matchDamage = matchDamage;
            this.matchNbt = matchNbt;
            this.minStackSize = minStackSize;
            this.roundRobinSlot = roundRobinSlot;
        }

        public void addItem(ProvidedItem item) {
            this.providedItems.add(item);
            isProvidedItem.add(item.getStack().getItem());
            if (matchOredict) {
                for (int oreID : item.getOreIDs()) {
                    isProvidedOre.add(oreID);
                }
            }
        }

        public boolean isMatchOredict() {
            return matchOredict;
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
            if (isProvidedItem.contains(stack.getItem())) {
                return true;
            }
            if (matchOredict) {
// @todo 1.14 oredict
                //                int[] oreIDs = OreDictionary.getOreIDs(stack);
//                return Arrays.stream(oreIDs).anyMatch(isProvidedOre::contains);
            }
            return false;
        }

        @Nullable
        public ProvidedItem getProvidedItem(ItemStack stack) {
            int[] oreIDs = EMPTY_OREIDS;
            if (matchOredict) {
// @todo 1.14 oredict
                //                oreIDs = OreDictionary.getOreIDs(stack);
            }
            if (isProvidedItem.contains(stack.getItem()) || Arrays.stream(oreIDs).anyMatch(isProvidedOre::contains)) {
                for (ProvidedItem item : providedItems) {
                    if (matchOredict) {
// @todo 1.14 oredict
                        //                        if (!OreDictionary.itemMatches(stack, item.getStack(), false)) {
//                            continue;
//                        }
                    } else {
                        if (!stack.sameItemStackIgnoreDurability(item.getStack())) {
                            continue;
                        }
                    }
                    if (matchDamage) {
                        if (!stack.sameItem(item.getStack())) {
                            continue;
                        }
                    }
                    if (matchNbt) {
                        if (!ItemStack.matches(stack, item.getStack())) {
                            continue;
                        }
                    }
                    return item;
                }
            }
            return null;
        }
    }

    public static class ProvidedItem {
        private final ItemStack stack;
        private final int[] oreIDs;

        public ProvidedItem(ItemStack stack, int[] oreIDs) {
            this.stack = stack;
            this.oreIDs = oreIDs;
        }

        public ItemStack getStack() {
            return stack;
        }

        public int[] getOreIDs() {
            return oreIDs;
        }
    }
}
