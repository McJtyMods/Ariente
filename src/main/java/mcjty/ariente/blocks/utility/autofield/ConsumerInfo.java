package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.PartPos;
import mcjty.lib.multipart.MultipartHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.stream.Stream;

public class ConsumerInfo {

    private Map<Item, List<WantedItem>> wantedItems = new HashMap<>();
    private Map<Integer, List<WantedItem>> wantedOredictItems = new HashMap<>();


    public ConsumerInfo(World world, Set<PartPos> itemNodes) {
        for (PartPos pair : itemNodes) {
            TileEntity te = MultipartHelper.getTileEntity(world, pair.getPos(), pair.getSlot());
            if (te instanceof ItemNodeTile) {
                ItemNodeTile itemNode = (ItemNodeTile) te;

                boolean inputDamage = itemNode.isInputDamage();
                boolean inputNbt = itemNode.isInputNbt();
                boolean inputOredict = itemNode.isInputOredict();
                for (ItemStack stack : itemNode.getInputFilter()) {
                    if (!stack.isEmpty()) {
                        if (inputOredict) {
                            int[] oreIDs = OreDictionary.getOreIDs(stack);
                            for (int oreID : oreIDs) {
                                wantedOredictItems.putIfAbsent(oreID, new ArrayList<>());
                                wantedOredictItems.get(oreID).add(new WantedItem(pair, inputDamage, inputNbt));
                            }
                        } else {
                            wantedItems.putIfAbsent(stack.getItem(), new ArrayList<>());
                            wantedItems.get(stack.getItem()).add(new WantedItem(pair, inputDamage, inputNbt));
                        }
                    }
                }
            }
        }
    }

    public Stream<PartPos> getWantedStream(ItemStack stack) {
        int[] oreIDs = OreDictionary.getOreIDs(stack);
        Stream<PartPos> oreDictStream = Arrays.stream(oreIDs)
                .boxed()
                .flatMap(oreID -> wantedOredictItems.getOrDefault(oreID, Collections.emptyList())
                        .stream()
                        .map(WantedItem::getPos));
        List<WantedItem> wantedItems = this.wantedItems.get(stack.getItem());
        if (wantedItems == null || wantedItems.isEmpty()) {
            return oreDictStream;
        } else {
            Stream<PartPos> itemStream = wantedItems
                    .stream()
                    .map(WantedItem::getPos);
            return Stream.concat(oreDictStream, itemStream);
        }
    }


    public static class WantedItem {
        private final PartPos pos;
        private final boolean matchDamage;
        private final boolean matchNbt;

        public WantedItem(PartPos pos, boolean matchDamage, boolean matchNbt) {
            this.pos = pos;
            this.matchDamage = matchDamage;
            this.matchNbt = matchNbt;
        }

        public PartPos getPos() {
            return pos;
        }

        public boolean isMatchDamage() {
            return matchDamage;
        }

        public boolean isMatchNbt() {
            return matchNbt;
        }
    }
}
