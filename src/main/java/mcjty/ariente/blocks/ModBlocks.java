package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BaseBlockBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {

    public static BaseBlock darkstone;

    public static void init() {
        darkstone = new BaseBlockBuilder<>(Ariente.instance, "darkstone")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
//                .info("message.xnet.shiftmessage")
//                .infoExtended("message.xnet.antenna")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        darkstone.initModel();
    }
}