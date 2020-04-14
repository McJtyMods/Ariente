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
import mcjty.lib.varia.ItemStackTools;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.parameter;

public class BlueprintItem extends Item implements ITooltipExtras, ITooltipSettings {

    private final TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(), parameter("info", stack -> getDestination(stack).getDisplayName().getFormattedText()));

    public BlueprintItem() {
        super(new Properties().group(Ariente.setup.getTab())
                .setISTER(BlueprintRenderer::createRenderer)
                .maxStackSize(1));
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
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, worldIn, tooltip, flag);
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
        CompoundNBT nbt = new CompoundNBT();
        JsonObject json = ItemStackTools.itemStackToJson(destination);
        nbt.putString("destination", json.toString());
        dest.setTag(nbt);
        return dest;
    }

    public static ItemStack getDestination(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null) {
            return ItemStack.EMPTY;
        }
        if (!nbt.contains("destination")) {
            return ItemStack.EMPTY;
        }
        String jsonString = nbt.getString("destination");
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(jsonString);
        return ItemStackTools.jsonToItemStack(json.getAsJsonObject());
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