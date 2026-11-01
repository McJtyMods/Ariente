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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class PersonalForcefieldTests {

    @GameTest(template = "empty")
    public static void lasersAndMagicAreBlockedAndUseAbsorbedDamageForCooldown(GameTestHelper helper) {
        Player player = equippedPlayer(helper);
        Player shooter = helper.makeMockSurvivalPlayer();
        LaserEntity laser = LaserEntity.create(helper.getLevel(), shooter, 1, 0, 0);
        DamageSource source = player.damageSources().indirectMagic(laser, shooter);
        hit(player, source, 24);
        helper.assertTrue(player.getHealth() == 20, "A fresh field must block actual indirect-magic laser damage");
        helper.assertTrue(remaining(player) == 40, "A fully absorbed advanced drone laser must start a two-second cooldown");
        hit(player, source, 24);
        helper.assertTrue(close(player.getHealth(), 8), "A cooling field must still absorb half of a laser hit");
        helper.assertTrue(remaining(player) == 20, "Every hit resets the timer using the damage actually absorbed");
        PersonalForcefield.updateState(player, 0, 0);
        hit(player, player.damageSources().magic(), 12);
        helper.assertTrue(player.getHealth() == 20 && remaining(player) == 20, "Direct magic must also be blocked");
        PersonalForcefield.updateState(player, 0, 0);
        DamageSource tagged = new DamageSource(helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.ARROW));
        helper.assertTrue(damageEvent(player, tagged, 4).getAmount() == 0,
                "Projectile-tagged damage must be blocked even without a projectile entity");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void arrowsRefreshCooldownAndFullProtectionReturnsAfterExpiry(GameTestHelper helper) {
        Player player = equippedPlayer(helper);
        Player shooter = helper.makeMockSurvivalPlayer();
        Arrow arrow = new Arrow(helper.getLevel(), shooter);
        DamageSource source = player.damageSources().arrow(arrow, shooter);
        hit(player, source, 4);
        helper.assertTrue(player.getHealth() == 20 && remaining(player) == 10, "A skeleton-strength arrow needs a half-second cooldown");
        helper.runAfterDelay(5, () -> {
            hit(player, source, 4);
            helper.assertTrue(player.getHealth() < 20 && remaining(player) == 10, "A second arrow must do reduced damage and restart the cooldown");
        });
        helper.runAfterDelay(14, () -> helper.assertTrue(PersonalForcefield.isCoolingDown(player), "The refreshed cooldown must still be active"));
        helper.runAfterDelay(15, () -> {
            helper.assertTrue(!PersonalForcefield.isCoolingDown(player), "Cooldown must expire at the specified tick");
            hit(player, source, 4);
            helper.assertTrue(player.getHealth() == 20, "An expired field must block arrows completely again");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void explosionsShareCooldownAndTakePriorityOverProjectileDamage(GameTestHelper helper) {
        Player player = equippedPlayer(helper);
        Player attacker = helper.makeMockSurvivalPlayer();
        Arrow projectile = new Arrow(helper.getLevel(), attacker);
        DamageSource fireworks = new DamageSource(helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.FIREWORKS), projectile, attacker);
        LivingDamageEvent first = damageEvent(player, fireworks, 30);
        helper.assertTrue(close(first.getAmount(), 6) && remaining(player) == 40,
                "An explosion must absorb 80%, even with a projectile as its direct entity");
        LivingDamageEvent second = damageEvent(player, fireworks, 30);
        helper.assertTrue(close(second.getAmount(), 18) && remaining(player) == 20, "A cooling field must absorb only 40% of explosions");
        LivingDamageEvent arrow = damageEvent(player, player.damageSources().arrow(projectile, attacker), 4);
        helper.assertTrue(close(arrow.getAmount(), 2), "Explosion cooldown must also weaken projectile protection");
        PersonalForcefield.updateState(player, 0, 0);
        damageEvent(player, player.damageSources().magic(), 12);
        LivingDamageEvent afterMagic = damageEvent(player, fireworks, 30);
        helper.assertTrue(close(afterMagic.getAmount(), 18), "Magic cooldown must also weaken explosion protection");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void meleeRetaliatesWithoutThornsRecursionOrRangedRetaliation(GameTestHelper helper) {
        Player player = equippedPlayer(helper);
        Player attacker = helper.makeMockSurvivalPlayer();
        LivingDamageEvent melee = damageEvent(player, player.damageSources().playerAttack(attacker), 8);
        helper.assertTrue(close(melee.getAmount(), 4), "Melee protection must remain 50%");
        helper.assertTrue(close(attacker.getHealth(), 16), "Melee retaliation must reflect half the incoming damage");
        attacker = equippedPlayer(helper);
        damageEvent(player, player.damageSources().playerAttack(attacker), 8);
        helper.assertTrue(attacker.getHealth() < 20 && attacker.getHealth() >= 18, "Thorns must hit even another shielded attacker");
        helper.assertTrue(player.getHealth() == 20 && remaining(player) == 0 && remaining(attacker) == 0,
                "Thorns must not bounce back or start a ranged cooldown");
        attacker.invulnerableTime = 0;
        attacker.setHealth(20);
        damageEvent(player, player.damageSources().indirectMagic(attacker, attacker), 8);
        helper.assertTrue(attacker.getHealth() == 20, "Magic must never cause melee retaliation, even with a living direct attacker");
        Arrow arrow = new Arrow(helper.getLevel(), attacker);
        damageEvent(player, player.damageSources().arrow(arrow, attacker), 8);
        helper.assertTrue(attacker.getHealth() == 20, "Projectiles must not retaliate against their shooter");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void inactiveShieldsAndUnrelatedBypassDamageRemainUnprotected(GameTestHelper helper) {
        Player player = equippedPlayer(helper);
        LivingDamageEvent fall = damageEvent(player, player.damageSources().fall(), 8);
        helper.assertTrue(close(fall.getAmount(), 8) && remaining(player) == 0, "The field must not grant immunity to falls");
        player.getItemBySlot(EquipmentSlot.CHEST).getOrCreateTag().putBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey(), false);
        hit(player, player.damageSources().magic(), 8);
        helper.assertTrue(close(player.getHealth(), 12) && remaining(player) == 0, "Disabled fields must not block magic");
        player = equippedPlayer(helper);
        player.getItemBySlot(EquipmentSlot.CHEST).getOrCreateTag().putBoolean(ArmorUpgradeType.FORCEFIELD.getWorkingKey(), false);
        hit(player, player.damageSources().magic(), 8);
        helper.assertTrue(close(player.getHealth(), 12), "Fuel loss must disable protection");
        player = equippedPlayer(helper);
        hit(player, player.damageSources().magic(), 12);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        helper.assertTrue(remaining(player) == 20, "Re-equipping armor must not reset the wearer's cooldown");
        helper.succeed();
    }

    private static Player equippedPlayer(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack chest = new ItemStack(Registration.POWERSUIT_CHEST.get());
        chest.getOrCreateTag().putBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey(), true);
        ItemStack holder = new ItemStack(Registration.ENERGY_HOLDER.get());
        holder.getOrCreateTag().putInt("negarite", 64);
        holder.getOrCreateTag().putInt("posirite", 64);
        player.getInventory().setItem(9, holder);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        ModuleSupport.updateWorkingState(chest, player);
        return player;
    }

    private static LivingDamageEvent damageEvent(Player player, DamageSource source, float damage) {
        LivingDamageEvent event = new LivingDamageEvent(player, source, damage);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    private static void hit(Player player, DamageSource source, float damage) {
        player.invulnerableTime = 0;
        player.setHealth(20);
        player.hurt(source, damage);
    }

    private static long remaining(Player player) {
        return Math.max(0, PersonalForcefield.getCooldownUntil(player) - player.level().getGameTime());
    }

    private static boolean close(float actual, float expected) {
        return Math.abs(actual - expected) < 0.0001f;
    }
}
