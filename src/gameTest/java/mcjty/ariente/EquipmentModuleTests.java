package mcjty.ariente;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.items.ItemData;
import mcjty.ariente.items.armor.ArmorGui;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.IGuiComponent;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class EquipmentModuleTests {

    @GameTest(template = "empty")
    public static void equipModulesRemainEditableInAllArmorPanels(GameTestHelper helper) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "EquipmentFixture"));
        var commands = helper.getLevel().getServer().getCommands();
        commands.performPrefixedCommand(player.createCommandSourceStack().withPermission(2), "ariente equip");
        check(helper, PowerSuit.hasFullArmor(player), "The equip command must create the complete loadout");
        List<Piece> pieces = List.of(
                new Piece(EquipmentSlot.HEAD, ArmorGui::createHelmetGui, 20,
                        List.of(Registration.MODULE_ENERGY.get(), Registration.MODULE_ARMOR.get())),
                new Piece(EquipmentSlot.CHEST, ArmorGui::createChestGui, 60,
                        List.of(Registration.MODULE_ENERGY.get(), Registration.MODULE_ARMOR.get(),
                                Registration.MODULE_FORCEFIELD.get(), Registration.MODULE_REGENERATION.get())),
                new Piece(EquipmentSlot.LEGS, ArmorGui::createLegsGui, 20,
                        List.of(Registration.MODULE_ENERGY.get(), Registration.MODULE_ARMOR.get())),
                new Piece(EquipmentSlot.FEET, ArmorGui::createBootsGui, 40,
                        List.of(Registration.MODULE_ENERGY.get(), Registration.MODULE_ARMOR.get(), Registration.MODULE_FEATHERFALLING.get())));
        for (Piece piece : pieces) {
            ItemStack stack = player.getItemBySlot(piece.slot());
            var usage = ModuleSupport.getPowerUsage(stack);
            check(helper, usage.getLeft() == piece.power() && usage.getRight().equals(UtilityConfiguration.POWERSUIT_MAXPOWER_OPTIMIZED.get()),
                    "The equipped " + piece.slot() + " must have its expected optimized power budget");
            check(helper, ModuleSupport.updateWorkingState(stack, player), "The equipped armor must have shared power");
            IGuiComponent<?> gui = piece.gui().apply(player);
            for (int i = 0; i < piece.modules().size(); i++) {
                ArmorModuleItem module = piece.modules().get(i);
                ArmorUpgradeType type = module.getType();
                boolean activated = type != ArmorUpgradeType.FORCEFIELD;
                check(helper, ItemData.get(stack).contains(type.getModuleKey()) && enabled(stack, type) == activated,
                        "The command must preinstall " + type + " on " + piece.slot() + " with the correct activation state");
                check(helper, ModuleSupport.hasWorkingUpgrade(stack, type) == activated,
                        "Installed active modules must actually work");
                double x = 1 + 4 * (i % 2), y = 2.4 + 1.2 * (i / 2);
                int before = player.getInventory().countItem(module);
                gui.hit(player, null, x + .5, y + .1);
                check(helper, !ItemData.get(stack).contains(type.getModuleKey()) && player.getInventory().countItem(module) == before + 1,
                        "The GUI must recognize and remove installed " + type + " from " + piece.slot());
                gui.hit(player, null, x + .5, y + .1);
                check(helper, ItemData.get(stack).contains(type.getModuleKey()) && !enabled(stack, type)
                                && player.getInventory().countItem(module) == before,
                        "The GUI must reinstall the module, disabled, without duplicating it");
                gui.hit(player, null, x + 1.5, y + .1);
                check(helper, enabled(stack, type), "The activation control must enable " + type);
                gui.hit(player, null, x + 2.5, y + .1);
                ModuleSupport.receivedHotkey(player, 1);
                check(helper, !enabled(stack, type), "The configured hotkey must disable " + type);
                for (int click = 0; click < 4; click++) {
                    gui.hit(player, null, x + 2.5, y + .1);
                }
                if (activated) {
                    gui.hit(player, null, x + 1.5, y + .1);
                }
                Ariente.setup.getLogger().info("EQUIPMENT CHECK: {} {} recognized, removed, reinstalled, activated and hotkey toggled", piece.slot(), type);
            }
        }
        // Modular weapons must remain supported by the same GUI validation.
        ItemStack weapon = player.getMainHandItem();
        player.getInventory().add(new ItemStack(Registration.MODULE_ENERGY.get()));
        ArmorGui.createSabreGui(player).hit(player, null, 1.5, 2.5);
        check(helper, ItemData.get(weapon).contains(ArmorUpgradeType.ENERGY.getModuleKey()), "The weapon GUI must still install compatible modules");
        // Reject a suit piece placed in a slot it cannot equip in.
        ItemStack misplaced = player.getItemBySlot(EquipmentSlot.CHEST).copy();
        player.setItemSlot(EquipmentSlot.HEAD, misplaced);
        var original = ItemData.get(misplaced);
        ArmorGui.createHelmetGui(player).hit(player, null, 1.5, 2.5);
        check(helper, original.equals(ItemData.get(misplaced)), "The helmet panel must reject chest armor in the head slot");
        Ariente.setup.getLogger().info("EQUIPMENT CHECK: all four armor panels, disabled forcefield, weapon support and wrong-slot rejection passed");
        helper.succeed();
    }

    private static boolean enabled(ItemStack stack, ArmorUpgradeType type) {
        return ItemData.get(stack).getBoolean(type.getModuleKey());
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, message);
    }

    private record Piece(EquipmentSlot slot, Function<Player, IGuiComponent<?>> gui, int power, List<ArmorModuleItem> modules) {
    }
}
