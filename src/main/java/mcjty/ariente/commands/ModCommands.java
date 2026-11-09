package mcjty.ariente.commands;

import net.minecraft.util.Prediction;
import net.minecraft.server.permissions.Permissions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.entities.fluxship.FluxShipEnergy;
import mcjty.ariente.items.EnergyHolderItem;
import mcjty.ariente.items.ItemData;
import mcjty.ariente.setup.Registration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Ariente.MODID)
public class ModCommands {

    private static final int FUEL_PAIRS = 1_000_000;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Ariente.MODID)
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("equip").executes(context -> equip(context.getSource()))));
    }

    private static int equip(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        equip(player, EquipmentSlot.HEAD, armor(Registration.POWERSUIT_HEAD.get()));
        ItemStack chest = armor(Registration.POWERSUIT_CHEST.get(), ArmorUpgradeType.REGENERATION);
        ItemData.update(chest, data -> data.putBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey(), false));
        equip(player, EquipmentSlot.CHEST, chest);
        equip(player, EquipmentSlot.LEGS, armor(Registration.POWERSUIT_LEGS.get()));
        equip(player, EquipmentSlot.FEET, armor(Registration.POWERSUIT_FEET.get(), ArmorUpgradeType.FEATHERFALLING));
        equip(player, EquipmentSlot.MAINHAND, new ItemStack(Registration.ENHANCED_ENERGY_SABRE.get()));
        // The lance requires an empty off hand to fire.
        equip(player, EquipmentSlot.OFFHAND, ItemStack.EMPTY);

        ItemStack holder = new ItemStack(Registration.ENERGY_HOLDER.get());
        CompoundTag fuel = ItemData.get(holder);
        fuel.putInt("negarite", FUEL_PAIRS);
        ItemData.set(holder, fuel);

        fuel.putInt("posirite", FUEL_PAIRS);
        ItemData.set(holder, fuel);

        fuel.putLong("chargeCapacity", EnergyHolderItem.powerCapacity());
        ItemData.set(holder, fuel);

        fuel.putLong("charge", EnergyHolderItem.powerCapacity());
        ItemData.set(holder, fuel);

        // Put the fueled holder first so an older empty holder cannot take priority.
        int holderSlot = player.getInventory().getSelectedSlot() == 0 ? 1 : 0;
        hotbar(player, holderSlot, holder);
        ItemStack ship = new ItemStack(Registration.FLUX_SHIP.get());
        ItemData.update(ship, data -> data.putInt(FluxShipEnergy.CHARGE_TAG, FluxShipEnergy.CAPACITY));
        int slot = 0;
        for (ItemStack stack : new ItemStack[]{new ItemStack(Registration.ENERGY_LANCE.get()), ship}) {
            while (slot == player.getInventory().getSelectedSlot() || slot == holderSlot) {
                slot++;
            }
            hotbar(player, slot++, stack);
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
        source.sendSuccess(() -> Component.translatable("command.ariente.equip.success"), true);
        return 1;
    }

    private static ItemStack armor(Item item, ArmorUpgradeType... modules) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = ItemData.get(stack);
        tag.putBoolean(ArmorUpgradeType.ENERGY.getModuleKey(), true);
        ItemData.set(stack, tag);

        tag.putBoolean(ArmorUpgradeType.ARMOR.getModuleKey(), true);
        ItemData.set(stack, tag);

        for (ArmorUpgradeType type : modules) {
            tag.putBoolean(type.getModuleKey(), true);
            ItemData.set(stack, tag);

        }
        return stack;
    }

    private static void equip(ServerPlayer player, EquipmentSlot slot, ItemStack stack) {
        ItemStack previous = player.getItemBySlot(slot);
        player.setItemSlot(slot, stack);
        player.getInventory().placeItemBackInInventory(previous, Prediction.SERVER_ONLY);
    }

    private static void hotbar(ServerPlayer player, int slot, ItemStack stack) {
        ItemStack previous = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, stack);
        player.getInventory().placeItemBackInInventory(previous, Prediction.SERVER_ONLY);
    }
}
