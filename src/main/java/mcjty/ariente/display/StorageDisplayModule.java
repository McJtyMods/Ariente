package mcjty.ariente.display;

import net.neoforged.neoforge.transfer.item.ItemUtil;
import mcjty.lib.varia.CapabilityTools;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import mcjty.ariente.items.ItemData;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

public class StorageDisplayModule implements DisplayModuleType {

    // Source snapshots are shared by modules, including modules on different display bases.
    private static final Map<Level, Map<Source, InventorySnapshot>> CACHE = new WeakHashMap<>();

    @Override
    public String title() {
        return "STORAGE MODULE";
    }

    public static int setting(ItemStack stack, String key, int fallback, int min, int max) {
        CompoundTag config = DisplayModuleItem.config(stack);
        return Mth.clamp(config.contains(key) ? config.getIntOr(key, 0) : fallback, min, max);
    }

    public static int count(ItemStack stack) {
        return Math.min(setting(stack, "Count", 2, 1, 64), columns(stack) * 25);
    }

    public static int columns(ItemStack stack) {
        return setting(stack, "Columns", 4, 1, 8);
    }

    public static boolean grid(ItemStack stack) {
        return DisplayModuleItem.config(stack).getBooleanOr("Grid", false);
    }

    @Override
    public Size size(ItemStack stack) {
        if (grid(stack)) {
            int cols = Math.min(columns(stack), count(stack));
            return new Size(cols * 24 + 12, 24 + ((count(stack) + cols - 1) / cols) * 24);
        }
        int count = Math.min(16, count(stack));
        return new Size(128, 20 + count * 20);
    }

    public static ResourceHandler<ItemResource> inventory(BlockEntity tile, Direction side) {
        ResourceHandler<ItemResource> handler = CapabilityTools.itemHandler(tile.getLevel().getCapability(Capabilities.Item.BLOCK, tile.getBlockPos(), side));
        if (handler == null) {
            handler = CapabilityTools.itemHandler(tile.getLevel().getCapability(Capabilities.Item.BLOCK, tile.getBlockPos(), null));
        }
        return handler != null ? handler : tile instanceof Container container ? VanillaContainerWrapper.of(container) : null;
    }

    @Override
    public boolean canLink(Level level, BlockPos pos, Direction side) {
        BlockEntity tile = level.getBlockEntity(pos);
        return tile != null && inventory(tile, side) != null;
    }

    private static InventorySnapshot scan(ModuleLink link) {
        long now = link.level().getGameTime();
        Map<Source, InventorySnapshot> cache = CACHE.computeIfAbsent(link.level(), level -> new HashMap<>());
        // Prune expired entries so a large number of historical links cannot grow the cache.
        cache.entrySet().removeIf(entry -> now - entry.getValue().tick() >= 20);
        Source key = new Source(link.pos(), link.side());
        InventorySnapshot saved = cache.get(key);
        if (saved != null) {
            return saved;
        }
        ResourceHandler<ItemResource> inventory = inventory(link.tile(), link.side());
        if (inventory == null) {
            return new InventorySnapshot(now, List.of(), "Inventory unavailable");
        }
        Map<StackKey, ItemCount> counts = new HashMap<>();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = ItemUtil.getStack(inventory, slot);
            if (!stack.isEmpty()) {
                StackKey stackKey = new StackKey(stack.getItem(), ItemData.get(stack));
                ItemCount count = counts.get(stackKey);
                counts.put(stackKey, count == null ? new ItemCount(stack.copyWithCount(1), stack.getCount())
                        : new ItemCount(count.stack(), count.count() + stack.getCount()));
            }
        }
        List<ItemCount> sorted = new ArrayList<>(counts.values());
        sorted.sort(Comparator.comparingLong(ItemCount::count).reversed()
                .thenComparing(count -> count.stack().getItem().getDescriptionId())
                .thenComparing(count -> String.valueOf(ItemData.get(count.stack()))));
        saved = new InventorySnapshot(now, sorted, "");
        cache.put(key, saved);
        return saved;
    }

    @Override
    public CompoundTag sample(Level level, ItemStack stack, CompoundTag previous) {
        CompoundTag data = new CompoundTag();
        ModuleLink link = ModuleLink.resolve(level, stack);
        if (link.tile() == null) {
            data.putString("Status", link.status());
            return data;
        }
        InventorySnapshot snapshot = scan(link);
        if (!snapshot.status().isEmpty()) {
            data.putString("Status", snapshot.status());
            return data;
        }
        int count = grid(stack) ? count(stack) : Math.min(16, count(stack));
        int pages = Math.max(1, (snapshot.items().size() + count - 1) / count);
        int page = grid(stack) ? Math.min(pages - 1, setting(stack, "Page", 1, 1, 9999) - 1) : 0;
        ListTag items = new ListTag();
        for (int index = page * count; index < Math.min(snapshot.items().size(), (page + 1) * count); index++) {
            ItemCount item = snapshot.items().get(index);
            CompoundTag entry = new CompoundTag();
            entry.put("Item", ItemData.encode(level.registryAccess(), item.stack()));
            entry.putLong("Count", item.count());
            items.add(entry);
        }
        data.put("Items", items);
        data.putInt("Types", snapshot.items().size());
        data.putInt("Page", page + 1);
        data.putInt("Pages", pages);
        return data;
    }

    @Override
    public void configure(IGuiComponentRegistry r, IPanel panel, ModuleSettings settings) {
        panel.add(r.textChoice(.3, 1, 7.2, .85).name("setting_Grid").scale(.48f)
                .addText("View: Most stored").addText("View: Item grid").getter(p -> settings.flag("Grid", false) ? 1 : 0)
                .hitEvent((c, p, e, x, y) -> settings.toggle(p, e, "Grid", false)));
        settings.addNumber(r, panel, 2.1, "Count", "Items / page", 2, 1, 64);
        settings.addNumber(r, panel, 3.2, "Columns", "Grid columns", 4, 1, 8);
        settings.addNumber(r, panel, 4.3, "Page", "Grid page", 1, 1, 9999);
        panel.add(r.text(.3, 5.6, 8, .6).text("Counts combine matching items and tags.").scale(.38f));
        panel.add(r.text(.3, 6.3, 8, .6).text("List: up to 16 most stored. Grid: all pages.").scale(.35f));
    }

    private record Source(BlockPos pos, Direction side) {
    }

    private record StackKey(Item item, CompoundTag tag) {
    }

    private record ItemCount(ItemStack stack, long count) {
    }

    private record InventorySnapshot(long tick, List<ItemCount> items, String status) {
    }
}
