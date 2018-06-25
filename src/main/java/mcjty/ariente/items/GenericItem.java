package mcjty.ariente.items;

import mcjty.ariente.Ariente;
import mcjty.lib.McJtyRegister;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GenericItem extends Item {

    public GenericItem(String name) {
        setUnlocalizedName(Ariente.MODID + "." + name);
        setRegistryName(name);
        setCreativeTab(Ariente.creativeTab);
        McJtyRegister.registerLater(this, Ariente.instance);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
