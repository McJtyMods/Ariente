package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.gui.ModGuis;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.hologui.api.Icons.*;

public class EnergyHolderItem extends Item {

    public static final int MODE_MANUAL = 0;
    public static final int MODE_AUTOMATIC = 1;

    public EnergyHolderItem() {
        super(new Properties().group(Ariente.setup.getTab()).maxStackSize(1));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.isEmpty() != newStack.isEmpty()) {
            return true;
        }
        return oldStack.getItem() != newStack.getItem();
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent("The Energy Holder can store"));
        tooltip.add(new StringTextComponent("negarite and posirite dust"));
        tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "Negarite: " + TextFormatting.GRAY + count(stack, "negarite")));
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "Posirite: " + TextFormatting.GRAY + count(stack, "posirite")));
        tooltip.add(new StringTextComponent(TextFormatting.GOLD + "Mode: " + TextFormatting.GRAY + (getAutomatic(stack) == MODE_MANUAL ? "Manual" : "Automatic")));
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entity, int itemSlot, boolean isSelected) {
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        CompoundNBT tag = stack.getOrCreateTag();
        if (getAutomatic(stack) == MODE_MANUAL) {
            return;
        }

        int index = tag.getInt("index");
        PlayerEntity player = (PlayerEntity) entity;
        if (index >= player.inventory.getSizeInventory()) {
            index = 0;
        }
        tag.putInt("index", index+1);
        ItemStack playerStack = player.inventory.getStackInSlot(index);
        if (playerStack.getItem() == ModBlocks.DUST_NEGARITE.get()) {
            tag.putInt("negarite", tag.getInt("negarite") + playerStack.getCount());
            player.inventory.setInventorySlotContents(index, ItemStack.EMPTY);
        } else if (playerStack.getItem() == ModBlocks.DUST_POSIRITE.get()) {
            tag.putInt("posirite", tag.getInt("posirite") + playerStack.getCount());
            player.inventory.setInventorySlotContents(index, ItemStack.EMPTY);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand handIn) {
        if (!worldIn.isRemote) {
            Ariente.guiHandler.openHoloGui(player, ModGuis.GUI_ENERGY_HOLDER, 1f);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(handIn));
    }

    public static IGuiComponent<?> createGui(PlayerEntity pl) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchGui(ModGuis.GUI_ENERGY_HOLDER_HOLD))
                .add(registry.text(0, 1, 1, 1).text("Energy Holder").color(registry.color(StyledColor.LABEL)));

        addDustControl(registry, panel, 2.3, ModBlocks.DUST_NEGARITE.get(), "negarite");
        addDustControl(registry, panel, 4.7, ModBlocks.DUST_POSIRITE.get(), "posirite");

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

                .add(registry.stackIcon(5, yy, 1, 1).itemStack(new ItemStack(ModBlocks.ENERGY_HOLDER.get())))
                .add(registry.number(6, yy, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((player, holo) -> count(player, negarite)));
    }

    private static int getAutomatic(PlayerEntity player) {
        CompoundNBT tag = getCompound(player);
        if (tag == null) {
            return MODE_MANUAL;
        }
        return tag.getInt("mode");
    }

    private static int getAutomatic(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            return MODE_MANUAL;
        }
        return tag.getInt("mode");
    }

    private static void changeAutomatic(PlayerEntity player) {
        CompoundNBT tag = getCompound(player);
        if (tag == null) {
            return;
        }
        int mode = tag.getInt("mode");
        mode = mode == 0 ? 1 : 0;
        tag.putInt("mode", mode);
    }

    private static void toPlayer(PlayerEntity player, int amount, String tagname, Item item) {
        CompoundNBT tag = getCompound(player);
        if (tag == null) {
            return;
        }
        int total = tag.getInt(tagname);
        int actuallyExtracted = Math.min(total, amount);
        if (actuallyExtracted <= 0) {
            return;
        }
        total -= actuallyExtracted;

        if (player.inventory.addItemStackToInventory(new ItemStack(item, actuallyExtracted))) {
            tag.putInt(tagname, total);
        }
    }

    private static void toItem(PlayerEntity player, int amount, String tagname, Item item) {
        CompoundNBT tag = getCompound(player);
        if (tag == null) {
            return;
        }
        int total = tag.getInt(tagname);
        ItemStack toTransfer = ItemStack.EMPTY;

        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
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

    private static CompoundNBT getCompound(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.getItem() != ModBlocks.ENERGY_HOLDER.get()) {
            return null;
        }
        return heldItem.getOrCreateTag();
    }

    private static int count(PlayerEntity player, String tagname) {
        CompoundNBT tag = getCompound(player);
        if (tag == null) {
            return 0;
        }
        return tag.getInt(tagname);
    }

    public static int count(ItemStack stack, String tagname) {
        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        return tag.getInt(tagname);
    }

    public static int extractIfPossible(ItemStack stack, String tagname, int amount) {
        CompoundNBT tag = stack.getTag();
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

    public static IGuiComponent<?> createHelpGui(PlayerEntity player) {
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