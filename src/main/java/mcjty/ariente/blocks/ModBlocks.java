package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BaseBlockBuilder;
import mcjty.lib.builder.BlockFlags;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ModBlocks {

    public static BaseBlock blackmarble_techpat1;

    public static MarbleBlock marble;
    public static BaseBlock graymarble_smooth;
    public static BaseBlock graymarble_pilar;
    public static BaseBlock graymarble_bricks;
    public static BaseBlock blackmarble_smooth;
    public static BaseBlock blackmarble_pilar;
    public static BaseBlock blackmarble_bricks;
    public static BaseBlock whitemarble_smooth;
    public static BaseBlock whitemarble_pilar;
    public static BaseBlock whitemarble_bricks;
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
    public static BaseBlock bluelog;
    public static BaseBlock blueleaves;

    public static void init() {
        blackmarble_techpat1 = new BaseBlockBuilder<>(Ariente.instance, "blackmarble_techpat1")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .flags(BlockFlags.RENDER_CUTOUT , BlockFlags.RENDER_SOLID)
                .build();

        marble = new MarbleBlock("marble");
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
        blackmarble_smooth = new BaseBlockBuilder<>(Ariente.instance, "blackmarble_smooth")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        blackmarble_pilar = new BaseBlockBuilder<>(Ariente.instance, "blackmarble_pilar")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        blackmarble_bricks = new BaseBlockBuilder<>(Ariente.instance, "blackmarble_bricks")
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
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.RENDER_TRANSLUCENT, BlockFlags.RENDER_NOSIDES)
                .build();
        glowleaves = new BaseBlockBuilder<>(Ariente.instance, "glowleaves")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .lightValue(10)
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.RENDER_TRANSLUCENT)
                .build();
        bluelog = new BaseBlockBuilder<>(Ariente.instance, "bluelog")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        blueleaves = new BaseBlockBuilder<>(Ariente.instance, "blueleaves")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.RENDER_CUTOUT)
                .build();
    }

    public static void initOreDict() {
        OreDictionary.registerOre("oreSilver", silverore);
        OreDictionary.registerOre("orePlatinum", platinumore);
        OreDictionary.registerOre("oreSilicon", siliconore);
        OreDictionary.registerOre("oreManganese", manganeseore);
        OreDictionary.registerOre("oreLithium", lithiumore);
        OreDictionary.registerOre("blockMarble", marble);
        OreDictionary.registerOre("logWood", glowlog);
        OreDictionary.registerOre("logWood", bluelog);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        blackmarble_techpat1.initModel();

        marble.initModel();
        blackmarble_smooth.initModel();
        blackmarble_pilar.initModel();
        blackmarble_bricks.initModel();
        graymarble_smooth.initModel();
        graymarble_pilar.initModel();
        graymarble_bricks.initModel();
        whitemarble_smooth.initModel();
        whitemarble_pilar.initModel();
        whitemarble_bricks.initModel();
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
        bluelog.initModel();
        blueleaves.initModel();
    }
}