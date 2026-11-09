package mcjty.ariente.items;

import java.util.ArrayList;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import java.util.List;
import java.util.Locale;
import mcjty.ariente.Ariente;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.gui.CompactNumber;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.gui.NumberFormatting;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.Tools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class EnergyHolderItem extends Item implements ITooltipSettings {

    public static final int MODE_MANUAL = 0;
    public static final int MODE_AUTOMATIC = 1;
    public static final long POWER_PER_FUEL_PAIR = 1_000;

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    parameter("negarite", stack -> Integer.toString(count(stack, "negarite"))),
                    parameter("posirite", stack -> Integer.toString(count(stack, "posirite"))),
                    parameter("power", EnergyHolderItem::formatStoredPower),
                    parameter("mode", stack -> getAutomatic(stack) == MODE_MANUAL ? "Manual" : "Automatic"));

    public EnergyHolderItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> output, TooltipFlag flag) {
        List<Component> lines = new ArrayList<>();
        appendHoverText(stack, context, lines, flag);
        lines.forEach(output);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {

        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }

    /** A held holder takes priority; otherwise the first holder in the inventory supplies the pool. */

    public static ItemStack findActiveHolder(Player player) {
        if (player.getMainHandItem().is(mcjty.ariente.setup.Registration.ENERGY_HOLDER.get())) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().is(mcjty.ariente.setup.Registration.ENERGY_HOLDER.get())) {
            return player.getOffhandItem();
        }
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(mcjty.ariente.setup.Registration.ENERGY_HOLDER.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean consumePower(Player player, int usage, int ticksPerPair) {
        ItemStack holder = findActiveHolder(player);
        if (player.level().isClientSide() || holder.isEmpty()) {
            return false;
        }
        // Both efficiency rates divide this capacity exactly, so mixed equipment shares every last tick.
        long capacity = powerCapacity();
        long cost = Math.min(capacity, Math.max(1L, usage * (capacity / ticksPerPair)));
        CompoundTag tag = ItemData.get(holder);
        long storedCapacity = tag.getLongOr("chargeCapacity", 0);
        long charge = Math.max(0, Math.min(tag.getLongOr("charge", 0), storedCapacity));
        if (storedCapacity > 0 && storedCapacity != capacity) {
            charge = Math.round((double) charge / storedCapacity * capacity);
        }
        if (charge < cost) {
            // A partial pair never consumes the available component or the remaining charge.
            if (count(holder, "negarite") <= 0 || count(holder, "posirite") <= 0) {
                return false;
            }
            tag.putInt("negarite", count(holder, "negarite") - 1);
            ItemData.set(holder, tag);

            tag.putInt("posirite", count(holder, "posirite") - 1);
            ItemData.set(holder, tag);

            charge += capacity;
        }
        tag.putLong("charge", charge - cost);
        ItemData.set(holder, tag);

        tag.putLong("chargeCapacity", capacity);
        ItemData.set(holder, tag);

        player.getInventory().setChanged();
        return true;
    }

    public static long powerCapacity() {
        long normal = UtilityConfiguration.POWERSUIT_TICKS.get();
        long optimized = UtilityConfiguration.POWERSUIT_TICKS_OPTIMIZED.get();
        long a = normal;
        long b = optimized;
        while (b != 0) {
            long remainder = a % b;
            a = b;
            b = remainder;
        }
        return normal / a * optimized;
    }

    public static int getChargePercentage(ItemStack holder) {
        CompoundTag tag = ItemData.get(holder);
        if (tag == null || tag.getLongOr("chargeCapacity", 0) <= 0) {
            return 0;
        }
        long capacity = tag.getLongOr("chargeCapacity", 0);
        long charge = Math.max(0, Math.min(capacity, tag.getLongOr("charge", 0)));
        return (int) ((charge * 100 + capacity - 1) / capacity);
    }

    /** Counts usable fuel pairs and the remaining charge using a fixed display unit. */

    public static long getStoredPower(ItemStack holder) {
        long pairs = Math.min(count(holder, "negarite"), count(holder, "posirite"));
        long power = pairs * POWER_PER_FUEL_PAIR;
        CompoundTag tag = ItemData.get(holder);
        if (tag == null || tag.getLongOr("chargeCapacity", 0) <= 0) {
            return power;
        }
        long capacity = tag.getLongOr("chargeCapacity", 0);
        long charge = Math.max(0, Math.min(capacity, tag.getLongOr("charge", 0)));
        return power + charge * POWER_PER_FUEL_PAIR / capacity;
    }

    public static String formatStoredPower(ItemStack holder) {
        return String.format(Locale.ROOT, "%,d", getStoredPower(holder));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot itemSlot) {
        if (level.isClientSide() || !(entity instanceof Player player)
                || getAutomatic(stack) != MODE_AUTOMATIC || findActiveHolder(player) != stack) {
            return;
        }
        CompoundTag tag = ItemData.get(stack);
        int index = Math.floorMod(tag.getIntOr("index", 0), player.getInventory().getContainerSize());
        tag.putInt("index", index + 1);
        ItemData.set(stack, tag);

        ItemStack fuel = player.getInventory().getItem(index);
        if (fuel.is(mcjty.ariente.setup.Registration.DUST_NEGARITE.get()) || fuel.is(mcjty.ariente.setup.Registration.DUST_POSIRITE.get())) {
            addFuel(player, fuel.getItem(), fuel.getCount());
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            Ariente.guiHandler.openHoloGui(player, ModGuis.GUI_ENERGY_HOLDER, 1f);
        }
        return InteractionResult.SUCCESS;
    }

    public static IGuiComponent<?> createGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        IPanel panel = GuiHelp.ENERGY_HOLDER.createPanel(registry, entity -> entity.switchGui(ModGuis.GUI_ENERGY_HOLDER_HOLD))
                .add(registry.text(0, .3, 8, 1).text("Shared equipment fuel").scale(.7f).color(registry.color(StyledColor.LABEL)))
                .add(registry.text(0, 1.1, 8, 1).text(() -> "Stored power: " + NumberFormatting.compact(getStoredPower(findActiveHolder(player))))
                        .scale(.45f).color(registry.color(StyledColor.INFORMATION)));

        addDustControl(registry, panel, 2.3, mcjty.ariente.setup.Registration.DUST_NEGARITE.get(), "negarite");
        addDustControl(registry, panel, 4.7, mcjty.ariente.setup.Registration.DUST_POSIRITE.get(), "posirite");
        panel.add(registry.textChoice(0, 7, 7.2, .9).scale(.55f).addText("Manual").addText("Automatic")
                .getter(p -> getAutomatic(findActiveHolder(p)))
                .hitEvent((component, p, entity, x, y) -> changeAutomatic(p)));
        return panel;
    }

    private static void addDustControl(IGuiComponentRegistry registry, IPanel panel, double y, Item dust, String key) {
        panel.add(registry.stackIcon(0, y, 1, 1).itemStack(new ItemStack(dust)))
                .add(registry.icon(1, y, 1, 1).icon(registry.image(WHITE_PLAYER)))
                .add(new CompactNumber(2, y, 2, 1).color(registry.color(StyledColor.INFORMATION))
                        .getter((p, h) -> HoloGuiTools.countItem(p, dust)))
                .add(registry.iconButton(2, y + 1, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((c, p, e, x, yy) -> removeFuel(p, dust, 64)))
                .add(registry.iconButton(3, y + 1, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((c, p, e, x, yy) -> removeFuel(p, dust, 1)))
                .add(registry.iconButton(5, y + 1, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((c, p, e, x, yy) -> addFuel(p, dust, 1)))
                .add(registry.iconButton(6, y + 1, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((c, p, e, x, yy) -> addFuel(p, dust, 64)))
                .add(registry.stackIcon(5, y, 1, 1).itemStack(new ItemStack(mcjty.ariente.setup.Registration.ENERGY_HOLDER.get())))
                .add(new CompactNumber(6, y, 2, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> count(p, key)));
    }

    private static int getAutomatic(ItemStack stack) {
        return ItemData.has(stack) ? ItemData.get(stack).getIntOr("mode", 0) : MODE_MANUAL;
    }

    private static void changeAutomatic(Player player) {
        ItemStack holder = findActiveHolder(player);
        if (!holder.isEmpty()) {
            ItemData.update(holder, data -> data.putInt("mode", getAutomatic(holder) == MODE_MANUAL ? MODE_AUTOMATIC : MODE_MANUAL));
            syncInventory(player);
        }
    }

    public static void removeFuel(Player player, Item dust, int amount) {
        ItemStack holder = findActiveHolder(player);
        if (player.level().isClientSide() || holder.isEmpty() || amount <= 0) {
            return;
        }
        String key = fuelKey(dust);
        int stored = count(holder, key);
        int requested = Math.min(stored, Math.min(amount, new ItemStack(dust).getMaxStackSize()));
        ItemStack extracted = new ItemStack(dust, requested);
        player.getInventory().add(extracted);
        ItemData.update(holder, data -> data.putInt(key, stored - (requested - extracted.getCount())));
        syncInventory(player);
    }

    public static void addFuel(Player player, Item dust, int amount) {
        ItemStack holder = findActiveHolder(player);
        if (player.level().isClientSide() || holder.isEmpty() || amount <= 0) {
            return;
        }
        String key = fuelKey(dust);
        int stored = count(holder, key);
        int remaining = Math.min(amount, Integer.MAX_VALUE - stored);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(dust)) {
                int moved = Math.min(remaining, stack.getCount());
                stack.shrink(moved);
                stored += moved;
                remaining -= moved;
                if (remaining == 0) {
                    break;
                }
            }
        }
        int storedFuel = stored;
        ItemData.update(holder, data -> data.putInt(key, storedFuel));
        syncInventory(player);
    }

    private static String fuelKey(Item dust) {
        if (dust == mcjty.ariente.setup.Registration.DUST_NEGARITE.get()) {
            return "negarite";
        }
        if (dust == mcjty.ariente.setup.Registration.DUST_POSIRITE.get()) {
            return "posirite";
        }
        throw new IllegalArgumentException("Not an equipment fuel: " + dust);
    }

    public static int count(Player player, String key) {
        return count(findActiveHolder(player), key);
    }

    public static int count(ItemStack stack, String key) {
        return ItemData.has(stack) ? Math.max(0, ItemData.get(stack).getIntOr(key, 0)) : 0;
    }

    private static void syncInventory(Player player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    public static IGuiComponent<?> createHelpGui(Player player) {
        return GuiHelp.ENERGY_HOLDER.createHelpGui(Ariente.guiHandler.getComponentRegistry(),
                entity -> entity.switchGui(ModGuis.GUI_ENERGY_HOLDER));
    }
}
