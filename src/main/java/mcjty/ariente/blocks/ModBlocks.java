package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.decorative.*;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.blocks.generators.NegariteTankRenderer;
import mcjty.ariente.blocks.generators.NegariteTankTile;
import mcjty.ariente.blocks.generators.PosiriteGeneratorTile;
import mcjty.ariente.blocks.utility.*;
import mcjty.ariente.cables.ConnectorBlock;
import mcjty.ariente.cables.NetCableBlock;
import mcjty.ariente.facade.FacadeBlock;
import mcjty.ariente.gui.HoloGuiHandler;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.builder.BaseBlockBuilder;
import mcjty.lib.builder.BlockFlags;
import mcjty.lib.builder.GenericBlockBuilderFactory;
import mcjty.lib.container.GenericContainer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ModBlocks {

    public static BlackTechBlock blackmarble_techpat;
    public static PatternBlock patternBlock;

    public static MarbleBlock marble;
    public static MarbleBlock marble_smooth;
    public static MarbleBlock marble_pilar;
    public static MarbleBlock marble_bricks;

    public static MarbleSlabBlock marbleSlabBlock;
    public static DoubleMarbleSlabBlock doubleMarbleSlabBlock;

    public static BaseBlock lapisore;
    public static BaseBlock glowstoneore;
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

    public static GenericBlock<NegariteGeneratorTile, GenericContainer> negariteGeneratorBlock;
    public static GenericBlock<NegariteTankTile, GenericContainer> negariteTankBlock;
    public static GenericBlock<PosiriteGeneratorTile, GenericContainer> posiriteGeneratorBlock;
    public static GenericBlock<StorageTile, GenericContainer> storageBlock;
    public static GenericBlock<ElevatorTile, GenericContainer> elevatorBlock;
    public static GenericBlock<LevelMarkerTile, GenericContainer> levelMarkerBlock;
    public static GenericBlock<ForceFieldTile, GenericContainer> forceFieldBlock;
    public static BaseBlock flatLightBlock;

    public static NetCableBlock netCableBlock;
    public static ConnectorBlock connectorBlock;
    public static FacadeBlock facadeBlock;

    public static RampBlock rampBlock;

    public static GenericBlockBuilderFactory builderFactory;

    public static void init() {
        builderFactory = new GenericBlockBuilderFactory(Ariente.instance).creativeTabs(Ariente.creativeTab);

        initDecorative();
        initOres();
        initPlants();
        initTechnical();
    }

    private static void initTechnical() {
        netCableBlock = new NetCableBlock();
        connectorBlock = new ConnectorBlock();
        facadeBlock = new FacadeBlock();

        flatLightBlock = new BaseBlockBuilder<>(Ariente.instance, "flatlight")
                .creativeTabs(Ariente.creativeTab)
                .lightValue(15)
                .flags(BlockFlags.NON_OPAQUE)
                .build();

        negariteGeneratorBlock = ModBlocks.builderFactory.<NegariteGeneratorTile> builder("negarite_generator")
                .tileEntityClass(NegariteGeneratorTile.class)
                .container(NegariteGeneratorTile.CONTAINER_FACTORY)
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.RENDER_SOLID, BlockFlags.RENDER_CUTOUT)
                .property(NegariteGeneratorTile.WORKING)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.negarite_generator")
                .build();

        posiriteGeneratorBlock = ModBlocks.builderFactory.<PosiriteGeneratorTile> builder("posirite_generator")
                .tileEntityClass(PosiriteGeneratorTile.class)
                .container(PosiriteGeneratorTile.CONTAINER_FACTORY)
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.RENDER_SOLID, BlockFlags.RENDER_CUTOUT)
                .property(PosiriteGeneratorTile.WORKING)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.posirite_generator")
                .build();

        negariteTankBlock = ModBlocks.builderFactory.<NegariteTankTile> builder("negarite_tank")
                .tileEntityClass(NegariteTankTile.class)
                .emptyContainer()
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.RENDER_SOLID, BlockFlags.RENDER_TRANSLUCENT)
                .property(NegariteTankTile.LOWER)
                .property(NegariteTankTile.UPPER)
                .rotationType(BaseBlock.RotationType.NONE)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.negarite_tank")
                .build();

        storageBlock = ModBlocks.builderFactory.<StorageTile> builder("storage")
                .tileEntityClass(StorageTile.class)
                .emptyContainer()
                .flags(BlockFlags.RENDER_SOLID, BlockFlags.RENDER_TRANSLUCENT)
                .rotationType(BaseBlock.RotationType.ROTATION)
                .clickAction(StorageTile::onClick)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> StorageTile.onActivate(world, pos, player, side, hitX, hitY, hitZ))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.storage")
                .build();

        elevatorBlock = ModBlocks.builderFactory.<ElevatorTile> builder("elevator")
                .tileEntityClass(ElevatorTile.class)
                .emptyContainer()
                .flags(BlockFlags.RENDER_SOLID, BlockFlags.RENDER_TRANSLUCENT)
                .rotationType(BaseBlock.RotationType.NONE)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.elevator")
                .build();

        levelMarkerBlock = ModBlocks.builderFactory.<LevelMarkerTile> builder("level_marker")
                .tileEntityClass(LevelMarkerTile.class)
                .emptyContainer()
                .rotationType(BaseBlock.RotationType.NONE)
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.NON_FULLCUBE)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .boundingBox((state, source, pos) -> LevelMarkerTile.BLOCK_AABB)
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.level_marker")
                .build();

        forceFieldBlock = ModBlocks.builderFactory.<ForceFieldTile> builder("forcefield")
                .tileEntityClass(ForceFieldTile.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_CHECK)
                .rotationType(BaseBlock.RotationType.NONE)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.forcefield")
                .build();
    }

    private static void initPlants() {
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

    private static void initDecorative() {
        blackmarble_techpat = new BlackTechBlock("blacktech");
        patternBlock = new PatternBlock("pattern");
        marble = new MarbleBlock("marble");
        marble_smooth = new MarbleBlock("marble_smooth");
        marble_pilar = new MarbleBlock("marble_pilar");
        marble_bricks = new MarbleBlock("marble_bricks");
        marbleSlabBlock = new MarbleSlabBlock("marble_slab");
        doubleMarbleSlabBlock = new DoubleMarbleSlabBlock("double_marble_slab");
        rampBlock = new RampBlock("ramp");
    }

    private static void initOres() {
        lapisore = new BaseBlockBuilder<>(Ariente.instance, "lapisore")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .build();
        glowstoneore = new BaseBlockBuilder<>(Ariente.instance, "glowstoneore")
                .rotationType(BaseBlock.RotationType.NONE)
                .creativeTabs(Ariente.creativeTab)
                .lightValue(13)
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
    }

    public static void initOreDict() {
        OreDictionary.registerOre("oreLapis", lapisore);
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
        netCableBlock.initModel();
        connectorBlock.initModel();
        facadeBlock.initModel();

        storageBlock.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(StorageTile.class, new StorageRenderer());

        elevatorBlock.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(ElevatorTile.class, new ElevatorRenderer());
        levelMarkerBlock.initModel();

        forceFieldBlock.initModel();

        flatLightBlock.initModel();
        posiriteGeneratorBlock.initModel();
        negariteGeneratorBlock.initModel();
        negariteTankBlock.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(NegariteTankTile.class, new NegariteTankRenderer());

        blackmarble_techpat.initModel();
        patternBlock.initModel();
        rampBlock.initModel();
        marble.initModel();
        marble_smooth.initModel();
        marble_pilar.initModel();
        marble_bricks.initModel();
        marbleSlabBlock.initModel();
        doubleMarbleSlabBlock.initModel();

        lapisore.initModel();
        glowstoneore.initModel();
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

    @SideOnly(Side.CLIENT)
    public static void initItemModels() {
        facadeBlock.initItemModel();
        netCableBlock.initItemModel();
        connectorBlock.initItemModel();
    }

    @SideOnly(Side.CLIENT)
    public static void initColorHandlers(BlockColors blockColors) {
        facadeBlock.initColorHandler(blockColors);
        connectorBlock.initColorHandler(blockColors);
        netCableBlock.initColorHandler(blockColors);
    }
}