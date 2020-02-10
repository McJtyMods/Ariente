package mcjty.ariente.blocks;

import mcjty.ariente.blocks.aicore.AICoreTile;
import mcjty.ariente.blocks.decorative.*;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.blocks.generators.*;
import mcjty.ariente.blocks.utility.*;
import mcjty.ariente.blocks.utility.autofield.*;
import mcjty.ariente.blocks.utility.door.DoorMarkerTile;
import mcjty.ariente.blocks.utility.door.InvisibleDoorTile;
import mcjty.ariente.blocks.utility.wireless.SignalReceiverTile;
import mcjty.ariente.blocks.utility.wireless.SignalTransmitterTile;
import mcjty.ariente.blocks.utility.wireless.WirelessButtonTile;
import mcjty.ariente.blocks.utility.wireless.WirelessLockTile;
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

    public static final RegistryObject<BaseBlock> NEGARITE_GENERATOR = BLOCKS.register("negarite_generator", NegariteGeneratorTile::createBlock);
    public static final RegistryObject<TileEntityType<NegariteGeneratorTile>> NEGARITE_GENERATOR_TILE = TILES.register("negarite_generator", () -> TileEntityType.Builder.create(NegariteGeneratorTile::new, NEGARITE_GENERATOR.get()).build(null));

    public static final RegistryObject<BaseBlock> POSIRITE_GENERATOR = BLOCKS.register("posirite_generator", PosiriteGeneratorTile::createBlock);
    public static final RegistryObject<TileEntityType<PosiriteGeneratorTile>> POSIRITE_GENERATOR_TILE = TILES.register("posirite_generator", () -> TileEntityType.Builder.create(PosiriteGeneratorTile::new, POSIRITE_GENERATOR.get()).build(null));

    public static final RegistryObject<BaseBlock> NEGARITE_TANK = BLOCKS.register("negarite_tank", NegariteTankTile::createBlock);
    public static final RegistryObject<TileEntityType<NegariteTankTile>> NEGARITE_TANK_TILE = TILES.register("negarite_tank", () -> TileEntityType.Builder.create(NegariteTankTile::new, NEGARITE_TANK.get()).build(null));

    public static final RegistryObject<BaseBlock> POSIRITE_TANK = BLOCKS.register("posirite_tank", PosiriteTankTile::createBlock);
    public static final RegistryObject<TileEntityType<PosiriteTankTile>> POSIRITE_TANK_TILE = TILES.register("posirite_tank", () -> TileEntityType.Builder.create(PosiriteTankTile::new, POSIRITE_TANK.get()).build(null));

    public static final RegistryObject<BaseBlock> STORAGE_BLOCK = BLOCKS.register("storage", StorageTile::createBlock);
    public static final RegistryObject<TileEntityType<StorageTile>> STORAGE_TILE = TILES.register("storage", () -> TileEntityType.Builder.create(StorageTile::new, STORAGE_BLOCK.get()).build(null));

    public static final RegistryObject<BaseBlock> ELEVATOR_BLOCK = BLOCKS.register("elevator", ElevatorTile::createBlock);
    public static final RegistryObject<TileEntityType<ElevatorTile>> ELEVATOR_TILE = TILES.register("elevator", () -> TileEntityType.Builder.create(ElevatorTile::new, ELEVATOR_BLOCK.get()).build(null));

    public static final RegistryObject<BaseBlock> LEVEL_MARKER = BLOCKS.register("level_marker", LevelMarkerTile::createBlock);
    public static final RegistryObject<TileEntityType<LevelMarkerTile>> LEVEL_MARKER_TILE = TILES.register("level_marker", () -> TileEntityType.Builder.create(LevelMarkerTile::new, LEVEL_MARKER.get()).build(null));

    public static final RegistryObject<BaseBlock> DOOR_MARKER = BLOCKS.register("door_marker", DoorMarkerTile::createBlock);
    public static final RegistryObject<TileEntityType<DoorMarkerTile>> DOOR_MARKER_TILE = TILES.register("door_marker", () -> TileEntityType.Builder.create(DoorMarkerTile::new, DOOR_MARKER.get()).build(null));

    public static final RegistryObject<BaseBlock> INVISIBLE_DOOR = BLOCKS.register("invisible_door", InvisibleDoorTile::createBlock);
    public static final RegistryObject<TileEntityType<InvisibleDoorTile>> INVISIBLE_DOOR_TILE = TILES.register("invisible_door", () -> TileEntityType.Builder.create(InvisibleDoorTile::new, INVISIBLE_DOOR.get()).build(null));

    public static final RegistryObject<BaseBlock> FORCEFIELD = BLOCKS.register("forcefield", ForceFieldTile::createBlock);
    public static final RegistryObject<TileEntityType<ForceFieldTile>> FORCEFIELD_TILE = TILES.register("forcefield", () -> TileEntityType.Builder.create(ForceFieldTile::new, FORCEFIELD.get()).build(null));

    public static final RegistryObject<BaseBlock> AICORE = BLOCKS.register("aicore", AICoreTile::createBlock);
    public static final RegistryObject<TileEntityType<AICoreTile>> AICORE_TILE = TILES.register("aicore", () -> TileEntityType.Builder.create(AICoreTile::new, AICORE.get()).build(null));

    public static final RegistryObject<BaseBlock> WARPER = BLOCKS.register("warper", WarperTile::createBlock);
    public static final RegistryObject<TileEntityType<WarperTile>> WARPER_TILE = TILES.register("warper", () -> TileEntityType.Builder.create(WarperTile::new, WARPER.get()).build(null));

    public static final RegistryObject<BaseBlock> LOCK = BLOCKS.register("lock", LockTile::createBlock);
    public static final RegistryObject<TileEntityType<LockTile>> LOCK_TILE = TILES.register("lock", () -> TileEntityType.Builder.create(LockTile::new, LOCK.get()).build(null));

    public static final RegistryObject<BaseBlock> SIGNAL_RECEIVER = BLOCKS.register("signal_receiver", SignalReceiverTile::createBlock);
    public static final RegistryObject<TileEntityType<SignalReceiverTile>> SIGNAL_RECEIVER_TILE = TILES.register("signal_receiver", () -> TileEntityType.Builder.create(SignalReceiverTile::new, SIGNAL_RECEIVER.get()).build(null));

    public static final RegistryObject<BaseBlock> SIGNAL_TRANSMITTER = BLOCKS.register("signal_transmitter", SignalTransmitterTile::createBlock);
    public static final RegistryObject<TileEntityType<SignalTransmitterTile>> SIGNAL_TRANSMITTER_TILE = TILES.register("signal_transmitter", () -> TileEntityType.Builder.create(SignalTransmitterTile::new, SIGNAL_TRANSMITTER.get()).build(null));

    public static final RegistryObject<BaseBlock> WIRELESS_BUTTON = BLOCKS.register("wireless_button", WirelessButtonTile::createBlock);
    public static final RegistryObject<TileEntityType<WirelessButtonTile>> WIRELESS_BUTTON_TILE = TILES.register("wireless_button", () -> TileEntityType.Builder.create(WirelessButtonTile::new, WIRELESS_BUTTON.get()).build(null));

    public static final RegistryObject<BaseBlock> WIRELESS_LOCK = BLOCKS.register("wireless_lock", WirelessLockTile::createBlock);
    public static final RegistryObject<TileEntityType<WirelessLockTile>> WIRELESS_LOCK_TILE = TILES.register("wireless_lock", () -> TileEntityType.Builder.create(WirelessLockTile::new, WIRELESS_LOCK.get()).build(null));

    public static final RegistryObject<BaseBlock> ALARM = BLOCKS.register("alarm", AlarmTile::createBlock);
    public static final RegistryObject<TileEntityType<AlarmTile>> ALARM_TILE = TILES.register("alarm", () -> TileEntityType.Builder.create(AlarmTile::new, ALARM.get()).build(null));

    public static final RegistryObject<BaseBlock> CONSTRUCTOR = BLOCKS.register("constructor", ConstructorTile::createBlock);
    public static final RegistryObject<TileEntityType<ConstructorTile>> CONSTRUCTOR_TILE = TILES.register("constructor", () -> TileEntityType.Builder.create(ConstructorTile::new, CONSTRUCTOR.get()).build(null));

    public static final RegistryObject<BaseBlock> AUTO_CONSTRUCTOR = BLOCKS.register("auto_constructor", AutoConstructorTile::createBlock);
    public static final RegistryObject<TileEntityType<AutoConstructorTile>> AUTO_CONSTRUCTOR_TILE = TILES.register("auto_constructor", () -> TileEntityType.Builder.create(AutoConstructorTile::new, AUTO_CONSTRUCTOR.get()).build(null));

    public static final RegistryObject<BaseBlock> BLUEPRINT_STORAGE = BLOCKS.register("blueprint_storage", BlueprintStorageTile::createBlock);
    public static final RegistryObject<TileEntityType<BlueprintStorageTile>> BLUEPRINT_STORAGE_TILE = TILES.register("blueprint_storage", () -> TileEntityType.Builder.create(BlueprintStorageTile::new, BLUEPRINT_STORAGE.get()).build(null));

    public static final RegistryObject<BaseBlock> AUTOMATION_FIELD = BLOCKS.register("automation_field", AutoFieldTile::createBlock);
    public static final RegistryObject<TileEntityType<AutoFieldTile>> AUTOFIELD_TILE = TILES.register("automation_field", () -> TileEntityType.Builder.create(AutoFieldTile::new, AUTOMATION_FIELD.get()).build(null));

    public static final RegistryObject<BaseBlock> FLAT_LIGHT_BLOCK = BLOCKS.register("flatlight", () -> new BaseBlock(new BlockBuilder()
            .properties(Block.Properties.create(Material.GLASS).lightValue(15))));

    public static final RegistryObject<NetCableBlock> NETCABLE = BLOCKS.register("netcable", NetCableBlock::new);
    public static final RegistryObject<ConnectorBlock> CONNECTOR = BLOCKS.register("connector", ConnectorBlock::new);
    public static final RegistryObject<FacadeBlock> FACADE = BLOCKS.register("facade", FacadeBlock::new);

    public static final AxisAlignedBB LIGHT_BLOCK_DOWN = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 0.875F, 0.125F, 0.875F);
    public static final AxisAlignedBB LIGHT_BLOCK_UP = new AxisAlignedBB(0.125F, 0.875F, 0.125F, 0.875F, 1.0F, 0.875F);
    public static final AxisAlignedBB LIGHT_BLOCK_NORTH = new AxisAlignedBB(0.125F, 0.125F, 0.0F, 0.875F, 0.875F, 0.125F);
    public static final AxisAlignedBB LIGHT_BLOCK_SOUTH = new AxisAlignedBB(0.125F, 0.125F, 0.875F, 0.875F, 0.8750F, 1.0F);
    public static final AxisAlignedBB LIGHT_BLOCK_WEST = new AxisAlignedBB(0.0F, 0.125F, 0.125F, 0.125F, 0.875F, 0.8750F);
    public static final AxisAlignedBB LIGHT_BLOCK_EAST = new AxisAlignedBB(0.875F, 0.125F, 0.125F, 1.0F, 0.875F, 0.8750F);


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