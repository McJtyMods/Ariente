package mcjty.ariente.items;

import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {

    public static GenericItem platinumIngot;

    public static void init() {
        platinumIngot = new GenericItem("ingot_platinum");
    }

    public static void initOreDict() {
//        OreDictionary.registerOre("oreSilver", silverore);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        platinumIngot.initModel();
    }

}
