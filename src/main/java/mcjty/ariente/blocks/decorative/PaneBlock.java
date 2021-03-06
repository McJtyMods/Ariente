package mcjty.ariente.blocks.decorative;

import mcjty.ariente.Ariente;
import mcjty.lib.McJtyLib;
import mcjty.lib.McJtyRegister;
import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;

public class PaneBlock extends BlockPane {

    private boolean transluscent = false;

    public PaneBlock(String name, Material materialIn, SoundType soundType, boolean canDrop) {
        super(materialIn, canDrop);
        setSoundType(soundType);
        setCreativeTab(Ariente.setup.getTab());
        setUnlocalizedName(Ariente.MODID + "." + name);
        setRegistryName(name);
        McJtyRegister.registerLater(this, Ariente.instance, ItemBlock::new);
    }

    public PaneBlock setTransluscent(boolean transluscent) {
        this.transluscent = transluscent;
        return this;
    }

    public void initModel() {
        McJtyLib.proxy.initStandardItemModel(this);
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
