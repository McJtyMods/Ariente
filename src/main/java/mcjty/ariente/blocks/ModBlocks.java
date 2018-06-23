package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BaseBlockBuilder;
import mcjty.lib.builder.BlockFlags;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ModBlocks {

    public static BlackTechBlock blackmarble_techpat;

    public static MarbleBlock marble;
    public static MarbleBlock marble_smooth;
    public static MarbleBlock marble_pilar;
    public static MarbleBlock marble_bricks;

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

        blackmarble_techpat = new BlackTechBlock("blacktech");
        marble = new MarbleBlock("marble");
        marble_smooth = new MarbleBlock("marble_smooth");
        marble_pilar = new MarbleBlock("marble_pilar");
        marble_bricks = new MarbleBlock("marble_bricks");


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
        blackmarble_techpat.initModel();
        marble.initModel();
        marble_smooth.initModel();
        marble_pilar.initModel();
        marble_bricks.initModel();

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