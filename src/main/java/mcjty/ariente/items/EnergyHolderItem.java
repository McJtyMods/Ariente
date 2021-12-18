package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.gui.ModGuis;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
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
                    parameter("mode", stack -> (getAutomatic(stack) == MODE_MANUAL ? "Manual" : "Automatic")));

    public EnergyHolderItem() {
        super(new Properties().tab(Ariente.setup.getTab()).stacksTo(1));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.isEmpty() != newStack.isEmpty()) {
            return true;
        }
        return oldStack.getItem() != newStack.getItem();
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flag);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entity, int itemSlot, boolean isSelected) {
        if (!(entity instanceof Player)) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (getAutomatic(stack) == MODE_MANUAL) {
            return;
        }

        int index = tag.getInt("index");
        Player player = (Player) entity;
        if (index >= player.getInventory().getContainerSize()) {
            index = 0;
        }
        tag.putInt("index", index+1);
        ItemStack playerStack = player.getInventory().getItem(index);
        if (playerStack.getItem() == Registration.DUST_NEGARITE.get()) {
            tag.putInt("negarite", tag.getInt("negarite") + playerStack.getCount());
            player.getInventory().setItem(index, ItemStack.EMPTY);
        } else if (playerStack.getItem() == Registration.DUST_POSIRITE.get()) {
            tag.putInt("posirite", tag.getInt("posirite") + playerStack.getCount());
            player.getInventory().setItem(index, ItemStack.EMPTY);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand handIn) {
        if (!worldIn.isClientSide) {
            Ariente.guiHandler.openHoloGui(player, ModGuis.GUI_ENERGY_HOLDER, 1f);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(handIn));
    }

    public static IGuiComponent<?> createGui(Player pl) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchGui(ModGuis.GUI_ENERGY_HOLDER_HOLD))
                .add(registry.text(0, 1, 1, 1).text("Energy Holder").color(registry.color(StyledColor.LABEL)));

        addDustControl(registry, panel, 2.3, Registration.DUST_NEGARITE.get(), "negarite");
        addDustControl(registry, panel, 4.7, Registration.DUST_POSIRITE.get(), "posirite");

        panel.add(registry.textChoice(0, 7, 2, 1).addText("Manual").addText("Automatic")
                .getter(EnergyHolderItem::getAutomatic)
                .hitEvent((component, player, entity, x, y) -> changeAutomatic(player)));

        return panel;
    }

    private static void addDustControl(IGuiComponentRegistry registry, IPanel panel, double yy, Item negariteDust, String negarite) {
        panel
                .add(registry.stackIcon(0, yy, 1, 1).itemStack(new ItemStack(negariteDust)))

                .add(registry.icon(1, yy, 1, 1).icon(registry.image(WHITE_PLAYER)))
                .add(registry.number(2, yy, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((p, h) -> HoloGuiTools.countItem(p, negariteDust)))

                .add(registry.iconButton(2, yy + 1, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_LEFT)).hover(registry.image(WHITE_DOUBLE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> toPlayer(player, 64, negarite, negariteDust)))
                .add(registry.iconButton(3, yy + 1, 1, 1).icon(registry.image(GRAY_ARROW_LEFT)).hover(registry.image(WHITE_ARROW_LEFT))
                        .hitEvent((component, player, e, x, y) -> toPlayer(player, 1, negarite, negariteDust)))
                .add(registry.iconButton(5, yy + 1, 1, 1).icon(registry.image(GRAY_ARROW_RIGHT)).hover(registry.image(WHITE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> toItem(player, 1, negarite, negariteDust)))
                .add(registry.iconButton(6, yy + 1, 1, 1).icon(registry.image(GRAY_DOUBLE_ARROW_RIGHT)).hover(registry.image(WHITE_DOUBLE_ARROW_RIGHT))
                        .hitEvent((component, player, e, x, y) -> toItem(player, 64, negarite, negariteDust)))

                .add(registry.stackIcon(5, yy, 1, 1).itemStack(new ItemStack(Registration.ENERGY_HOLDER.get())))
                .add(registry.number(6, yy, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((player, holo) -> count(player, negarite)));
    }

    private static int getAutomatic(Player player) {
        CompoundTag tag = getCompound(player);
        if (tag == null) {
            return MODE_MANUAL;
        }
        return tag.getInt("mode");
    }

    private static int getAutomatic(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return MODE_MANUAL;
        }
        return tag.getInt("mode");
    }

    private static void changeAutomatic(Player player) {
        CompoundTag tag = getCompound(player);
        if (tag == null) {
            return;
        }
        int mode = tag.getInt("mode");
        mode = mode == 0 ? 1 : 0;
        tag.putInt("mode", mode);
    }

    private static void toPlayer(Player player, int amount, String tagname, Item item) {
        CompoundTag tag = getCompound(player);
        if (tag == null) {
            return;
        }
        int total = tag.getInt(tagname);
        int actuallyExtracted = Math.min(total, amount);
        if (actuallyExtracted <= 0) {
            return;
        }
        total -= actuallyExtracted;

        if (player.getInventory().add(new ItemStack(item, actuallyExtracted))) {
            tag.putInt(tagname, total);
        }
    }

    private static void toItem(Player player, int amount, String tagname, Item item) {
        CompoundTag tag = getCompound(player);
        if (tag == null) {
            return;
        }
        int total = tag.getInt(tagname);
        ItemStack toTransfer = ItemStack.EMPTY;

        for (int i = 0 ; i < player.getInventory().getContainerSize() ; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == item) {
                ItemStack splitted = stack.split(amount);
                if ((!splitted.isEmpty())) {
                    if (toTransfer.isEmpty()) {
                        toTransfer = splitted;
                    } else {
                        toTransfer.grow(amount);
                    }
                    amount -= splitted.getCount();
                    if (amount <= 0) {
                        break;
                    }
                }
            }
        }

        if (!toTransfer.isEmpty()) {
            if (total != 0) {
                toTransfer.grow(total);
            }
            tag.putInt(tagname, toTransfer.getCount());
        }
    }

    private static CompoundTag getCompound(Player player) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.getItem() != Registration.ENERGY_HOLDER.get()) {
            return null;
        }
        return heldItem.getOrCreateTag();
    }

    private static int count(Player player, String tagname) {
        CompoundTag tag = getCompound(player);
        if (tag == null) {
            return 0;
        }
        return tag.getInt(tagname);
    }

    public static int count(ItemStack stack, String tagname) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        return tag.getInt(tagname);
    }

    public static int extractIfPossible(ItemStack stack, String tagname, int amount) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        int count = tag.getInt(tagname);
        if (count <= 0) {
            return 0;
        }
        int amountToExtract = Math.min(amount, count);
        count -= amountToExtract;
        tag.putInt(tagname, count);
        return amountToExtract;
    }

    public static IGuiComponent<?> createHelpGui(Player player) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        return HoloGuiTools.createHelpGui(registry, HelpBuilder.create()
                        .line("The Energy Holder can store")
                        .line("negarite and posirite dust.")
                        .line("The Power Armor can automatically")
                        .line("extract from this item."),
                entity -> entity.switchGui(ModGuis.GUI_ENERGY_HOLDER)
        );
    }



}