package mcjty.ariente.items;

import mcjty.lib.varia.Tools;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import java.util.function.Consumer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import mcjty.ariente.Ariente;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.BlueprintExchangeRecipe;
import mcjty.ariente.recipes.BlueprintRarity;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipExtras;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.JSonTools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class BlueprintItem extends Item implements ITooltipExtras, ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(), parameter("info", stack -> getDestination(stack).getHoverName().getString()));

    public BlueprintItem() {
        super(new Properties()
                // @todo 1.18 .setISTER(BlueprintRenderer::createRenderer)
                .stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlueprintRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    Minecraft mc = Minecraft.getInstance();
                    renderer = new BlueprintRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
                }
                return renderer;
            }
        });
    }

    @Override
    public List<Pair<ItemStack, Integer>> getItems(ItemStack stack) {
        ItemStack destination = getDestination(stack);
        ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
        if (recipe == null) {
            return Collections.emptyList();
        } else {
            return recipe.getIngredientList().stream().map(s -> Pair.of(s, ITooltipExtras.NOERROR)).collect(Collectors.toList());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, tooltip, flag);
        if (BlueprintExchangeRecipe.isPreview(stack)) {
            tooltip.add(Component.translatable("message.ariente.blueprint.exchange_preview"));
            BlueprintRarity rarity = BlueprintExchangeRecipe.averageRarity(stack);
            if (rarity != null) {
                tooltip.add(Component.translatable("message.ariente.blueprint.average_rarity", rarity.displayName()));
            }
            return;
        }
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
        ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(getDestination(stack));
        BlueprintRarity rarity = recipe == null ? null : BlueprintRarity.of(recipe);
        if (rarity != null) {
            tooltip.add(Component.translatable("message.ariente.blueprint.rarity", rarity.displayName()));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return BlueprintExchangeRecipe.isPreview(stack)
                ? Component.translatable("item.ariente.random_blueprint") : super.getName(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        // Also reveal paid outputs from crafting integrations that do not fire the player crafting event.
        if (!level.isClientSide && BlueprintExchangeRecipe.isPreview(stack)) {
            BlueprintExchangeRecipe.resolve(stack, level.random);
        }
    }

    public static final ResourceLocation EMPTY_BLUEPRINT_MODEL = new ResourceLocation(Ariente.MODID, "empty_blueprint");

    public static ItemStack makeBluePrint(ItemStack destination) {
        ItemStack dest = new ItemStack(Registration.BLUEPRINT.get());
        CompoundTag nbt = new CompoundTag();
        JsonObject json = JSonTools.itemStackToJson(destination);
        nbt.putString("destination", json.toString());
        dest.setTag(nbt);
        return dest;
    }

    public static ItemStack getDestination(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            return ItemStack.EMPTY;
        }
        if (!nbt.contains("destination")) {
            return ItemStack.EMPTY;
        }
        try {
            JsonElement json = JsonParser.parseString(nbt.getString("destination"));
            if (!json.isJsonObject()) {
                return ItemStack.EMPTY;
            }
            JsonObject object = json.getAsJsonObject();
            if (!object.has("item") || !object.get("item").isJsonPrimitive() || !object.getAsJsonPrimitive("item").isString()) {
                return ItemStack.EMPTY;
            }
            return JSonTools.jsonToItemStack(object);
        } catch (JsonParseException | IllegalArgumentException | IllegalStateException | UnsupportedOperationException e) {
            return ItemStack.EMPTY;
        }
    }

}
