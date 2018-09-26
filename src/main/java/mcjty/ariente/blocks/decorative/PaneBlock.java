package mcjty.ariente.blocks.decorative;

import mcjty.ariente.Ariente;
import mcjty.lib.McJtyRegister;
import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PaneBlock extends BlockPane {

    private boolean transluscent = false;

    public PaneBlock(String name, Material materialIn, SoundType soundType, boolean canDrop) {
        super(materialIn, canDrop);
        setSoundType(soundType);
        setCreativeTab(Ariente.creativeTab);
        setUnlocalizedName(Ariente.MODID + "." + name);
        setRegistryName(name);
        McJtyRegister.registerLater(this, Ariente.instance, ItemBlock::new);
    }

    public PaneBlock setTransluscent(boolean transluscent) {
        this.transluscent = transluscent;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        if (transluscent) {
            return BlockRenderLayer.TRANSLUCENT;
        } else {
            return super.getBlockLayer();
        }
    }
}
