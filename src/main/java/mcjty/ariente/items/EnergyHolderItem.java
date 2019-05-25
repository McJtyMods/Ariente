package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.ariente.gui.HelpBuilder;
import mcjty.ariente.gui.HoloGuiTools;
import mcjty.ariente.gui.ModGuis;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.StyledColor;
import mcjty.hologui.api.components.IPanel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static mcjty.hologui.api.Icons.*;

public class EnergyHolderItem extends GenericItem {

    public EnergyHolderItem() {
        super("energy_holder");
        this.maxStackSize = 1;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("The Energy Holder can");
        tooltip.add("store negarite and posirite");
        tooltip.add("dust");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand handIn) {
        if (!worldIn.isRemote) {
            Ariente.guiHandler.openHoloGui(player, ModGuis.GUI_ENERGY_HOLDER, 1f);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
    }

    public static IGuiComponent<?> createGui(EntityPlayer pl) {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        IPanel panel = HoloGuiTools.createPanelWithHelp(registry, entity -> entity.switchGui(ModGuis.GUI_ENERGY_HOLDER_HOLD))
                .add(registry.text(0, 1, 1, 1).text("Energy Holder").color(registry.color(StyledColor.LABEL)));

        addDustControl(registry, panel, 2.3, ModItems.negariteDust, "negarite");
        addDustControl(registry, panel, 4.7, ModItems.posiriteDust, "posirite");

        panel.add(registry.textChoice(0, 7, 2, 1).addText("Manual").addText("Automatic").getter(player -> 0));

        return panel;
    }

    private static void addDustControl(IGuiComponentRegistry registry, IPanel panel, double yy, GenericItem negariteDust, String negarite) {
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

                .add(registry.stackIcon(5, yy, 1, 1).itemStack(new ItemStack(ModItems.energyHolderItem)))
                .add(registry.number(6, yy, 1, 1).color(registry.color(StyledColor.INFORMATION)).getter((player, holo) -> countNegarite(player, negarite)));
    }

    private static void toPlayer(EntityPlayer player, int amount, String tagname, Item item) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.getItem() != ModItems.energyHolderItem) {
            return;
        }
        NBTTagCompound tag = heldItem.getTagCompound();
        if (tag == null) {
            return;
        }
        int total = tag.getInteger(tagname);
        int actuallyExtracted = Math.min(total, amount);
        if (actuallyExtracted <= 0) {
            return;
        }
        total -= actuallyExtracted;

        if (player.inventory.addItemStackToInventory(new ItemStack(item, actuallyExtracted))) {
            tag.setInteger(tagname, total);
        }
    }

    private static void toItem(EntityPlayer player, int amount, String tagname, Item item) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.getItem() != ModItems.energyHolderItem) {
            return;
        }
        NBTTagCompound tag = heldItem.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            heldItem.setTagCompound(tag);
        }
        int total = tag.getInteger(tagname);
        ItemStack toTransfer = ItemStack.EMPTY;

        for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack.getItem() == item) {
                ItemStack splitted = stack.splitStack(amount);
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
            tag.setInteger(tagname, toTransfer.getCount());
        }
    }

    private static int countNegarite(EntityPlayer player, String tagname) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.getItem() != ModItems.energyHolderItem) {
            return 0;
        }
        NBTTagCompound tag = heldItem.getTagCompound();
        if (tag == null) {
            return 0;
        }
        return tag.getInteger(tagname);
    }


    public static IGuiComponent<?> createHelpGui(EntityPlayer player) {
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