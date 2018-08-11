package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.aicore.AICoreTile;
import mcjty.ariente.blocks.decorative.*;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.blocks.generators.*;
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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
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

    public static BaseBlock guardDummy;
    public static BaseBlock soldierDummy;

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
    public static GenericBlock<PosiriteTankTile, GenericContainer> posiriteTankBlock;
    public static GenericBlock<PosiriteGeneratorTile, GenericContainer> posiriteGeneratorBlock;
    public static GenericBlock<StorageTile, GenericContainer> storageBlock;
    public static GenericBlock<ElevatorTile, GenericContainer> elevatorBlock;
    public static GenericBlock<LevelMarkerTile, GenericContainer> levelMarkerBlock;
    public static GenericBlock<DoorMarkerTile, GenericContainer> doorMarkerBlock;
    public static GenericBlock<InvisibleDoorTile, GenericContainer> invisibleDoorBlock;
    public static GenericBlock<ForceFieldTile, GenericContainer> forceFieldBlock;
    public static GenericBlock<AICoreTile, GenericContainer> aiCoreBlock;
    public static GenericBlock<WarperTile, GenericContainer> warperBlock;
    public static GenericBlock<LockTile, GenericContainer> lockBlock;

    public static BaseBlock flatLightBlock;

    public static NetCableBlock netCableBlock;
    public static ConnectorBlock connectorBlock;
    public static FacadeBlock facadeBlock;

    public static RampBlock rampBlock;

    public static GenericBlockBuilderFactory builderFactory;

    public static final AxisAlignedBB LIGHT_BLOCK_DOWN = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.2F, 1.0F);
    public static final AxisAlignedBB LIGHT_BLOCK_UP = new AxisAlignedBB(0.0F, 0.8F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB LIGHT_BLOCK_NORTH = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.2F);
    public static final AxisAlignedBB LIGHT_BLOCK_SOUTH = new AxisAlignedBB(0.0F, 0.0F, 0.8F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB LIGHT_BLOCK_WEST = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.2F, 1.0F, 1.0F);
    public static final AxisAlignedBB LIGHT_BLOCK_EAST = new AxisAlignedBB(0.8F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);


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
                .boundingBox((state, source, pos) -> getFlatBox(state))
                .build();

        guardDummy = new BaseBlockBuilder<>(Ariente.instance, "guard_dummy")
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .build();
        soldierDummy = new BaseBlockBuilder<>(Ariente.instance, "soldier_dummy")
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .build();

        lockBlock = builderFactory.<LockTile> builder("lock")
                .tileEntityClass(LockTile.class)
                .property(LockTile.LOCKED)
                .flags(BlockFlags.NON_OPAQUE)
                .property(LockTile.LOCKED)
                .boundingBox((state, source, pos) -> getFlatBox(state))
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.lock")
                .build();


        negariteGeneratorBlock = builderFactory.<NegariteGeneratorTile> builder("negarite_generator")
                .tileEntityClass(NegariteGeneratorTile.class)
                .container(NegariteGeneratorTile.CONTAINER_FACTORY)
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.RENDER_SOLID, BlockFlags.RENDER_CUTOUT)
                .property(NegariteGeneratorTile.WORKING)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.negarite_generator")
                .build();

        posiriteGeneratorBlock = builderFactory.<PosiriteGeneratorTile> builder("posirite_generator")
                .tileEntityClass(PosiriteGeneratorTile.class)
                .container(PosiriteGeneratorTile.CONTAINER_FACTORY)
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .flags(BlockFlags.REDSTONE_CHECK, BlockFlags.RENDER_SOLID, BlockFlags.RENDER_CUTOUT)
                .property(PosiriteGeneratorTile.WORKING)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.posirite_generator")
                .build();

        negariteTankBlock = builderFactory.<NegariteTankTile> builder("negarite_tank")
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

        posiriteTankBlock = builderFactory.<PosiriteTankTile> builder("posirite_tank")
                .tileEntityClass(PosiriteTankTile.class)
                .emptyContainer()
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.RENDER_SOLID, BlockFlags.RENDER_TRANSLUCENT)
                .property(PosiriteTankTile.LOWER)
                .property(PosiriteTankTile.UPPER)
                .rotationType(BaseBlock.RotationType.NONE)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.posirite_tank")
                .build();

        storageBlock = builderFactory.<StorageTile> builder("storage")
                .tileEntityClass(StorageTile.class)
                .emptyContainer()
                .flags(BlockFlags.RENDER_SOLID, BlockFlags.RENDER_TRANSLUCENT)
                .rotationType(BaseBlock.RotationType.ROTATION)
                .clickAction(StorageTile::onClick)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> StorageTile.onActivate(world, pos, player, side, hitX, hitY, hitZ))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.storage")
                .build();

        elevatorBlock = builderFactory.<ElevatorTile> builder("elevator")
                .tileEntityClass(ElevatorTile.class)
                .emptyContainer()
                .flags(BlockFlags.RENDER_SOLID, BlockFlags.RENDER_TRANSLUCENT)
                .rotationType(BaseBlock.RotationType.NONE)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.elevator")
                .build();

        levelMarkerBlock = builderFactory.<LevelMarkerTile> builder("level_marker")
                .tileEntityClass(LevelMarkerTile.class)
                .emptyContainer()
                .rotationType(BaseBlock.RotationType.NONE)
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.NON_FULLCUBE, BlockFlags.NO_COLLISION)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .boundingBox((state, source, pos) -> LevelMarkerTile.BLOCK_AABB)
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.level_marker")
                .build();

        doorMarkerBlock = builderFactory.<DoorMarkerTile> builder("door_marker")
                .tileEntityClass(DoorMarkerTile.class)
                .emptyContainer()
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.NON_FULLCUBE)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .addCollisionBoxToList(DoorMarkerTile::addCollisionBoxToList)
                .boundingBox((state, source, pos) -> DoorMarkerTile.BLOCK_AABB)
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.door_marker")
                .build();

        invisibleDoorBlock = builderFactory.<InvisibleDoorTile> builder("invisible_door")
                .tileEntityClass(InvisibleDoorTile.class)
                .emptyContainer()
                .rotationType(BaseBlock.RotationType.HORIZROTATION)
                .flags(BlockFlags.NON_OPAQUE, BlockFlags.NON_FULLCUBE)
                .addCollisionBoxToList(InvisibleDoorTile::addCollisionBoxToList)
                .build();

        forceFieldBlock = builderFactory.<ForceFieldTile> builder("forcefield")
                .tileEntityClass(ForceFieldTile.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_CHECK)
                .rotationType(BaseBlock.RotationType.NONE)
                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> HoloGuiHandler.openHoloGui(world, pos, player))
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.forcefield")
                .build();

        warperBlock = builderFactory.<WarperTile> builder("warper")
                .tileEntityClass(WarperTile.class)
                .emptyContainer()
                .rotationType(BaseBlock.RotationType.NONE)
                .info("message.ariente.shiftmessage")
                .infoExtended("message.ariente.warper")
                .build();

        aiCoreBlock = builderFactory.<AICoreTile> builder("aicore")
                .tileEntityClass(AICoreTile.class)
                .emptyContainer()
                .rotationType(BaseBlock.RotationType.NONE)
                .build();
        aiCoreBlock.setHardness(10.0f);
        aiCoreBlock.setHarvestLevel("pickaxe", 2);
    }

    private static AxisAlignedBB getFlatBox(IBlockState state) {
        EnumFacing facing = state.getValue(BaseBlock.FACING);
        switch (facing) {
            case UP:
                return LIGHT_BLOCK_DOWN;
            case DOWN:
                return LIGHT_BLOCK_UP;
            case SOUTH:
                return LIGHT_BLOCK_NORTH;
            case NORTH:
                return LIGHT_BLOCK_SOUTH;
            case EAST:
                return LIGHT_BLOCK_WEST;
            case WEST:
                return LIGHT_BLOCK_EAST;
        }
        return Block.FULL_BLOCK_AABB;
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

        doorMarkerBlock.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(DoorMarkerTile.class, new DoorMarkerRenderer());
        invisibleDoorBlock.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(InvisibleDoorTile.class, new InvisibleDoorRenderer());

        forceFieldBlock.initModel();

        flatLightBlock.initModel();
        posiriteGeneratorBlock.initModel();
        negariteGeneratorBlock.initModel();
        negariteTankBlock.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(NegariteTankTile.class, new NegariteTankRenderer());
        posiriteTankBlock.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(PosiriteTankTile.class, new PosiriteTankRenderer());

        warperBlock.initModel();
        lockBlock.initModel();

        aiCoreBlock.initModel();
        guardDummy.initModel();
        soldierDummy.initModel();

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