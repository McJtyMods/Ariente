package mcjty.ariente.setup;

import java.util.Set;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import mcjty.lib.setup.DeferredRegister;
import org.apache.commons.lang3.tuple.Pair;

import static mcjty.ariente.Ariente.MODID;
import static mcjty.ariente.compat.ArienteTOPDriver.DRIVER;

public class Registration {


    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final Supplier<DataComponentType<CustomData>> MACHINE_DATA = COMPONENTS.register("machine_data",
            () -> DataComponentType.<CustomData>builder()
                    .persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC).build());

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    public static final Supplier<SimpleParticleType> PURIFIER_RING_X = PARTICLES.register("purifier_ring_x", () -> FabricParticleTypes.simple());
    public static final Supplier<SimpleParticleType> PURIFIER_RING_Z = PARTICLES.register("purifier_ring_z", () -> FabricParticleTypes.simple());

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final Supplier<CreativeModeTab> TAB = TABS.register("ariente", () -> FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ariente"))
            .icon(() -> new ItemStack(Registration.POSIRITE_GENERATOR.get()))
            .displayItems((parameters, output) -> {
                ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                BlueprintRecipeRegistry.getRecipes().forEach(recipe -> output.accept(BlueprintItem.makeBluePrint(recipe.getDestination())));
            })
            .build());

    public static void register() {
        COMPONENTS.register();
        WorldGen.register();
        ModSounds.register();
        TABS.register();
        BLOCKS.register();
        ITEMS.register();
        TILES.register();
        CONTAINERS.register();
        SOUNDS.register();
        ENTITIES.register();
        PARTICLES.register();
        registerCapabilities();
    }

    private static void registerCapabilities() {
        ItemStorage.SIDED.registerForBlockEntities(
                (tile, side) -> tile instanceof ArienteBlockEntity machine ? machine.getItemVariantHandler(side) : null,
                TILES.getEntries().stream().map(Supplier::get).toArray(BlockEntityType<?>[]::new));
    }

    public static final TagKey<Item> TAG_INGOT_SILVER = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/silver"));
    public static final TagKey<Item> TAG_INGOT_PLATINUM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/platinum"));
    public static final TagKey<Item> TAG_INGOT_LITHIUM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/lithium"));
    public static final TagKey<Item> TAG_INGOT_MANGANESE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/manganese"));
    public static final TagKey<Item> TAG_RAW_SILVER = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "raw_materials/silver"));
    public static final TagKey<Item> TAG_RAW_PLATINUM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "raw_materials/platinum"));
    public static final TagKey<Item> TAG_RAW_LITHIUM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "raw_materials/lithium"));
    public static final TagKey<Item> TAG_RAW_MANGANESE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "raw_materials/manganese"));
    public static final TagKey<Item> TAG_DUSTS_SILICON = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/silicon"));
    public static final TagKey<Item> TAG_DUSTS_NEGARITE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/negarite"));
    public static final TagKey<Item> TAG_DUSTS_POSIRITE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/posirite"));
    public static final TagKey<Block> TAG_ORE_SILVER = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores/silver"));
    public static final TagKey<Block> TAG_ORE_PLATINUM = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores/platinum"));
    public static final TagKey<Block> TAG_ORE_LITHIUM = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores/lithium"));
    public static final TagKey<Block> TAG_ORE_MANGANESE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores/manganese"));
    public static final TagKey<Block> TAG_ORE_SILICON = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores/silicon"));
    public static final TagKey<Block> TAG_ORE_NEGARITE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores/negarite"));
    public static final TagKey<Block> TAG_ORE_POSIRITE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores/posirite"));
    public static final TagKey<Item> TAG_ORE_SILVER_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores/silver"));
    public static final TagKey<Item> TAG_ORE_PLATINUM_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores/platinum"));
    public static final TagKey<Item> TAG_ORE_LITHIUM_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores/lithium"));
    public static final TagKey<Item> TAG_ORE_MANGANESE_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores/manganese"));
    public static final TagKey<Item> TAG_ORE_SILICON_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores/silicon"));
    public static final TagKey<Item> TAG_ORE_NEGARITE_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores/negarite"));
    public static final TagKey<Item> TAG_ORE_POSIRITE_ITEM = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ores/posirite"));
    public static final TagKey<Item> TAG_MARBLE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "marble"));

    public static final AABB FLAT_BLOCK_AABB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 0.1D, 1.0D);
    public static final AABB BEAM_BLOCK_NS_AABB = new AABB(0.0D, 0.0D, 0.3D, 1.0D, 0.3D, 0.7D);
    public static final AABB BEAM_BLOCK_EW_AABB = new AABB(0.3D, 0.0D, 0.0D, 0.7D, 0.3D, 1.0D);

    public static final AABB LIGHT_BLOCK_DOWN = new AABB(0.125F, 0.0F, 0.125F, 0.875F, 0.125F, 0.875F);
    public static final AABB LIGHT_BLOCK_UP = new AABB(0.125F, 0.875F, 0.125F, 0.875F, 1.0F, 0.875F);
    public static final AABB LIGHT_BLOCK_NORTH = new AABB(0.125F, 0.125F, 0.0F, 0.875F, 0.875F, 0.125F);
    public static final AABB LIGHT_BLOCK_SOUTH = new AABB(0.125F, 0.125F, 0.875F, 0.875F, 0.8750F, 1.0F);
    public static final AABB LIGHT_BLOCK_WEST = new AABB(0.0F, 0.125F, 0.125F, 0.125F, 0.875F, 0.8750F);
    public static final AABB LIGHT_BLOCK_EAST = new AABB(0.875F, 0.125F, 0.125F, 1.0F, 0.875F, 0.8750F);

    public static final Supplier<MultipartBlock> MULTIPART_BLOCK = BLOCKS.registerBlock("multipart", MultipartBlock::new);
    public static final Supplier<BlockEntityType<MultipartTE>> TYPE_MULTIPART = TILES.register("multipart", () -> new BlockEntityType<>(MultipartTE::new, Set.of(MULTIPART_BLOCK.get())));

    public static final Supplier<PatternBlock> PATTERN = BLOCKS.registerBlock("pattern", PatternBlock::new);
    public static final Map<PatternType, Supplier<BlockStateItem>> PATTERN_ITEMS = Arrays.stream(PatternType.values())
            .map(type -> Pair.of(type, ITEMS.registerItem("pattern_" + type.getSerializedName(), properties -> new BlockStateItem(PATTERN.get().defaultBlockState().setValue(PatternBlock.TYPE, type), properties))))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

    public static final Supplier<BaseBlock> ORE_LITHIUM = BLOCKS.registerBlock("lithiumore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_LITHIUM_ITEM = ITEMS.registerItem("lithiumore", properties -> new BlockItem(ORE_LITHIUM.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> ORE_MANGANESE = BLOCKS.registerBlock("manganeseore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_MANGANESE_ITEM = ITEMS.registerItem("manganeseore", properties -> new BlockItem(ORE_MANGANESE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> ORE_SILICON = BLOCKS.registerBlock("siliconore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_SILICON_ITEM = ITEMS.registerItem("siliconore", properties -> new BlockItem(ORE_SILICON.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> ORE_SILVER = BLOCKS.registerBlock("silverore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_SILVER_ITEM = ITEMS.registerItem("silverore", properties -> new BlockItem(ORE_SILVER.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> ORE_PLATINUM = BLOCKS.registerBlock("platinumore", BaseOreBlock::new);
    public static final Supplier<Item> ORE_PLATINUM_ITEM = ITEMS.registerItem("platinumore", properties -> new BlockItem(ORE_PLATINUM.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> ORE_POSIRITE = BLOCKS.registerBlock("posirite", BaseOreBlock::new);
    public static final Supplier<Item> ORE_POSIRITE_ITEM = ITEMS.registerItem("posirite", properties -> new BlockItem(ORE_POSIRITE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> ORE_NEGARITE = BLOCKS.registerBlock("negarite", BaseOreBlock::new);
    public static final Supplier<Item> ORE_NEGARITE_ITEM = ITEMS.registerItem("negarite", properties -> new BlockItem(ORE_NEGARITE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> FLUX_BEAM = BLOCKS.registerBlock("flux_beam", properties -> new BaseBeamBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)), false));
    public static final Supplier<Item> FLUX_BEAM_ITEM = ITEMS.registerItem("flux_beam", properties -> new BlockItem(FLUX_BEAM.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> FLUX_BEND_BEAM = BLOCKS.registerBlock("flux_bend_beam", properties -> new BaseBeamBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL)), true));
    public static final Supplier<Item> FLUX_BEND_BEAM_ITEM = ITEMS.registerItem("flux_bend_beam", properties -> new BlockItem(FLUX_BEND_BEAM.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> FLUX_SLOPE_BEAM = BLOCKS.registerBlock("flux_slope_beam", FluxSlopeBeamBlock::new);
    public static final Supplier<Item> FLUX_SLOPE_BEAM_ITEM = ITEMS.registerItem("flux_slope_beam", properties -> new BlockItem(FLUX_SLOPE_BEAM.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> SENSOR_ITEM_NODE = BLOCKS.registerBlock("sensor_item_node", SensorItemNodeTile::createBlock);
    public static final Supplier<Item> SENSOR_ITEM_NODE_ITEM = ITEMS.registerItem("sensor_item_node", properties -> new MultipartItemBlock(SENSOR_ITEM_NODE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<SensorItemNodeTile>> SENSOR_ITEM_TILE = TILES.register("sensor_item_node", () -> new BlockEntityType<>(SensorItemNodeTile::new, Set.of(SENSOR_ITEM_NODE.get())));

    public static final Supplier<BaseBlock> INPUT_ITEM_NODE = BLOCKS.registerBlock("input_item_node", InputItemNodeTile::createBlock);
    public static final Supplier<Item> INPUT_ITEM_NODE_ITEM = ITEMS.registerItem("input_item_node", properties -> new MultipartItemBlock(INPUT_ITEM_NODE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<InputItemNodeTile>> INPUT_ITEM_TILE = TILES.register("input_item_node", () -> new BlockEntityType<>(InputItemNodeTile::new, Set.of(INPUT_ITEM_NODE.get())));

    public static final Supplier<BaseBlock> OUTPUT_ITEM_NODE = BLOCKS.registerBlock("output_item_node", properties -> new BaseNodeBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
            .tileEntitySupplier(OutputItemNodeTile::new)));
    public static final Supplier<Item> OUTPUT_ITEM_NODE_ITEM = ITEMS.registerItem("output_item_node", properties -> new MultipartItemBlock(OUTPUT_ITEM_NODE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<OutputItemNodeTile>> OUTPUT_ITEM_TILE = TILES.register("output_item_node", () -> new BlockEntityType<>(OutputItemNodeTile::new, Set.of(OUTPUT_ITEM_NODE.get())));

    public static final Supplier<BaseBlock> ROUND_ROBIN_NODE = BLOCKS.registerBlock("round_robin_node", properties -> new BaseNodeBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
            .topDriver(DRIVER)
            .tileEntitySupplier(RoundRobinNodeTile::new)));
    public static final Supplier<Item> ROUND_ROBIN_NODE_ITEM = ITEMS.registerItem("round_robin_node", properties -> new MultipartItemBlock(ROUND_ROBIN_NODE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<RoundRobinNodeTile>> ROUND_ROBIN_TILE = TILES.register("round_robin_node", () -> new BlockEntityType<>(RoundRobinNodeTile::new, Set.of(ROUND_ROBIN_NODE.get())));

    public static final Supplier<BaseBlock> FIELD_MARKER = BLOCKS.registerBlock("field_marker", FieldMarkerTile::createBlock);
    public static final Supplier<Item> FIELD_MARKER_ITEM = ITEMS.registerItem("field_marker", properties -> new MultipartItemBlock(FIELD_MARKER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<FieldMarkerTile>> FIELD_MARKER_TILE = TILES.register("field_marker", () -> new BlockEntityType<>(FieldMarkerTile::new, Set.of(FIELD_MARKER.get())));

    public static final Supplier<RampBlock> RAMP = BLOCKS.registerBlock("ramp", RampBlock::new);
    public static final Supplier<Item> RAMP_ITEM = ITEMS.registerItem("ramp", properties -> new BlockItem(RAMP.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<SlopeBlock> SLOPE = BLOCKS.registerBlock("slope", SlopeBlock::new);
    public static final Supplier<Item> SLOPE_ITEM = ITEMS.registerItem("slope", properties -> new BlockItem(SLOPE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<PaneBlock> GLASS_FENCE = BLOCKS.registerBlock("glass_fence", properties -> new PaneBlock(properties, MapColor.NONE, SoundType.GLASS));
    public static final Supplier<Item> GLASS_FENCE_ITEM = ITEMS.registerItem("glass_fence", properties -> new BlockItem(GLASS_FENCE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<PaneBlock> BLUE_GLASS_FENCE = BLOCKS.registerBlock("blue_glass_fence", properties -> new PaneBlock(properties, MapColor.NONE, SoundType.GLASS));
    public static final Supplier<Item> BLUE_GLASS_FENCE_ITEM = ITEMS.registerItem("blue_glass_fence", properties -> new BlockItem(BLUE_GLASS_FENCE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<PaneBlock> MARBLE_FENCE = BLOCKS.registerBlock("marble_fence", properties -> new PaneBlock(properties, MapColor.STONE, SoundType.STONE));
    public static final Supplier<Item> MARBLE_FENCE_ITEM = ITEMS.registerItem("marble_fence", properties -> new BlockItem(MARBLE_FENCE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<PaneBlock> TECH_FENCE = BLOCKS.registerBlock("tech_fence", properties -> new PaneBlock(properties, MapColor.STONE, SoundType.STONE));
    public static final Supplier<Item> TECH_FENCE_ITEM = ITEMS.registerItem("tech_fence", properties -> new BlockItem(TECH_FENCE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> REINFORCED_MARBLE = BLOCKS.registerBlock("reinforced_marble", properties -> new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
            .properties(properties.mapColor(MapColor.STONE).strength(80.0f, 3000.0f))
    ) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });
    public static final Supplier<Item> REINFORCED_MARBLE_ITEM = ITEMS.registerItem("reinforced_marble", properties -> new BlockItem(REINFORCED_MARBLE.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> FLUX_GLOW = BLOCKS.registerBlock("fluxglow", properties -> new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
        .properties(properties.mapColor(MapColor.NONE).lightLevel((light) -> { return 15; }))) {
        @Override
        public RotationType getRotationType() {
            return RotationType.NONE;
        }
    });
    public static final Supplier<Item> FLUX_GLOW_ITEM = ITEMS.registerItem("fluxglow", properties -> new BlockItem(FLUX_GLOW.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<BaseBlock> POWER_COMBINER = BLOCKS.registerBlock("power_combiner", PowerCombinerTile::createBlock);
    public static final Supplier<Item> POWER_COMBINER_ITEM = ITEMS.registerItem("power_combiner", properties -> new BlockItem(POWER_COMBINER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<PowerCombinerTile>> POWER_COMBINER_TILE = TILES.register("power_combiner", () -> new BlockEntityType<>(PowerCombinerTile::new, Set.of(POWER_COMBINER.get())));

    public static final Supplier<BaseBlock> NEGARITE_GENERATOR = BLOCKS.registerBlock("negarite_generator", NegariteGeneratorTile::createBlock);
    public static final Supplier<Item> NEGARITE_GENERATOR_ITEM = ITEMS.registerItem("negarite_generator", properties -> new BlockItem(NEGARITE_GENERATOR.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<NegariteGeneratorTile>> NEGARITE_GENERATOR_TILE = TILES.register("negarite_generator", () -> new BlockEntityType<>(NegariteGeneratorTile::new, Set.of(NEGARITE_GENERATOR.get())));

    public static final Supplier<BaseBlock> POSIRITE_GENERATOR = BLOCKS.registerBlock("posirite_generator", PosiriteGeneratorTile::createBlock);
    public static final Supplier<Item> POSIRITE_GENERATOR_ITEM = ITEMS.registerItem("posirite_generator", properties -> new BlockItem(POSIRITE_GENERATOR.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<PosiriteGeneratorTile>> POSIRITE_GENERATOR_TILE = TILES.register("posirite_generator", () -> new BlockEntityType<>(PosiriteGeneratorTile::new, Set.of(POSIRITE_GENERATOR.get())));

    public static final Supplier<BaseBlock> NEGARITE_TANK = BLOCKS.registerBlock("negarite_tank", NegariteTankTile::createBlock);
    public static final Supplier<Item> NEGARITE_TANK_ITEM = ITEMS.registerItem("negarite_tank", properties -> new BlockItem(NEGARITE_TANK.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<NegariteTankTile>> NEGARITE_TANK_TILE = TILES.register("negarite_tank", () -> new BlockEntityType<>(NegariteTankTile::new, Set.of(NEGARITE_TANK.get())));

    public static final Supplier<BaseBlock> POSIRITE_TANK = BLOCKS.registerBlock("posirite_tank", PosiriteTankTile::createBlock);
    public static final Supplier<Item> POSIRITE_TANK_ITEM = ITEMS.registerItem("posirite_tank", properties -> new BlockItem(POSIRITE_TANK.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<PosiriteTankTile>> POSIRITE_TANK_TILE = TILES.register("posirite_tank", () -> new BlockEntityType<>(PosiriteTankTile::new, Set.of(POSIRITE_TANK.get())));

    public static final Supplier<BaseBlock> DISPLAY_BASE = BLOCKS.registerBlock("display_base", DisplayBaseTile::createBlock);
    public static final Supplier<Item> DISPLAY_BASE_ITEM = ITEMS.registerItem("display_base", properties -> new BlockItem(DISPLAY_BASE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<DisplayBaseTile>> DISPLAY_BASE_TILE = TILES.register("display_base", () -> new BlockEntityType<>(DisplayBaseTile::new, Set.of(DISPLAY_BASE.get())));
    public static final Supplier<DisplayModuleItem> DISPLAY_POWER = ITEMS.registerItem("display_module_power", properties -> new DisplayModuleItem(properties, new PowerDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_FLUIDS = ITEMS.registerItem("display_module_fluids", properties -> new DisplayModuleItem(properties, new FluidDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_FE = ITEMS.registerItem("display_module_fe", properties -> new DisplayModuleItem(properties, new ForgeEnergyDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_STORAGE = ITEMS.registerItem("display_module_storage", properties -> new DisplayModuleItem(properties, new StorageDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_TEXT = ITEMS.registerItem("display_module_text", properties -> new DisplayModuleItem(properties, new TextDisplayModule()));
    public static final Supplier<DisplayModuleItem> DISPLAY_ALIEN_TEXT = ITEMS.registerItem("display_module_alien_text", properties -> new DisplayModuleItem(properties, new AlienTextDisplayModule()));

    public static final Supplier<DisplayModuleItem> DISPLAY_CAMERA = ITEMS.registerItem("display_module_camera", properties -> new DisplayModuleItem(properties, new CameraDisplayModule()));
    public static final Supplier<BaseBlock> CAMERA = BLOCKS.registerBlock("camera", CameraTile::createBlock);
    public static final Supplier<Item> CAMERA_ITEM = ITEMS.registerItem("camera", properties -> new BlockItem(CAMERA.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<CameraTile>> CAMERA_TILE = TILES.register("camera", () -> new BlockEntityType<>(CameraTile::new, Set.of(CAMERA.get())));

    public static final Supplier<BaseBlock> DOOR_MARKER = BLOCKS.registerBlock("door_marker", DoorMarkerTile::createBlock);
    public static final Supplier<Item> DOOR_MARKER_ITEM = ITEMS.registerItem("door_marker", properties -> new BlockItem(DOOR_MARKER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<DoorMarkerTile>> DOOR_MARKER_TILE = TILES.register("door_marker", () -> new BlockEntityType<>(DoorMarkerTile::new, Set.of(DOOR_MARKER.get())));

    public static final Supplier<BaseBlock> AICORE = BLOCKS.registerBlock("aicore", AICoreTile::createBlock);
    public static final Supplier<Item> AICORE_ITEM = ITEMS.registerItem("aicore", properties -> new BlockItem(AICORE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<AICoreTile>> AICORE_TILE = TILES.register("aicore", () -> new BlockEntityType<>(AICoreTile::new, Set.of(AICORE.get())));

    public static final Supplier<BaseBlock> ALARM = BLOCKS.registerBlock("alarm", AlarmTile::createBlock);
    public static final Supplier<Item> ALARM_ITEM = ITEMS.registerItem("alarm", properties -> new BlockItem(ALARM.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<AlarmTile>> ALARM_TILE = TILES.register("alarm", () -> new BlockEntityType<>(AlarmTile::new, Set.of(ALARM.get())));

    public static final Supplier<BaseBlock> CONSTRUCTOR = BLOCKS.registerBlock("constructor", ConstructorTile::createBlock);
    public static final Supplier<Item> CONSTRUCTOR_ITEM = ITEMS.registerItem("constructor", properties -> new BlockItem(CONSTRUCTOR.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<ConstructorTile>> CONSTRUCTOR_TILE = TILES.register("constructor", () -> new BlockEntityType<>(ConstructorTile::new, Set.of(CONSTRUCTOR.get())));

    public static final Supplier<BaseBlock> AUTO_CONSTRUCTOR = BLOCKS.registerBlock("auto_constructor", AutoConstructorTile::createBlock);
    public static final Supplier<Item> AUTO_CONSTRUCTOR_ITEM = ITEMS.registerItem("auto_constructor", properties -> new BlockItem(AUTO_CONSTRUCTOR.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<AutoConstructorTile>> AUTO_CONSTRUCTOR_TILE = TILES.register("auto_constructor", () -> new BlockEntityType<>(AutoConstructorTile::new, Set.of(AUTO_CONSTRUCTOR.get())));

    public static final Supplier<BaseBlock> RECYCLER = BLOCKS.registerBlock("recycler", RecyclerTile::createBlock);
    public static final Supplier<Item> RECYCLER_ITEM = ITEMS.registerItem("recycler", properties -> new BlockItem(RECYCLER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<RecyclerTile>> RECYCLER_TILE = TILES.register("recycler", () -> new BlockEntityType<>(RecyclerTile::new, Set.of(RECYCLER.get())));

    public static final Supplier<BaseBlock> BLUEPRINT_STORAGE = BLOCKS.registerBlock("blueprint_storage", BlueprintStorageTile::createBlock);
    public static final Supplier<Item> BLUEPRINT_STORAGE_ITEM = ITEMS.registerItem("blueprint_storage", properties -> new BlockItem(BLUEPRINT_STORAGE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<BlueprintStorageTile>> BLUEPRINT_STORAGE_TILE = TILES.register("blueprint_storage", () -> new BlockEntityType<>(BlueprintStorageTile::new, Set.of(BLUEPRINT_STORAGE.get())));

    public static final Supplier<BaseBlock> AUTOMATION_FIELD = BLOCKS.registerBlock("automation_field", AutoFieldTile::createBlock);
    public static final Supplier<Item> AUTOMATION_FIELD_ITEM = ITEMS.registerItem("automation_field", properties -> new BlockItem(AUTOMATION_FIELD.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<AutoFieldTile>> AUTOFIELD_TILE = TILES.register("automation_field", () -> new BlockEntityType<>(AutoFieldTile::new, Set.of(AUTOMATION_FIELD.get())));

    public static final Supplier<BaseBlock> STORAGE = BLOCKS.registerBlock("storage", StorageTile::createBlock);
    public static final Supplier<Item> STORAGE_ITEM = ITEMS.registerItem("storage", properties -> new BlockItem(STORAGE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BaseBlock> STORAGE_3X3 = BLOCKS.registerBlock("storage_3x3", StorageTile::createBlock);
    public static final Supplier<Item> STORAGE_3X3_ITEM = ITEMS.registerItem("storage_3x3", properties -> new BlockItem(STORAGE_3X3.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<StorageTile>> STORAGE_TILE = TILES.register("storage", () -> new BlockEntityType<>(StorageTile::new, Set.of(STORAGE.get(), STORAGE_3X3.get())));

    public static final Supplier<BaseBlock> HOLO_STORAGE_1 = BLOCKS.registerBlock("holo_storage_1", StorageCubeTile::createBlock);
    public static final Supplier<Item> HOLO_STORAGE_1_ITEM = ITEMS.registerItem("holo_storage_1", properties -> new BlockItem(HOLO_STORAGE_1.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BaseBlock> HOLO_STORAGE_2 = BLOCKS.registerBlock("holo_storage_2", StorageCubeTile::createBlock);
    public static final Supplier<Item> HOLO_STORAGE_2_ITEM = ITEMS.registerItem("holo_storage_2", properties -> new BlockItem(HOLO_STORAGE_2.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BaseBlock> HOLO_STORAGE_3 = BLOCKS.registerBlock("holo_storage_3", StorageCubeTile::createBlock);
    public static final Supplier<Item> HOLO_STORAGE_3_ITEM = ITEMS.registerItem("holo_storage_3", properties -> new BlockItem(HOLO_STORAGE_3.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<StorageCubeTile>> HOLO_STORAGE_TILE = TILES.register("holo_storage", () -> new BlockEntityType<>(StorageCubeTile::new, Set.of(HOLO_STORAGE_1.get(), HOLO_STORAGE_2.get(), HOLO_STORAGE_3.get())));

    public static final Supplier<BaseBlock> SMELTER = BLOCKS.registerBlock("smelter", SmelterTile::createBlock);
    public static final Supplier<Item> SMELTER_ITEM = ITEMS.registerItem("smelter", properties -> new BlockItem(SMELTER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<SmelterTile>> SMELTER_TILE = TILES.register("smelter", () -> new BlockEntityType<>(SmelterTile::new, Set.of(SMELTER.get())));

    public static final Supplier<BaseBlock> PURIFIER_START = BLOCKS.registerBlock("purifier_start", PurifierTile::createBlock);
    public static final Supplier<Item> PURIFIER_START_ITEM = ITEMS.registerItem("purifier_start", properties -> new BlockItem(PURIFIER_START.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BaseBlock> PURIFIER_FINISH = BLOCKS.registerBlock("purifier_finish", PurifierTile::createBlock);
    public static final Supplier<Item> PURIFIER_FINISH_ITEM = ITEMS.registerItem("purifier_finish", properties -> new BlockItem(PURIFIER_FINISH.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<PurifierTile>> PURIFIER_TILE = TILES.register("purifier", () -> new BlockEntityType<>(PurifierTile::new, Set.of(PURIFIER_START.get(), PURIFIER_FINISH.get())));
    public static final Supplier<PurifierTubeBlock> PURIFIER_TUBE = BLOCKS.registerBlock("purifier_tube", PurifierTubeBlock::new);
    public static final Supplier<Item> PURIFIER_TUBE_ITEM = ITEMS.registerItem("purifier_tube", properties -> new BlockItem(PURIFIER_TUBE.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<PurifierTubeTile>> PURIFIER_TUBE_TILE = TILES.register("purifier_tube", () -> new BlockEntityType<>(PurifierTubeTile::new, Set.of(PURIFIER_TUBE.get())));

    public static final Supplier<BaseBlock> CHARGER = BLOCKS.registerBlock("charger", ChargerTile::createBlock);
    public static final Supplier<Item> CHARGER_ITEM = ITEMS.registerItem("charger", properties -> new BlockItem(CHARGER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<ChargerTile>> CHARGER_TILE = TILES.register("charger", () -> new BlockEntityType<>(ChargerTile::new, Set.of(CHARGER.get())));

    public static final Supplier<BaseBlock> ELEVATOR = BLOCKS.registerBlock("elevator", ElevatorTile::createBlock);
    public static final Supplier<Item> ELEVATOR_ITEM = ITEMS.registerItem("elevator", properties -> new BlockItem(ELEVATOR.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<ElevatorTile>> ELEVATOR_TILE = TILES.register("elevator", () -> new BlockEntityType<>(ElevatorTile::new, Set.of(ELEVATOR.get())));

    public static final Supplier<BaseBlock> LEVEL_MARKER = BLOCKS.registerBlock("level_marker", LevelMarkerTile::createBlock);
    public static final Supplier<Item> LEVEL_MARKER_ITEM = ITEMS.registerItem("level_marker", properties -> new BlockItem(LEVEL_MARKER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<LevelMarkerTile>> LEVEL_MARKER_TILE = TILES.register("level_marker", () -> new BlockEntityType<>(LevelMarkerTile::new, Set.of(LEVEL_MARKER.get())));

    public static final Supplier<BaseBlock> INVISIBLE_DOOR = BLOCKS.registerBlock("invisible_door", InvisibleDoorTile::createBlock);
    public static final Supplier<BlockEntityType<InvisibleDoorTile>> INVISIBLE_DOOR_TILE = TILES.register("invisible_door", () -> new BlockEntityType<>(InvisibleDoorTile::new, Set.of(INVISIBLE_DOOR.get())));

    public static final Supplier<BaseBlock> FORCEFIELD = BLOCKS.registerBlock("forcefield", ForceFieldTile::createBlock);
    public static final Supplier<Item> FORCEFIELD_ITEM = ITEMS.registerItem("forcefield", properties -> new BlockItem(FORCEFIELD.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<ForceFieldTile>> FORCEFIELD_TILE = TILES.register("forcefield", () -> new BlockEntityType<>(ForceFieldTile::new, Set.of(FORCEFIELD.get())));

    public static final Supplier<BaseBlock> WARPER = BLOCKS.registerBlock("warper", WarperTile::createBlock);
    public static final Supplier<Item> WARPER_ITEM = ITEMS.registerItem("warper", properties -> new BlockItem(WARPER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<WarperTile>> WARPER_TILE = TILES.register("warper", () -> new BlockEntityType<>(WarperTile::new, Set.of(WARPER.get())));

    public static final Supplier<BaseBlock> LOCK = BLOCKS.registerBlock("lock", LockTile::createBlock);
    public static final Supplier<Item> LOCK_ITEM = ITEMS.registerItem("lock", properties -> new BlockItem(LOCK.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<LockTile>> LOCK_TILE = TILES.register("lock", () -> new BlockEntityType<>(LockTile::new, Set.of(LOCK.get())));

    public static final Supplier<BaseBlock> SIGNAL_RECEIVER = BLOCKS.registerBlock("signal_receiver", SignalReceiverTile::createBlock);
    public static final Supplier<Item> SIGNAL_RECEIVER_ITEM = ITEMS.registerItem("signal_receiver", properties -> new BlockItem(SIGNAL_RECEIVER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<SignalReceiverTile>> SIGNAL_RECEIVER_TILE = TILES.register("signal_receiver", () -> new BlockEntityType<>(SignalReceiverTile::new, Set.of(SIGNAL_RECEIVER.get())));

    public static final Supplier<BaseBlock> SIGNAL_TRANSMITTER = BLOCKS.registerBlock("signal_transmitter", SignalTransmitterTile::createBlock);
    public static final Supplier<Item> SIGNAL_TRANSMITTER_ITEM = ITEMS.registerItem("signal_transmitter", properties -> new BlockItem(SIGNAL_TRANSMITTER.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<SignalTransmitterTile>> SIGNAL_TRANSMITTER_TILE = TILES.register("signal_transmitter", () -> new BlockEntityType<>(SignalTransmitterTile::new, Set.of(SIGNAL_TRANSMITTER.get())));

    public static final Supplier<BaseBlock> WIRELESS_BUTTON = BLOCKS.registerBlock("wireless_button", WirelessButtonTile::createBlock);
    public static final Supplier<Item> WIRELESS_BUTTON_ITEM = ITEMS.registerItem("wireless_button", properties -> new BlockItem(WIRELESS_BUTTON.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<WirelessButtonTile>> WIRELESS_BUTTON_TILE = TILES.register("wireless_button", () -> new BlockEntityType<>(WirelessButtonTile::new, Set.of(WIRELESS_BUTTON.get())));

    public static final Supplier<BaseBlock> WIRELESS_LOCK = BLOCKS.registerBlock("wireless_lock", WirelessLockTile::createBlock);
    public static final Supplier<Item> WIRELESS_LOCK_ITEM = ITEMS.registerItem("wireless_lock", properties -> new BlockItem(WIRELESS_LOCK.get(), properties.useBlockDescriptionPrefix()));
    public static final Supplier<BlockEntityType<WirelessLockTile>> WIRELESS_LOCK_TILE = TILES.register("wireless_lock", () -> new BlockEntityType<>(WirelessLockTile::new, Set.of(WIRELESS_LOCK.get())));

    public static final Supplier<BaseBlock> FLAT_LIGHT = BLOCKS.registerBlock("flatlight", Registration::createFlatLightBlock);

    public static final Supplier<Item> FLAT_LIGHT_ITEM = ITEMS.registerItem("flatlight", properties -> new BlockItem(FLAT_LIGHT.get(), properties.useBlockDescriptionPrefix()));

    public static final Supplier<NetCableBlock> NETCABLE = BLOCKS.registerBlock("netcable", properties -> new NetCableBlock(properties));
    public static final Supplier<ConnectorBlock> CONNECTOR = BLOCKS.registerBlock("connector", ConnectorBlock::new);

    public static final Supplier<Item> NETCABLE_NEGARITE = ITEMS.registerItem("netcable_negarite", properties -> new CableBlockItem(NETCABLE.get(), properties, CableColor.NEGARITE));
    public static final Supplier<Item> NETCABLE_POSIRITE = ITEMS.registerItem("netcable_posirite", properties -> new CableBlockItem(NETCABLE.get(), properties, CableColor.POSIRITE));
    public static final Supplier<Item> NETCABLE_COMBINED = ITEMS.registerItem("netcable_combined", properties -> new CableBlockItem(NETCABLE.get(), properties, CableColor.COMBINED));
    public static final Supplier<Item> NETCABLE_DATA = ITEMS.registerItem("netcable_data", properties -> new CableBlockItem(NETCABLE.get(), properties, CableColor.DATA));
    public static final Supplier<Item> CONNECTOR_NEGARITE = ITEMS.registerItem("connector_negarite", properties -> new CableBlockItem(CONNECTOR.get(), properties, CableColor.NEGARITE));
    public static final Supplier<Item> CONNECTOR_POSIRITE = ITEMS.registerItem("connector_posirite", properties -> new CableBlockItem(CONNECTOR.get(), properties, CableColor.POSIRITE));
    public static final Supplier<Item> CONNECTOR_COMBINED = ITEMS.registerItem("connector_combined", properties -> new CableBlockItem(CONNECTOR.get(), properties, CableColor.COMBINED));
    public static final Supplier<Item> CONNECTOR_DATA = ITEMS.registerItem("connector_data", properties -> new CableBlockItem(CONNECTOR.get(), properties, CableColor.DATA));

    public static final Supplier<BlockEntityType<NetCableTileEntity>> NETCABLE_TILE = TILES.register("netcable", () -> new BlockEntityType<>(NetCableTileEntity::new, Set.of(NETCABLE.get())));
    public static final Supplier<BlockEntityType<ConnectorTileEntity>> CONNECTOR_TILE = TILES.register("connector", () -> new BlockEntityType<>(ConnectorTileEntity::new, Set.of(CONNECTOR.get())));
    public static final Supplier<FacadeBlock> FACADE = BLOCKS.registerBlock("facade", FacadeBlock::new);
    public static final Supplier<Item> FACADE_ITEM = ITEMS.registerItem("facade", properties -> new FacadeItemBlock(properties, FACADE.get()));

    public static final Supplier<Item> INGOT_PLATINUM = ITEMS.registerItem("ingot_platinum", properties -> new Item(properties));
    public static final Supplier<Item> INGOT_LITHIUM = ITEMS.registerItem("ingot_lithium", properties -> new Item(properties));
    public static final Supplier<Item> INGOT_MANGANESE = ITEMS.registerItem("ingot_manganese", properties -> new Item(properties));
    public static final Supplier<Item> INGOT_SILVER = ITEMS.registerItem("ingot_silver", properties -> new Item(properties));
    public static final Supplier<Item> RAW_PLATINUM = ITEMS.registerItem("raw_platinum", properties -> new Item(properties));
    public static final Supplier<Item> RAW_LITHIUM = ITEMS.registerItem("raw_lithium", properties -> new Item(properties));
    public static final Supplier<Item> RAW_MANGANESE = ITEMS.registerItem("raw_manganese", properties -> new Item(properties));
    public static final Supplier<Item> RAW_SILVER = ITEMS.registerItem("raw_silver", properties -> new Item(properties));
    public static final Supplier<Item> DUST_SILICON = ITEMS.registerItem("silicon", properties -> new Item(properties));
    public static final Supplier<Item> DUST_NEGARITE = ITEMS.registerItem("dust_negarite", properties -> new Item(properties));
    public static final Supplier<Item> DUST_POSIRITE = ITEMS.registerItem("dust_posirite", properties -> new Item(properties));
    public static final Supplier<EnergySabreItem> ENERGY_SABRE = ITEMS.registerItem("energy_sabre", EnergySabreItem::new);
    public static final Supplier<EnergyLanceItem> ENERGY_LANCE = ITEMS.registerItem("energy_lance", EnergyLanceItem::new);
    public static final Supplier<EnhancedEnergySabreItem> ENHANCED_ENERGY_SABRE = ITEMS.registerItem("enhanced_energy_sabre", EnhancedEnergySabreItem::new);
    public static final Supplier<KeyCardItem> KEY_CARD = ITEMS.registerItem("key_card", KeyCardItem::new);
    public static final Supplier<BlueprintItem> BLUEPRINT = ITEMS.registerItem("blueprint", BlueprintItem::new);
    public static final Supplier<TabletItem> TABLET = ITEMS.registerItem("tablet", TabletItem::new);
    public static final Supplier<ArientePearlItem> ARIENTE_PEARL = ITEMS.registerItem("ariente_pearl", ArientePearlItem::new);
    public static final Supplier<FluxLevitatorItem> FLUX_LEVITATOR = ITEMS.registerItem("flux_levitator", FluxLevitatorItem::new);
    public static final Supplier<FluxShipItem> FLUX_SHIP = ITEMS.registerItem("flux_ship", FluxShipItem::new);
    public static final Supplier<FluxCapacitorItem> FLUX_CAPACITOR = ITEMS.registerItem("flux_capacitor", FluxCapacitorItem::new);
    public static final Supplier<CircuitItem> CIRCUIT = ITEMS.registerItem("circuit", properties -> new CircuitItem(properties, false));
    public static final Supplier<CircuitItem> ADVANCED_CIRCUIT = ITEMS.registerItem("advanced_circuit", properties -> new CircuitItem(properties, true));
    public static final Supplier<ExtremeCircuitItem> EXTREME_CIRCUIT = ITEMS.registerItem("extreme_circuit", ExtremeCircuitItem::new);
    public static final Supplier<EnergyHolderItem> ENERGY_HOLDER = ITEMS.registerItem("energy_holder", EnergyHolderItem::new);
    public static final Supplier<PowerSuit> POWERSUIT_HEAD = ITEMS.registerItem("powersuit_head", properties -> new PowerSuit(properties, EquipmentSlot.HEAD));
    public static final Supplier<PowerSuit> POWERSUIT_CHEST = ITEMS.registerItem("powersuit_chest", properties -> new PowerSuit(properties, EquipmentSlot.CHEST));
    public static final Supplier<PowerSuit> POWERSUIT_LEGS = ITEMS.registerItem("powersuit_legs", properties -> new PowerSuit(properties, EquipmentSlot.LEGS));
    public static final Supplier<PowerSuit> POWERSUIT_FEET = ITEMS.registerItem("powersuit_feet", properties -> new PowerSuit(properties, EquipmentSlot.FEET));
    public static final Supplier<ArmorModuleItem> MODULE_ARMOR = ITEMS.registerItem("module_armor", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.ARMOR));
    public static final Supplier<ArmorModuleItem> MODULE_ENERGY = ITEMS.registerItem("module_energy", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.ENERGY));
    public static final Supplier<ArmorModuleItem> MODULE_FEATHERFALLING = ITEMS.registerItem("module_featherfalling", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.FEATHERFALLING));
    public static final Supplier<ArmorModuleItem> MODULE_FLIGHT = ITEMS.registerItem("module_flight", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.FLIGHT));
    public static final Supplier<ArmorModuleItem> MODULE_HOVER = ITEMS.registerItem("module_hover", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.HOVER));
    public static final Supplier<ArmorModuleItem> MODULE_FORCEFIELD = ITEMS.registerItem("module_forcefield", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.FORCEFIELD));
    public static final Supplier<ArmorModuleItem> MODULE_INVISIBILITY = ITEMS.registerItem("module_invisibility", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.INVISIBILITY));
    public static final Supplier<ArmorModuleItem> MODULE_NIGHTVISION = ITEMS.registerItem("module_nightvision", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.NIGHTVISION));
    public static final Supplier<ArmorModuleItem> MODULE_REGENERATION = ITEMS.registerItem("module_regeneration", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.REGENERATION));
    public static final Supplier<ArmorModuleItem> MODULE_SCRAMBLE = ITEMS.registerItem("module_scramble", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.SCRAMBLE));
    public static final Supplier<ArmorModuleItem> MODULE_SPEED = ITEMS.registerItem("module_speed", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.SPEED));
    public static final Supplier<ArmorModuleItem> MODULE_STEPASSIST = ITEMS.registerItem("module_stepassist", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.STEPASSIST));
    public static final Supplier<ArmorModuleItem> MODULE_INHIBIT = ITEMS.registerItem("module_inhibit", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.INHIBIT));
    public static final Supplier<ArmorModuleItem> MODULE_POWER = ITEMS.registerItem("module_power", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.POWER));
    public static final Supplier<ArmorModuleItem> MODULE_LOOTING = ITEMS.registerItem("module_looting", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.LOOTING));
    public static final Supplier<ArmorModuleItem> MODULE_FIRE = ITEMS.registerItem("module_fire", properties -> new ArmorModuleItem(properties, ArmorUpgradeType.FIRE));

    public static final Supplier<EntityType<FluxElevatorEntity>> ENTITY_ELEVATOR = ENTITIES.register("ariente_flux_elevator", () -> EntityType.Builder.of(FluxElevatorEntity::new, MobCategory.MISC)
            .sized(1.30F, 0.9F)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "ariente_flux_elevator"))));
    public static final Supplier<EntityType<EntityArientePearl>> ENTITY_PEARL = ENTITIES.register("ariente_ariente_pearl", () -> EntityType.Builder.of(EntityArientePearl::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "ariente_ariente_pearl"))));
    public static final Supplier<EntityType<FluxShipEntity>> ENTITY_FLUX_SHIP = ENTITIES.register("ariente_flux_ship", () -> EntityType.Builder.of(FluxShipEntity::new, MobCategory.MISC)
            .sized(5.30F, 1.8F)
            .clientTrackingRange(16)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "ariente_flux_ship"))));
    public static final Supplier<EntityType<HoloGuiEntity>> ENTITY_SHIP_PANEL = ENTITIES.register("flux_ship_panel", () -> EntityType.Builder.<HoloGuiEntity>of(HoloGuiEntity::new, MobCategory.MISC)
            .sized(1, 1)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "flux_ship_panel"))));
    public static final Supplier<EntityType<FluxLevitatorEntity>> ENTITY_FLUX_LEVITATOR = ENTITIES.register("ariente_flux_levitator", () -> EntityType.Builder.of(FluxLevitatorEntity::new, MobCategory.MISC)
            .sized(1.30F, 0.9F)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "ariente_flux_levitator"))));
    public static final Supplier<EntityType<LaserEntity>> ENTITY_LASER = ENTITIES.register("ariente_laser", () -> EntityType.Builder.of(LaserEntity::new, MobCategory.MISC)
            .sized(1, 1)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "ariente_laser"))));

    public static Item.Properties createStandardProperties() {
        return new Item.Properties();
    }

    private static final MountedPanelShapes FLAT_LIGHT_SHAPES = new MountedPanelShapes(1, 2);

    private static BaseBlock createFlatLightBlock(BlockBehaviour.Properties properties) {
        return new BaseBlock(new BlockBuilder().properties(properties.mapColor(MapColor.METAL).strength(2.0f).requiresCorrectToolForDrops().sound(SoundType.METAL))
                .properties(properties.mapColor(MapColor.NONE).lightLevel((light) -> { return 15; }))) {
            @Override
            public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
                return FLAT_LIGHT_SHAPES.get(state.getValue(BlockStateProperties.FACING));
            }

        };
    }

}
