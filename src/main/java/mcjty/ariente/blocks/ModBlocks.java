package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BaseBlockBuilder;
import mcjty.lib.builder.BlockFlags;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ModBlocks {

    public static BaseBlock graymarble;
    public static BaseBlock graymarble_smooth;
    public static BaseBlock graymarble_pilar;
    public static BaseBlock graymarble_bricks;
    public static BaseBlock whitemarble;
    public static BaseBlock whitemarble_smooth;
    public static BaseBlock whitemarble_pilar;
    public static BaseBlock whitemarble_bricks;
    public static BaseBlock bluemarble;
    public static BaseBlock bluemarble_smooth;
    public static BaseBlock bluemarble_pilar;
    public static BaseBlock bluemarble_bricks;

    public static BaseBlock lithiumore;
    public static BaseBlock manganeseore;
    public static BaseBlock siliconore;
    public static BaseBlock silverore;
    public static BaseBlock platinumore;
    public static BaseBlock posirite;
    public static BaseBlock negarite;

    public static BaseBlock glowlog;
    public static BaseBlock glowleaves;

    public static void init() {
        graymarble = new BaseBlockBuilder<>(Ariente.instance, "graymarble")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        graymarble_smooth = new BaseBlockBuilder<>(Ariente.instance, "graymarble_smooth")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        graymarble_pilar = new BaseBlockBuilder<>(Ariente.instance, "graymarble_pilar")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        graymarble_bricks = new BaseBlockBuilder<>(Ariente.instance, "graymarble_bricks")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        bluemarble = new BaseBlockBuilder<>(Ariente.instance, "bluemarble")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        bluemarble_smooth = new BaseBlockBuilder<>(Ariente.instance, "bluemarble_smooth")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        bluemarble_pilar = new BaseBlockBuilder<>(Ariente.instance, "bluemarble_pilar")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        bluemarble_bricks = new BaseBlockBuilder<>(Ariente.instance, "bluemarble_bricks")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        whitemarble = new BaseBlockBuilder<>(Ariente.instance, "whitemarble")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        whitemarble_smooth = new BaseBlockBuilder<>(Ariente.instance, "whitemarble_smooth")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        whitemarble_pilar = new BaseBlockBuilder<>(Ariente.instance, "whitemarble_pilar")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        whitemarble_bricks = new BaseBlockBuilder<>(Ariente.instance, "whitemarble_bricks")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();

        lithiumore = new BaseBlockBuilder<>(Ariente.instance, "lithiumore")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        manganeseore = new BaseBlockBuilder<>(Ariente.instance, "manganeseore")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        siliconore = new BaseBlockBuilder<>(Ariente.instance, "siliconore")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        silverore = new BaseBlockBuilder<>(Ariente.instance, "silverore")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        platinumore = new BaseBlockBuilder<>(Ariente.instance, "platinumore")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        posirite = new BaseBlockBuilder<>(Ariente.instance, "posirite")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        negarite = new BaseBlockBuilder<>(Ariente.instance, "negarite")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();

        glowlog = new BaseBlockBuilder<>(Ariente.instance, "glowlog")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .lightValue(10)
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.RENDER_TRANSLUCENT)
                .build();
        glowleaves = new BaseBlockBuilder<>(Ariente.instance, "glowleaves")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .lightValue(10)
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.RENDER_TRANSLUCENT)
                .build();

        OreDictionary.registerOre("oreSilver", silverore);
        OreDictionary.registerOre("orePlatinum", platinumore);
        OreDictionary.registerOre("oreSilicon", siliconore);
        OreDictionary.registerOre("oreManganese", manganeseore);
        OreDictionary.registerOre("oreLithium", lithiumore);
        OreDictionary.registerOre("blockMarble", whitemarble);
        OreDictionary.registerOre("logWood", glowlog);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        graymarble.initModel();
        graymarble_smooth.initModel();
        graymarble_pilar.initModel();
        graymarble_bricks.initModel();
        whitemarble.initModel();
        whitemarble_smooth.initModel();
        whitemarble_pilar.initModel();
        whitemarble_bricks.initModel();
        bluemarble.initModel();
        bluemarble_smooth.initModel();
        bluemarble_pilar.initModel();
        bluemarble_bricks.initModel();
        lithiumore.initModel();
        manganeseore.initModel();
        siliconore.initModel();
        silverore.initModel();
        platinumore.initModel();
        posirite.initModel();
        negarite.initModel();
        glowlog.initModel();
        glowleaves.initModel();
    }
}