package mcjty.ariente;

import com.mojang.authlib.GameProfile;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.entities.fluxship.FluxShipEnergy;
import mcjty.ariente.items.EnergyHolderItem;
import mcjty.ariente.items.FluxShipItem;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class EquipCommandTests {

    @GameTest(template = "empty")
    public static void loadoutRequiresAnOperatorPlayerAndProvidesWorkingEquipment(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var commands = helper.getLevel().getServer().getCommands();
        var source = player.createCommandSourceStack();
        helper.assertTrue(commands.performPrefixedCommand(source.withPermission(0), "ariente equip") == 0
                        && commands.performPrefixedCommand(source.withPermission(1), "ariente equip") == 0
                        && player.getInventory().isEmpty(),
                "Players below operator level 2 must not receive any equipment");
        helper.assertTrue(commands.performPrefixedCommand(helper.getLevel().getServer().createCommandSourceStack(), "ariente equip") == 0,
                "A console must fail cleanly without a player target");
        // Check every hotbar selection, including an empty holder that used to take priority.
        for (int selected = 0; selected < 9; selected++) {
            player.getInventory().clearContent();
            player.getInventory().selected = selected;
            player.getInventory().setItem(0, new ItemStack(Registration.ENERGY_HOLDER.get()));
            player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            helper.assertTrue(commands.performPrefixedCommand(source.withPermission(2), "ariente equip") == 1,
                    "An operator must receive the loadout from any selected slot");
            helper.assertTrue(player.getMainHandItem().is(Registration.ENHANCED_ENERGY_SABRE.get())
                            && player.getOffhandItem().isEmpty()
                            && player.getInventory().countItem(Items.SHIELD) == 1,
                    "Equip the sabre and keep the off hand clear for the lance without losing its previous item");
            ItemStack holder = EnergyHolderItem.findActiveHolder(player);
            helper.assertTrue(EnergyHolderItem.count(holder, "negarite") == 1_000_000
                            && EnergyHolderItem.count(holder, "posirite") == 1_000_000
                            && EnergyHolderItem.getChargePercentage(holder) == 100,
                    "The active holder must start full with a million pairs, even when an old empty holder is present");
            boolean lance = false;
            boolean ship = false;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                lance |= stack.is(Registration.ENERGY_LANCE.get());
                ship |= stack.is(Registration.FLUX_SHIP.get()) && FluxShipItem.getCharge(stack) == FluxShipEnergy.CAPACITY;
            }
            helper.assertTrue(lance && ship, "The hotbar must include a lance and a fully charged ship");
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                    continue;
                }
                ItemStack armor = player.getItemBySlot(slot);
                helper.assertTrue(armor.getItem() instanceof PowerSuit
                                && ModuleSupport.updateWorkingState(armor, player)
                                && ModuleSupport.hasWorkingUpgrade(armor, ArmorUpgradeType.ENERGY)
                                && ModuleSupport.hasWorkingUpgrade(armor, ArmorUpgradeType.ARMOR),
                        "Every suit piece must have powered efficiency and armor upgrades within its budget");
            }
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            helper.assertTrue(ModuleSupport.hasWorkingUpgrade(chest, ArmorUpgradeType.REGENERATION)
                            && chest.getTag().contains(ArmorUpgradeType.FORCEFIELD.getModuleKey())
                            && !chest.getTag().getBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey())
                            && !ModuleSupport.hasWorkingUpgrade(chest, ArmorUpgradeType.FORCEFIELD)
                            && ModuleSupport.hasWorkingUpgrade(player.getItemBySlot(EquipmentSlot.FEET), ArmorUpgradeType.FEATHERFALLING),
                    "Regeneration and feather falling must work; the installed forcefield must stay disabled");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void replacingAFullInventoryPreservesDisplacedEquipment(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().selected = 4;
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        var commands = helper.getLevel().getServer().getCommands();
        helper.assertTrue(commands.performPrefixedCommand(player.createCommandSourceStack().withPermission(2), "ariente equip") == 1,
                "A full inventory must still receive the complete loadout");
        var dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(player.blockPosition()).inflate(5));
        int stone = player.getInventory().countItem(Items.STONE);
        int helmets = player.getInventory().countItem(Items.DIAMOND_HELMET);
        int shields = player.getInventory().countItem(Items.SHIELD);
        for (ItemEntity item : dropped) {
            ItemStack stack = item.getItem();
            stone += stack.is(Items.STONE) ? stack.getCount() : 0;
            helmets += stack.is(Items.DIAMOND_HELMET) ? stack.getCount() : 0;
            shields += stack.is(Items.SHIELD) ? stack.getCount() : 0;
        }
        helper.assertTrue(stone == 36 * 64 && helmets == 1 && shields == 1,
                "Displaced inventory and equipment must be retained or dropped without loss");
        helper.assertTrue(EnergyHolderItem.count(EnergyHolderItem.findActiveHolder(player), "negarite") == 1_000_000
                        && player.getInventory().countItem(Registration.ENERGY_LANCE.get()) == 1
                        && player.getInventory().countItem(Registration.FLUX_SHIP.get()) == 1,
                "A full inventory must keep the fueled holder, lance and ship accessible");
        dropped.forEach(ItemEntity::discard);
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "EquipFixture"));
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(1, 3, 1)));
        return player;
    }
}
