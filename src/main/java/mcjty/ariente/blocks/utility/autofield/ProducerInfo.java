package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.PartPos;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.*;

public class ProducerInfo {

    private Map<PartPos, Producer> producers = new HashMap<>();
    public static final int[] EMPTY_OREIDS = new int[0];

    public ProducerInfo(World world, Set<PartPos> itemNodes) {
        for (PartPos pair : itemNodes) {
            TileEntity te = MultipartHelper.getTileEntity(world, pair.getPos(), pair.getSlot());
            if (te instanceof OutputItemNodeTile) {
                OutputItemNodeTile itemNode = (OutputItemNodeTile) te;

                boolean outputDamage = itemNode.isOutputDamage();
                boolean outputNbt = itemNode.isOutputNbt();
                boolean outputOredict = itemNode.isOutputOredict();
                for (ItemStack stack : itemNode.getOutputFilter()) {
                    if (!stack.isEmpty()) {
                        producers.putIfAbsent(pair, new Producer(outputOredict, outputDamage, outputNbt, itemNode.getOutputStackSize()));
                        int[] oreIDs;
                        if (outputOredict) {
                            oreIDs = OreDictionary.getOreIDs(stack);
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
        private final int minStackSize;
        private final List<ProvidedItem> providedItems = new ArrayList<>();
        private final Set<Item> isProvidedItem = new HashSet<>();
        private final Set<Integer> isProvidedOre = new HashSet<>();

        public Producer(boolean matchOredict, boolean matchDamage, boolean matchNbt, int minStackSize) {
            this.matchOredict = matchOredict;
            this.matchDamage = matchDamage;
            this.matchNbt = matchNbt;
            this.minStackSize = minStackSize;
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

        public List<ProvidedItem> getProvidedItems() {
            return providedItems;
        }

        public boolean provides(ItemStack stack) {
            if (isProvidedItem.contains(stack.getItem())) {
                return true;
            }
            if (matchOredict) {
                int[] oreIDs = OreDictionary.getOreIDs(stack);
                return Arrays.stream(oreIDs).anyMatch(isProvidedOre::contains);
            }
            return false;
        }

        @Nullable
        public ProvidedItem getProvidedItem(ItemStack stack) {
            int[] oreIDs = EMPTY_OREIDS;
            if (matchOredict) {
                oreIDs = OreDictionary.getOreIDs(stack);
            }
            if (isProvidedItem.contains(stack.getItem()) || Arrays.stream(oreIDs).anyMatch(isProvidedOre::contains)) {
                for (ProvidedItem item : providedItems) {
                    if (matchOredict) {
                        if (!OreDictionary.itemMatches(stack, item.getStack(), true)) {
                            continue;
                        }
                    } else {
                        if (!stack.isItemEqualIgnoreDurability(item.getStack())) {
                            continue;
                        }
                    }
                    if (matchDamage) {
                        if (!stack.isItemEqual(item.getStack())) {
                            continue;
                        }
                    }
                    if (matchNbt) {
                        if (!ItemStack.areItemStacksEqual(stack, item.getStack())) {
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
