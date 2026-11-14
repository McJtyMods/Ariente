package mcjty.ariente.compat.jei;

import com.mojang.blaze3d.platform.NativeImage;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Checks the discovered production plugin through JEI's real runtime and recipe GUI. */
public class JeiClientChecks implements ClientModInitializer, IModPlugin {

    private static IJeiRuntime runtime;
    private static int ticks;
    private static int waiting;
    private static int phase;
    private static int recipeCount;
    private static CompletableFuture<NativeImage> capture;

    @Override
    public Identifier getPluginUid() {
        return Identifier.parse("ariente_jei_client_tests:runtime_checks");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(JeiClientChecks::tick);
    }

    private static void tick(Minecraft mc) {
        mc.options.pauseOnLostFocus = false;
        if (mc.level == null || mc.player == null || runtime == null) {
            if (++waiting % 40 == 0 && mc.screen != null) {
                for (var child : mc.screen.children()) {
                    if (child instanceof AbstractButton button && (button.getMessage().getString().startsWith("Proceed")
                            || button.getMessage().getString().equals("Continue"))) {
                        button.onPress(new KeyEvent(257, 0, 0));
                        break;
                    }
                }
            }
            require(waiting < 1800, "JEI runtime/disposable test world did not start");
            return;
        }
        if (phase == 0 && ++ticks >= 40) {
            verifyRuntime();
            runtime.getRecipesGui().showTypes(List.of(JeiPlugin.CONSTRUCTOR));
            phase = 1;
            ticks = 0;
        } else if (phase == 1 && ++ticks >= 20) {
            require(mc.screen != null && mc.screen.getClass().getName().startsWith("mezz.jei."),
                    "Constructor recipe GUI must actually open");
            capture = new CompletableFuture<>();
            Screenshot.takeScreenshot(mc.getMainRenderTarget(), capture::complete);
            phase = 2;
        } else if (phase == 2 && capture.isDone()) {
            try (NativeImage image = capture.join()) {
                image.writeToFile(mc.gameDirectory.toPath().resolve("jei-constructor.png"));
                Files.writeString(mc.gameDirectory.toPath().resolve("jei-client-results.txt"),
                        "JEI CLIENT CHECKS PASSED: " + recipeCount + " Constructor recipes, blueprint/output slots, both catalysts, "
                                + "output recipe lookup, Purifier category and actual recipe GUI\n");
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
            System.out.println("JEI CLIENT CHECKS PASSED: " + recipeCount + " Constructor recipes and rendered GUI");
            phase = 3;
            mc.stop();
        }
    }

    private static void verifyRuntime() {
        var discovered = FabricLoader.getInstance().getEntrypointContainers("jei_mod_plugin", IModPlugin.class).stream()
                .filter(entry -> entry.getProvider().getMetadata().getId().equals("ariente")).toList();
        require(discovered.size() == 1 && discovered.getFirst().getEntrypoint() instanceof JeiPlugin,
                "JEI must discover exactly one production Ariente plugin");
        var manager = runtime.getRecipeManager();
        var category = manager.getRecipeCategory(JeiPlugin.CONSTRUCTOR);
        require(category != null && category.getTitle().getString().equals("Constructor"), "Constructor category must be registered");
        var recipes = manager.createRecipeLookup(JeiPlugin.CONSTRUCTOR).get().toList();
        var expected = BlueprintRecipeRegistry.getRecipes();
        require(!expected.isEmpty() && new HashSet<>(recipes).equals(new HashSet<>(expected)),
                "All active Constructor blueprint recipes must reach JEI");
        recipeCount = recipes.size();
        var focusFactory = runtime.getJeiHelpers().getFocusFactory();
        for (var recipe : recipes) {
            var layout = manager.createRecipeLayoutDrawable(category, recipe, focusFactory.getEmptyFocusGroup()).orElseThrow();
            var slots = layout.getRecipeSlotsView();
            require(slots.getSlotViews(RecipeIngredientRole.INPUT).size() == recipe.getIngredientList().size(),
                    "Every Constructor ingredient must have its own recipe slot");
            require(slots.getSlotViews(RecipeIngredientRole.OUTPUT).stream().flatMap(slot -> slot.getItemStacks())
                    .anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, recipe.getDestination())),
                    "The recipe result must be visible");
            require(slots.getSlotViews(RecipeIngredientRole.CRAFTING_STATION).stream().flatMap(slot -> slot.getItemStacks())
                    .anyMatch(stack -> stack.is(Registration.BLUEPRINT.get())
                            && ItemStack.isSameItemSameComponents(BlueprintItem.getDestination(stack), recipe.getDestination())),
                    "The matching required blueprint must be visible");
            var output = focusFactory.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, recipe.getDestination());
            require(manager.createRecipeLookup(JeiPlugin.CONSTRUCTOR).limitFocus(List.of(output)).get().anyMatch(recipe::equals),
                    "Looking up an output item must find its Constructor recipe");
        }
        var catalysts = manager.createRecipeCatalystLookup(JeiPlugin.CONSTRUCTOR).getItemStack().toList();
        require(catalysts.stream().anyMatch(stack -> stack.is(Registration.CONSTRUCTOR.get().asItem()))
                        && catalysts.stream().anyMatch(stack -> stack.is(Registration.AUTO_CONSTRUCTOR.get().asItem())),
                "Constructor and Auto Constructor must both be registered as recipe catalysts");
        require(manager.getRecipeCategory(JeiPlugin.PURIFYING) != null
                        && manager.createRecipeLookup(JeiPlugin.PURIFYING).get().findAny().isPresent(),
                "The same plugin must also restore Purifier recipes");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
