package mcjty.ariente.blocks.decorative;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DoubleMarbleSlabBlock extends MarbleSlabBlock {

    public DoubleMarbleSlabBlock(String name) {
        super(name);
    }

    @Override
    public boolean isDouble() {
        return true;
    }


    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (MarbleColor type : MarbleColor.VALUES) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.ordinal(), new ModelResourceLocation(getRegistryName(),
                    "type=" + type.getName()));
        }

//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
