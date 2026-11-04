package mcjty.ariente;

import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.items.armor.PersonalForcefield;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class PowerSuitProtectionTests {

    @GameTest(template = "empty")
    public static void energyProtectionScalesWithPoweredPiecesAndStopsWhenFuelIsRemoved(GameTestHelper helper) {
        Player player = player(helper);
        List<Item> pieces = List.of(Registration.POWERSUIT_FEET.get(), Registration.POWERSUIT_LEGS.get(),
                Registration.POWERSUIT_CHEST.get(), Registration.POWERSUIT_HEAD.get());
        List<EquipmentSlot> slots = List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
        for (int count = 0; count <= 4; count++) {
            if (count > 0) {
                equip(player, slots.get(count - 1), pieces.get(count - 1));
            }
            helper.assertTrue(close(damage(player, player.damageSources().magic()), 25 - count * 5),
                    "Every powered armor piece must add 20% energy protection, without requiring a complete set");
        }
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        head.getTag().putBoolean(ArmorUpgradeType.ARMOR.getModuleKey(), false);
        helper.assertTrue(close(damage(player, player.damageSources().magic()), 10),
                "A disabled module must stop protecting immediately, even if its old working flag remains");
        head.getTag().putBoolean(ArmorUpgradeType.ARMOR.getModuleKey(), true);
        player.getInventory().setItem(9, ItemStack.EMPTY);
        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getItemBySlot(slot);
            stack.getItem().inventoryTick(stack, player.level(), player, 0, false);
        }
        helper.assertTrue(close(damage(player, player.damageSources().magic()), 25),
                "Removing the shared fuel holder must remove all powered energy protection");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void armorPiercingProjectilesAreProtectedAndForcefieldAbsorbsTheRemainder(GameTestHelper helper) {
        Player player = fullSuit(helper);
        DamageSource ship = new DamageSource(helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(LaserEntity.SHIP_DAMAGE));
        helper.assertTrue(close(damage(player, ship), 5), "Projectile tags must protect against armor-piercing ship fire");
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        chest.getTag().putBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey(), true);
        ModuleSupport.updateWorkingState(chest, player);
        helper.assertTrue(close(damage(player, ship), 0), "A ready field must absorb the suit's remaining energy damage");
        helper.assertTrue(close(damage(player, ship), 2.5f), "A cooling field must halve the remaining energy damage");
        PersonalForcefield.updateState(player, 0, 0);
        helper.assertTrue(close(damage(player, player.damageSources().fall()), 25)
                        && close(damage(player, player.damageSources().drown()), 25)
                        && close(damage(player, player.damageSources().fellOutOfWorld()), 25),
                "Armor and forcefield must not turn unrelated environmental damage into energy damage");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ordinaryArrowsUseArmorOnceAndVanillaArmorDoesNotAcquireEnergyProtection(GameTestHelper helper) {
        Player suit = fullSuit(helper);
        Player diamond = player(helper);
        equip(diamond, EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE);
        DamageSource source = suit.damageSources().arrow(new Arrow(helper.getLevel(), diamond), diamond);
        // 25 damage against a powered suit reaches vanilla's 80% physical reduction cap.
        helper.assertTrue(close(damage(suit, source), 5), "Ordinary arrows must not receive energy protection a second time");
        helper.assertTrue(close(damage(diamond, diamond.damageSources().magic()), 25),
                "Vanilla armor must retain its existing vulnerability to magic and lasers");
        suit.discard();
        diamond.discard();
        helper.succeed();
    }

    private static Player fullSuit(GameTestHelper helper) {
        Player player = player(helper);
        equip(player, EquipmentSlot.HEAD, Registration.POWERSUIT_HEAD.get());
        equip(player, EquipmentSlot.CHEST, Registration.POWERSUIT_CHEST.get());
        equip(player, EquipmentSlot.LEGS, Registration.POWERSUIT_LEGS.get());
        equip(player, EquipmentSlot.FEET, Registration.POWERSUIT_FEET.get());
        return player;
    }

    private static Player player(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
        player.setHealth(200);
        ItemStack holder = new ItemStack(Registration.ENERGY_HOLDER.get());
        holder.getOrCreateTag().putInt("negarite", 64);
        holder.getOrCreateTag().putInt("posirite", 64);
        player.getInventory().setItem(9, holder);
        return player;
    }

    private static void equip(Player player, EquipmentSlot slot, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putBoolean(ArmorUpgradeType.ARMOR.getModuleKey(), true);
        player.setItemSlot(slot, stack);
        ModuleSupport.updateWorkingState(stack, player);
        player.getAttributes().addTransientAttributeModifiers(stack.getAttributeModifiers(slot));
    }

    private static float damage(Player player, DamageSource source) {
        player.setHealth(200);
        player.invulnerableTime = 0;
        player.hurt(source, 25);
        return 200 - player.getHealth();
    }

    private static boolean close(float actual, float expected) {
        return Math.abs(actual - expected) < .001f;
    }
}
