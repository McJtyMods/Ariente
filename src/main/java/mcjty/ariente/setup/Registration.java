package mcjty.ariente.setup;

import mcjty.ariente.display.AlienTextDisplayModule;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.PowerDisplayModule;
import mcjty.ariente.display.StorageDisplayModule;
import mcjty.ariente.display.TextDisplayModule;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import mcjty.ariente.multipart.MultipartBlock;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.ariente.sounds.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.oregen.WorldGen;
import mcjty.ariente.blocks.MountedPanelShapes;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.BaseOreBlock;
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
import mcjty.ariente.cables.*;
import mcjty.ariente.entities.EntityArientePearl;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.entities.drone.DroneEntity;
import mcjty.ariente.entities.drone.SentinelDroneEntity;
import mcjty.ariente.entities.fluxelevator.FluxElevatorEntity;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.entities.soldier.MasterSoldierEntity;
import mcjty.ariente.entities.soldier.SoldierEntity;
import mcjty.ariente.facade.FacadeBlock;
import mcjty.ariente.facade.FacadeItemBlock;
import mcjty.ariente.items.*;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.lib.blocks.BaseBlock;
import mcjty.ariente.items.BlockStateItem;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.ariente.multipart.MultipartItemBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import mcjty.ariente.loot.DungeonLootModifier;

import static mcjty.ariente.Ariente.MODID;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class Registration {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> DUNGEON_LOOT = LOOT_MODIFIERS.register("dungeon_loot", () -> DungeonLootModifier.CODEC);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("ariente", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ariente"))
            .icon(() -> new ItemStack(Registration.POSIRITE_GENERATOR.get()))
            .displayItems((parameters, output) -> {
                ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                BlueprintRecipeRegistry.getRecipes().forEach(recipe -> output.accept(BlueprintItem.makeBluePrint(recipe.getDestination())));
            })
            .build());

    public static void register() {
        LOOT_MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        WorldGen.register();
        ModSounds.register();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Registration::registerAttributes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Registration::registerSpawnPlacements);
        TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ENTITY_SOLDIER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SoldierEntity::checkSoldierSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ENTITY_MASTER_SOLDIER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SoldierEntity::checkSoldierSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ENTITY_SOLDIER.get(), SoldierEntity.registerAttributes().build());
        event.put(ENTITY_MASTER_SOLDIER.get(), MasterSoldierEntity.registerAttributes().build());
        event.put(ENTITY_DRONE.get(), DroneEntity.registerAttributes().build());
        event.put(ENTITY_SENTINEL_DRONE.get(), SentinelDroneEntity.registerAttributes().build());
    }

    public static final TagKey<Item> TAG_INGOT_SILVER = ItemTags.create(new ResourceLocation("forge", "ingots/silver"));
    public static final TagKey<Item> TAG_INGOT_PLATINUM = ItemTags.create(new ResourceLocation("forge", "ingots/platinum"));
    public static final TagKey<Item> TAG_INGOT_LITHIUM = ItemTags.create(new ResourceLocation("forge", "ingots/lithium"));
    public static final TagKey<Item> TAG_INGOT_MANGANESE = ItemTags.create(new ResourceLocation("forge", "ingots/manganese"));
    public static final TagKey<Item> TAG_DUSTS_SILICON = ItemTags.create(new ResourceLocation("forge", "dusts/silicon"));
    public static final TagKey<Item> TAG_DUSTS_NEGARITE = ItemTags.create(new ResourceLocation("forge", "dusts/negarite"));
    public static final TagKey<Item> TAG_DUSTS_POSIRITE = ItemTags.create(new ResourceLocation("forge", "dusts/posirite"));
    public static final TagKey<Block> TAG_ORE_SILVER = BlockTags.create(new ResourceLocation("forge", "ores/silver"));
    public static final TagKey<Block> TAG_ORE_PLATINUM = BlockTags.create(new ResourceLocation("forge", "ores/platinum"));
    public static final TagKey<Block> TAG_ORE_LITHIUM = BlockTags.create(new ResourceLocation("forge", "ores/lithium"));
    public static final TagKey<Block> TAG_ORE_MANGANESE = BlockTags.create(new ResourceLocation("forge", "ores/manganese"));
    public static final TagKey<Block> TAG_ORE_SILICON = BlockTags.create(new ResourceLocation("forge", "ores/silicon"));
    public static final TagKey<Block> TAG_ORE_NEGARITE = BlockTags.create(new ResourceLocation("forge", "ores/negarite"));
    public static final TagKey<Block> TAG_ORE_POSIRITE = BlockTags.create(new ResourceLocation("forge", "ores/posirite"));
    public static final TagKey<Item> TAG_ORE_SILVER_ITEM = ItemTags.create(new ResourceLocation("forge", "ores/silver"));
    public static final TagKey<Item> TAG_ORE_PLATINUM_ITEM = ItemTags.create(new ResourceLocation("forge", "ores/platinum"));
    public static final TagKey<Item> TAG_ORE_LITHIUM_ITEM = ItemTags.create(new ResourceLocation("forge", "ores/lithium"));
    public static final TagKey<Item> TAG_ORE_MANGANESE_ITEM = ItemTags.create(new ResourceLocation("forge", "ores/manganese"));
    public static final TagKey<Item> TAG_ORE_SILICON_ITEM = ItemTags.create(new ResourceLocation("forge", "ores/silicon"));
    public static final TagKey<Item> TAG_ORE_NEGARITE_ITEM = ItemTags.create(new ResourceLocation("forge", "ores/negarite"));
    public static final TagKey<Item> TAG_ORE_POSIRITE_ITEM = ItemTags.create(new ResourceLocation("forge", "ores/posirite"));
    public static final TagKey<Item> TAG_MARBLE = ItemTags.create(new ResourceLocation("forge", "marble"));

    public static final AABB FLAT_BLOCK_AABB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);
    public static final AABB BEAM_BLOCK_NS_AABB = new AABB(0.0D, 0.0D, 0.3D, 1.0D, 0.3D, 0.7D);
    public static final AABB BEAM_BLOCK_EW_AABB = new AABB(0.3D, 0.0D, 0.0D, 0.7D, 0.3D, 1.0D);

    public static final AABB LIGHT_BLOCK_DOWN = new AABB(0.125F, 0.0F, 0.125F, 0.875F, 0.125F, 0.875F);
    public static final AABB LIGHT_BLOCK_UP = new AABB(0.125F, 0.875F, 0.125F, 0.875F, 1.0F, 0.875F);
    public static final AABB LIGHT_BLOCK_NORTH = new AABB(0.125F, 0.125F, 0.0F, 0.875F, 0.875F, 0.125F);
    public static final AABB LIGHT_BLOCK_SOUTH = new AABB(0.125F, 0.125F, 0.875F, 0.875F, 0.8750F, 1.0F);
    public static final AABB LIGHT_BLOCK_WEST = new AABB(0.0F, 0.125F, 0.125F, 0.125F, 0.875F, 0.8750F);
    public static final AABB LIGHT_BLOCK_EAST = new AABB(0.875F, 0.125F, 0.125F, 1.0F, 0.875F, 0.8750F);

    public static final RegistryObject<MultipartBlock> MULTIPART_BLOCK = BLOCKS.register("multipart", MultipartBlock::new);
    public static final RegistryObject<BlockEntityType<MultipartTE>> TYPE_MULTIPART = TILES.register("multipart", () -> BlockEntityType.Builder.of(MultipartTE::new, MULTIPART_BLOCK.get()).build(null));

    public static final RegistryObject<PatternBlock> PATTERN = BLOCKS.register("pattern", PatternBlock::new);
    public static final Map<PatternType, RegistryObject<BlockStateItem>> PATTERN_ITEMS = Arrays.stream(PatternType.values())
            .map(type -> Pair.of(type, ITEMS.register("pattern_" + type.getSerializedName(), () -> new BlockStateItem(PATTERN.get().defaultBlockState().setValue(PatternBlock.TYPE, type), createStandardProperties()))))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

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

    public static final RegistryObject<BaseBlock> FLUX_BEAM = BLOCKS.register("flux_beam", () -> new BaseBeamBlock(new BlockBuilder(), false));
    public static final RegistryObject<Item> FLUX_BEAM_ITEM = ITEMS.register("flux_beam", () -> new BlockItem(FLUX_BEAM.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> FLUX_BEND_BEAM = BLOCKS.register("flux_bend_beam", () -> new BaseBeamBlock(new BlockBuilder(), true));
    public static final RegistryObject<Item> FLUX_BEND_BEAM_ITEM = ITEMS.register("flux_bend_beam", () -> new BlockItem(FLUX_BEND_BEAM.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> FLUX_SLOPE_BEAM = BLOCKS.register("flux_slope_beam", FluxSlopeBeamBlock::new);
    public static final RegistryObject<Item> FLUX_SLOPE_BEAM_ITEM = ITEMS.register("flux_slope_beam", () -> new BlockItem(FLUX_SLOPE_BEAM.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> SENSOR_ITEM_NODE = BLOCKS.register("sensor_item_node", SensorItemNodeTile::createBlock);
    public static final RegistryObject<Item> SENSOR_ITEM_NODE_ITEM = ITEMS.register("sensor_item_node", () -> new MultipartItemBlock(SENSOR_ITEM_NODE.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<SensorItemNodeTile>> SENSOR_ITEM_TILE = TILES.register("sensor_item_node", () -> BlockEntityType.Builder.of(SensorItemNodeTile::new, SENSOR_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> INPUT_ITEM_NODE = BLOCKS.register("input_item_node", InputItemNodeTile::createBlock);
    public static final RegistryObject<Item> INPUT_ITEM_NODE_ITEM = ITEMS.register("input_item_node", () -> new MultipartItemBlock(INPUT_ITEM_NODE.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<InputItemNodeTile>> INPUT_ITEM_TILE = TILES.register("input_item_node", () -> BlockEntityType.Builder.of(InputItemNodeTile::new, INPUT_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> OUTPUT_ITEM_NODE = BLOCKS.register("output_item_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(OutputItemNodeTile::new)));
    public static final RegistryObject<Item> OUTPUT_ITEM_NODE_ITEM = ITEMS.register("output_item_node", () -> new MultipartItemBlock(OUTPUT_ITEM_NODE.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<OutputItemNodeTile>> OUTPUT_ITEM_TILE = TILES.register("output_item_node", () -> BlockEntityType.Builder.of(OutputItemNodeTile::new, OUTPUT_ITEM_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> ROUND_ROBIN_NODE = BLOCKS.register("round_robin_node", () -> new BaseNodeBlock(new BlockBuilder()
            .topDriver(DRIVER)
            .tileEntitySupplier(RoundRobinNodeTile::new)));
    public static final RegistryObject<Item> ROUND_ROBIN_NODE_ITEM = ITEMS.register("round_robin_node", () -> new MultipartItemBlock(ROUND_ROBIN_NODE.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<RoundRobinNodeTile>> ROUND_ROBIN_TILE = TILES.register("round_robin_node", () -> BlockEntityType.Builder.of(RoundRobinNodeTile::new, ROUND_ROBIN_NODE.get()).build(null));

    public static final RegistryObject<BaseBlock> FIELD_MARKER = BLOCKS.register("field_marker", FieldMarkerTile::createBlock);
    public static final RegistryObject<Item> FIELD_MARKER_ITEM = ITEMS.register("field_marker", () -> new MultipartItemBlock(FIELD_MARKER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<FieldMarkerTile>> FIELD_MARKER_TILE = TILES.register("field_marker", () -> BlockEntityType.Builder.of(FieldMarkerTile::new, FIELD_MARKER.get()).build(null));

    public static final RegistryObject<RampBlock> RAMP = BLOCKS.register("ramp", RampBlock::new);
    public static final RegistryObject<Item> RAMP_ITEM = ITEMS.register("ramp", () -> new BlockItem(RAMP.get(), createStandardProperties()));

    public static final RegistryObject<SlopeBlock> SLOPE = BLOCKS.register("slope", SlopeBlock::new);
    public static final RegistryObject<Item> SLOPE_ITEM = ITEMS.register("slope", () -> new BlockItem(SLOPE.get(), createStandardProperties()));

    public static final RegistryObject<PaneBlock> GLASS_FENCE = BLOCKS.register("glass_fence", () -> new PaneBlock(MapColor.NONE, SoundType.GLASS));
    public static final RegistryObject<Item> GLASS_FENCE_ITEM = ITEMS.register("glass_fence", () -> new BlockItem(GLASS_FENCE.get(), createStandardProperties()));

    public static final RegistryObject<PaneBlock> BLUE_GLASS_FENCE = BLOCKS.register("blue_glass_fence", () -> new PaneBlock(MapColor.NONE, SoundType.GLASS));
    public static final RegistryObject<Item> BLUE_GLASS_FENCE_ITEM = ITEMS.register("blue_glass_fence", () -> new BlockItem(BLUE_GLASS_FENCE.get(), createStandardProperties()));

    public static final RegistryObject<PaneBlock> MARBLE_FENCE = BLOCKS.register("marble_fence", () -> new PaneBlock(MapColor.STONE, SoundType.STONE));
    public static final RegistryObject<Item> MARBLE_FENCE_ITEM = ITEMS.register("marble_fence", () -> new BlockItem(MARBLE_FENCE.get(), createStandardProperties()));

    public static final RegistryObject<PaneBlock> TECH_FENCE = BLOCKS.register("tech_fence", () -> new PaneBlock(MapColor.STONE, SoundType.STONE));
    public static final RegistryObject<Item> TECH_FENCE_ITEM = ITEMS.register("tech_fence", () -> new BlockItem(TECH_FENCE.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> REINFORCED_MARBLE = BLOCKS.register("reinforced_marble", () -> new BaseBlock(new BlockBuilder()
            .properties(Block.Properties.of().mapColor(MapColor.STONE).strength(80.0f, 3000.0f))
    ) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });
    public static final RegistryObject<Item> REINFORCED_MARBLE_ITEM = ITEMS.register("reinforced_marble", () -> new BlockItem(REINFORCED_MARBLE.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> FLUX_GLOW = BLOCKS.register("fluxglow", () -> new BaseBlock(new BlockBuilder()
        .properties(Block.Properties.of().mapColor(MapColor.NONE).lightLevel((light) -> { return 15; }))) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });
    public static final RegistryObject<Item> FLUX_GLOW_ITEM = ITEMS.register("fluxglow", () -> new BlockItem(FLUX_GLOW.get(), createStandardProperties()));

    public static final RegistryObject<BaseBlock> POWER_COMBINER = BLOCKS.register("power_combiner", PowerCombinerTile::createBlock);
    public static final RegistryObject<Item> POWER_COMBINER_ITEM = ITEMS.register("power_combiner", () -> new BlockItem(POWER_COMBINER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<PowerCombinerTile>> POWER_COMBINER_TILE = TILES.register("power_combiner", () -> BlockEntityType.Builder.of(PowerCombinerTile::new, POWER_COMBINER.get()).build(null));

    public static final RegistryObject<BaseBlock> NEGARITE_GENERATOR = BLOCKS.register("negarite_generator", NegariteGeneratorTile::createBlock);
    public static final RegistryObject<Item> NEGARITE_GENERATOR_ITEM = ITEMS.register("negarite_generator", () -> new BlockItem(NEGARITE_GENERATOR.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<NegariteGeneratorTile>> NEGARITE_GENERATOR_TILE = TILES.register("negarite_generator", () -> BlockEntityType.Builder.of(NegariteGeneratorTile::new, NEGARITE_GENERATOR.get()).build(null));

    public static final RegistryObject<BaseBlock> POSIRITE_GENERATOR = BLOCKS.register("posirite_generator", PosiriteGeneratorTile::createBlock);
    public static final RegistryObject<Item> POSIRITE_GENERATOR_ITEM = ITEMS.register("posirite_generator", () -> new BlockItem(POSIRITE_GENERATOR.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<PosiriteGeneratorTile>> POSIRITE_GENERATOR_TILE = TILES.register("posirite_generator", () -> BlockEntityType.Builder.of(PosiriteGeneratorTile::new, POSIRITE_GENERATOR.get()).build(null));

    public static final RegistryObject<BaseBlock> NEGARITE_TANK = BLOCKS.register("negarite_tank", NegariteTankTile::createBlock);
    public static final RegistryObject<Item> NEGARITE_TANK_ITEM = ITEMS.register("negarite_tank", () -> new BlockItem(NEGARITE_TANK.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<NegariteTankTile>> NEGARITE_TANK_TILE = TILES.register("negarite_tank", () -> BlockEntityType.Builder.of(NegariteTankTile::new, NEGARITE_TANK.get()).build(null));

    public static final RegistryObject<BaseBlock> POSIRITE_TANK = BLOCKS.register("posirite_tank", PosiriteTankTile::createBlock);
    public static final RegistryObject<Item> POSIRITE_TANK_ITEM = ITEMS.register("posirite_tank", () -> new BlockItem(POSIRITE_TANK.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<PosiriteTankTile>> POSIRITE_TANK_TILE = TILES.register("posirite_tank", () -> BlockEntityType.Builder.of(PosiriteTankTile::new, POSIRITE_TANK.get()).build(null));

    public static final RegistryObject<BaseBlock> DISPLAY_BASE = BLOCKS.register("display_base", DisplayBaseTile::createBlock);
    public static final RegistryObject<Item> DISPLAY_BASE_ITEM = ITEMS.register("display_base", () -> new BlockItem(DISPLAY_BASE.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<DisplayBaseTile>> DISPLAY_BASE_TILE = TILES.register("display_base", () -> BlockEntityType.Builder.of(DisplayBaseTile::new, DISPLAY_BASE.get()).build(null));
    public static final RegistryObject<DisplayModuleItem> DISPLAY_POWER = ITEMS.register("display_module_power", () -> new DisplayModuleItem(new PowerDisplayModule()));
    public static final RegistryObject<DisplayModuleItem> DISPLAY_STORAGE = ITEMS.register("display_module_storage", () -> new DisplayModuleItem(new StorageDisplayModule()));
    public static final RegistryObject<DisplayModuleItem> DISPLAY_TEXT = ITEMS.register("display_module_text", () -> new DisplayModuleItem(new TextDisplayModule()));
    public static final RegistryObject<DisplayModuleItem> DISPLAY_ALIEN_TEXT = ITEMS.register("display_module_alien_text", () -> new DisplayModuleItem(new AlienTextDisplayModule()));

    public static final RegistryObject<BaseBlock> DOOR_MARKER = BLOCKS.register("door_marker", DoorMarkerTile::createBlock);
    public static final RegistryObject<Item> DOOR_MARKER_ITEM = ITEMS.register("door_marker", () -> new BlockItem(DOOR_MARKER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<DoorMarkerTile>> DOOR_MARKER_TILE = TILES.register("door_marker", () -> BlockEntityType.Builder.of(DoorMarkerTile::new, DOOR_MARKER.get()).build(null));

    public static final RegistryObject<BaseBlock> AICORE = BLOCKS.register("aicore", AICoreTile::createBlock);
    public static final RegistryObject<Item> AICORE_ITEM = ITEMS.register("aicore", () -> new BlockItem(AICORE.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<AICoreTile>> AICORE_TILE = TILES.register("aicore", () -> BlockEntityType.Builder.of(AICoreTile::new, AICORE.get()).build(null));

    public static final RegistryObject<BaseBlock> ALARM = BLOCKS.register("alarm", AlarmTile::createBlock);
    public static final RegistryObject<Item> ALARM_ITEM = ITEMS.register("alarm", () -> new BlockItem(ALARM.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<AlarmTile>> ALARM_TILE = TILES.register("alarm", () -> BlockEntityType.Builder.of(AlarmTile::new, ALARM.get()).build(null));

    public static final RegistryObject<BaseBlock> CONSTRUCTOR = BLOCKS.register("constructor", ConstructorTile::createBlock);
    public static final RegistryObject<Item> CONSTRUCTOR_ITEM = ITEMS.register("constructor", () -> new BlockItem(CONSTRUCTOR.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<ConstructorTile>> CONSTRUCTOR_TILE = TILES.register("constructor", () -> BlockEntityType.Builder.of(ConstructorTile::new, CONSTRUCTOR.get()).build(null));

    public static final RegistryObject<BaseBlock> AUTO_CONSTRUCTOR = BLOCKS.register("auto_constructor", AutoConstructorTile::createBlock);
    public static final RegistryObject<Item> AUTO_CONSTRUCTOR_ITEM = ITEMS.register("auto_constructor", () -> new BlockItem(AUTO_CONSTRUCTOR.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<AutoConstructorTile>> AUTO_CONSTRUCTOR_TILE = TILES.register("auto_constructor", () -> BlockEntityType.Builder.of(AutoConstructorTile::new, AUTO_CONSTRUCTOR.get()).build(null));

    public static final RegistryObject<BaseBlock> BLUEPRINT_STORAGE = BLOCKS.register("blueprint_storage", BlueprintStorageTile::createBlock);
    public static final RegistryObject<Item> BLUEPRINT_STORAGE_ITEM = ITEMS.register("blueprint_storage", () -> new BlockItem(BLUEPRINT_STORAGE.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<BlueprintStorageTile>> BLUEPRINT_STORAGE_TILE = TILES.register("blueprint_storage", () -> BlockEntityType.Builder.of(BlueprintStorageTile::new, BLUEPRINT_STORAGE.get()).build(null));

    public static final RegistryObject<BaseBlock> AUTOMATION_FIELD = BLOCKS.register("automation_field", AutoFieldTile::createBlock);
    public static final RegistryObject<Item> AUTOMATION_FIELD_ITEM = ITEMS.register("automation_field", () -> new BlockItem(AUTOMATION_FIELD.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<AutoFieldTile>> AUTOFIELD_TILE = TILES.register("automation_field", () -> BlockEntityType.Builder.of(AutoFieldTile::new, AUTOMATION_FIELD.get()).build(null));

    public static final RegistryObject<BaseBlock> STORAGE = BLOCKS.register("storage", StorageTile::createBlock);
    public static final RegistryObject<Item> STORAGE_ITEM = ITEMS.register("storage", () -> new BlockItem(STORAGE.get(), createStandardProperties()));
    public static final RegistryObject<BaseBlock> STORAGE_3X3 = BLOCKS.register("storage_3x3", StorageTile::createBlock);
    public static final RegistryObject<Item> STORAGE_3X3_ITEM = ITEMS.register("storage_3x3", () -> new BlockItem(STORAGE_3X3.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<StorageTile>> STORAGE_TILE = TILES.register("storage", () -> BlockEntityType.Builder.of(StorageTile::new, STORAGE.get(), STORAGE_3X3.get()).build(null));

    public static final RegistryObject<BaseBlock> HOLO_STORAGE_1 = BLOCKS.register("holo_storage_1", StorageCubeTile::createBlock);
    public static final RegistryObject<Item> HOLO_STORAGE_1_ITEM = ITEMS.register("holo_storage_1", () -> new BlockItem(HOLO_STORAGE_1.get(), createStandardProperties()));
    public static final RegistryObject<BaseBlock> HOLO_STORAGE_2 = BLOCKS.register("holo_storage_2", StorageCubeTile::createBlock);
    public static final RegistryObject<Item> HOLO_STORAGE_2_ITEM = ITEMS.register("holo_storage_2", () -> new BlockItem(HOLO_STORAGE_2.get(), createStandardProperties()));
    public static final RegistryObject<BaseBlock> HOLO_STORAGE_3 = BLOCKS.register("holo_storage_3", StorageCubeTile::createBlock);
    public static final RegistryObject<Item> HOLO_STORAGE_3_ITEM = ITEMS.register("holo_storage_3", () -> new BlockItem(HOLO_STORAGE_3.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<StorageCubeTile>> HOLO_STORAGE_TILE = TILES.register("holo_storage", () -> BlockEntityType.Builder.of(StorageCubeTile::new, HOLO_STORAGE_1.get(), HOLO_STORAGE_2.get(), HOLO_STORAGE_3.get()).build(null));

    public static final RegistryObject<BaseBlock> SMELTER = BLOCKS.register("smelter", SmelterTile::createBlock);
    public static final RegistryObject<Item> SMELTER_ITEM = ITEMS.register("smelter", () -> new BlockItem(SMELTER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<SmelterTile>> SMELTER_TILE = TILES.register("smelter", () -> BlockEntityType.Builder.of(SmelterTile::new, SMELTER.get()).build(null));

    public static final RegistryObject<BaseBlock> ELEVATOR = BLOCKS.register("elevator", ElevatorTile::createBlock);
    public static final RegistryObject<Item> ELEVATOR_ITEM = ITEMS.register("elevator", () -> new BlockItem(ELEVATOR.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<ElevatorTile>> ELEVATOR_TILE = TILES.register("elevator", () -> BlockEntityType.Builder.of(ElevatorTile::new, ELEVATOR.get()).build(null));

    public static final RegistryObject<BaseBlock> LEVEL_MARKER = BLOCKS.register("level_marker", LevelMarkerTile::createBlock);
    public static final RegistryObject<Item> LEVEL_MARKER_ITEM = ITEMS.register("level_marker", () -> new BlockItem(LEVEL_MARKER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<LevelMarkerTile>> LEVEL_MARKER_TILE = TILES.register("level_marker", () -> BlockEntityType.Builder.of(LevelMarkerTile::new, LEVEL_MARKER.get()).build(null));

    public static final RegistryObject<BaseBlock> INVISIBLE_DOOR = BLOCKS.register("invisible_door", InvisibleDoorTile::createBlock);
    public static final RegistryObject<BlockEntityType<InvisibleDoorTile>> INVISIBLE_DOOR_TILE = TILES.register("invisible_door", () -> BlockEntityType.Builder.of(InvisibleDoorTile::new, INVISIBLE_DOOR.get()).build(null));

    public static final RegistryObject<BaseBlock> FORCEFIELD = BLOCKS.register("forcefield", ForceFieldTile::createBlock);
    public static final RegistryObject<Item> FORCEFIELD_ITEM = ITEMS.register("forcefield", () -> new BlockItem(FORCEFIELD.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<ForceFieldTile>> FORCEFIELD_TILE = TILES.register("forcefield", () -> BlockEntityType.Builder.of(ForceFieldTile::new, FORCEFIELD.get()).build(null));

    public static final RegistryObject<BaseBlock> WARPER = BLOCKS.register("warper", WarperTile::createBlock);
    public static final RegistryObject<Item> WARPER_ITEM = ITEMS.register("warper", () -> new BlockItem(WARPER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<WarperTile>> WARPER_TILE = TILES.register("warper", () -> BlockEntityType.Builder.of(WarperTile::new, WARPER.get()).build(null));

    public static final RegistryObject<BaseBlock> LOCK = BLOCKS.register("lock", LockTile::createBlock);
    public static final RegistryObject<Item> LOCK_ITEM = ITEMS.register("lock", () -> new BlockItem(LOCK.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<LockTile>> LOCK_TILE = TILES.register("lock", () -> BlockEntityType.Builder.of(LockTile::new, LOCK.get()).build(null));

    public static final RegistryObject<BaseBlock> SIGNAL_RECEIVER = BLOCKS.register("signal_receiver", SignalReceiverTile::createBlock);
    public static final RegistryObject<Item> SIGNAL_RECEIVER_ITEM = ITEMS.register("signal_receiver", () -> new BlockItem(SIGNAL_RECEIVER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<SignalReceiverTile>> SIGNAL_RECEIVER_TILE = TILES.register("signal_receiver", () -> BlockEntityType.Builder.of(SignalReceiverTile::new, SIGNAL_RECEIVER.get()).build(null));

    public static final RegistryObject<BaseBlock> SIGNAL_TRANSMITTER = BLOCKS.register("signal_transmitter", SignalTransmitterTile::createBlock);
    public static final RegistryObject<Item> SIGNAL_TRANSMITTER_ITEM = ITEMS.register("signal_transmitter", () -> new BlockItem(SIGNAL_TRANSMITTER.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<SignalTransmitterTile>> SIGNAL_TRANSMITTER_TILE = TILES.register("signal_transmitter", () -> BlockEntityType.Builder.of(SignalTransmitterTile::new, SIGNAL_TRANSMITTER.get()).build(null));

    public static final RegistryObject<BaseBlock> WIRELESS_BUTTON = BLOCKS.register("wireless_button", WirelessButtonTile::createBlock);
    public static final RegistryObject<Item> WIRELESS_BUTTON_ITEM = ITEMS.register("wireless_button", () -> new BlockItem(WIRELESS_BUTTON.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<WirelessButtonTile>> WIRELESS_BUTTON_TILE = TILES.register("wireless_button", () -> BlockEntityType.Builder.of(WirelessButtonTile::new, WIRELESS_BUTTON.get()).build(null));

    public static final RegistryObject<BaseBlock> WIRELESS_LOCK = BLOCKS.register("wireless_lock", WirelessLockTile::createBlock);
    public static final RegistryObject<Item> WIRELESS_LOCK_ITEM = ITEMS.register("wireless_lock", () -> new BlockItem(WIRELESS_LOCK.get(), createStandardProperties()));
    public static final RegistryObject<BlockEntityType<WirelessLockTile>> WIRELESS_LOCK_TILE = TILES.register("wireless_lock", () -> BlockEntityType.Builder.of(WirelessLockTile::new, WIRELESS_LOCK.get()).build(null));

    public static final RegistryObject<BaseBlock> FLAT_LIGHT = BLOCKS.register("flatlight", Registration::createFlatLightBlock);

    public static final RegistryObject<Item> FLAT_LIGHT_ITEM = ITEMS.register("flatlight", () -> new BlockItem(FLAT_LIGHT.get(), createStandardProperties()));

    public static final RegistryObject<NetCableBlock> NETCABLE = BLOCKS.register("netcable", NetCableBlock::new);
    public static final RegistryObject<ConnectorBlock> CONNECTOR = BLOCKS.register("connector", ConnectorBlock::new);

    public static final RegistryObject<Item> NETCABLE_NEGARITE = ITEMS.register("netcable_negarite", () -> new CableBlockItem(NETCABLE.get(), createStandardProperties(), CableColor.NEGARITE));
    public static final RegistryObject<Item> NETCABLE_POSIRITE = ITEMS.register("netcable_posirite", () -> new CableBlockItem(NETCABLE.get(), createStandardProperties(), CableColor.POSIRITE));
    public static final RegistryObject<Item> NETCABLE_COMBINED = ITEMS.register("netcable_combined", () -> new CableBlockItem(NETCABLE.get(), createStandardProperties(), CableColor.COMBINED));
    public static final RegistryObject<Item> NETCABLE_DATA = ITEMS.register("netcable_data", () -> new CableBlockItem(NETCABLE.get(), createStandardProperties(), CableColor.DATA));
    public static final RegistryObject<Item> CONNECTOR_NEGARITE = ITEMS.register("connector_negarite", () -> new CableBlockItem(CONNECTOR.get(), createStandardProperties(), CableColor.NEGARITE));
    public static final RegistryObject<Item> CONNECTOR_POSIRITE = ITEMS.register("connector_posirite", () -> new CableBlockItem(CONNECTOR.get(), createStandardProperties(), CableColor.POSIRITE));
    public static final RegistryObject<Item> CONNECTOR_COMBINED = ITEMS.register("connector_combined", () -> new CableBlockItem(CONNECTOR.get(), createStandardProperties(), CableColor.COMBINED));
    public static final RegistryObject<Item> CONNECTOR_DATA = ITEMS.register("connector_data", () -> new CableBlockItem(CONNECTOR.get(), createStandardProperties(), CableColor.DATA));

    public static final RegistryObject<BlockEntityType<NetCableTileEntity>> NETCABLE_TILE = TILES.register("netcable", () -> BlockEntityType.Builder.of(NetCableTileEntity::new, NETCABLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ConnectorTileEntity>> CONNECTOR_TILE = TILES.register("connector", () -> BlockEntityType.Builder.of(ConnectorTileEntity::new, CONNECTOR.get()).build(null));
    public static final RegistryObject<FacadeBlock> FACADE = BLOCKS.register("facade", FacadeBlock::new);
    public static final RegistryObject<Item> FACADE_ITEM = ITEMS.register("facade", () -> new FacadeItemBlock(FACADE.get()));

    public static final RegistryObject<Item> INGOT_PLATINUM = ITEMS.register("ingot_platinum", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> INGOT_LITHIUM = ITEMS.register("ingot_lithium", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> INGOT_MANGANESE = ITEMS.register("ingot_manganese", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> INGOT_SILVER = ITEMS.register("ingot_silver", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> DUST_SILICON = ITEMS.register("silicon", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> DUST_NEGARITE = ITEMS.register("dust_negarite", () -> new Item(createStandardProperties()));
    public static final RegistryObject<Item> DUST_POSIRITE = ITEMS.register("dust_posirite", () -> new Item(createStandardProperties()));
    public static final RegistryObject<EnergySabreItem> ENERGY_SABRE = ITEMS.register("energy_sabre", EnergySabreItem::new);
    public static final RegistryObject<EnergyLanceItem> ENERGY_LANCE = ITEMS.register("energy_lance", EnergyLanceItem::new);
    public static final RegistryObject<EnhancedEnergySabreItem> ENHANCED_ENERGY_SABRE = ITEMS.register("enhanced_energy_sabre", EnhancedEnergySabreItem::new);
    public static final RegistryObject<KeyCardItem> KEY_CARD = ITEMS.register("key_card", KeyCardItem::new);
    public static final RegistryObject<BlueprintItem> BLUEPRINT = ITEMS.register("blueprint", BlueprintItem::new);
    public static final RegistryObject<TabletItem> TABLET = ITEMS.register("tablet", TabletItem::new);
    public static final RegistryObject<ArientePearlItem> ARIENTE_PEARL = ITEMS.register("ariente_pearl", ArientePearlItem::new);
    public static final RegistryObject<FluxLevitatorItem> FLUX_LEVITATOR = ITEMS.register("flux_levitator", FluxLevitatorItem::new);
    public static final RegistryObject<FluxShipItem> FLUX_SHIP = ITEMS.register("flux_ship", FluxShipItem::new);
    public static final RegistryObject<FluxCapacitorItem> FLUX_CAPACITOR = ITEMS.register("flux_capacitor", FluxCapacitorItem::new);
    public static final RegistryObject<CircuitItem> CIRCUIT = ITEMS.register("circuit", () -> new CircuitItem(false));
    public static final RegistryObject<CircuitItem> ADVANCED_CIRCUIT = ITEMS.register("advanced_circuit", () -> new CircuitItem(true));
    public static final RegistryObject<EnergyHolderItem> ENERGY_HOLDER = ITEMS.register("energy_holder", EnergyHolderItem::new);
    public static final RegistryObject<PowerSuit> POWERSUIT_HEAD = ITEMS.register("powersuit_head", () -> new PowerSuit(EquipmentSlot.HEAD));
    public static final RegistryObject<PowerSuit> POWERSUIT_CHEST = ITEMS.register("powersuit_chest", () -> new PowerSuit(EquipmentSlot.CHEST));
    public static final RegistryObject<PowerSuit> POWERSUIT_LEGS = ITEMS.register("powersuit_legs", () -> new PowerSuit(EquipmentSlot.LEGS));
    public static final RegistryObject<PowerSuit> POWERSUIT_FEET = ITEMS.register("powersuit_feet", () -> new PowerSuit(EquipmentSlot.FEET));
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
    public static final RegistryObject<ArmorModuleItem> MODULE_SPEED = ITEMS.register("module_speed", () -> new ArmorModuleItem(ArmorUpgradeType.SPEED));
    public static final RegistryObject<ArmorModuleItem> MODULE_STEPASSIST = ITEMS.register("module_stepassist", () -> new ArmorModuleItem(ArmorUpgradeType.STEPASSIST));
    public static final RegistryObject<ArmorModuleItem> MODULE_INHIBIT = ITEMS.register("module_inhibit", () -> new ArmorModuleItem(ArmorUpgradeType.INHIBIT));
    public static final RegistryObject<ArmorModuleItem> MODULE_POWER = ITEMS.register("module_power", () -> new ArmorModuleItem(ArmorUpgradeType.POWER));
    public static final RegistryObject<ArmorModuleItem> MODULE_LOOTING = ITEMS.register("module_looting", () -> new ArmorModuleItem(ArmorUpgradeType.LOOTING));
    public static final RegistryObject<ArmorModuleItem> MODULE_FIRE = ITEMS.register("module_fire", () -> new ArmorModuleItem(ArmorUpgradeType.FIRE));

    public static final RegistryObject<EntityType<MasterSoldierEntity>> ENTITY_MASTER_SOLDIER = ENTITIES.register("master_soldier", () -> EntityType.Builder.of(MasterSoldierEntity::new, MobCategory.MONSTER)
            .sized(0.7F, 2.7F)
            .setShouldReceiveVelocityUpdates(false)
            .build("master_soldier"));
    public static final RegistryObject<EntityType<SoldierEntity>> ENTITY_SOLDIER = ENTITIES.register("soldier", () -> EntityType.Builder.of(SoldierEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .setShouldReceiveVelocityUpdates(false)
            .build("soldier"));
    public static final RegistryObject<EntityType<SentinelDroneEntity>> ENTITY_SENTINEL_DRONE = ENTITIES.register("ariente_sentinel_drone", () -> EntityType.Builder.of(SentinelDroneEntity::new, MobCategory.MISC)
            .sized(1.3F, 1.3F)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_sentinel_drone"));
    public static final RegistryObject<EntityType<DroneEntity>> ENTITY_DRONE = ENTITIES.register("ariente_drone", () -> EntityType.Builder.of(DroneEntity::new, MobCategory.MISC)
            .sized(2.0F, 2.0F)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_drone"));
    public static final RegistryObject<EntityType<FluxElevatorEntity>> ENTITY_ELEVATOR = ENTITIES.register("ariente_flux_elevator", () -> EntityType.Builder.of(FluxElevatorEntity::new, MobCategory.MISC)
            .sized(1.30F, 0.9F)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_flux_elevator"));
    public static final RegistryObject<EntityType<EntityArientePearl>> ENTITY_PEARL = ENTITIES.register("ariente_ariente_pearl", () -> EntityType.Builder.of(EntityArientePearl::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_ariente_pearl"));
    public static final RegistryObject<EntityType<FluxShipEntity>> ENTITY_FLUX_SHIP = ENTITIES.register("ariente_flux_ship", () -> EntityType.Builder.of(FluxShipEntity::new, MobCategory.MISC)
            .sized(5.30F, 1.8F)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_flux_ship"));
    public static final RegistryObject<EntityType<FluxLevitatorEntity>> ENTITY_FLUX_LEVITATOR = ENTITIES.register("ariente_flux_levitator", () -> EntityType.Builder.of(FluxLevitatorEntity::new, MobCategory.MISC)
            .sized(1.30F, 0.9F)
            .updateInterval(1)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_flux_levitator"));
    public static final RegistryObject<EntityType<LaserEntity>> ENTITY_LASER = ENTITIES.register("ariente_laser", () -> EntityType.Builder.of(LaserEntity::new, MobCategory.MISC)
            .sized(1, 1)
            .updateInterval(1)
            .setShouldReceiveVelocityUpdates(true)
            .build("ariente_laser"));

    public static Item.Properties createStandardProperties() {
        return new Item.Properties();
    }

    private static final MountedPanelShapes FLAT_LIGHT_SHAPES = new MountedPanelShapes(1, 2);

    private static BaseBlock createFlatLightBlock() {
        return new BaseBlock(new BlockBuilder()
                .properties(Block.Properties.of().mapColor(MapColor.NONE).lightLevel((light) -> { return 15; }))) {
            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return FLAT_LIGHT_SHAPES.get(state.getValue(BlockStateProperties.FACING));
            }

        };
    }

}
