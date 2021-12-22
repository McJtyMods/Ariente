package mcjty.ariente.items;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.ariente.Ariente;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.setup.Registration;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipExtras;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.JSonTools;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
        super(new Properties().tab(Ariente.setup.getTab())
                // @todo 1.18 .setISTER(BlueprintRenderer::createRenderer)
                .stacksTo(1));
        // @todo 1.14
//        setHasSubtypes(true);
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
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flag);
    }

    public static ModelResourceLocation EMPTY_BLUEPRINT_MODEL = new ModelResourceLocation(new ResourceLocation(Ariente.MODID, "empty_blueprint"), "inventory");

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
        String jsonString = nbt.getString("destination");
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(jsonString);
        return JSonTools.jsonToItemStack(json.getAsJsonObject());
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