package mcjty.ariente.blocks;

import mcjty.ariente.blocks.decorative.*;
import mcjty.ariente.blocks.generators.PowerCombinerTile;
import mcjty.ariente.blocks.utility.BaseBeamBlock;
import mcjty.ariente.blocks.utility.autofield.*;
import mcjty.ariente.cables.ConnectorBlock;
import mcjty.ariente.cables.NetCableBlock;
import mcjty.ariente.facade.FacadeBlock;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.ariente.setup.Registration.BLOCKS;
import static mcjty.ariente.setup.Registration.TILES;

public class ModBlocks {

    public static final AxisAlignedBB FLAT_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);
    public static final AxisAlignedBB BEAM_BLOCK_NS_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.3D, 1.0D, 0.3D, 0.7D);
    public static final AxisAlignedBB BEAM_BLOCK_EW_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.0D, 0.7D, 0.3D, 1.0D);

    public static final RegistryObject<BlackTechBlock> BLACK_TECH_BLOCK = BLOCKS.register("blacktech", BlackTechBlock::new);
    public static final RegistryObject<MarbleTechBlock> MARBLE_TECH_BLOCK = BLOCKS.register("marbletech", MarbleTechBlock::new);
    public static final RegistryObject<PatternBlock> PATTERN_BLOCK = BLOCKS.register("pattern", PatternBlock::new);

    public static final RegistryObject<MarbleBlock> MARBLE = BLOCKS.register("marble", MarbleBlock::new);
    public static final RegistryObject<MarbleBlock> MARBLE_SMOOTH = BLOCKS.register("marble_smooth", MarbleBlock::new);
    public static final RegistryObject<MarbleBlock> MARBLE_PILAR = BLOCKS.register("marble_pilar", MarbleBlock::new);
    public static final RegistryObject<MarbleBlock> MARBLE_BRICKS = BLOCKS.register("marble_bricks", MarbleBlock::new);

    public static final RegistryObject<MarbleSlabBlock> MARBLE_SLAB_BLOCK = BLOCKS.register("marble_slab", MarbleSlabBlock::new);
    public static final RegistryObject<DoubleMarbleSlabBlock> DOUBLE_MARBLE_SLAB_BLOCK = BLOCKS.register("double_marble_slab", DoubleMarbleSlabBlock::new);

    public static final RegistryObject<BaseBlock> ORE_LITHIUM = BLOCKS.register("lithiumore", BaseOreBlock::new);
    public static final RegistryObject<BaseBlock> ORE_MANGANESE = BLOCKS.register("manganeseore", BaseOreBlock::new);
    public static final RegistryObject<BaseBlock> ORE_SILICON = BLOCKS.register("siliconore", BaseOreBlock::new);
    public static final RegistryObject<BaseBlock> ORE_SILVER = BLOCKS.register("silverore", BaseOreBlock::new);
    public static final RegistryObject<BaseBlock> ORE_PLATINUM = BLOCKS.register("platinumore", BaseOreBlock::new);
    public static final RegistryObject<BaseBlock> ORE_POSIRITE = BLOCKS.register("posirite", BaseOreBlock::new);
    public static final RegistryObject<BaseBlock> ORE_NEGARITE = BLOCKS.register("negarite", BaseOreBlock::new);

    public static final RegistryObject<BaseBlock> FLUX_BEAM_BLOCK = BLOCKS.register("flux_beam", () -> new BaseBeamBlock(new BlockBuilder()));
    public static final RegistryObject<BaseBlock> FLUX_BEND_BEAM_BLOCK = BLOCKS.register("flux_bend_beam", () -> new BaseBeamBlock(new BlockBuilder()));

    public static final RegistryObject<BaseBlock> SENSOR_ITEM_NODE = BLOCKS.register("sensor_item_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(SensorItemNodeTile::new)));
    public static final RegistryObject<TileEntityType<SensorItemNodeTile>> SENSOR_ITEM_TILE = TILES.register("sensor_item_node", () -> TileEntityType.Builder.create(SensorItemNodeTile::new, SENSOR_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> INPUT_ITEM_NODE = BLOCKS.register("input_item_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(InputItemNodeTile::new)));
    public static final RegistryObject<TileEntityType<InputItemNodeTile>> INPUT_ITEM_TILE = TILES.register("input_item_node", () -> TileEntityType.Builder.create(InputItemNodeTile::new, INPUT_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> OUTPUT_ITEM_NODE = BLOCKS.register("output_item_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(OutputItemNodeTile::new)));
    public static final RegistryObject<TileEntityType<OutputItemNodeTile>> OUTPUT_ITEM_TILE = TILES.register("output_item_node", () -> TileEntityType.Builder.create(OutputItemNodeTile::new, OUTPUT_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> ROUND_ROBIN_NODE = BLOCKS.register("round_robin_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(RoundRobinNodeTile::new)));
    public static final RegistryObject<TileEntityType<RoundRobinNodeTile>> ROUND_ROBIN_TILE = TILES.register("round_robin_node", () -> TileEntityType.Builder.create(RoundRobinNodeTile::new, ROUND_ROBIN_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> FIELD_MARKER = BLOCKS.register("field_marker", FieldMarkerTile::createBlock);
    public static final RegistryObject<TileEntityType<FieldMarkerTile>> FIELD_MARKER_TILE = TILES.register("field_marker", () -> TileEntityType.Builder.create(FieldMarkerTile::new, ROUND_ROBIN_NODE.get()).build(null));

    public static final RegistryObject<RampBlock> RAMP_BLOCK = BLOCKS.register("ramp", RampBlock::new);
    public static final RegistryObject<SlopeBlock> SLOPE_BLOCK = BLOCKS.register("slope", SlopeBlock::new);
    public static final RegistryObject<PaneBlock> GLANCE_FENCE = BLOCKS.register("glance_fence", () -> new PaneBlock(Material.GLASS, SoundType.GLASS)
            .setTransluscent(true));
    public static final RegistryObject<PaneBlock> BLUE_GLASS_FENCE = BLOCKS.register("blue_glass_fence", () -> new PaneBlock(Material.GLASS, SoundType.GLASS)
            .setTransluscent(true));
    public static final RegistryObject<PaneBlock> MARBLE_FENCE = BLOCKS.register("marble_fence", () -> new PaneBlock(Material.ROCK, SoundType.STONE));
    public static final RegistryObject<PaneBlock> TECH_FENCE = BLOCKS.register("tech_fence", () -> new PaneBlock(Material.ROCK, SoundType.STONE));

    public static final RegistryObject<BaseBlock> REINFORCED_MARBLE = BLOCKS.register("reinforced_marble", () -> new BaseBlock(new BlockBuilder()
            .properties(Block.Properties.create(Material.ROCK).hardnessAndResistance(80.0f, 3000.0f))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.lock")
    ) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });

    public static final RegistryObject<BaseBlock> FLUX_GLOW = BLOCKS.register("fluxglow", () -> new BaseBlock(new BlockBuilder()
        .properties(Block.Properties.create(Material.GLASS).lightValue(15))) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });

    public static final RegistryObject<BaseBlock> POWER_COMBINER = BLOCKS.register("power_combiner", PowerCombinerTile::createBlock);
    public static final RegistryObject<TileEntityType<PowerCombinerTile>> POWER_COMBINER_TILE = TILES.register("power_combiner", () -> TileEntityType.Builder.create(PowerCombinerTile::new, POWER_COMBINER.get()).build(null));

    public static final RegistryObject<BaseBlock> negariteGeneratorBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> negariteTankBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> posiriteTankBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> posiriteGeneratorBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> storageBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> elevatorBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> levelMarkerBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> doorMarkerBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> invisibleDoorBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> forceFieldBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> aiCoreBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> warperBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> lockBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> signalReceiverBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> signalTransmitterBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> wirelessButtonBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> wirelessLockBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> alarmBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> constructorBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> autoConstructorBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> blueprintStorageBlock = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> autoFieldBlock = BLOCKS.register("", null);

    public static final RegistryObject<BaseBlock> FLAT_LIGHT_BLOCK = BLOCKS.register("flatlight", () -> new BaseBlock(new BlockBuilder()
            .properties(Block.Properties.create(Material.GLASS).lightValue(15))));

    public static NetCableBlock netCableBlock;
    public static ConnectorBlock connectorBlock;
    public static FacadeBlock facadeBlock;

    public static final AxisAlignedBB LIGHT_BLOCK_DOWN = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 0.875F, 0.125F, 0.875F);
    public static final AxisAlignedBB LIGHT_BLOCK_UP = new AxisAlignedBB(0.125F, 0.875F, 0.125F, 0.875F, 1.0F, 0.875F);
    public static final AxisAlignedBB LIGHT_BLOCK_NORTH = new AxisAlignedBB(0.125F, 0.125F, 0.0F, 0.875F, 0.875F, 0.125F);
    public static final AxisAlignedBB LIGHT_BLOCK_SOUTH = new AxisAlignedBB(0.125F, 0.125F, 0.875F, 0.875F, 0.8750F, 1.0F);
    public static final AxisAlignedBB LIGHT_BLOCK_WEST = new AxisAlignedBB(0.0F, 0.125F, 0.125F, 0.125F, 0.875F, 0.8750F);
    public static final AxisAlignedBB LIGHT_BLOCK_EAST = new AxisAlignedBB(0.875F, 0.125F, 0.125F, 1.0F, 0.875F, 0.8750F);


    public static void init() {
    }

    private static void initTechnical() {
        netCableBlock = new NetCableBlock();
        connectorBlock = new ConnectorBlock();
        facadeBlock = new FacadeBlock();

        // @todo 1.14
//        fluxBeamBlock = new BaseBlockBuilder<>(Ariente.instance, "flux_beam")
//                .rotationType(HORIZROTATION)
//                .flags(NON_OPAQUE, NON_FULLCUBE, NO_COLLISION)
//                .boundingBox((state, source, pos) -> getBeamBox(state))
//                .build();
//
//        fluxBendBeamBlock = new BaseBlockBuilder<>(Ariente.instance, "flux_bend_beam")
//                .rotationType(HORIZROTATION)
//                .flags(NON_OPAQUE, NON_FULLCUBE, NO_COLLISION)
//                .boundingBox((state, source, pos) -> getBeamBox(state))
//                .build();
//
//        constructorBlock = builderFactory.<ConstructorTile> builder("constructor")
//                .tileEntityClass(ConstructorTile.class)
//                .rotationType(HORIZROTATION)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.constructor")
//                .build();
//
//        autoConstructorBlock = builderFactory.<AutoConstructorTile> builder("auto_constructor")
//                .tileEntityClass(AutoConstructorTile.class)
//                .container(AutoConstructorTile.CONTAINER_FACTORY)
//                .rotationType(HORIZROTATION)
//                .flags(REDSTONE_CHECK)
//                .property(AutoConstructorTile.WORKING)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.auto_constructor")
//                .build();
//
//        autoFieldBlock = builderFactory.<AutoFieldTile> builder("automation_field")
//                .tileEntityClass(AutoFieldTile.class)
//                .emptyContainer()
//                .rotationType(HORIZROTATION)
//                .flags(RENDER_SOLID, RENDER_TRANSLUCENT, REDSTONE_CHECK)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.automation_field")
//                .build();
//
//        blueprintStorageBlock = builderFactory.<BlueprintStorageTile> builder("blueprint_storage")
//                .tileEntityClass(BlueprintStorageTile.class)
//                .container(BlueprintStorageTile.CONTAINER_FACTORY)
//                .rotationType(HORIZROTATION)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.blueprint_storage")
//                .build();
//
//        alarmBlock = builderFactory.<AlarmTile> builder("alarm")
//                .tileEntityClass(AlarmTile.class)
//                .property(AlarmTile.ALARM)
//                .flags(NON_OPAQUE, NON_FULLCUBE, RENDER_SOLID, RENDER_CUTOUT)
//                .boundingBox((state, source, pos) -> getFlatBox(state))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.alarm")
//                .build();
//
//        lockBlock = builderFactory.<LockTile> builder("lock")
//                .tileEntityClass(LockTile.class)
//                .property(LockTile.LOCKED)
//                .flags(NON_OPAQUE, NON_FULLCUBE, RENDER_SOLID, RENDER_CUTOUT)
//                .boundingBox((state, source, pos) -> getFlatBox(state))
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.lock")
//                .build();
//
//        wirelessLockBlock = builderFactory.<WirelessLockTile> builder("wireless_lock")
//                .tileEntityClass(WirelessLockTile.class)
//                .property(WirelessLockTile.LOCKED)
//                .flags(NON_OPAQUE, NON_FULLCUBE, RENDER_SOLID, RENDER_CUTOUT)
//                .boundingBox((state, source, pos) -> getFlatBox(state))
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.wireless_lock")
//                .infoExtendedParameter(ItemStackTools.intGetter("channel", -1))
//                .build();
//
//        wirelessButtonBlock = builderFactory.<WirelessButtonTile> builder("wireless_button")
//                .tileEntityClass(WirelessButtonTile.class)
//                .property(WirelessButtonTile.POWER)
//                .flags(NON_OPAQUE, NON_FULLCUBE, RENDER_SOLID, RENDER_CUTOUT)
//                .boundingBox((state, source, pos) -> getFlatBox(state))
//                .activateAction(WirelessButtonTile::onBlockActivatedWithToggle)
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.wireless_button")
//                .infoExtendedParameter(ItemStackTools.intGetter("channel", -1))
//                .build();
//
//        signalReceiverBlock = builderFactory.<SignalReceiverTile> builder("signal_receiver")
//                .tileEntityClass(SignalReceiverTile.class)
//                .property(SignalChannelTileEntity.POWER)
//                .flags(NON_OPAQUE, NON_FULLCUBE, REDSTONE_OUTPUT, RENDER_SOLID, RENDER_CUTOUT)
//                .boundingBox((state, source, pos) -> getFlatBox(state))
//                .activateAction(SignalChannelTileEntity::onBlockActivated)
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.signal_receiver")
//                .infoExtendedParameter(ItemStackTools.intGetter("channel", -1))
//                .build();
//
//        signalTransmitterBlock = builderFactory.<SignalTransmitterTile> builder("signal_transmitter")
//                .tileEntityClass(SignalTransmitterTile.class)
//                .property(SignalChannelTileEntity.POWER)
//                .flags(NON_OPAQUE, NON_FULLCUBE, REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
//                .boundingBox((state, source, pos) -> getFlatBox(state))
//                .activateAction(SignalChannelTileEntity::onBlockActivated)
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.signal_transmitter")
//                .infoExtendedParameter(ItemStackTools.intGetter("channel", -1))
//                .build();
//
//        negariteGeneratorBlock = builderFactory.<NegariteGeneratorTile> builder("negarite_generator")
//                .tileEntityClass(NegariteGeneratorTile.class)
//                .container(NegariteGeneratorTile.CONTAINER_FACTORY)
//                .rotationType(HORIZROTATION)
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
//                .property(NegariteGeneratorTile.WORKING)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.negarite_generator")
//                .build();
//
//        posiriteGeneratorBlock = builderFactory.<PosiriteGeneratorTile> builder("posirite_generator")
//                .tileEntityClass(PosiriteGeneratorTile.class)
//                .container(PosiriteGeneratorTile.CONTAINER_FACTORY)
//                .rotationType(HORIZROTATION)
//                .flags(REDSTONE_CHECK, RENDER_SOLID, RENDER_CUTOUT)
//                .property(PosiriteGeneratorTile.WORKING)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.posirite_generator")
//                .build();
//
//        negariteTankBlock = builderFactory.<NegariteTankTile> builder("negarite_tank")
//                .tileEntityClass(NegariteTankTile.class)
//                .emptyContainer()
//                .flags(NON_OPAQUE, RENDER_SOLID, RENDER_TRANSLUCENT)
//                .property(NegariteTankTile.LOWER)
//                .property(NegariteTankTile.UPPER)
//                .rotationType(NONE)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.negarite_tank")
//                .build();
//
//        posiriteTankBlock = builderFactory.<PosiriteTankTile> builder("posirite_tank")
//                .tileEntityClass(PosiriteTankTile.class)
//                .emptyContainer()
//                .flags(NON_OPAQUE, RENDER_SOLID, RENDER_TRANSLUCENT)
//                .property(PosiriteTankTile.LOWER)
//                .property(PosiriteTankTile.UPPER)
//                .rotationType(NONE)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.posirite_tank")
//                .build();
//
//        storageBlock = builderFactory.<StorageTile> builder("storage")
//                .tileEntityClass(StorageTile.class)
//                .emptyContainer()
////                .property(StorageTile.LOCKED)
//                .flags(RENDER_SOLID, RENDER_TRANSLUCENT)
//                .rotationType(BaseBlock.RotationType.ROTATION)
//                .clickAction(StorageTile::onClick)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> StorageTile.onActivate(world, pos, player, side, hitX, hitY, hitZ))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.storage")
//                .build();
//
//        elevatorBlock = builderFactory.<ElevatorTile> builder("elevator")
//                .tileEntityClass(ElevatorTile.class)
//                .emptyContainer()
//                .flags(RENDER_SOLID, RENDER_TRANSLUCENT)
//                .rotationType(NONE)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.elevator")
//                .build();
//
//        levelMarkerBlock = builderFactory.<LevelMarkerTile> builder("level_marker")
//                .tileEntityClass(LevelMarkerTile.class)
//                .emptyContainer()
//                .rotationType(NONE)
//                .flags(NON_OPAQUE, NON_FULLCUBE, NO_COLLISION)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .boundingBox((state, source, pos) -> FLAT_BLOCK_AABB)
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.level_marker")
//                .build();
//
//        doorMarkerBlock = builderFactory.<DoorMarkerTile> builder("door_marker")
//                .tileEntityClass(DoorMarkerTile.class)
//                .emptyContainer()
//                .rotationType(HORIZROTATION)
//                .flags(NON_OPAQUE, NON_FULLCUBE)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .addCollisionBoxToList(DoorMarkerTile::addCollisionBoxToList)
//                .boundingBox(DoorMarkerTile::getCollisionBoundingBox)
//                .getAIPathNodeType(DoorMarkerTile::getAiPathNodeType)
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.door_marker")
//                .build();
//
//        invisibleDoorBlock = builderFactory.<InvisibleDoorTile> builder("invisible_door")
//                .tileEntityClass(InvisibleDoorTile.class)
//                .emptyContainer()
//                .rotationType(HORIZROTATION)
//                .flags(NON_OPAQUE, NON_FULLCUBE)
//                .addCollisionBoxToList(InvisibleDoorTile::addCollisionBoxToList)
//                .boundingBox(InvisibleDoorTile::getCollisionBoundingBox)
//                .getAIPathNodeType(InvisibleDoorTile::getAiPathNodeType)
//                .build();
//
//        forceFieldBlock = builderFactory.<ForceFieldTile> builder("forcefield")
//                .tileEntityClass(ForceFieldTile.class)
//                .emptyContainer()
//                .flags(REDSTONE_CHECK)
//                .rotationType(NONE)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.forcefield")
//                .build();
//
//        warperBlock = builderFactory.<WarperTile> builder("warper")
//                .tileEntityClass(WarperTile.class)
//                .emptyContainer()
//                .rotationType(NONE)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .lightValue(8)
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.warper")
//                .build();
//
//        aiCoreBlock = builderFactory.<AICoreTile> builder("aicore")
//                .tileEntityClass(AICoreTile.class)
//                .emptyContainer()
//                .rotationType(NONE)
//                .build();
//        aiCoreBlock.setHardness(20.0f).setResistance(800);
//        aiCoreBlock.setHarvestLevel("pickaxe", 2);
    }

//    public static AxisAlignedBB getFlatBox(BlockState state) {
//        Direction facing = state.get(BaseBlock.FACING);
//        switch (facing) {
//            case UP:
//                return LIGHT_BLOCK_DOWN;
//            case DOWN:
//                return LIGHT_BLOCK_UP;
//            case SOUTH:
//                return LIGHT_BLOCK_NORTH;
//            case NORTH:
//                return LIGHT_BLOCK_SOUTH;
//            case EAST:
//                return LIGHT_BLOCK_WEST;
//            case WEST:
//                return LIGHT_BLOCK_EAST;
//        }
//        return Block.FULL_BLOCK_AABB;
//    }

//    public static AxisAlignedBB getBeamBox(BlockState state) {
//        Direction facing = state.getValue(BaseBlock.FACING_HORIZ);
//        switch (facing) {
//            case SOUTH:
//                return BEAM_BLOCK_NS_AABB;
//            case NORTH:
//                return BEAM_BLOCK_NS_AABB;
//            case EAST:
//                return BEAM_BLOCK_EW_AABB;
//            case WEST:
//                return BEAM_BLOCK_EW_AABB;
//        }
//        return Block.FULL_BLOCK_AABB;
//    }


    public static void initOreDict() {
        // @todo 1.14
//        OreDictionary.registerOre("blockMarble", marble);
//        OreDictionary.registerOre("oreSilver", silverore);
//        OreDictionary.registerOre("orePlatinum", platinumore);
//        OreDictionary.registerOre("oreSilicon", siliconore);
//        OreDictionary.registerOre("oreManganese", manganeseore);
//        OreDictionary.registerOre("oreLithium", lithiumore);
//        OreDictionary.registerOre("oreNegarite", negarite);
//        OreDictionary.registerOre("orePosirite", posirite);
    }

    // @todo 1.14
//    public static void initModels() {
//        netCableBlock.initModel();
//        connectorBlock.initModel();
//        facadeBlock.initModel();
//
//        fluxBeamBlock.initModel();
//        fluxBendBeamBlock.initModel();
//
//        storageBlock.initModel();
//        StorageRenderer.register();
//
//        elevatorBlock.initModel();
//        ElevatorRenderer.register();
//        levelMarkerBlock.initModel();
//
//        doorMarkerBlock.initModel();
//        DoorMarkerRenderer.register();
//        invisibleDoorBlock.initModel();
//        InvisibleDoorRenderer.register();
//
//        forceFieldBlock.initModel();
//
//        roundRobinNode.initModel();
//        inputItemNode.initModel();
//        sensorItemNode.initModel();
//        outputItemNode.initModel();
//        fieldMarker.initModel();
//        autoFieldBlock.initModel();
//        AutoFieldRenderer.register();
//
//        constructorBlock.initModel();
//        autoConstructorBlock.initModel();
//        blueprintStorageBlock.initModel();
//        flatLightBlock.initModel();
//        fluxGlow.initModel();
//        powerCombinerBlock.initModel();
//        posiriteGeneratorBlock.initModel();
//        negariteGeneratorBlock.initModel();
//        negariteTankBlock.initModel();
//        NegariteTankRenderer.register();
//        posiriteTankBlock.initModel();
//        PosiriteTankRenderer.register();
//
//        alarmBlock.initModel();
//        warperBlock.initModel();
//        WarperRenderer.register();
//        lockBlock.initModel();
//        wirelessButtonBlock.initModel();
//        wirelessLockBlock.initModel();
//        signalReceiverBlock.initModel();
//        signalTransmitterBlock.initModel();
//
//        reinforcedMarble.initModel();
//        aiCoreBlock.initModel();
//
//        blackTechBlock.initModel();
//        marbleTechBlock.initModel();
//        patternBlock.initModel();
//        rampBlock.initModel();
//        slopeBlock.initModel();
//        marble.initModel();
//        marble_smooth.initModel();
//        marble_pilar.initModel();
//        marble_bricks.initModel();
//        marbleSlabBlock.initModel();
//        doubleMarbleSlabBlock.initModel();
//        blueGlassFence.initModel();
//        glassFence.initModel();
//        marbleFence.initModel();
//        techFence.initModel();
//
//        lithiumore.initModel();
//        manganeseore.initModel();
//        siliconore.initModel();
//        silverore.initModel();
//        platinumore.initModel();
//        posirite.initModel();
//        negarite.initModel();
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void initItemModels() {
//        facadeBlock.initItemModel();
//        netCableBlock.initItemModel();
//        connectorBlock.initItemModel();
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void initColorHandlers(BlockColors blockColors) {
//        facadeBlock.initColorHandler(blockColors);
//        connectorBlock.initColorHandler(blockColors);
//        netCableBlock.initColorHandler(blockColors);
//    }
}