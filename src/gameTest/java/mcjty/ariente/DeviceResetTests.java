package mcjty.ariente;

import mcjty.ariente.items.ItemData;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class DeviceResetTests {

    @GameTest(template = "empty")
    public static void resetsDiscardDataInEveryInventoryAndTableSlot(GameTestHelper helper) {
        String[] devices = new String[]{"aicore", "auto_constructor", "automation_field", "blueprint_storage", "charger", "door_marker", "elevator", "field_marker", "forcefield", "holo_storage_1", "holo_storage_2", "holo_storage_3", "input_item_node", "lock", "negarite_generator", "output_item_node", "posirite_generator", "power_combiner", "purifier_finish", "purifier_start", "recycler", "round_robin_node", "sensor_item_node", "signal_receiver", "signal_transmitter", "smelter", "storage", "storage_3x3", "warper", "wireless_button", "wireless_lock"};
        var manager = helper.getLevel().getRecipeManager();
        for (String name : devices) {
            var id = ResourceLocation.fromNamespaceAndPath("ariente", name);
            var item = BuiltInRegistries.ITEM.get(id);
            var key = ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath("ariente", name + "_reset"));
            CraftingRecipe recipe = (CraftingRecipe) manager.byKey(key.location()).orElseThrow().value();
            for (int width : new int[]{2, 3}) {
                for (int slot = 0; slot < width * width; slot++) {
                    ItemStack polluted = new ItemStack(item);
                    CompoundTag data = new CompoundTag();
                    data.putString("Contents", "stored items, fuel, upgrades, links and settings");
                    ItemData.set(polluted, data);
                    polluted.set(DataComponents.CUSTOM_NAME, Component.literal("Configured device"));
                    polluted.set(Registration.MACHINE_DATA.get(), CustomData.of(data));
                    ItemStack original = polluted.copy();
                    List<ItemStack> stacks = new ArrayList<>(Collections.nCopies(width * width, ItemStack.EMPTY));
                    stacks.set(slot, polluted);
                    CraftingInput input = CraftingInput.of(width, width, stacks);
                    helper.assertTrue(recipe.matches(input, helper.getLevel()), "Reset must match " + name + " in slot " + slot);
                    ItemStack output = recipe.assemble(input, helper.getLevel().registryAccess());
                    helper.assertTrue(output.getCount() == 1 && ItemStack.isSameItemSameComponents(output, new ItemStack(item)),
                            "Reset must return a fresh stackable default " + name);
                    helper.assertTrue(ItemStack.matches(original, polluted), "Recipe preview must preserve its input");
                    helper.assertTrue(recipe.getRemainingItems(input).stream().allMatch(ItemStack::isEmpty), "Reset must not return stored contents or upgrades");
                    helper.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow().id().equals(key.location()),
                            "The recipe manager must select the reset without conflicts");
                    stacks.set((slot + 1) % stacks.size(), new ItemStack(Items.DIRT));
                    helper.assertTrue(!recipe.matches(CraftingInput.of(width, width, stacks), helper.getLevel()), "Extra ingredients reject the reset");
                }
            }
        }
        var removed = ResourceKey.create(Registries.RECIPE, ResourceLocation.parse("ariente:enhanced_energy_sabre"));
        helper.assertTrue(manager.byKey(removed.location()).isEmpty(), "The crafting-table sabre exploit must remain removed");
        helper.assertTrue(BlueprintRecipeRegistry.findRecipe(new ItemStack(Registration.ENHANCED_ENERGY_SABRE.get())) != null,
                "The existing enhanced sabre Constructor blueprint must remain available");
        helper.succeed();
    }
}
