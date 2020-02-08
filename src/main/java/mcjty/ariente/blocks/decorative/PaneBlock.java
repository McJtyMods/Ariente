package mcjty.ariente.blocks.decorative;

import mcjty.lib.McJtyLib;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class PaneBlock extends net.minecraft.block.PaneBlock {

    private boolean transluscent = false;

    public PaneBlock(Material materialIn, SoundType soundType) {
        super(Properties.create(materialIn)
                .sound(soundType));
//        setRegistryName(name);
    }

    public PaneBlock setTransluscent(boolean transluscent) {
        this.transluscent = transluscent;
        return this;
    }

    public void initModel() {
        McJtyLib.proxy.initStandardItemModel(this);
    }

    // @todo 1.15
//    @Override
//    public BlockRenderLayer getRenderLayer() {
//        if (transluscent) {
//            return BlockRenderLayer.TRANSLUCENT;
//        } else {
//            return super.getRenderLayer();
//        }
//    }
}
