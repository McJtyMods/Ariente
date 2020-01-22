package mcjty.ariente.blocks;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.decorative.*;
import mcjty.ariente.cables.ConnectorBlock;
import mcjty.ariente.cables.NetCableBlock;
import mcjty.ariente.facade.FacadeBlock;
import mcjty.ariente.setup.Registration;
import mcjty.lib.blocks.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.ariente.setup.Registration.BLOCKS;

public class ModBlocks {

    public static final AxisAlignedBB FLAT_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);
    public static final AxisAlignedBB BEAM_BLOCK_NS_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.3D, 1.0D, 0.3D, 0.7D);
    public static final AxisAlignedBB BEAM_BLOCK_EW_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.0D, 0.7D, 0.3D, 1.0D);

    public static BlackTechBlock blackTechBlock;
    public static MarbleTechBlock marbleTechBlock;
    public static PatternBlock patternBlock;

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


    public static MarbleSlabBlock marbleSlabBlock;
    public static DoubleMarbleSlabBlock doubleMarbleSlabBlock;

    public static BaseBlock fluxBeamBlock;
    public static BaseBlock fluxBendBeamBlock;

    //    public static final RegistryObject<BaseBlock> BUILDER = BLOCKS.register("builder", BuilderTileEntity::createBlock);

    public static final RegistryObject<BaseBlock> sensorItemNode = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> inputItemNode = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> outputItemNode = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> roundRobinNode = BLOCKS.register("", null);
    public static final RegistryObject<BaseBlock> fieldMarker = BLOCKS.register("", null);

    public static BaseBlock reinforcedMarble;
    public static BaseBlock fluxGlow;

    public static final RegistryObject<BaseBlock> powerCombinerBlock = BLOCKS.register("", null);
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

    public static BaseBlock flatLightBlock;

    public static NetCableBlock netCableBlock;
    public static ConnectorBlock connectorBlock;
    public static FacadeBlock facadeBlock;

    public static RampBlock rampBlock;
    public static SlopeBlock slopeBlock;
    public static PaneBlock glassFence;
    public static PaneBlock blueGlassFence;
    public static PaneBlock marbleFence;
    public static PaneBlock techFence;

    public static final AxisAlignedBB LIGHT_BLOCK_DOWN  = new AxisAlignedBB(0.125F, 0.0F, 0.125F, 0.875F, 0.125F, 0.875F);
    public static final AxisAlignedBB LIGHT_BLOCK_UP    = new AxisAlignedBB(0.125F, 0.875F, 0.125F, 0.875F, 1.0F, 0.875F);
    public static final AxisAlignedBB LIGHT_BLOCK_NORTH = new AxisAlignedBB(0.125F, 0.125F, 0.0F, 0.875F, 0.875F, 0.125F);
    public static final AxisAlignedBB LIGHT_BLOCK_SOUTH = new AxisAlignedBB(0.125F, 0.125F, 0.875F, 0.875F, 0.8750F, 1.0F);
    public static final AxisAlignedBB LIGHT_BLOCK_WEST  = new AxisAlignedBB(0.0F, 0.125F, 0.125F, 0.125F, 0.875F, 0.8750F);
    public static final AxisAlignedBB LIGHT_BLOCK_EAST  = new AxisAlignedBB(0.875F, 0.125F, 0.125F, 1.0F, 0.875F, 0.8750F);


    public static void init() {
        initDecorative();
        initOres();
        initTechnical();
    }

    private static void initTechnical() {
        netCableBlock = new NetCableBlock();
        connectorBlock = new ConnectorBlock();
        facadeBlock = new FacadeBlock();

        // @todo 1.14
//        flatLightBlock = new BaseBlockBuilder<>(Ariente.instance, "flatlight")
//                .creativeTabs(Ariente.setup.getTab())
//                .lightValue(15)
//                .flags(NON_OPAQUE, NON_FULLCUBE)
//                .boundingBox((state, source, pos) -> getFlatBox(state))
//                .build();
//
//        fluxGlow = new BaseBlockBuilder<>(Ariente.instance, "fluxglow")
//                .creativeTabs(Ariente.setup.getTab())
//                .rotationType(NONE)
//                .lightValue(15)
//                .build();
//
//        sensorItemNode = builderFactory.<SensorItemNodeTile> builder("sensor_item_node")
//                .tileEntityClass(SensorItemNodeTile.class)
//                .emptyContainer()
//                .rotationType(NONE)
//                .itemBlockFactory(MultipartItemBlock::new)
//                .property(AbstractNodeTile.ORIENTATION)
//                .placementGetter(SensorItemNodeTile::getStateForPlacement)
//                .slotGetter((world, pos, newState) -> newState.getValue(AbstractNodeTile.ORIENTATION).getSlot())
//                .flags(NON_OPAQUE, NON_FULLCUBE)
//                .boundingBox(AbstractNodeTile::getBoundingBox)
//                .build();
//        inputItemNode = builderFactory.<InputItemNodeTile> builder("input_item_node")
//                .tileEntityClass(InputItemNodeTile.class)
//                .emptyContainer()
//                .rotationType(NONE)
//                .itemBlockFactory(MultipartItemBlock::new)
//                .property(AbstractNodeTile.ORIENTATION)
//                .placementGetter(InputItemNodeTile::getStateForPlacement)
//                .slotGetter((world, pos, newState) -> newState.getValue(AbstractNodeTile.ORIENTATION).getSlot())
//                .flags(NON_OPAQUE, NON_FULLCUBE)
//                .boundingBox(AbstractNodeTile::getBoundingBox)
//                .build();
//        outputItemNode = builderFactory.<OutputItemNodeTile> builder("output_item_node")
//                .tileEntityClass(OutputItemNodeTile.class)
//                .emptyContainer()
//                .rotationType(NONE)
//                .itemBlockFactory(MultipartItemBlock::new)
//                .property(AbstractNodeTile.ORIENTATION)
//                .placementGetter(OutputItemNodeTile::getStateForPlacement)
//                .slotGetter((world, pos, newState) -> newState.getValue(AbstractNodeTile.ORIENTATION).getSlot())
//                .flags(NON_OPAQUE, NON_FULLCUBE)
//                .boundingBox(AbstractNodeTile::getBoundingBox)
//                .build();
//        roundRobinNode = builderFactory.<RoundRobinNodeTile> builder("round_robin_node")
//                .tileEntityClass(RoundRobinNodeTile.class)
//                .emptyContainer()
//                .rotationType(NONE)
//                .itemBlockFactory(MultipartItemBlock::new)
//                .property(RoundRobinNodeTile.ORIENTATION)
//                .placementGetter(RoundRobinNodeTile::getStateForPlacement)
//                .slotGetter((world, pos, newState) -> newState.getValue(RoundRobinNodeTile.ORIENTATION).getBackSlot())
//                .flags(NON_OPAQUE, NON_FULLCUBE)
//                .boundingBox(RoundRobinNodeTile::getBoundingBox)
//                .build();
//        fieldMarker = builderFactory.<FieldMarkerTile> builder("field_marker")
//                .tileEntityClass(FieldMarkerTile.class)
//                .emptyContainer()
//                .rotationType(NONE)
//                .itemBlockFactory(MultipartItemBlock::new)
//                .slotGetter((world, pos, newState) -> PartSlot.DOWN)
//                .flags(NON_OPAQUE, NON_FULLCUBE)
//                .boundingBox((state, source, pos) -> FLAT_BLOCK_AABB)
//                .build();
//
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
//        reinforcedMarble = new BaseBlockBuilder<>(Ariente.instance, "reinforced_marble")
//                .creativeTabs(Ariente.setup.getTab())
//                .rotationType(NONE)
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.lock")
//                .build();
//        reinforcedMarble.setHardness(80.0F).setResistance(3000.0F);
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
//        powerCombinerBlock = builderFactory.<PowerCombinerTile> builder("power_combiner")
//                .tileEntityClass(PowerCombinerTile.class)
//                .rotationType(BaseBlock.RotationType.ROTATION)
//                .flags(RENDER_SOLID, RENDER_CUTOUT)
//                .activateAction((world, pos, player, hand, side, hitX, hitY, hitZ) -> Ariente.guiHandler.openHoloGui(world, pos, player))
//                .info("message.ariente.shiftmessage")
//                .infoExtended("message.ariente.power_combiner")
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


    private static void initDecorative() {
        blackTechBlock = new BlackTechBlock("blacktech");
        marbleTechBlock = new MarbleTechBlock("marbletech");
        patternBlock = new PatternBlock("pattern");
        marble = new MarbleBlock("marble");
        marble_smooth = new MarbleBlock("marble_smooth");
        marble_pilar = new MarbleBlock("marble_pilar");
        marble_bricks = new MarbleBlock("marble_bricks");
        marbleSlabBlock = new MarbleSlabBlock();    // @todo 1.14 "marble_slab"
        doubleMarbleSlabBlock = new DoubleMarbleSlabBlock("double_marble_slab");
        rampBlock = new RampBlock();    // @todo 1.14 "ramp"
        slopeBlock = new SlopeBlock();      // @todo 1.14 "slope"
        glassFence = (PaneBlock) new PaneBlock(Material.GLASS, SoundType.GLASS)   // @todo 1.14 "glass_fence",
                .setTransluscent(true);
//                .setHardness(0.3F);   // @todo 1.14
        blueGlassFence = (PaneBlock) new PaneBlock(Material.GLASS, SoundType.GLASS)   // @todo 1.14 "blue_glass_fence",
                .setTransluscent(true);
//                .setHardness(0.3F);   // @todo 1.14
        marbleFence = (PaneBlock) new PaneBlock(Material.ROCK, SoundType.STONE);   // @todo 1.14 "marble_fence",
                //.setHardness(3.0F).setResistance(8.0F);   // @todo 1.14
        techFence = (PaneBlock) new PaneBlock(Material.ROCK, SoundType.STONE); // @todo 1.14 "tech_fence",
//                .setHardness(3.0F).setResistance(8.0F);   // @todo 1.14
    }

    private static void initOres() {
        // @todo 1.14
//        lithiumore = new BaseBlockBuilder<>(Ariente.instance, "lithiumore")
//                .rotationType(NONE)
//                .creativeTabs(Ariente.setup.getTab())
//                .build();
//        manganeseore = new BaseBlockBuilder<>(Ariente.instance, "manganeseore")
//                .rotationType(NONE)
//                .creativeTabs(Ariente.setup.getTab())
//                .build();
//        siliconore = new BaseBlockBuilder<>(Ariente.instance, "siliconore")
//                .rotationType(NONE)
//                .creativeTabs(Ariente.setup.getTab())
//                .build();
//        silverore = new BaseBlockBuilder<>(Ariente.instance, "silverore")
//                .rotationType(NONE)
//                .creativeTabs(Ariente.setup.getTab())
//                .build();
//        platinumore = new BaseBlockBuilder<>(Ariente.instance, "platinumore")
//                .rotationType(NONE)
//                .creativeTabs(Ariente.setup.getTab())
//                .build();
//        posirite = new BaseBlockBuilder<>(Ariente.instance, "posirite")
//                .rotationType(NONE)
//                .creativeTabs(Ariente.setup.getTab())
//                .build();
//        negarite = new BaseBlockBuilder<>(Ariente.instance, "negarite")
//                .rotationType(NONE)
//                .creativeTabs(Ariente.setup.getTab())
//                .build();
    }


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