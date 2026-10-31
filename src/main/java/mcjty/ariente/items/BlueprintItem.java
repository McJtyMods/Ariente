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
        // @todo 1.14
//        setHasSubtypes(true);
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
        tooltipBuilder.makeTooltip(Tools.getId(this), stack, tooltip, flag);
    }

    public static final ResourceLocation EMPTY_BLUEPRINT_MODEL = new ResourceLocation(Ariente.MODID, "empty_blueprint");

    // @todo 1.14
//    @Override
//    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
//        if (isInCreativeTab(tab)) {
//            for (ConstructorRecipe recipe : BlueprintRecipeRegistry.getRecipes()) {
//                items.add(makeBluePrint(recipe.getDestination()));
//            }
//        }
//    }

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
            return json.isJsonObject() ? JSonTools.jsonToItemStack(json.getAsJsonObject()) : ItemStack.EMPTY;
        } catch (JsonParseException | IllegalArgumentException | IllegalStateException e) {
            return ItemStack.EMPTY;
        }
    }


    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    public void initModel() {
//        ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
//            @Override
//            public ModelResourceLocation getModelLocation(ItemStack stack) {
//                ItemStack destination = getDestination(stack);
//                if (destination.isEmpty()) {
//                    return new ModelResourceLocation(getRegistryName(), "inventory");
//                } else {
//                    return EMPTY_BLUEPRINT_MODEL;
//                }
//            }
//        });
//
//        setTileEntityItemStackRenderer(new BlueprintRenderer());
//    }
}
