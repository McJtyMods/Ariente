package mcjty.ariente.items;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.ariente.Ariente;
import mcjty.ariente.recipes.ConstructorRecipe;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.lib.tooltips.ITooltipExtras;
import mcjty.lib.varia.ItemStackTools;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BlueprintItem extends GenericItem implements ITooltipExtras {

    public BlueprintItem() {
        super("blueprint");
        this.maxStackSize = 1;
        setHasSubtypes(true);
    }

    @Override
    public List<Pair<ItemStack, Integer>> getItems(ItemStack stack) {
        ItemStack destination = getDestination(stack);
        ConstructorRecipe recipe = BlueprintRecipeRegistry.findRecipe(destination);
        if (recipe == null) {
            return Collections.emptyList();
        } else {
            return recipe.getIngredients().stream().map(s -> Pair.of(s, -1)).collect(Collectors.toList());
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemStack destination = getDestination(stack);
        tooltip.add(TextFormatting.GRAY + "Result: " + TextFormatting.GREEN + destination.getDisplayName());
    }

    public static ModelResourceLocation EMPTY_BLUEPRINT_MODEL = new ModelResourceLocation(new ResourceLocation(Ariente.MODID, "empty_blueprint"), "inventory");

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (ConstructorRecipe recipe : BlueprintRecipeRegistry.getRecipes()) {
                items.add(makeBluePrint(recipe.getDestination()));
            }
        }
    }

    public static ItemStack makeBluePrint(ItemStack destination) {
        ItemStack dest = new ItemStack(ModItems.blueprintItem);
        CompoundNBT nbt = new CompoundNBT();
        JsonObject json = ItemStackTools.itemStackToJson(destination);
        nbt.setString("destination", json.toString());
        dest.setTagCompound(nbt);
        return dest;
    }

    public static ItemStack getDestination(ItemStack stack) {
        CompoundNBT nbt = stack.getTagCompound();
        if (nbt == null) {
            return ItemStack.EMPTY;
        }
        if (!nbt.hasKey("destination")) {
            return ItemStack.EMPTY;
        }
        String jsonString = nbt.getString("destination");
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(jsonString);
        return ItemStackTools.jsonToItemStack(json.getAsJsonObject());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack) {
                ItemStack destination = getDestination(stack);
                if (destination.isEmpty()) {
                    return new ModelResourceLocation(getRegistryName(), "inventory");
                } else {
                    return EMPTY_BLUEPRINT_MODEL;
                }
            }
        });

        setTileEntityItemStackRenderer(new BlueprintRenderer());
    }
}