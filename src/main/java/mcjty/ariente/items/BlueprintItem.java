package mcjty.ariente.items;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import mcjty.lib.varia.ItemStackTools;
import mcjty.lib.varia.JSonTools;
import mcjty.lib.varia.NBTTools;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nullable;
import java.util.List;

public class BlueprintItem extends GenericItem {

    public BlueprintItem() {
        super("blueprint");
        this.maxStackSize = 1;
        setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
//        tooltip.add("Security card");
//        Set<String> tags = getSecurityTags(stack);
//        if (tags.isEmpty()) {
//            tooltip.add(TextFormatting.YELLOW + "Security card is empty!");
//        } else {
//            tooltip.add(TextFormatting.YELLOW + "Tags:");
//            for (String tag : tags) {
//                tooltip.add("   " + TextFormatting.GREEN + tag);
//            }
//        }
    }

    public static ModelResourceLocation EMPTY_BLUEPRINT_MODEL = new ModelResourceLocation(new ResourceLocation(Ariente.MODID, "empty_blueprint"), "inventory");

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(makeBluePrint(new ItemStack(ModBlocks.negariteGeneratorBlock)));
            items.add(makeBluePrint(new ItemStack(ModBlocks.posiriteGeneratorBlock)));
            items.add(makeBluePrint(new ItemStack(ModBlocks.forceFieldBlock)));
            items.add(makeBluePrint(new ItemStack(ModBlocks.levelMarkerBlock)));
            items.add(makeBluePrint(new ItemStack(ModBlocks.alarmBlock)));
            items.add(makeBluePrint(new ItemStack(ModBlocks.signalReceiverBlock)));
            items.add(makeBluePrint(new ItemStack(ModItems.powerSuitChest)));
            items.add(makeBluePrint(new ItemStack(ModItems.powerSuitBoots)));
            items.add(makeBluePrint(new ItemStack(ModItems.powerSuitHelmet)));
            items.add(makeBluePrint(new ItemStack(ModItems.powerSuitLegs)));
            items.add(makeBluePrint(new ItemStack(ModItems.moduleAutofeed)));
            items.add(makeBluePrint(new ItemStack(ModItems.moduleEnergy)));
            items.add(makeBluePrint(new ItemStack(ModItems.moduleArmor)));
            items.add(makeBluePrint(new ItemStack(ModItems.moduleFeatherFalling)));
        }
    }

    private static ItemStack makeBluePrint(ItemStack destination) {
        ItemStack dest = new ItemStack(ModItems.blueprintItem);
        NBTTagCompound nbt = new NBTTagCompound();
        JsonObject json = ItemStackTools.itemStackToJson(destination);
        nbt.setString("destination", json.toString());
        dest.setTagCompound(nbt);
        return dest;
    }

    public static ItemStack getDestination(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
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