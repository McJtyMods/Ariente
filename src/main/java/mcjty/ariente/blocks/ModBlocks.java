package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
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
import mcjty.ariente.items.*;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.ariente.setup.Registration.*;

public class ModBlocks {

    public static final AxisAlignedBB FLAT_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);
    public static final AxisAlignedBB BEAM_BLOCK_NS_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.3D, 1.0D, 0.3D, 0.7D);
    public static final AxisAlignedBB BEAM_BLOCK_EW_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.0D, 0.7D, 0.3D, 1.0D);

    public static final AxisAlignedBB LIGHT_BLOCK_DOWN = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 0.875F, 0.125F, 0.875F);
    public static final AxisAlignedBB LIGHT_BLOCK_UP = new AxisAlignedBB(0.125F, 0.875F, 0.125F, 0.875F, 1.0F, 0.875F);
    public static final AxisAlignedBB LIGHT_BLOCK_NORTH = new AxisAlignedBB(0.125F, 0.125F, 0.0F, 0.875F, 0.875F, 0.125F);
    public static final AxisAlignedBB LIGHT_BLOCK_SOUTH = new AxisAlignedBB(0.125F, 0.125F, 0.875F, 0.875F, 0.8750F, 1.0F);
    public static final AxisAlignedBB LIGHT_BLOCK_WEST = new AxisAlignedBB(0.0F, 0.125F, 0.125F, 0.125F, 0.875F, 0.8750F);
    public static final AxisAlignedBB LIGHT_BLOCK_EAST = new AxisAlignedBB(0.875F, 0.125F, 0.125F, 1.0F, 0.875F, 0.8750F);

    public static final RegistryObject<BlackTechBlock> BLACK_TECH_BLOCK = BLOCKS.register("blacktech", BlackTechBlock::new);
    public static final RegistryObject<Item> BLACK_TECH_BLOCK_ITEM = ITEMS.register("blacktech", () -> new BlockItem(BLACK_TECH_BLOCK.get(), createStandardProperties()));

    public static final RegistryObject<MarbleTechBlock> MARBLE_TECH_BLOCK = BLOCKS.register("marbletech", MarbleTechBlock::new);
    public static final RegistryObject<Item> MARBLE_TECH_BLOCK_ITEM = ITEMS.register("marbletech", () -> new BlockItem(MARBLE_TECH_BLOCK.get(), createStandardProperties()));

    public static final RegistryObject<PatternBlock> PATTERN_BLOCK = BLOCKS.register("pattern", PatternBlock::new);
    public static final RegistryObject<Item> PATTERN_BLOCK_ITEM = ITEMS.register("pattern", () -> new BlockItem(PATTERN_BLOCK.get(), createStandardProperties()));

    public static final RegistryObject<MarbleBlock> MARBLE = BLOCKS.register("marble", MarbleBlock::new);
    public static final RegistryObject<Item> MARBLE_ITEM = ITEMS.register("marble", () -> new BlockItem(MARBLE.get(), createStandardProperties()));

    public static final RegistryObject<MarbleBlock> MARBLE_SMOOTH = BLOCKS.register("marble_smooth", MarbleBlock::new);
    public static final RegistryObject<Item> MARBLE_SMOOTH_ITEM = ITEMS.register("marble_smooth", () -> new BlockItem(MARBLE_SMOOTH.get(), createStandardProperties()));

    public static final RegistryObject<MarbleBlock> MARBLE_PILAR = BLOCKS.register("marble_pilar", MarbleBlock::new);
    public static final RegistryObject<Item> MARBLE_PILAR_ITEM = ITEMS.register("marble_pilar", () -> new BlockItem(MARBLE_PILAR.get(), createStandardProperties()));

    public static final RegistryObject<MarbleBlock> MARBLE_BRICKS = BLOCKS.register("marble_bricks", MarbleBlock::new);
    public static final RegistryObject<Item> MARBLE_BRICKS_ITEM = ITEMS.register("marble_bricks", () -> new BlockItem(MARBLE_BRICKS.get(), createStandardProperties()));

    public static final RegistryObject<MarbleSlabBlock> MARBLE_SLAB = BLOCKS.register("marble_slab", MarbleSlabBlock::new);
    public static final RegistryObject<Item> MARBLE_SLAB_ITEM = ITEMS.register("marble_slab", () -> new BlockItem(MARBLE_SLAB.get(), createStandardProperties()));

    public static final RegistryObject<DoubleMarbleSlabBlock> DOUBLE_MARBLE_SLAB = BLOCKS.register("double_marble_slab", DoubleMarbleSlabBlock::new);
    public static final RegistryObject<Item> DOUBLE_MARBLE_SLAB_ITEM = ITEMS.register("double_marble_slab", () -> new BlockItem(DOUBLE_MARBLE_SLAB.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> ORE_LITHIUM = BLOCKS.register("lithiumore", BaseOreBlock::new);
    public static final RegistryObject<Item> ORE_LITHIUM_ITEM = ITEMS.register("lithiumore", () -> new BlockItem(ORE_LITHIUM.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> ORE_MANGANESE = BLOCKS.register("manganeseore", BaseOreBlock::new);
    public static final RegistryObject<Item> ORE_MANGANESE_ITEM = ITEMS.register("manganeseore", () -> new BlockItem(ORE_MANGANESE.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> ORE_SILICON = BLOCKS.register("siliconore", BaseOreBlock::new);
    public static final RegistryObject<Item> ORE_SILICON_ITEM = ITEMS.register("siliconore", () -> new BlockItem(ORE_SILICON.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> ORE_SILVER = BLOCKS.register("silverore", BaseOreBlock::new);
    public static final RegistryObject<Item> ORE_SILVER_ITEM = ITEMS.register("silverore", () -> new BlockItem(ORE_SILVER.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> ORE_PLATINUM = BLOCKS.register("platinumore", BaseOreBlock::new);
    public static final RegistryObject<Item> ORE_PLATINUM_ITEM = ITEMS.register("platinumore", () -> new BlockItem(ORE_PLATINUM.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> ORE_POSIRITE = BLOCKS.register("posirite", BaseOreBlock::new);
    public static final RegistryObject<Item> ORE_POSIRITE_ITEM = ITEMS.register("posirite", () -> new BlockItem(ORE_POSIRITE.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> ORE_NEGARITE = BLOCKS.register("negarite", BaseOreBlock::new);
    public static final RegistryObject<Item> ORE_NEGARITE_ITEM = ITEMS.register("negarite", () -> new BlockItem(ORE_NEGARITE.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> FLUX_BEAM = BLOCKS.register("flux_beam", () -> new BaseBeamBlock(new BlockBuilder()));
    public static final RegistryObject<Item> FLUX_BEAM_ITEM = ITEMS.register("flux_beam", () -> new BlockItem(FLUX_BEAM.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> FLUX_BEND_BEAM = BLOCKS.register("flux_bend_beam", () -> new BaseBeamBlock(new BlockBuilder()));
    public static final RegistryObject<Item> FLUX_BEND_BEAM_ITEM = ITEMS.register("flux_bend_beam", () -> new BlockItem(FLUX_BEND_BEAM.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> SENSOR_ITEM_NODE = BLOCKS.register("sensor_item_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(SensorItemNodeTile::new)));
    public static final RegistryObject<Item> SENSOR_ITEM_NODE_ITEM = ITEMS.register("sensor_item_node", () -> new BlockItem(SENSOR_ITEM_NODE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<SensorItemNodeTile>> SENSOR_ITEM_TILE = TILES.register("sensor_item_node", () -> TileEntityType.Builder.create(SensorItemNodeTile::new, SENSOR_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> INPUT_ITEM_NODE = BLOCKS.register("input_item_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(InputItemNodeTile::new)));
    public static final RegistryObject<Item> INPUT_ITEM_NODE_ITEM = ITEMS.register("input_item_node", () -> new BlockItem(INPUT_ITEM_NODE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<InputItemNodeTile>> INPUT_ITEM_TILE = TILES.register("input_item_node", () -> TileEntityType.Builder.create(InputItemNodeTile::new, INPUT_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> OUTPUT_ITEM_NODE = BLOCKS.register("output_item_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(OutputItemNodeTile::new)));
    public static final RegistryObject<Item> OUTPUT_ITEM_NODE_ITEM = ITEMS.register("output_item_node", () -> new BlockItem(OUTPUT_ITEM_NODE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<OutputItemNodeTile>> OUTPUT_ITEM_TILE = TILES.register("output_item_node", () -> TileEntityType.Builder.create(OutputItemNodeTile::new, OUTPUT_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> ROUND_ROBIN_NODE = BLOCKS.register("round_robin_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(RoundRobinNodeTile::new)));
    public static final RegistryObject<Item> ROUND_ROBIN_NODE_ITEM = ITEMS.register("round_robin_node", () -> new BlockItem(ROUND_ROBIN_NODE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<RoundRobinNodeTile>> ROUND_ROBIN_TILE = TILES.register("round_robin_node", () -> TileEntityType.Builder.create(RoundRobinNodeTile::new, ROUND_ROBIN_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> FIELD_MARKER = BLOCKS.register("field_marker", FieldMarkerTile::createBlock);
    public static final RegistryObject<Item> FIELD_MARKER_ITEM = ITEMS.register("field_marker", () -> new BlockItem(FIELD_MARKER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<FieldMarkerTile>> FIELD_MARKER_TILE = TILES.register("field_marker", () -> TileEntityType.Builder.create(FieldMarkerTile::new, ROUND_ROBIN_NODE.get()).build(null));

    public static final RegistryObject<RampBlock> RAMP = BLOCKS.register("ramp", RampBlock::new);
    public static final RegistryObject<Item> RAMP_ITEM = ITEMS.register("ramp", () -> new BlockItem(RAMP.get(), createStandardProperties()));

    public static final RegistryObject<SlopeBlock> SLOPE = BLOCKS.register("slope", SlopeBlock::new);
    public static final RegistryObject<Item> SLOPE_ITEM = ITEMS.register("slope", () -> new BlockItem(SLOPE.get(), createStandardProperties()));

    public static final RegistryObject<PaneBlock> GLASS_FENCE = BLOCKS.register("glass_fence", () -> new PaneBlock(Material.GLASS, SoundType.GLASS)
            .setTransluscent(true));
    public static final RegistryObject<Item> GLASS_FENCE_ITEM = ITEMS.register("glass_fence", () -> new BlockItem(GLASS_FENCE.get(), createStandardProperties()));

    public static final RegistryObject<PaneBlock> BLUE_GLASS_FENCE = BLOCKS.register("blue_glass_fence", () -> new PaneBlock(Material.GLASS, SoundType.GLASS)
            .setTransluscent(true));
    public static final RegistryObject<Item> BLUE_GLASS_FENCE_ITEM = ITEMS.register("blue_glass_fence", () -> new BlockItem(BLUE_GLASS_FENCE.get(), createStandardProperties()));

    public static final RegistryObject<PaneBlock> MARBLE_FENCE = BLOCKS.register("marble_fence", () -> new PaneBlock(Material.ROCK, SoundType.STONE));
    public static final RegistryObject<Item> MARBLE_FENCE_ITEM = ITEMS.register("marble_fence", () -> new BlockItem(MARBLE_FENCE.get(), createStandardProperties()));

    public static final RegistryObject<PaneBlock> TECH_FENCE = BLOCKS.register("tech_fence", () -> new PaneBlock(Material.ROCK, SoundType.STONE));
    public static final RegistryObject<Item> TECH_FENCE_ITEM = ITEMS.register("tech_fence", () -> new BlockItem(TECH_FENCE.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> REINFORCED_MARBLE = BLOCKS.register("reinforced_marble", () -> new BaseBlock(new BlockBuilder()
            .properties(Block.Properties.create(Material.ROCK).hardnessAndResistance(80.0f, 3000.0f))
    ) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });
    public static final RegistryObject<Item> REINFORCED_MARBLE_ITEM = ITEMS.register("reinforced_marble", () -> new BlockItem(REINFORCED_MARBLE.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> FLUX_GLOW = BLOCKS.register("fluxglow", () -> new BaseBlock(new BlockBuilder()
        .properties(Block.Properties.create(Material.GLASS).lightValue(15))) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });
    public static final RegistryObject<Item> FLUX_GLOW_ITEM = ITEMS.register("fluxglow", () -> new BlockItem(FLUX_GLOW.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> POWER_COMBINER = BLOCKS.register("power_combiner", PowerCombinerTile::createBlock);
    public static final RegistryObject<Item> POWER_COMBINER_ITEM = ITEMS.register("power_combiner", () -> new BlockItem(POWER_COMBINER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<PowerCombinerTile>> POWER_COMBINER_TILE = TILES.register("power_combiner", () -> TileEntityType.Builder.create(PowerCombinerTile::new, POWER_COMBINER.get()).build(null));

    public static final RegistryObject<BaseBlock> NEGARITE_GENERATOR = BLOCKS.register("negarite_generator", NegariteGeneratorTile::createBlock);
    public static final RegistryObject<Item> NEGARITE_GENERATOR_ITEM = ITEMS.register("negarite_generator", () -> new BlockItem(NEGARITE_GENERATOR.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<NegariteGeneratorTile>> NEGARITE_GENERATOR_TILE = TILES.register("negarite_generator", () -> TileEntityType.Builder.create(NegariteGeneratorTile::new, NEGARITE_GENERATOR.get()).build(null));

    public static final RegistryObject<BaseBlock> POSIRITE_GENERATOR = BLOCKS.register("posirite_generator", PosiriteGeneratorTile::createBlock);
    public static final RegistryObject<Item> POSIRITE_GENERATOR_ITEM = ITEMS.register("posirite_generator", () -> new BlockItem(POSIRITE_GENERATOR.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<PosiriteGeneratorTile>> POSIRITE_GENERATOR_TILE = TILES.register("posirite_generator", () -> TileEntityType.Builder.create(PosiriteGeneratorTile::new, POSIRITE_GENERATOR.get()).build(null));

    public static final RegistryObject<BaseBlock> NEGARITE_TANK = BLOCKS.register("negarite_tank", NegariteTankTile::createBlock);
    public static final RegistryObject<Item> NEGARITE_TANK_ITEM = ITEMS.register("negarite_tank", () -> new BlockItem(NEGARITE_TANK.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<NegariteTankTile>> NEGARITE_TANK_TILE = TILES.register("negarite_tank", () -> TileEntityType.Builder.create(NegariteTankTile::new, NEGARITE_TANK.get()).build(null));

    public static final RegistryObject<BaseBlock> POSIRITE_TANK = BLOCKS.register("posirite_tank", PosiriteTankTile::createBlock);
    public static final RegistryObject<Item> POSIRITE_TANK_ITEM = ITEMS.register("posirite_tank", () -> new BlockItem(POSIRITE_TANK.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<PosiriteTankTile>> POSIRITE_TANK_TILE = TILES.register("posirite_tank", () -> TileEntityType.Builder.create(PosiriteTankTile::new, POSIRITE_TANK.get()).build(null));

    public static final RegistryObject<BaseBlock> STORAGE = BLOCKS.register("storage", StorageTile::createBlock);
    public static final RegistryObject<Item> STORAGE_ITEM = ITEMS.register("storage", () -> new BlockItem(STORAGE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<StorageTile>> STORAGE_TILE = TILES.register("storage", () -> TileEntityType.Builder.create(StorageTile::new, STORAGE.get()).build(null));

    public static final RegistryObject<BaseBlock> ELEVATOR = BLOCKS.register("elevator", ElevatorTile::createBlock);
    public static final RegistryObject<Item> ELEVATOR_ITEM = ITEMS.register("elevator", () -> new BlockItem(ELEVATOR.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<ElevatorTile>> ELEVATOR_TILE = TILES.register("elevator", () -> TileEntityType.Builder.create(ElevatorTile::new, ELEVATOR.get()).build(null));

    public static final RegistryObject<BaseBlock> LEVEL_MARKER = BLOCKS.register("level_marker", LevelMarkerTile::createBlock);
    public static final RegistryObject<Item> LEVEL_MARKER_ITEM = ITEMS.register("level_marker", () -> new BlockItem(LEVEL_MARKER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<LevelMarkerTile>> LEVEL_MARKER_TILE = TILES.register("level_marker", () -> TileEntityType.Builder.create(LevelMarkerTile::new, LEVEL_MARKER.get()).build(null));

    public static final RegistryObject<BaseBlock> DOOR_MARKER = BLOCKS.register("door_marker", DoorMarkerTile::createBlock);
    public static final RegistryObject<Item> DOOR_MARKER_ITEM = ITEMS.register("door_marker", () -> new BlockItem(DOOR_MARKER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<DoorMarkerTile>> DOOR_MARKER_TILE = TILES.register("door_marker", () -> TileEntityType.Builder.create(DoorMarkerTile::new, DOOR_MARKER.get()).build(null));

    public static final RegistryObject<BaseBlock> INVISIBLE_DOOR = BLOCKS.register("invisible_door", InvisibleDoorTile::createBlock);
    public static final RegistryObject<TileEntityType<InvisibleDoorTile>> INVISIBLE_DOOR_TILE = TILES.register("invisible_door", () -> TileEntityType.Builder.create(InvisibleDoorTile::new, INVISIBLE_DOOR.get()).build(null));

    public static final RegistryObject<BaseBlock> FORCEFIELD = BLOCKS.register("forcefield", ForceFieldTile::createBlock);
    public static final RegistryObject<Item> FORCEFIELD_ITEM = ITEMS.register("forcefield", () -> new BlockItem(FORCEFIELD.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<ForceFieldTile>> FORCEFIELD_TILE = TILES.register("forcefield", () -> TileEntityType.Builder.create(ForceFieldTile::new, FORCEFIELD.get()).build(null));

    public static final RegistryObject<BaseBlock> AICORE = BLOCKS.register("aicore", AICoreTile::createBlock);
    public static final RegistryObject<Item> AICORE_ITEM = ITEMS.register("aicore", () -> new BlockItem(AICORE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<AICoreTile>> AICORE_TILE = TILES.register("aicore", () -> TileEntityType.Builder.create(AICoreTile::new, AICORE.get()).build(null));

    public static final RegistryObject<BaseBlock> WARPER = BLOCKS.register("warper", WarperTile::createBlock);
    public static final RegistryObject<Item> WARPER_ITEM = ITEMS.register("warper", () -> new BlockItem(WARPER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<WarperTile>> WARPER_TILE = TILES.register("warper", () -> TileEntityType.Builder.create(WarperTile::new, WARPER.get()).build(null));

    public static final RegistryObject<BaseBlock> LOCK = BLOCKS.register("lock", LockTile::createBlock);
    public static final RegistryObject<Item> LOCK_ITEM = ITEMS.register("lock", () -> new BlockItem(LOCK.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<LockTile>> LOCK_TILE = TILES.register("lock", () -> TileEntityType.Builder.create(LockTile::new, LOCK.get()).build(null));

    public static final RegistryObject<BaseBlock> SIGNAL_RECEIVER = BLOCKS.register("signal_receiver", SignalReceiverTile::createBlock);
    public static final RegistryObject<Item> SIGNAL_RECEIVER_ITEM = ITEMS.register("signal_receiver", () -> new BlockItem(SIGNAL_RECEIVER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<SignalReceiverTile>> SIGNAL_RECEIVER_TILE = TILES.register("signal_receiver", () -> TileEntityType.Builder.create(SignalReceiverTile::new, SIGNAL_RECEIVER.get()).build(null));

    public static final RegistryObject<BaseBlock> SIGNAL_TRANSMITTER = BLOCKS.register("signal_transmitter", SignalTransmitterTile::createBlock);
    public static final RegistryObject<Item> SIGNAL_TRANSMITTER_ITEM = ITEMS.register("signal_transmitter", () -> new BlockItem(SIGNAL_TRANSMITTER.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<SignalTransmitterTile>> SIGNAL_TRANSMITTER_TILE = TILES.register("signal_transmitter", () -> TileEntityType.Builder.create(SignalTransmitterTile::new, SIGNAL_TRANSMITTER.get()).build(null));

    public static final RegistryObject<BaseBlock> WIRELESS_BUTTON = BLOCKS.register("wireless_button", WirelessButtonTile::createBlock);
    public static final RegistryObject<Item> WIRELESS_BUTTON_ITEM = ITEMS.register("wireless_button", () -> new BlockItem(WIRELESS_BUTTON.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<WirelessButtonTile>> WIRELESS_BUTTON_TILE = TILES.register("wireless_button", () -> TileEntityType.Builder.create(WirelessButtonTile::new, WIRELESS_BUTTON.get()).build(null));

    public static final RegistryObject<BaseBlock> WIRELESS_LOCK = BLOCKS.register("wireless_lock", WirelessLockTile::createBlock);
    public static final RegistryObject<Item> WIRELESS_LOCK_ITEM = ITEMS.register("wireless_lock", () -> new BlockItem(WIRELESS_LOCK.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<WirelessLockTile>> WIRELESS_LOCK_TILE = TILES.register("wireless_lock", () -> TileEntityType.Builder.create(WirelessLockTile::new, WIRELESS_LOCK.get()).build(null));

    public static final RegistryObject<BaseBlock> ALARM = BLOCKS.register("alarm", AlarmTile::createBlock);
    public static final RegistryObject<Item> ALARM_ITEM = ITEMS.register("alarm", () -> new BlockItem(ALARM.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<AlarmTile>> ALARM_TILE = TILES.register("alarm", () -> TileEntityType.Builder.create(AlarmTile::new, ALARM.get()).build(null));

    public static final RegistryObject<BaseBlock> CONSTRUCTOR = BLOCKS.register("constructor", ConstructorTile::createBlock);
    public static final RegistryObject<Item> CONSTRUCTOR_ITEM = ITEMS.register("constructor", () -> new BlockItem(CONSTRUCTOR.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<ConstructorTile>> CONSTRUCTOR_TILE = TILES.register("constructor", () -> TileEntityType.Builder.create(ConstructorTile::new, CONSTRUCTOR.get()).build(null));

    public static final RegistryObject<BaseBlock> AUTO_CONSTRUCTOR = BLOCKS.register("auto_constructor", AutoConstructorTile::createBlock);
    public static final RegistryObject<Item> AUTO_CONSTRUCTOR_ITEM = ITEMS.register("auto_constructor", () -> new BlockItem(AUTO_CONSTRUCTOR.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<AutoConstructorTile>> AUTO_CONSTRUCTOR_TILE = TILES.register("auto_constructor", () -> TileEntityType.Builder.create(AutoConstructorTile::new, AUTO_CONSTRUCTOR.get()).build(null));

    public static final RegistryObject<BaseBlock> BLUEPRINT_STORAGE = BLOCKS.register("blueprint_storage", BlueprintStorageTile::createBlock);
    public static final RegistryObject<Item> BLUEPRINT_STORAGE_ITEM = ITEMS.register("blueprint_storage", () -> new BlockItem(BLUEPRINT_STORAGE.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<BlueprintStorageTile>> BLUEPRINT_STORAGE_TILE = TILES.register("blueprint_storage", () -> TileEntityType.Builder.create(BlueprintStorageTile::new, BLUEPRINT_STORAGE.get()).build(null));

    public static final RegistryObject<BaseBlock> AUTOMATION_FIELD = BLOCKS.register("automation_field", AutoFieldTile::createBlock);
    public static final RegistryObject<Item> AUTOMATION_FIELD_ITEM = ITEMS.register("automation_field", () -> new BlockItem(AUTOMATION_FIELD.get(), createStandardProperties()));
    public static final RegistryObject<TileEntityType<AutoFieldTile>> AUTOFIELD_TILE = TILES.register("automation_field", () -> TileEntityType.Builder.create(AutoFieldTile::new, AUTOMATION_FIELD.get()).build(null));

    public static final RegistryObject<BaseBlock> FLAT_LIGHT = BLOCKS.register("flatlight", () -> new BaseBlock(new BlockBuilder()
            .properties(Block.Properties.create(Material.GLASS).lightValue(15))));
    public static final RegistryObject<Item> FLAT_LIGHT_ITEM = ITEMS.register("flatlight", () -> new BlockItem(FLAT_LIGHT.get(), createStandardProperties()));

    public static final RegistryObject<NetCableBlock> NETCABLE = BLOCKS.register("netcable", NetCableBlock::new);
    public static final RegistryObject<Item> NETCABLE_ITEM = ITEMS.register("netcable", () -> new BlockItem(NETCABLE.get(), createStandardProperties()));
    public static final RegistryObject<ConnectorBlock> CONNECTOR = BLOCKS.register("connector", ConnectorBlock::new);
    public static final RegistryObject<Item> CONNECTOR_ITEM = ITEMS.register("connector", () -> new BlockItem(CONNECTOR.get(), createStandardProperties()));
    public static final RegistryObject<FacadeBlock> FACADE = BLOCKS.register("facade", FacadeBlock::new);
    public static final RegistryObject<Item> FACADE_ITEM = ITEMS.register("facade", () -> new BlockItem(FACADE.get(), createStandardProperties()));

    public static final RegistryObject<Item> INGOT_PLATINUM = ITEMS.register("ingot_platinum", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> INGOT_LITHIUM = ITEMS.register("ingot_lithium", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> INGOT_MANGANESE = ITEMS.register("ingot_manganese", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> INGOT_SILVER = ITEMS.register("ingot_silver", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> SILICON = ITEMS.register("silicon", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> DUST_NEGARITE = ITEMS.register("dust_negarite", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> DUST_POSIRITE = ITEMS.register("dust_posirite", () -> new Item(createStandardProperties()));
    public static final RegistryObject<EnergySabreItem> ENERGY_SABRE = ITEMS.register("energy_sabre", EnergySabreItem::new);
    public static final RegistryObject<EnhancedEnergySabreItem> ENHANCED_ENERGY_SABRE = ITEMS.register("enhanced_energy_sabre", EnhancedEnergySabreItem::new);
    public static final RegistryObject<KeyCardItem> KEY_CARD = ITEMS.register("key_card", KeyCardItem::new);
    public static final RegistryObject<BlueprintItem> BLUEPRINT = ITEMS.register("blueprint", BlueprintItem::new);
    public static final RegistryObject<ArientePearlItem> ARIENTE_PEARL = ITEMS.register("ariente_pearl", ArientePearlItem::new);
    public static final RegistryObject<FluxLevitatorItem> FLUX_LEVITATOR = ITEMS.register("flux_levitator", FluxLevitatorItem::new);
    public static final RegistryObject<FluxShipItem> FLUX_SHIP = ITEMS.register("flux_ship", FluxShipItem::new);
    public static final RegistryObject<FluxCapacitorItem> FLUX_CAPACITOR = ITEMS.register("flux_capacitor", FluxCapacitorItem::new);
    public static final RegistryObject<CircuitItem> CIRCUIT = ITEMS.register("circuit", () -> new CircuitItem(false));
    public static final RegistryObject<CircuitItem> ADVANCED_CIRCUIT = ITEMS.register("advanced_circuit", () -> new CircuitItem(true));
    public static final RegistryObject<EnergyHolderItem> ENERGY_HOLDER = ITEMS.register("energy_holder", EnergyHolderItem::new);
    public static final RegistryObject<PowerSuit> POWERSUIT_HEAD = ITEMS.register("powersuit_head", () -> new PowerSuit(EquipmentSlotType.HEAD));
    public static final RegistryObject<PowerSuit> POWERSUIT_CHEST = ITEMS.register("powersuit_chest", () -> new PowerSuit(EquipmentSlotType.CHEST));
    public static final RegistryObject<PowerSuit> POWERSUIT_LEGS = ITEMS.register("powersuit_legs", () -> new PowerSuit(EquipmentSlotType.LEGS));
    public static final RegistryObject<PowerSuit> POWERSUIT_FEET = ITEMS.register("powersuit_feet", () -> new PowerSuit(EquipmentSlotType.FEET));
    public static final RegistryObject<ArmorModuleItem> MODULE_ARMOR = ITEMS.register("module_armor", () -> new ArmorModuleItem(ArmorUpgradeType.ARMOR));
    public static final RegistryObject<ArmorModuleItem> MODULE_ENERGY = ITEMS.register("module_energy", () -> new ArmorModuleItem(ArmorUpgradeType.ENERGY));
    public static final RegistryObject<ArmorModuleItem> MODULE_FEATHERFALLING = ITEMS.register("module_featherfalling", () -> new ArmorModuleItem(ArmorUpgradeType.FEATHERFALLING));
    public static final RegistryObject<ArmorModuleItem> MODULE_FLIGHT = ITEMS.register("module_flight", () -> new ArmorModuleItem(ArmorUpgradeType.FLIGHT));
    public static final RegistryObject<ArmorModuleItem> MODULE_HOVER = ITEMS.register("module_hover", () -> new ArmorModuleItem(ArmorUpgradeType.HOVER));
    public static final RegistryObject<ArmorModuleItem> MODULE_FORCEFIELD = ITEMS.register("module_forcefield", () -> new ArmorModuleItem(ArmorUpgradeType.FORCEFIELD));
    public static final RegistryObject<ArmorModuleItem> MODULE_INVISIBILITY = ITEMS.register("module_invisibility", () -> new ArmorModuleItem(ArmorUpgradeType.INVISIBILITY));
    public static final RegistryObject<ArmorModuleItem> MODULE_NIGHTVISION = ITEMS.register("module_nightvision", () -> new ArmorModuleItem(ArmorUpgradeType.NIGHTVISION));
    public static final RegistryObject<ArmorModuleItem> MODULE_REGENERATION = ITEMS.register("module_regeneration", () -> new ArmorModuleItem(ArmorUpgradeType.REGENERATION));
    public static final RegistryObject<ArmorModuleItem> MODULE_SCRAMBLE = ITEMS.register("module_scramble", () -> new ArmorModuleItem(ArmorUpgradeType.SCRAMBLE));
    public static final RegistryObject<ArmorModuleItem> MODULE_AUTOFEED = ITEMS.register("module_autofeed", () -> new ArmorModuleItem(ArmorUpgradeType.AUTOFEED));
    public static final RegistryObject<ArmorModuleItem> MODULE_SPEED = ITEMS.register("module_speed", () -> new ArmorModuleItem(ArmorUpgradeType.SPEED));
    public static final RegistryObject<ArmorModuleItem> MODULE_STEPASSIST = ITEMS.register("module_stepassist", () -> new ArmorModuleItem(ArmorUpgradeType.STEPASSIST));
    public static final RegistryObject<ArmorModuleItem> MODULE_INHIBIT = ITEMS.register("module_inhibit", () -> new ArmorModuleItem(ArmorUpgradeType.INHIBIT));
    public static final RegistryObject<ArmorModuleItem> MODULE_POWER = ITEMS.register("module_power", () -> new ArmorModuleItem(ArmorUpgradeType.POWER));
    public static final RegistryObject<ArmorModuleItem> MODULE_LOOTING = ITEMS.register("module_looting", () -> new ArmorModuleItem(ArmorUpgradeType.LOOTING));
    public static final RegistryObject<ArmorModuleItem> MODULE_FIRE = ITEMS.register("module_fire", () -> new ArmorModuleItem(ArmorUpgradeType.FIRE));


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


    public static Item.Properties createStandardProperties() {
        return new Item.Properties().group(Ariente.setup.getTab());
    }

}