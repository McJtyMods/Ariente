package mcjty.ariente.setup;

import mcjty.ariente.blocks.decorative.*;
import mcjty.ariente.blocks.generators.*;
import mcjty.ariente.blocks.utility.*;
import mcjty.ariente.blocks.utility.autofield.*;
import mcjty.ariente.cables.*;
import mcjty.ariente.items.*;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.blocks.ArienteBlockEntity;
import mcjty.ariente.blocks.BaseOreBlock;
import mcjty.ariente.blocks.MountedPanelShapes;
import mcjty.ariente.blocks.aicore.AICoreTile;
import mcjty.ariente.blocks.defense.ForceFieldTile;
import mcjty.ariente.blocks.purifier.PurifierTile;
import mcjty.ariente.blocks.purifier.PurifierTubeBlock;
import mcjty.ariente.blocks.purifier.PurifierTubeTile;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.blocks.utility.camera.CameraTile;
import mcjty.ariente.blocks.utility.door.DoorMarkerTile;
import mcjty.ariente.blocks.utility.door.InvisibleDoorTile;
import mcjty.ariente.blocks.utility.wireless.SignalReceiverTile;
import mcjty.ariente.blocks.utility.wireless.SignalTransmitterTile;
import mcjty.ariente.blocks.utility.wireless.WirelessButtonTile;
import mcjty.ariente.blocks.utility.wireless.WirelessLockTile;
import mcjty.ariente.display.AlienTextDisplayModule;
import mcjty.ariente.display.CameraDisplayModule;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.FluidDisplayModule;
import mcjty.ariente.display.ForgeEnergyDisplayModule;
import mcjty.ariente.display.PowerDisplayModule;
import mcjty.ariente.display.StorageDisplayModule;
import mcjty.ariente.display.TextDisplayModule;
import mcjty.ariente.entities.EntityArientePearl;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.entities.fluxelevator.FluxElevatorEntity;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.levitator.FluxLevitatorEntity;
import mcjty.ariente.facade.FacadeBlock;
import mcjty.ariente.facade.FacadeItemBlock;
import mcjty.ariente.items.BlockStateItem;
import mcjty.ariente.items.armor.PowerSuit;
import mcjty.ariente.items.modules.ArmorModuleItem;
import mcjty.ariente.loot.DungeonLootModifier;
import mcjty.ariente.multipart.MultipartBlock;
import mcjty.ariente.multipart.MultipartItemBlock;
import mcjty.ariente.multipart.MultipartTE;
import mcjty.ariente.oregen.WorldGen;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.sounds.ModSounds;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.ariente.Ariente.MODID;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class Registration {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> DUNGEON_LOOT = LOOT_MODIFIERS.register("dungeon_loot", () -> DungeonLootModifier.CODEC);

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final Supplier<DataComponentType<CustomData>> MACHINE_DATA = COMPONENTS.register("machine_data",
            () -> DataComponentType.<CustomData>builder()
                    .persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC).build());

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MODID);
    public static final Supplier<SimpleParticleType> PURIFIER_RING_X = PARTICLES.register("purifier_ring_x", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> PURIFIER_RING_Z = PARTICLES.register("purifier_ring_z", () -> new SimpleParticleType(false));

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final Supplier<CreativeModeTab> TAB = TABS.register("ariente", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ariente"))
            .icon(() -> new ItemStack(Registration.POSIRITE_GENERATOR.get()))
            .displayItems((parameters, output) -> {
                ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                BlueprintRecipeRegistry.getRecipes().forEach(recipe -> output.accept(BlueprintItem.makeBluePrint(recipe.getDestination())));
            })
            .build());

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
        LOOT_MODIFIERS.register(bus);
        WorldGen.register(bus);
        bus.addListener(Registration::registerCapabilities);
        ModSounds.register();
        TABS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
        CONTAINERS.register(bus);
        SOUNDS.register(bus);
        ENTITIES.register(bus);
        PARTICLES.register(bus);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (var type : TILES.getEntries()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type.get(),
                    (tile, side) -> tile instanceof ArienteBlockEntity machine ? machine.getAutomationHandler(side) : null);
        }
    }

    public static final TagKey<Item> TAG_INGOT_SILVER = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/silver"));
    public static final TagKey<Item> TAG_INGOT_PLATINUM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/platinum"));
    public static final TagKey<Item> TAG_INGOT_LITHIUM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/lithium"));
    public static final TagKey<Item> TAG_INGOT_MANGANESE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/manganese"));
    public static final TagKey<Item> TAG_RAW_SILVER = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/silver"));
    public static final TagKey<Item> TAG_RAW_PLATINUM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/platinum"));
    public static final TagKey<Item> TAG_RAW_LITHIUM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/lithium"));
    public static final TagKey<Item> TAG_RAW_MANGANESE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "raw_materials/manganese"));
    public static final TagKey<Item> TAG_DUSTS_SILICON = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/silicon"));
    public static final TagKey<Item> TAG_DUSTS_NEGARITE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/negarite"));
    public static final TagKey<Item> TAG_DUSTS_POSIRITE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "dusts/posirite"));
    public static final TagKey<Block> TAG_ORE_SILVER = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/silver"));
    public static final TagKey<Block> TAG_ORE_PLATINUM = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/platinum"));
    public static final TagKey<Block> TAG_ORE_LITHIUM = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/lithium"));
    public static final TagKey<Block> TAG_ORE_MANGANESE = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/manganese"));
    public static final TagKey<Block> TAG_ORE_SILICON = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/silicon"));
    public static final TagKey<Block> TAG_ORE_NEGARITE = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/negarite"));
    public static final TagKey<Block> TAG_ORE_POSIRITE = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/posirite"));
    public static final TagKey<Item> TAG_ORE_SILVER_ITEM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/silver"));
    public static final TagKey<Item> TAG_ORE_PLATINUM_ITEM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/platinum"));
    public static final TagKey<Item> TAG_ORE_LITHIUM_ITEM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/lithium"));
    public static final TagKey<Item> TAG_ORE_MANGANESE_ITEM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/manganese"));
    public static final TagKey<Item> TAG_ORE_SILICON_ITEM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/silicon"));
    public static final TagKey<Item> TAG_ORE_NEGARITE_ITEM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/negarite"));
    public static final TagKey<Item> TAG_ORE_POSIRITE_ITEM = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/posirite"));
    public static final TagKey<Item> TAG_MARBLE = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "marble"));

    public static final AABB FLAT_BLOCK_AABB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);
    public static final AABB BEAM_BLOCK_NS_AABB = new AABB(0.0D, 0.0D, 0.3D, 1.0D, 0.3D, 0.7D);
    public static final AABB BEAM_BLOCK_EW_AABB = new AABB(0.3D, 0.0D, 0.0D, 0.7D, 0.3D, 1.0D);

    public static final AABB LIGHT_BLOCK_DOWN = new AABB(0.125F, 0.0F, 0.125F, 0.875F, 0.125F, 0.875F);
    public static final AABB LIGHT_BLOCK_UP = new AABB(0.125F, 0.875F, 0.125F, 0.875F, 1.0F, 0.875F);
    public static final AABB LIGHT_BLOCK_NORTH = new AABB(0.125F, 0.125F, 0.0F, 0.875F, 0.875F, 0.125F);
    public static final AABB LIGHT_BLOCK_SOUTH = new AABB(0.125F, 0.125F, 0.875F, 0.875F, 0.8750F, 1.0F);
    public static final AABB LIGHT_BLOCK_WEST = new AABB(0.0F, 0.125F, 0.125F, 0.125F, 0.875F, 0.8750F);
    public static final AABB LIGHT_BLOCK_EAST = new AABB(0.875F, 0.125F, 0.125F, 1.0F, 0.875F, 0.8750F);

    public static final Supplier<MultipartBlock> MULTIPART_BLOCK = BLOCKS.register("multipart", MultipartBlock::new);
    public static final Supplier<BlockEntityType<MultipartTE>> TYPE_MULTIPART = TILES.register("multipart", () -> BlockEntityType.Builder.of(MultipartTE::new, MULTIPART_BLOCK.get()).build(null));

    public static final Supplier<PatternBlock> PATTERN = BLOCKS.register("pattern", PatternBlock::new);
    public static final Map<PatternType, Supplier<BlockStateItem>> PATTERN_ITEMS = Arrays.stream(PatternType.values())
            .map(type -> Pair.of(type, ITEMS.register("pattern_" + type.getSerializedName(), () -> new BlockStateItem(PATTERN.get().defaultBlockState().setValue(PatternBlock.TYPE, type), createStandardProperties()))))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

    public static final Supplier<BaseBlock> ORE_LITHIUM = BLOCKS.register("lithiumore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_LITHIUM_ITEM = ITEMS.register("lithiumore", () -> new BlockItem(ORE_LITHIUM.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> ORE_MANGANESE = BLOCKS.register("manganeseore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_MANGANESE_ITEM = ITEMS.register("manganeseore", () -> new BlockItem(ORE_MANGANESE.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> ORE_SILICON = BLOCKS.register("siliconore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_SILICON_ITEM = ITEMS.register("siliconore", () -> new BlockItem(ORE_SILICON.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> ORE_SILVER = BLOCKS.register("silverore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_SILVER_ITEM = ITEMS.register("silverore", () -> new BlockItem(ORE_SILVER.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> ORE_PLATINUM = BLOCKS.register("platinumore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_PLATINUM_ITEM = ITEMS.register("platinumore", () -> new BlockItem(ORE_PLATINUM.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> ORE_POSIRITE = BLOCKS.register("posirite", BaseOreBlock::new);
    public static final Supplier<Item> ORE_POSIRITE_ITEM = ITEMS.register("posirite", () -> new BlockItem(ORE_POSIRITE.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> ORE_NEGARITE = BLOCKS.register("negarite", BaseOreBlock::new);
    public static final Supplier<Item> ORE_NEGARITE_ITEM = ITEMS.register("negarite", () -> new BlockItem(ORE_NEGARITE.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> FLUX_BEAM = BLOCKS.register("flux_beam", () -> new BaseBeamBlock(new BlockBuilder(), false));
    public static final Supplier<Item> FLUX_BEAM_ITEM = ITEMS.register("flux_beam", () -> new BlockItem(FLUX_BEAM.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> FLUX_BEND_BEAM = BLOCKS.register("flux_bend_beam", () -> new BaseBeamBlock(new BlockBuilder(), true));
    public static final Supplier<Item> FLUX_BEND_BEAM_ITEM = ITEMS.register("flux_bend_beam", () -> new BlockItem(FLUX_BEND_BEAM.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> FLUX_SLOPE_BEAM = BLOCKS.register("flux_slope_beam", FluxSlopeBeamBlock::new);
    public static final Supplier<Item> FLUX_SLOPE_BEAM_ITEM = ITEMS.register("flux_slope_beam", () -> new BlockItem(FLUX_SLOPE_BEAM.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> SENSOR_ITEM_NODE = BLOCKS.register("sensor_item_node", SensorItemNodeTile::createBlock);
    public static final Supplier<Item> SENSOR_ITEM_NODE_ITEM = ITEMS.register("sensor_item_node", () -> new MultipartItemBlock(SENSOR_ITEM_NODE.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<SensorItemNodeTile>> SENSOR_ITEM_TILE = TILES.register("sensor_item_node", () -> BlockEntityType.Builder.of(SensorItemNodeTile::new, SENSOR_ITEM_NODE.get()).build(null));

    public static final Supplier<BaseBlock> INPUT_ITEM_NODE = BLOCKS.register("input_item_node", InputItemNodeTile::createBlock);
    public static final Supplier<Item> INPUT_ITEM_NODE_ITEM = ITEMS.register("input_item_node", () -> new MultipartItemBlock(INPUT_ITEM_NODE.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<InputItemNodeTile>> INPUT_ITEM_TILE = TILES.register("input_item_node", () -> BlockEntityType.Builder.of(InputItemNodeTile::new, INPUT_ITEM_NODE.get()).build(null));

    public static final Supplier<BaseBlock> OUTPUT_ITEM_NODE = BLOCKS.register("output_item_node", () -> new BaseNodeBlock(new BlockBuilder()
            .tileEntitySupplier(OutputItemNodeTile::new)));
    public static final Supplier<Item> OUTPUT_ITEM_NODE_ITEM = ITEMS.register("output_item_node", () -> new MultipartItemBlock(OUTPUT_ITEM_NODE.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<OutputItemNodeTile>> OUTPUT_ITEM_TILE = TILES.register("output_item_node", () -> BlockEntityType.Builder.of(OutputItemNodeTile::new, OUTPUT_ITEM_NODE.get()).build(null));

    public static final Supplier<BaseBlock> ROUND_ROBIN_NODE = BLOCKS.register("round_robin_node", () -> new BaseNodeBlock(new BlockBuilder()
            .topDriver(DRIVER)
            .tileEntitySupplier(RoundRobinNodeTile::new)));
    public static final Supplier<Item> ROUND_ROBIN_NODE_ITEM = ITEMS.register("round_robin_node", () -> new MultipartItemBlock(ROUND_ROBIN_NODE.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<RoundRobinNodeTile>> ROUND_ROBIN_TILE = TILES.register("round_robin_node", () -> BlockEntityType.Builder.of(RoundRobinNodeTile::new, ROUND_ROBIN_NODE.get()).build(null));

    public static final Supplier<BaseBlock> FIELD_MARKER = BLOCKS.register("field_marker", FieldMarkerTile::createBlock);
    public static final Supplier<Item> FIELD_MARKER_ITEM = ITEMS.register("field_marker", () -> new MultipartItemBlock(FIELD_MARKER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<FieldMarkerTile>> FIELD_MARKER_TILE = TILES.register("field_marker", () -> BlockEntityType.Builder.of(FieldMarkerTile::new, FIELD_MARKER.get()).build(null));

    public static final Supplier<RampBlock> RAMP = BLOCKS.register("ramp", RampBlock::new);
    public static final Supplier<Item> RAMP_ITEM = ITEMS.register("ramp", () -> new BlockItem(RAMP.get(), createStandardProperties()));

    public static final Supplier<SlopeBlock> SLOPE = BLOCKS.register("slope", SlopeBlock::new);
    public static final Supplier<Item> SLOPE_ITEM = ITEMS.register("slope", () -> new BlockItem(SLOPE.get(), createStandardProperties()));

    public static final Supplier<PaneBlock> GLASS_FENCE = BLOCKS.register("glass_fence", () -> new PaneBlock(MapColor.NONE, SoundType.GLASS));
    public static final Supplier<Item> GLASS_FENCE_ITEM = ITEMS.register("glass_fence", () -> new BlockItem(GLASS_FENCE.get(), createStandardProperties()));

    public static final Supplier<PaneBlock> BLUE_GLASS_FENCE = BLOCKS.register("blue_glass_fence", () -> new PaneBlock(MapColor.NONE, SoundType.GLASS));
    public static final Supplier<Item> BLUE_GLASS_FENCE_ITEM = ITEMS.register("blue_glass_fence", () -> new BlockItem(BLUE_GLASS_FENCE.get(), createStandardProperties()));

    public static final Supplier<PaneBlock> MARBLE_FENCE = BLOCKS.register("marble_fence", () -> new PaneBlock(MapColor.STONE, SoundType.STONE));
    public static final Supplier<Item> MARBLE_FENCE_ITEM = ITEMS.register("marble_fence", () -> new BlockItem(MARBLE_FENCE.get(), createStandardProperties()));

    public static final Supplier<PaneBlock> TECH_FENCE = BLOCKS.register("tech_fence", () -> new PaneBlock(MapColor.STONE, SoundType.STONE));
    public static final Supplier<Item> TECH_FENCE_ITEM = ITEMS.register("tech_fence", () -> new BlockItem(TECH_FENCE.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> REINFORCED_MARBLE = BLOCKS.register("reinforced_marble", () -> new BaseBlock(new BlockBuilder()
            .properties(Block.Properties.of().mapColor(MapColor.STONE).strength(80.0f, 3000.0f))
    ) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });
    public static final Supplier<Item> REINFORCED_MARBLE_ITEM = ITEMS.register("reinforced_marble", () -> new BlockItem(REINFORCED_MARBLE.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> FLUX_GLOW = BLOCKS.register("fluxglow", () -> new BaseBlock(new BlockBuilder()
        .properties(Block.Properties.of().mapColor(MapColor.NONE).lightLevel((light) -> { return 15; }))) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });
    public static final Supplier<Item> FLUX_GLOW_ITEM = ITEMS.register("fluxglow", () -> new BlockItem(FLUX_GLOW.get(), createStandardProperties()));

    public static final Supplier<BaseBlock> POWER_COMBINER = BLOCKS.register("power_combiner", PowerCombinerTile::createBlock);
    public static final Supplier<Item> POWER_COMBINER_ITEM = ITEMS.register("power_combiner", () -> new BlockItem(POWER_COMBINER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<PowerCombinerTile>> POWER_COMBINER_TILE = TILES.register("power_combiner", () -> BlockEntityType.Builder.of(PowerCombinerTile::new, POWER_COMBINER.get()).build(null));

    public static final Supplier<BaseBlock> NEGARITE_GENERATOR = BLOCKS.register("negarite_generator", NegariteGeneratorTile::createBlock);
    public static final Supplier<Item> NEGARITE_GENERATOR_ITEM = ITEMS.register("negarite_generator", () -> new BlockItem(NEGARITE_GENERATOR.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<NegariteGeneratorTile>> NEGARITE_GENERATOR_TILE = TILES.register("negarite_generator", () -> BlockEntityType.Builder.of(NegariteGeneratorTile::new, NEGARITE_GENERATOR.get()).build(null));

    public static final Supplier<BaseBlock> POSIRITE_GENERATOR = BLOCKS.register("posirite_generator", PosiriteGeneratorTile::createBlock);
    public static final Supplier<Item> POSIRITE_GENERATOR_ITEM = ITEMS.register("posirite_generator", () -> new BlockItem(POSIRITE_GENERATOR.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<PosiriteGeneratorTile>> POSIRITE_GENERATOR_TILE = TILES.register("posirite_generator", () -> BlockEntityType.Builder.of(PosiriteGeneratorTile::new, POSIRITE_GENERATOR.get()).build(null));

    public static final Supplier<BaseBlock> NEGARITE_TANK = BLOCKS.register("negarite_tank", NegariteTankTile::createBlock);
    public static final Supplier<Item> NEGARITE_TANK_ITEM = ITEMS.register("negarite_tank", () -> new BlockItem(NEGARITE_TANK.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<NegariteTankTile>> NEGARITE_TANK_TILE = TILES.register("negarite_tank", () -> BlockEntityType.Builder.of(NegariteTankTile::new, NEGARITE_TANK.get()).build(null));

    public static final Supplier<BaseBlock> POSIRITE_TANK = BLOCKS.register("posirite_tank", PosiriteTankTile::createBlock);
    public static final Supplier<Item> POSIRITE_TANK_ITEM = ITEMS.register("posirite_tank", () -> new BlockItem(POSIRITE_TANK.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<PosiriteTankTile>> POSIRITE_TANK_TILE = TILES.register("posirite_tank", () -> BlockEntityType.Builder.of(PosiriteTankTile::new, POSIRITE_TANK.get()).build(null));

    public static final Supplier<BaseBlock> DISPLAY_BASE = BLOCKS.register("display_base", DisplayBaseTile::createBlock);
    public static final Supplier<Item> DISPLAY_BASE_ITEM = ITEMS.register("display_base", () -> new BlockItem(DISPLAY_BASE.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<DisplayBaseTile>> DISPLAY_BASE_TILE = TILES.register("display_base", () -> BlockEntityType.Builder.of(DisplayBaseTile::new, DISPLAY_BASE.get()).build(null));
    public static final Supplier<DisplayModuleItem> DISPLAY_POWER = ITEMS.register("display_module_power", () -> new DisplayModuleItem(new PowerDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_FLUIDS = ITEMS.register("display_module_fluids", () -> new DisplayModuleItem(new FluidDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_FE = ITEMS.register("display_module_fe", () -> new DisplayModuleItem(new ForgeEnergyDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_STORAGE = ITEMS.register("display_module_storage", () -> new DisplayModuleItem(new StorageDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_TEXT = ITEMS.register("display_module_text", () -> new DisplayModuleItem(new TextDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_ALIEN_TEXT = ITEMS.register("display_module_alien_text", () -> new DisplayModuleItem(new AlienTextDisplayModule()));

    public static final Supplier<DisplayModuleItem> DISPLAY_CAMERA = ITEMS.register("display_module_camera", () -> new DisplayModuleItem(new CameraDisplayModule()));
    public static final Supplier<BaseBlock> CAMERA = BLOCKS.register("camera", CameraTile::createBlock);
    public static final Supplier<Item> CAMERA_ITEM = ITEMS.register("camera", () -> new BlockItem(CAMERA.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<CameraTile>> CAMERA_TILE = TILES.register("camera", () -> BlockEntityType.Builder.of(CameraTile::new, CAMERA.get()).build(null));

    public static final Supplier<BaseBlock> DOOR_MARKER = BLOCKS.register("door_marker", DoorMarkerTile::createBlock);
    public static final Supplier<Item> DOOR_MARKER_ITEM = ITEMS.register("door_marker", () -> new BlockItem(DOOR_MARKER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<DoorMarkerTile>> DOOR_MARKER_TILE = TILES.register("door_marker", () -> BlockEntityType.Builder.of(DoorMarkerTile::new, DOOR_MARKER.get()).build(null));

    public static final Supplier<BaseBlock> AICORE = BLOCKS.register("aicore", AICoreTile::createBlock);
    public static final Supplier<Item> AICORE_ITEM = ITEMS.register("aicore", () -> new BlockItem(AICORE.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<AICoreTile>> AICORE_TILE = TILES.register("aicore", () -> BlockEntityType.Builder.of(AICoreTile::new, AICORE.get()).build(null));

    public static final Supplier<BaseBlock> ALARM = BLOCKS.register("alarm", AlarmTile::createBlock);
    public static final Supplier<Item> ALARM_ITEM = ITEMS.register("alarm", () -> new BlockItem(ALARM.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<AlarmTile>> ALARM_TILE = TILES.register("alarm", () -> BlockEntityType.Builder.of(AlarmTile::new, ALARM.get()).build(null));

    public static final Supplier<BaseBlock> CONSTRUCTOR = BLOCKS.register("constructor", ConstructorTile::createBlock);
    public static final Supplier<Item> CONSTRUCTOR_ITEM = ITEMS.register("constructor", () -> new BlockItem(CONSTRUCTOR.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<ConstructorTile>> CONSTRUCTOR_TILE = TILES.register("constructor", () -> BlockEntityType.Builder.of(ConstructorTile::new, CONSTRUCTOR.get()).build(null));

    public static final Supplier<BaseBlock> AUTO_CONSTRUCTOR = BLOCKS.register("auto_constructor", AutoConstructorTile::createBlock);
    public static final Supplier<Item> AUTO_CONSTRUCTOR_ITEM = ITEMS.register("auto_constructor", () -> new BlockItem(AUTO_CONSTRUCTOR.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<AutoConstructorTile>> AUTO_CONSTRUCTOR_TILE = TILES.register("auto_constructor", () -> BlockEntityType.Builder.of(AutoConstructorTile::new, AUTO_CONSTRUCTOR.get()).build(null));

    public static final Supplier<BaseBlock> RECYCLER = BLOCKS.register("recycler", RecyclerTile::createBlock);
    public static final Supplier<Item> RECYCLER_ITEM = ITEMS.register("recycler", () -> new BlockItem(RECYCLER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<RecyclerTile>> RECYCLER_TILE = TILES.register("recycler", () -> BlockEntityType.Builder.of(RecyclerTile::new, RECYCLER.get()).build(null));

    public static final Supplier<BaseBlock> BLUEPRINT_STORAGE = BLOCKS.register("blueprint_storage", BlueprintStorageTile::createBlock);
    public static final Supplier<Item> BLUEPRINT_STORAGE_ITEM = ITEMS.register("blueprint_storage", () -> new BlockItem(BLUEPRINT_STORAGE.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<BlueprintStorageTile>> BLUEPRINT_STORAGE_TILE = TILES.register("blueprint_storage", () -> BlockEntityType.Builder.of(BlueprintStorageTile::new, BLUEPRINT_STORAGE.get()).build(null));

    public static final Supplier<BaseBlock> AUTOMATION_FIELD = BLOCKS.register("automation_field", AutoFieldTile::createBlock);
    public static final Supplier<Item> AUTOMATION_FIELD_ITEM = ITEMS.register("automation_field", () -> new BlockItem(AUTOMATION_FIELD.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<AutoFieldTile>> AUTOFIELD_TILE = TILES.register("automation_field", () -> BlockEntityType.Builder.of(AutoFieldTile::new, AUTOMATION_FIELD.get()).build(null));

    public static final Supplier<BaseBlock> STORAGE = BLOCKS.register("storage", StorageTile::createBlock);
    public static final Supplier<Item> STORAGE_ITEM = ITEMS.register("storage", () -> new BlockItem(STORAGE.get(), createStandardProperties()));
    public static final Supplier<BaseBlock> STORAGE_3X3 = BLOCKS.register("storage_3x3", StorageTile::createBlock);
    public static final Supplier<Item> STORAGE_3X3_ITEM = ITEMS.register("storage_3x3", () -> new BlockItem(STORAGE_3X3.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<StorageTile>> STORAGE_TILE = TILES.register("storage", () -> BlockEntityType.Builder.of(StorageTile::new, STORAGE.get(), STORAGE_3X3.get()).build(null));

    public static final Supplier<BaseBlock> HOLO_STORAGE_1 = BLOCKS.register("holo_storage_1", StorageCubeTile::createBlock);
    public static final Supplier<Item> HOLO_STORAGE_1_ITEM = ITEMS.register("holo_storage_1", () -> new BlockItem(HOLO_STORAGE_1.get(), createStandardProperties()));
    public static final Supplier<BaseBlock> HOLO_STORAGE_2 = BLOCKS.register("holo_storage_2", StorageCubeTile::createBlock);
    public static final Supplier<Item> HOLO_STORAGE_2_ITEM = ITEMS.register("holo_storage_2", () -> new BlockItem(HOLO_STORAGE_2.get(), createStandardProperties()));
    public static final Supplier<BaseBlock> HOLO_STORAGE_3 = BLOCKS.register("holo_storage_3", StorageCubeTile::createBlock);
    public static final Supplier<Item> HOLO_STORAGE_3_ITEM = ITEMS.register("holo_storage_3", () -> new BlockItem(HOLO_STORAGE_3.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<StorageCubeTile>> HOLO_STORAGE_TILE = TILES.register("holo_storage", () -> BlockEntityType.Builder.of(StorageCubeTile::new, HOLO_STORAGE_1.get(), HOLO_STORAGE_2.get(), HOLO_STORAGE_3.get()).build(null));

    public static final Supplier<BaseBlock> SMELTER = BLOCKS.register("smelter", SmelterTile::createBlock);
    public static final Supplier<Item> SMELTER_ITEM = ITEMS.register("smelter", () -> new BlockItem(SMELTER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<SmelterTile>> SMELTER_TILE = TILES.register("smelter", () -> BlockEntityType.Builder.of(SmelterTile::new, SMELTER.get()).build(null));

    public static final Supplier<BaseBlock> PURIFIER_START = BLOCKS.register("purifier_start", PurifierTile::createBlock);
    public static final Supplier<Item> PURIFIER_START_ITEM = ITEMS.register("purifier_start", () -> new BlockItem(PURIFIER_START.get(), createStandardProperties()));
    public static final Supplier<BaseBlock> PURIFIER_FINISH = BLOCKS.register("purifier_finish", PurifierTile::createBlock);
    public static final Supplier<Item> PURIFIER_FINISH_ITEM = ITEMS.register("purifier_finish", () -> new BlockItem(PURIFIER_FINISH.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<PurifierTile>> PURIFIER_TILE = TILES.register("purifier", () -> BlockEntityType.Builder.of(PurifierTile::new, PURIFIER_START.get(), PURIFIER_FINISH.get()).build(null));
    public static final Supplier<PurifierTubeBlock> PURIFIER_TUBE = BLOCKS.register("purifier_tube", PurifierTubeBlock::new);
    public static final Supplier<Item> PURIFIER_TUBE_ITEM = ITEMS.register("purifier_tube", () -> new BlockItem(PURIFIER_TUBE.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<PurifierTubeTile>> PURIFIER_TUBE_TILE = TILES.register("purifier_tube", () -> BlockEntityType.Builder.of(PurifierTubeTile::new, PURIFIER_TUBE.get()).build(null));

    public static final Supplier<BaseBlock> CHARGER = BLOCKS.register("charger", ChargerTile::createBlock);
    public static final Supplier<Item> CHARGER_ITEM = ITEMS.register("charger", () -> new BlockItem(CHARGER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<ChargerTile>> CHARGER_TILE = TILES.register("charger", () -> BlockEntityType.Builder.of(ChargerTile::new, CHARGER.get()).build(null));

    public static final Supplier<BaseBlock> ELEVATOR = BLOCKS.register("elevator", ElevatorTile::createBlock);
    public static final Supplier<Item> ELEVATOR_ITEM = ITEMS.register("elevator", () -> new BlockItem(ELEVATOR.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<ElevatorTile>> ELEVATOR_TILE = TILES.register("elevator", () -> BlockEntityType.Builder.of(ElevatorTile::new, ELEVATOR.get()).build(null));

    public static final Supplier<BaseBlock> LEVEL_MARKER = BLOCKS.register("level_marker", LevelMarkerTile::createBlock);
    public static final Supplier<Item> LEVEL_MARKER_ITEM = ITEMS.register("level_marker", () -> new BlockItem(LEVEL_MARKER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<LevelMarkerTile>> LEVEL_MARKER_TILE = TILES.register("level_marker", () -> BlockEntityType.Builder.of(LevelMarkerTile::new, LEVEL_MARKER.get()).build(null));

    public static final Supplier<BaseBlock> INVISIBLE_DOOR = BLOCKS.register("invisible_door", InvisibleDoorTile::createBlock);
    public static final Supplier<BlockEntityType<InvisibleDoorTile>> INVISIBLE_DOOR_TILE = TILES.register("invisible_door", () -> BlockEntityType.Builder.of(InvisibleDoorTile::new, INVISIBLE_DOOR.get()).build(null));

    public static final Supplier<BaseBlock> FORCEFIELD = BLOCKS.register("forcefield", ForceFieldTile::createBlock);
    public static final Supplier<Item> FORCEFIELD_ITEM = ITEMS.register("forcefield", () -> new BlockItem(FORCEFIELD.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<ForceFieldTile>> FORCEFIELD_TILE = TILES.register("forcefield", () -> BlockEntityType.Builder.of(ForceFieldTile::new, FORCEFIELD.get()).build(null));

    public static final Supplier<BaseBlock> WARPER = BLOCKS.register("warper", WarperTile::createBlock);
    public static final Supplier<Item> WARPER_ITEM = ITEMS.register("warper", () -> new BlockItem(WARPER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<WarperTile>> WARPER_TILE = TILES.register("warper", () -> BlockEntityType.Builder.of(WarperTile::new, WARPER.get()).build(null));

    public static final Supplier<BaseBlock> LOCK = BLOCKS.register("lock", LockTile::createBlock);
    public static final Supplier<Item> LOCK_ITEM = ITEMS.register("lock", () -> new BlockItem(LOCK.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<LockTile>> LOCK_TILE = TILES.register("lock", () -> BlockEntityType.Builder.of(LockTile::new, LOCK.get()).build(null));

    public static final Supplier<BaseBlock> SIGNAL_RECEIVER = BLOCKS.register("signal_receiver", SignalReceiverTile::createBlock);
    public static final Supplier<Item> SIGNAL_RECEIVER_ITEM = ITEMS.register("signal_receiver", () -> new BlockItem(SIGNAL_RECEIVER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<SignalReceiverTile>> SIGNAL_RECEIVER_TILE = TILES.register("signal_receiver", () -> BlockEntityType.Builder.of(SignalReceiverTile::new, SIGNAL_RECEIVER.get()).build(null));

    public static final Supplier<BaseBlock> SIGNAL_TRANSMITTER = BLOCKS.register("signal_transmitter", SignalTransmitterTile::createBlock);
    public static final Supplier<Item> SIGNAL_TRANSMITTER_ITEM = ITEMS.register("signal_transmitter", () -> new BlockItem(SIGNAL_TRANSMITTER.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<SignalTransmitterTile>> SIGNAL_TRANSMITTER_TILE = TILES.register("signal_transmitter", () -> BlockEntityType.Builder.of(SignalTransmitterTile::new, SIGNAL_TRANSMITTER.get()).build(null));

    public static final Supplier<BaseBlock> WIRELESS_BUTTON = BLOCKS.register("wireless_button", WirelessButtonTile::createBlock);
    public static final Supplier<Item> WIRELESS_BUTTON_ITEM = ITEMS.register("wireless_button", () -> new BlockItem(WIRELESS_BUTTON.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<WirelessButtonTile>> WIRELESS_BUTTON_TILE = TILES.register("wireless_button", () -> BlockEntityType.Builder.of(WirelessButtonTile::new, WIRELESS_BUTTON.get()).build(null));

    public static final Supplier<BaseBlock> WIRELESS_LOCK = BLOCKS.register("wireless_lock", WirelessLockTile::createBlock);
    public static final Supplier<Item> WIRELESS_LOCK_ITEM = ITEMS.register("wireless_lock", () -> new BlockItem(WIRELESS_LOCK.get(), createStandardProperties()));
    public static final Supplier<BlockEntityType<WirelessLockTile>> WIRELESS_LOCK_TILE = TILES.register("wireless_lock", () -> BlockEntityType.Builder.of(WirelessLockTile::new, WIRELESS_LOCK.get()).build(null));

    public static final Supplier<BaseBlock> FLAT_LIGHT = BLOCKS.register("flatlight", Registration::createFlatLightBlock);

    public static final Supplier<Item> FLAT_LIGHT_ITEM = ITEMS.register("flatlight", () -> new BlockItem(FLAT_LIGHT.get(), createStandardProperties()));

    public static final Supplier<NetCableBlock> NETCABLE = BLOCKS.register("netcable", () -> new NetCableBlock());
    public static final Supplier<ConnectorBlock> CONNECTOR = BLOCKS.register("connector", ConnectorBlock::new);

    public static final Supplier<Item> NETCABLE_NEGARITE = ITEMS.register("netcable_negarite", () -> new CableBlockItem(NETCABLE.get(), createStandardProperties(), CableColor.NEGARITE));
    public static final Supplier<Item> NETCABLE_POSIRITE = ITEMS.register("netcable_posirite", () -> new CableBlockItem(NETCABLE.get(), createStandardProperties(), CableColor.POSIRITE));
    public static final Supplier<Item> NETCABLE_COMBINED = ITEMS.register("netcable_combined", () -> new CableBlockItem(NETCABLE.get(), createStandardProperties(), CableColor.COMBINED));
    public static final Supplier<Item> NETCABLE_DATA = ITEMS.register("netcable_data", () -> new CableBlockItem(NETCABLE.get(), createStandardProperties(), CableColor.DATA));
    public static final Supplier<Item> CONNECTOR_NEGARITE = ITEMS.register("connector_negarite", () -> new CableBlockItem(CONNECTOR.get(), createStandardProperties(), CableColor.NEGARITE));
    public static final Supplier<Item> CONNECTOR_POSIRITE = ITEMS.register("connector_posirite", () -> new CableBlockItem(CONNECTOR.get(), createStandardProperties(), CableColor.POSIRITE));
    public static final Supplier<Item> CONNECTOR_COMBINED = ITEMS.register("connector_combined", () -> new CableBlockItem(CONNECTOR.get(), createStandardProperties(), CableColor.COMBINED));
    public static final Supplier<Item> CONNECTOR_DATA = ITEMS.register("connector_data", () -> new CableBlockItem(CONNECTOR.get(), createStandardProperties(), CableColor.DATA));

    public static final Supplier<BlockEntityType<NetCableTileEntity>> NETCABLE_TILE = TILES.register("netcable", () -> BlockEntityType.Builder.of(NetCableTileEntity::new, NETCABLE.get()).build(null));
    public static final Supplier<BlockEntityType<ConnectorTileEntity>> CONNECTOR_TILE = TILES.register("connector", () -> BlockEntityType.Builder.of(ConnectorTileEntity::new, CONNECTOR.get()).build(null));
    public static final Supplier<FacadeBlock> FACADE = BLOCKS.register("facade", FacadeBlock::new);
    public static final Supplier<Item> FACADE_ITEM = ITEMS.register("facade", () -> new FacadeItemBlock(FACADE.get()));

    public static final Supplier<Item> INGOT_PLATINUM = ITEMS.register("ingot_platinum", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> INGOT_LITHIUM = ITEMS.register("ingot_lithium", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> INGOT_MANGANESE = ITEMS.register("ingot_manganese", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> INGOT_SILVER = ITEMS.register("ingot_silver", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> RAW_PLATINUM = ITEMS.register("raw_platinum", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> RAW_LITHIUM = ITEMS.register("raw_lithium", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> RAW_MANGANESE = ITEMS.register("raw_manganese", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> RAW_SILVER = ITEMS.register("raw_silver", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> DUST_SILICON = ITEMS.register("silicon", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> DUST_NEGARITE = ITEMS.register("dust_negarite", () -> new Item(createStandardProperties()));
    public static final Supplier<Item> DUST_POSIRITE = ITEMS.register("dust_posirite", () -> new Item(createStandardProperties()));
    public static final Supplier<EnergySabreItem> ENERGY_SABRE = ITEMS.register("energy_sabre", EnergySabreItem::new);
    public static final Supplier<EnergyLanceItem> ENERGY_LANCE = ITEMS.register("energy_lance", EnergyLanceItem::new);
    public static final Supplier<EnhancedEnergySabreItem> ENHANCED_ENERGY_SABRE = ITEMS.register("enhanced_energy_sabre", EnhancedEnergySabreItem::new);
    public static final Supplier<KeyCardItem> KEY_CARD = ITEMS.register("key_card", KeyCardItem::new);
    public static final Supplier<BlueprintItem> BLUEPRINT = ITEMS.register("blueprint", BlueprintItem::new);
    public static final Supplier<TabletItem> TABLET = ITEMS.register("tablet", TabletItem::new);
    public static final Supplier<ArientePearlItem> ARIENTE_PEARL = ITEMS.register("ariente_pearl", ArientePearlItem::new);
    public static final Supplier<FluxLevitatorItem> FLUX_LEVITATOR = ITEMS.register("flux_levitator", FluxLevitatorItem::new);
    public static final Supplier<FluxShipItem> FLUX_SHIP = ITEMS.register("flux_ship", FluxShipItem::new);
    public static final Supplier<FluxCapacitorItem> FLUX_CAPACITOR = ITEMS.register("flux_capacitor", FluxCapacitorItem::new);
    public static final Supplier<CircuitItem> CIRCUIT = ITEMS.register("circuit", () -> new CircuitItem(false));
    public static final Supplier<CircuitItem> ADVANCED_CIRCUIT = ITEMS.register("advanced_circuit", () -> new CircuitItem(true));
    public static final Supplier<ExtremeCircuitItem> EXTREME_CIRCUIT = ITEMS.register("extreme_circuit", ExtremeCircuitItem::new);
    public static final Supplier<EnergyHolderItem> ENERGY_HOLDER = ITEMS.register("energy_holder", EnergyHolderItem::new);
    public static final Supplier<PowerSuit> POWERSUIT_HEAD = ITEMS.register("powersuit_head", () -> new PowerSuit(EquipmentSlot.HEAD));
    public static final Supplier<PowerSuit> POWERSUIT_CHEST = ITEMS.register("powersuit_chest", () -> new PowerSuit(EquipmentSlot.CHEST));
    public static final Supplier<PowerSuit> POWERSUIT_LEGS = ITEMS.register("powersuit_legs", () -> new PowerSuit(EquipmentSlot.LEGS));
    public static final Supplier<PowerSuit> POWERSUIT_FEET = ITEMS.register("powersuit_feet", () -> new PowerSuit(EquipmentSlot.FEET));
    public static final Supplier<ArmorModuleItem> MODULE_ARMOR = ITEMS.register("module_armor", () -> new ArmorModuleItem(ArmorUpgradeType.ARMOR));
    public static final Supplier<ArmorModuleItem> MODULE_ENERGY = ITEMS.register("module_energy", () -> new ArmorModuleItem(ArmorUpgradeType.ENERGY));
    public static final Supplier<ArmorModuleItem> MODULE_FEATHERFALLING = ITEMS.register("module_featherfalling", () -> new ArmorModuleItem(ArmorUpgradeType.FEATHERFALLING));
    public static final Supplier<ArmorModuleItem> MODULE_FLIGHT = ITEMS.register("module_flight", () -> new ArmorModuleItem(ArmorUpgradeType.FLIGHT));
    public static final Supplier<ArmorModuleItem> MODULE_HOVER = ITEMS.register("module_hover", () -> new ArmorModuleItem(ArmorUpgradeType.HOVER));
    public static final Supplier<ArmorModuleItem> MODULE_FORCEFIELD = ITEMS.register("module_forcefield", () -> new ArmorModuleItem(ArmorUpgradeType.FORCEFIELD));
    public static final Supplier<ArmorModuleItem> MODULE_INVISIBILITY = ITEMS.register("module_invisibility", () -> new ArmorModuleItem(ArmorUpgradeType.INVISIBILITY));
    public static final Supplier<ArmorModuleItem> MODULE_NIGHTVISION = ITEMS.register("module_nightvision", () -> new ArmorModuleItem(ArmorUpgradeType.NIGHTVISION));
    public static final Supplier<ArmorModuleItem> MODULE_REGENERATION = ITEMS.register("module_regeneration", () -> new ArmorModuleItem(ArmorUpgradeType.REGENERATION));
    public static final Supplier<ArmorModuleItem> MODULE_SCRAMBLE = ITEMS.register("module_scramble", () -> new ArmorModuleItem(ArmorUpgradeType.SCRAMBLE));
    public static final Supplier<ArmorModuleItem> MODULE_SPEED = ITEMS.register("module_speed", () -> new ArmorModuleItem(ArmorUpgradeType.SPEED));
    public static final Supplier<ArmorModuleItem> MODULE_STEPASSIST = ITEMS.register("module_stepassist", () -> new ArmorModuleItem(ArmorUpgradeType.STEPASSIST));
    public static final Supplier<ArmorModuleItem> MODULE_INHIBIT = ITEMS.register("module_inhibit", () -> new ArmorModuleItem(ArmorUpgradeType.INHIBIT));
    public static final Supplier<ArmorModuleItem> MODULE_POWER = ITEMS.register("module_power", () -> new ArmorModuleItem(ArmorUpgradeType.POWER));
    public static final Supplier<ArmorModuleItem> MODULE_LOOTING = ITEMS.register("module_looting", () -> new ArmorModuleItem(ArmorUpgradeType.LOOTING));
    public static final Supplier<ArmorModuleItem> MODULE_FIRE = ITEMS.register("module_fire", () -> new ArmorModuleItem(ArmorUpgradeType.FIRE));

    public static final Supplier<EntityType<FluxElevatorEntity>> ENTITY_ELEVATOR = ENTITIES.register("ariente_flux_elevator", () -> EntityType.Builder.of(FluxElevatorEntity::new, MobCategory.MISC)
            .sized(1.30F, 0.9F)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_flux_elevator"));
    public static final Supplier<EntityType<EntityArientePearl>> ENTITY_PEARL = ENTITIES.register("ariente_ariente_pearl", () -> EntityType.Builder.of(EntityArientePearl::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_ariente_pearl"));
    public static final Supplier<EntityType<FluxShipEntity>> ENTITY_FLUX_SHIP = ENTITIES.register("ariente_flux_ship", () -> EntityType.Builder.of(FluxShipEntity::new, MobCategory.MISC)
            .sized(5.30F, 1.8F)
            .clientTrackingRange(16)
            .updateInterval(1)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_flux_ship"));
    public static final Supplier<EntityType<HoloGuiEntity>> ENTITY_SHIP_PANEL = ENTITIES.register("flux_ship_panel", () -> EntityType.Builder.<HoloGuiEntity>of(HoloGuiEntity::new, MobCategory.MISC)
            .sized(1, 1)
            .updateInterval(1)
            .setShouldReceiveVelocityUpdates(false)
            .build("flux_ship_panel"));
    public static final Supplier<EntityType<FluxLevitatorEntity>> ENTITY_FLUX_LEVITATOR = ENTITIES.register("ariente_flux_levitator", () -> EntityType.Builder.of(FluxLevitatorEntity::new, MobCategory.MISC)
            .sized(1.30F, 0.9F)
            .updateInterval(1)
            .setShouldReceiveVelocityUpdates(false)
            .build("ariente_flux_levitator"));
    public static final Supplier<EntityType<LaserEntity>> ENTITY_LASER = ENTITIES.register("ariente_laser", () -> EntityType.Builder.of(LaserEntity::new, MobCategory.MISC)
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
