package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.gui.GuiHelp;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.setup.Registration;
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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.hologui.api.Icons.*;
import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class EnergyHolderItem extends Item implements ITooltipSettings {

    public static final int MODE_MANUAL = 0;
    public static final int MODE_AUTOMATIC = 1;

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    parameter("negarite", stack -> Integer.toString(count(stack, "negarite"))),
                    parameter("posirite", stack -> Integer.toString(count(stack, "posirite"))),
                    parameter("charge", stack -> getChargePercentage(stack) + "%"),
                    parameter("mode", stack -> getAutomatic(stack) == MODE_MANUAL ? "Manual" : "Automatic"));

    public EnergyHolderItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }

    /** A held holder takes priority; otherwise the first holder in the inventory supplies the pool. */
    public static ItemStack findActiveHolder(Player player) {
        if (player.getMainHandItem().is(Registration.ENERGY_HOLDER.get())) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().is(Registration.ENERGY_HOLDER.get())) {
            return player.getOffhandItem();
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Registration.ENERGY_HOLDER.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean consumePower(Player player, int usage, int ticksPerPair) {
        ItemStack holder = findActiveHolder(player);
        if (player.level().isClientSide || holder.isEmpty()) {
            return false;
        }
        // Both efficiency rates divide this capacity exactly, so mixed equipment shares every last tick.
        long capacity = powerCapacity();
        long cost = Math.min(capacity, Math.max(1L, usage * (capacity / ticksPerPair)));
        CompoundTag tag = holder.getOrCreateTag();
        long storedCapacity = tag.getLong("chargeCapacity");
        long charge = Math.max(0, Math.min(tag.getLong("charge"), storedCapacity));
        if (storedCapacity > 0 && storedCapacity != capacity) {
            charge = Math.round((double) charge / storedCapacity * capacity);
        }
        if (charge < cost) {
            // A partial pair never consumes the available component or the remaining charge.
            if (count(holder, "negarite") <= 0 || count(holder, "posirite") <= 0) {
                return false;
            }
            tag.putInt("negarite", count(holder, "negarite") - 1);
            tag.putInt("posirite", count(holder, "posirite") - 1);
            charge += capacity;
        }
        tag.putLong("charge", charge - cost);
        tag.putLong("chargeCapacity", capacity);
        player.getInventory().setChanged();
        return true;
    }

    private static long powerCapacity() {
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
        CompoundTag tag = holder.getTag();
        if (tag == null || tag.getLong("chargeCapacity") <= 0) {
            return 0;
        }
        long capacity = tag.getLong("chargeCapacity");
        long charge = Math.max(0, Math.min(capacity, tag.getLong("charge")));
        return (int) ((charge * 100 + capacity - 1) / capacity);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)
                || getAutomatic(stack) != MODE_AUTOMATIC || findActiveHolder(player) != stack) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int index = Math.floorMod(tag.getInt("index"), player.getInventory().getContainerSize());
        tag.putInt("index", index + 1);
        ItemStack fuel = player.getInventory().getItem(index);
        if (fuel.is(Registration.DUST_NEGARITE.get()) || fuel.is(Registration.DUST_POSIRITE.get())) {
            addFuel(player, fuel.getItem(), fuel.getCount());
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            Ariente.guiHandler.openHoloGui(player, ModGuis.GUI_ENERGY_HOLDER, 1f);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    public static IGuiComponent<?> createGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        IPanel panel = GuiHelp.ENERGY_HOLDER.createPanel(registry, entity -> entity.switchGui(ModGuis.GUI_ENERGY_HOLDER_HOLD))
                .add(registry.text(0, .3, 8, 1).text("Shared equipment fuel").scale(.7f).color(registry.color(StyledColor.LABEL)))
                .add(registry.text(0, 1.1, 8, 1).text(() -> "Current pair: " + getChargePercentage(findActiveHolder(player)) + "%")
                        .scale(.55f).color(registry.color(StyledColor.INFORMATION)));

        addDustControl(registry, panel, 2.3, Registration.DUST_NEGARITE.get(), "negarite");
        addDustControl(registry, panel, 4.7, Registration.DUST_POSIRITE.get(), "posirite");
        panel.add(registry.textChoice(0, 7, 7.2, .9).scale(.55f).addText("Manual").addText("Automatic")
                .getter(p -> getAutomatic(findActiveHolder(p)))
                .hitEvent((component, p, entity, x, y) -> changeAutomatic(p)));
        return panel;
    }

    private static void addDustControl(IGuiComponentRegistry registry, IPanel panel, double y, Item dust, String key) {
        panel.add(registry.stackIcon(0, y, 1, 1).itemStack(new ItemStack(dust)))
                .add(registry.icon(1, y, 1, 1).icon(registry.image(WHITE_PLAYER)))
                .add(registry.number(2, y, 1, 1).color(registry.color(StyledColor.INFORMATION))
                        .getter((p, h) -> HoloGuiTools.countItem(p, dust)))
                .add(registry.iconButton(2, y + 1, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((c, p, e, x, yy) -> removeFuel(p, dust, 64)))
                .add(registry.iconButton(3, y + 1, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((c, p, e, x, yy) -> removeFuel(p, dust, 1)))
                .add(registry.iconButton(5, y + 1, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((c, p, e, x, yy) -> addFuel(p, dust, 1)))
                .add(registry.iconButton(6, y + 1, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((c, p, e, x, yy) -> addFuel(p, dust, 64)))
                .add(registry.stackIcon(5, y, 1, 1).itemStack(new ItemStack(Registration.ENERGY_HOLDER.get())))
                .add(registry.number(6, y, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> count(p, key)));
    }

    private static int getAutomatic(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt("mode") : MODE_MANUAL;
    }

    private static void changeAutomatic(Player player) {
        ItemStack holder = findActiveHolder(player);
        if (!holder.isEmpty()) {
            holder.getOrCreateTag().putInt("mode", getAutomatic(holder) == MODE_MANUAL ? MODE_AUTOMATIC : MODE_MANUAL);
            syncInventory(player);
        }
    }

    public static void removeFuel(Player player, Item dust, int amount) {
        ItemStack holder = findActiveHolder(player);
        if (player.level().isClientSide || holder.isEmpty() || amount <= 0) {
            return;
        }
        String key = fuelKey(dust);
        int stored = count(holder, key);
        int requested = Math.min(stored, Math.min(amount, dust.getMaxStackSize()));
        ItemStack extracted = new ItemStack(dust, requested);
        player.getInventory().add(extracted);
        holder.getOrCreateTag().putInt(key, stored - (requested - extracted.getCount()));
        syncInventory(player);
    }

    public static void addFuel(Player player, Item dust, int amount) {
        ItemStack holder = findActiveHolder(player);
        if (player.level().isClientSide || holder.isEmpty() || amount <= 0) {
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
        holder.getOrCreateTag().putInt(key, stored);
        syncInventory(player);
    }

    private static String fuelKey(Item dust) {
        if (dust == Registration.DUST_NEGARITE.get()) {
            return "negarite";
        }
        if (dust == Registration.DUST_POSIRITE.get()) {
            return "posirite";
        }
        throw new IllegalArgumentException("Not an equipment fuel: " + dust);
    }

    public static int count(Player player, String key) {
        return count(findActiveHolder(player), key);
    }

    public static int count(ItemStack stack, String key) {
        return stack.hasTag() ? Math.max(0, stack.getTag().getInt(key)) : 0;
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
