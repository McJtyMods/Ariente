package mcjty.ariente;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.items.EnergyHolderItem;
import mcjty.ariente.items.armor.ArmorGui;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.function.Function;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class SharedEquipmentPowerTests {

    @GameTest(template = "empty")
    public static void suitAndWeaponShareOnePairWithoutSeparateReserves(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack holder = holder(1, 1);
        player.getInventory().setItem(9, holder);
        ItemStack head = gear(Registration.POWERSUIT_HEAD.get(), ArmorUpgradeType.NIGHTVISION);
        ItemStack feet = gear(Registration.POWERSUIT_FEET.get(), ArmorUpgradeType.STEPASSIST);
        ItemStack sabre = gear(Registration.ENHANCED_ENERGY_SABRE.get(), ArmorUpgradeType.POWER);
        player.setItemSlot(EquipmentSlot.HEAD, head);
        player.setItemSlot(EquipmentSlot.FEET, feet);
        player.setItemSlot(EquipmentSlot.MAINHAND, sabre);
        int duration = UtilityConfiguration.POWERSUIT_TICKS.get() / 60;
        for (int i = 0; i < duration; i++) {
            for (ItemStack stack : List.of(head, feet, sabre)) {
                tick(stack, player);
            }
            helper.assertTrue(ModuleSupport.hasWorkingUpgrade(head, ArmorUpgradeType.NIGHTVISION)
                            && ModuleSupport.hasWorkingUpgrade(feet, ArmorUpgradeType.STEPASSIST)
                            && ModuleSupport.hasWorkingUpgrade(sabre, ArmorUpgradeType.POWER),
                    "One pair must power the combined usage, including its last tick");
        }
        helper.assertTrue(EnergyHolderItem.count(holder, "negarite") == 0 && EnergyHolderItem.count(holder, "posirite") == 0
                        && EnergyHolderItem.getChargePercentage(holder) == 0,
                "The equipment must share exactly one fuel pair");
        for (ItemStack stack : List.of(head, feet, sabre)) {
            tick(stack, player);
            helper.assertTrue(!stack.getTag().contains("power") && !stack.getTag().contains("negarite")
                            && !stack.getTag().contains("posirite"), "No player equipment may store its own fuel or countdown");
        }
        helper.assertTrue(!ModuleSupport.hasWorkingUpgrade(head, ArmorUpgradeType.NIGHTVISION)
                        && !ModuleSupport.hasWorkingUpgrade(sabre, ArmorUpgradeType.POWER), "The shared pool must exhaust");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void holderRemovalAndEquipmentSwapsPreserveTheSharedCharge(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack holder = holder(2, 2);
        player.getInventory().setItem(9, holder);
        ItemStack sabre = gear(Registration.ENHANCED_ENERGY_SABRE.get(), ArmorUpgradeType.POWER);
        player.setItemSlot(EquipmentSlot.MAINHAND, sabre);
        tick(sabre, player);
        long charge = holder.getTag().getLong("charge");
        player.getInventory().setItem(9, ItemStack.EMPTY);
        tick(sabre, player);
        helper.assertTrue(!ModuleSupport.hasWorkingUpgrade(sabre, ArmorUpgradeType.POWER)
                        && holder.getTag().getLong("charge") == charge,
                "Removing the holder must stop modules without losing its remaining charge");
        player.getInventory().setItem(9, ItemStack.of(holder.save(new CompoundTag())));
        ItemStack lance = gear(Registration.ENERGY_LANCE.get(), ArmorUpgradeType.POWER);
        player.setItemSlot(EquipmentSlot.MAINHAND, lance);
        tick(lance, player);
        helper.assertTrue(ModuleSupport.hasWorkingUpgrade(lance, ArmorUpgradeType.POWER)
                        && EnergyHolderItem.findActiveHolder(player).getTag().getLong("charge") < charge
                        && EnergyHolderItem.count(player, "negarite") == 1,
                "A different weapon must resume the same saved charge without burning another pair");
        long remaining = EnergyHolderItem.findActiveHolder(player).getTag().getLong("charge");
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TORCH));
        tick(lance, player);
        tick(sabre, player);
        helper.assertTrue(!ModuleSupport.hasWorkingUpgrade(lance, ArmorUpgradeType.POWER)
                        && !ModuleSupport.hasWorkingUpgrade(sabre, ArmorUpgradeType.POWER)
                        && EnergyHolderItem.findActiveHolder(player).getTag().getLong("charge") == remaining,
                "An unusable lance and a spare sabre must consume nothing");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fuelPairsAreAtomicAndAutomaticCollectionFeedsOnlyTheActiveHolder(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack first = holder(3, 0);
        ItemStack spare = holder(0, 4);
        player.getInventory().setItem(9, first);
        player.getInventory().setItem(10, spare);
        helper.assertTrue(!EnergyHolderItem.consumePower(player, 30, UtilityConfiguration.POWERSUIT_TICKS.get())
                        && EnergyHolderItem.count(first, "negarite") == 3 && EnergyHolderItem.count(spare, "posirite") == 4,
                "Separate holders must not combine partial pairs or waste the available component");
        player.getInventory().setItem(0, new ItemStack(Registration.DUST_NEGARITE.get(), 5));
        player.getInventory().setItem(1, new ItemStack(Registration.DUST_POSIRITE.get(), 6));
        first.getTag().putInt("mode", EnergyHolderItem.MODE_AUTOMATIC);
        spare.getTag().putInt("mode", EnergyHolderItem.MODE_AUTOMATIC);
        first.getItem().inventoryTick(first, player.level(), player, 9, false);
        spare.getItem().inventoryTick(spare, player.level(), player, 10, false);
        first.getItem().inventoryTick(first, player.level(), player, 9, false);
        helper.assertTrue(EnergyHolderItem.count(first, "negarite") == 8 && EnergyHolderItem.count(first, "posirite") == 6
                        && EnergyHolderItem.count(spare, "posirite") == 4,
                "Only the active holder must gather fuel from the player's inventory");
        helper.assertTrue(EnergyHolderItem.consumePower(player, 30, UtilityConfiguration.POWERSUIT_TICKS.get()),
                "Automatic collection must supply the shared pool without equipment Autofeed modules");
        player.setItemSlot(EquipmentSlot.OFFHAND, spare);
        helper.assertTrue(EnergyHolderItem.findActiveHolder(player) == spare, "A held holder must take priority");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void efficiencyAndBudgetStayPerPiece(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack holder = holder(3, 3);
        player.getInventory().setItem(9, holder);
        ItemStack chest = gear(Registration.POWERSUIT_CHEST.get(), ArmorUpgradeType.FLIGHT,
                ArmorUpgradeType.FORCEFIELD, ArmorUpgradeType.HOVER, ArmorUpgradeType.ARMOR);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        tick(chest, player);
        helper.assertTrue(!ModuleSupport.hasWorkingUpgrade(chest, ArmorUpgradeType.FLIGHT)
                        && EnergyHolderItem.count(holder, "negarite") == 3, "Over-budget gear must draw no fuel");
        chest.getTag().putBoolean(ArmorUpgradeType.ENERGY.getModuleKey(), true);
        tick(chest, player);
        helper.assertTrue(ModuleSupport.hasWorkingUpgrade(chest, ArmorUpgradeType.FLIGHT), "Energy must allow a 120-usage chest");
        long before = holder.getTag().getLong("charge");
        tick(chest, player);
        long optimizedCost = before - holder.getTag().getLong("charge");
        chest.getTag().putBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey(), false);
        chest.getTag().putBoolean(ArmorUpgradeType.HOVER.getModuleKey(), false);
        chest.getTag().putBoolean(ArmorUpgradeType.ARMOR.getModuleKey(), false);
        chest.getTag().putBoolean(ArmorUpgradeType.ENERGY.getModuleKey(), false);
        before = holder.getTag().getLong("charge");
        tick(chest, player);
        long ordinaryCost = before - holder.getTag().getLong("charge");
        helper.assertTrue(optimizedCost * UtilityConfiguration.POWERSUIT_TICKS_OPTIMIZED.get()
                        == ordinaryCost * 4 * UtilityConfiguration.POWERSUIT_TICKS.get(),
                "Only the optimized piece's draw must use the improved fuel duration");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fuelCanOnlyBeLoadedThroughTheHolderGui(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack holder = holder(0, 0);
        player.getInventory().setItem(9, holder);
        player.getInventory().setItem(10, new ItemStack(Registration.DUST_NEGARITE.get(), 10));
        player.getInventory().setItem(11, new ItemStack(Registration.DUST_POSIRITE.get(), 10));
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Registration.POWERSUIT_HEAD.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Registration.POWERSUIT_CHEST.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Registration.POWERSUIT_LEGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Registration.POWERSUIT_FEET.get()));
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Registration.ENERGY_LANCE.get()));
        List<Function<Player, IGuiComponent<?>>> factories = List.of(ArmorGui::createHelmetGui, ArmorGui::createChestGui,
                ArmorGui::createLegsGui, ArmorGui::createBootsGui, ArmorGui::createSabreGui);
        for (Function<Player, IGuiComponent<?>> factory : factories) {
            IGuiComponent<?> gui = factory.apply(player);
            for (double x : new double[]{3.5, 4.5}) {
                gui.hit(player, null, x, 6.5);
                gui.hit(player, null, x, 7.5);
            }
            helper.assertTrue(EnergyHolderItem.count(holder, "negarite") == 0
                            && EnergyHolderItem.count(holder, "posirite") == 0
                            && player.getInventory().countItem(Registration.DUST_NEGARITE.get()) == 10
                            && player.getInventory().countItem(Registration.DUST_POSIRITE.get()) == 10,
                    "Equipment panels must show fuel status without offering fuel input controls");
        }
        IGuiComponent<?> holderGui = EnergyHolderItem.createGui(player);
        holderGui.hit(player, null, 5.5, 3.8);
        holderGui.hit(player, null, 5.5, 6.2);
        helper.assertTrue(EnergyHolderItem.count(holder, "negarite") == 1
                        && EnergyHolderItem.count(holder, "posirite") == 1,
                "The holder's own fuel controls must remain usable");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void partialWithdrawalAndFullStorageConserveDust(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            player.getInventory().setItem(i, new ItemStack(Items.STONE, 64));
        }
        ItemStack holder = holder(64, 0);
        player.getInventory().setItem(9, holder);
        player.getInventory().setItem(0, new ItemStack(Registration.DUST_NEGARITE.get(), 63));
        EnergyHolderItem.removeFuel(player, Registration.DUST_NEGARITE.get(), 64);
        helper.assertTrue(EnergyHolderItem.count(holder, "negarite") == 63
                        && player.getInventory().getItem(0).getCount() == 64,
                "A partial inventory insertion must deduct exactly the one transferred dust");
        holder.getTag().putInt("negarite", Integer.MAX_VALUE - 1);
        EnergyHolderItem.addFuel(player, Registration.DUST_NEGARITE.get(), 64);
        helper.assertTrue(EnergyHolderItem.count(holder, "negarite") == Integer.MAX_VALUE
                        && player.getInventory().getItem(0).getCount() == 63,
                "A full holder must neither overflow nor consume excess dust");
        helper.succeed();
    }

    private static ItemStack holder(int negarite, int posirite) {
        ItemStack stack = new ItemStack(Registration.ENERGY_HOLDER.get());
        stack.getOrCreateTag().putInt("negarite", negarite);
        stack.getOrCreateTag().putInt("posirite", posirite);
        return stack;
    }

    private static ItemStack gear(Item item, ArmorUpgradeType... modules) {
        ItemStack stack = new ItemStack(item);
        for (ArmorUpgradeType type : modules) {
            stack.getOrCreateTag().putBoolean(type.getModuleKey(), true);
        }
        return stack;
    }

    private static void tick(ItemStack stack, Player player) {
        stack.getItem().inventoryTick(stack, player.level(), player, 0, false);
    }
}
