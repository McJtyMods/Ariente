package mcjty.ariente;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameType;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.items.TeleportationCardItem;
import mcjty.ariente.items.ItemData;
import mcjty.ariente.compat.ArienteTOPDriver;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.ProbeHitData;
import mcjty.theoneprobe.apiimpl.ProbeInfo;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.teleport.TeleportationCardGui;
import mcjty.ariente.teleport.WarperDestination;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.components.ITextEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Ariente.MODID)
public class WarperTests {

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, registry -> {
            registry.register(Identifier.parse("ariente:legacy_saves_and_named_card_encoding"), WarperTests::legacySavesAndNamedCardEncoding);
            registry.register(Identifier.parse("ariente:destination_names_reach_probe_client_and_older_saves"), WarperTests::destinationNamesReachProbeClientAndOlderSaves);
            registry.register(Identifier.parse("ariente:stale_card_gui_cannot_configure_another_card"), WarperTests::staleCardGuiCannotConfigureAnotherCard);
            registry.register(Identifier.parse("ariente:recipe_is_constructor_only_and_uses_pearls"), WarperTests::recipeIsConstructorOnlyAndUsesPearls);
            registry.register(Identifier.parse("ariente:reciprocal_travel_needs_exit_before_reentry"), WarperTests::reciprocalTravelNeedsExitBeforeReentry);
            registry.register(Identifier.parse("ariente:removed_and_blocked_targets_fail_then_recover"), WarperTests::removedAndBlockedTargetsFailThenRecover);
            registry.register(Identifier.parse("ariente:expiry_starts_only_after_travel_and_resets_for_last_traveler"), WarperTests::expiryStartsOnlyAfterTravelAndResetsForLastTraveler);
            registry.register(Identifier.parse("ariente:pinned_destination_survives_travel_and_reload"), WarperTests::pinnedDestinationSurvivesTravelAndReload);
            registry.register(Identifier.parse("ariente:arbitrary_dimension_mob_travel_and_missing_dimensions"), WarperTests::arbitraryDimensionMobTravelAndMissingDimensions);
            registry.register(Identifier.parse("ariente:preview_has_exact_footprint_and_no_live_world_data"), WarperTests::previewHasExactFootprintAndNoLiveWorldData);
        });
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Identifier.parse("ariente:recent_warpertests"));
        event.registerTest(Identifier.parse("ariente:legacy_saves_and_named_card_encoding"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:legacy_saves_and_named_card_encoding")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:destination_names_reach_probe_client_and_older_saves"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:destination_names_reach_probe_client_and_older_saves")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:stale_card_gui_cannot_configure_another_card"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:stale_card_gui_cannot_configure_another_card")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:recipe_is_constructor_only_and_uses_pearls"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:recipe_is_constructor_only_and_uses_pearls")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:reciprocal_travel_needs_exit_before_reentry"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:reciprocal_travel_needs_exit_before_reentry")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 200, 0, true));
        event.registerTest(Identifier.parse("ariente:removed_and_blocked_targets_fail_then_recover"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:removed_and_blocked_targets_fail_then_recover")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 100, 0, true));
        event.registerTest(Identifier.parse("ariente:expiry_starts_only_after_travel_and_resets_for_last_traveler"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:expiry_starts_only_after_travel_and_resets_for_last_traveler")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 1600, 0, true));
        event.registerTest(Identifier.parse("ariente:pinned_destination_survives_travel_and_reload"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:pinned_destination_survives_travel_and_reload")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 680, 0, true));
        event.registerTest(Identifier.parse("ariente:arbitrary_dimension_mob_travel_and_missing_dimensions"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:arbitrary_dimension_mob_travel_and_missing_dimensions")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 600, 0, true));
        event.registerTest(Identifier.parse("ariente:preview_has_exact_footprint_and_no_live_world_data"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:preview_has_exact_footprint_and_no_live_world_data")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 200, 0, true));
    }


    private static WarperTile warper(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Registration.WARPER.get().defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos.above(2), Blocks.AIR.defaultBlockState());
        return (WarperTile) level.getBlockEntity(pos);
    }

    private static WarperTile pad(GameTestHelper helper, int x) {
        return warper(helper.getLevel(), helper.absolutePos(new BlockPos(x + 16, 6, 17)));
    }

    private static WarperDestination address(WarperTile tile) {
        return new WarperDestination(tile.getLevel().dimension(), tile.getBlockPos());
    }

    private static ItemEntity item(WarperTile tile) {
        ItemEntity entity = new ItemEntity(tile.getLevel(), tile.getBlockPos().getX() + .5,
                tile.getBlockPos().getY() + 1.1, tile.getBlockPos().getZ() + .5, new ItemStack(Items.DIAMOND));
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setNeverPickUp();
        tile.getLevel().addFreshEntity(entity);
        return entity;
    }

    public static void legacySavesAndNamedCardEncoding(GameTestHelper helper) {
        WarperTile tile = pad(helper, 1);
        CompoundTag legacy = new CompoundTag();
        legacy.put("Info", new CompoundTag());
        legacy.getCompoundOrEmpty("Info").putInt("charges", 7);
        tile.loadAdditional(legacy, helper.getLevel().registryAccess());
        helper.assertTrue(tile.getCharges() == 7 && tile.getDestination() == null && tile.getWarperName().isEmpty(),
                "Released Info.charges saves retain charges and have no card destination");
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.snapTo(Vec3.atCenterOf(tile.getBlockPos()).add(0, 0, 2));
        IGuiComponent<?> gui = tile.createGui("", Ariente.guiHandler.getComponentRegistry());
        ((ITextEntry) gui.findChild("warper_name").orElseThrow()).commit(player, null, "Home\nLanding");
        helper.assertTrue(tile.getWarperName().equals("Home Landing"), "Actual hologram field names the pad and strips newlines");
        ItemStack card = new ItemStack(Registration.TELEPORTATION_CARD.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, card);
        player.setShiftKeyDown(true);
        Registration.TELEPORTATION_CARD.get().onItemUseFirst(card, new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(tile.getBlockPos()), Direction.UP, tile.getBlockPos(), false)));
        player.setShiftKeyDown(false);
        helper.assertTrue(TeleportationCardItem.destinations(card).size() == 1 && TeleportationCardItem.selected(card) == null,
                "Sneak click encodes without silently selecting a destination");
        helper.assertTrue(TeleportationCardItem.destinationName(card, 0).equals("Home Landing"), "The list uses the Warper name");
        tile.setWarperName("Renamed");
        TeleportationCardItem.encode(card, tile);
        helper.assertTrue(TeleportationCardItem.destinations(card).size() == 1
                && TeleportationCardItem.destinationName(card, 0).equals("Renamed"), "Re-encoding refreshes rather than duplicates");
        helper.assertTrue(!ItemData.get(card).contains("Blocks") && !ItemData.get(card).contains("DestinationScene"), "Item updates never contain terrain");
        CompoundTag saved = tile.saveWithFullMetadata(helper.getLevel().registryAccess());
        tile.loadAdditional(saved, helper.getLevel().registryAccess());
        helper.assertTrue(tile.getWarperName().equals("Renamed") && tile.getCharges() == 7, "Names and legacy charges survive reload");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    public static void destinationNamesReachProbeClientAndOlderSaves(GameTestHelper helper) {
        WarperTile source = pad(helper, 1), target = pad(helper, 5);
        source.setWarperName("Departure");
        target.setWarperName("Garden landing");
        source.setDestination(address(target), new CompoundTag());
        String description = "Garden landing (" + target.getBlockPos().getX() + ", " + target.getBlockPos().getY() + ", " + target.getBlockPos().getZ() + ")";
        RecordingProbeInfo probe = new RecordingProbeInfo();
        ArienteTOPDriver.DRIVER.addProbeInfo(ProbeMode.NORMAL, probe, helper.makeMockPlayer(GameType.SURVIVAL), helper.getLevel(), source.getBlockState(),
                new ProbeHitData(source.getBlockPos(), Vec3.atCenterOf(source.getBlockPos()), Direction.UP, ItemStack.EMPTY));
        helper.assertTrue(probe.lines.contains(CompoundText.createLabelInfo("Name: ", "Departure").get().getString())
                && probe.lines.contains(CompoundText.createLabelInfo("Destination: ", description).get().getString()),
                "The actual TOP driver shows the source name and named destination with coordinates");
        WarperTile client = new WarperTile(source.getBlockPos(), source.getBlockState());
        client.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), source.getUpdateTag(helper.getLevel().registryAccess())));
        helper.assertTrue(client.getDestinationDescription().equals(description), "The client can display the cached name without loading a remote world");
        source.loadAdditional(source.saveWithFullMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        helper.assertTrue(source.getDestinationDescription().equals(description), "Destination names survive saved-data reloads");
        target.setWarperName("New garden");
        source.syncToClient();
        client.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), source.getUpdateTag(helper.getLevel().registryAccess())));
        helper.assertTrue(client.getDestinationDescription().startsWith("New garden ("), "Open holograms pick up loaded destination renames");
        CompoundTag older = source.saveWithFullMetadata(helper.getLevel().registryAccess());
        older.getCompoundOrEmpty("Info").getCompoundOrEmpty("Destination").remove("Name");
        source.loadAdditional(older, helper.getLevel().registryAccess());
        helper.assertTrue(source.getDestinationDescription().startsWith("New garden ("), "Existing configured saves acquire the destination name on demand");
        target.setWarperName("");
        source.syncToClient();
        helper.assertTrue(source.getDestinationDescription().equals(address(target).coordinates()), "Unnamed destinations fall back to coordinates");
        helper.succeed();
    }

    public static void staleCardGuiCannotConfigureAnotherCard(GameTestHelper helper) {
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        ItemStack card = new ItemStack(Registration.TELEPORTATION_CARD.get());
        TeleportationCardItem.ensureId(card);
        UUID oldId = TeleportationCardItem.id(card);
        player.setItemInHand(InteractionHand.OFF_HAND, card);
        helper.assertTrue(TeleportationCardGui.boundCard(player, InteractionHand.OFF_HAND, oldId) == card, "The correct hand is bound");
        ItemStack other = new ItemStack(Registration.TELEPORTATION_CARD.get());
        TeleportationCardItem.ensureId(other);
        player.setItemInHand(InteractionHand.OFF_HAND, other);
        helper.assertTrue(TeleportationCardGui.boundCard(player, InteractionHand.OFF_HAND, oldId).isEmpty(), "An open GUI cannot alter a substituted card");
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        helper.succeed();
    }

    public static void recipeIsConstructorOnlyAndUsesPearls(GameTestHelper helper) {
        var recipe = BlueprintRecipeRegistry.findRecipe(new ItemStack(Registration.TELEPORTATION_CARD.get()));
        helper.assertTrue(recipe != null && recipe.getIngredientList().stream().anyMatch(stack -> stack.is(Items.ENDER_PEARL) && stack.getCount() >= 2),
                "Constructor blueprint consumes multiple Ender Pearls");
        helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Identifier.parse("ariente:teleportation_card"))).isEmpty(),
                "No ordinary crafting recipe can produce a Teleportation Card");
        helper.succeed();
    }

    public static void reciprocalTravelNeedsExitBeforeReentry(GameTestHelper helper) {
        WarperTile source = pad(helper, 1), target = pad(helper, 5);
        source.setDestination(address(target), new CompoundTag());
        target.setDestination(address(source), new CompoundTag());
        ItemEntity traveler = item(source);
        source.tickServer();
        helper.assertTrue(traveler.distanceToSqr(Vec3.atCenterOf(target.getBlockPos()).add(0, .5, 0)) < .01, "Stepping items reach the target pad");
        CompoundTag receiverSave = target.saveWithFullMetadata(helper.getLevel().registryAccess());
        target.loadAdditional(receiverSave, helper.getLevel().registryAccess());
        for (int i = 0; i < 100; i++) {
            target.tickServer();
        }
        helper.assertTrue(traveler.blockPosition().equals(target.getBlockPos().above()), "Arrivals do not bounce, even after receiver reload");
        traveler.teleportTo(target.getBlockPos().getX() + 3, target.getBlockPos().getY() + 1, target.getBlockPos().getZ());
        target.tickServer();
        traveler.teleportTo(target.getBlockPos().getX() + .5, target.getBlockPos().getY() + 1, target.getBlockPos().getZ() + .5);
        target.tickServer();
        helper.assertTrue(traveler.blockPosition().equals(source.getBlockPos().above()), "Leaving and reentering enables the next trip");
        traveler.discard();
        helper.succeed();
    }

    public static void removedAndBlockedTargetsFailThenRecover(GameTestHelper helper) {
        WarperTile source = pad(helper, 1), target = pad(helper, 5);
        WarperDestination address = address(target);
        source.setDestination(address, new CompoundTag());
        helper.getLevel().removeBlock(target.getBlockPos(), false);
        ItemEntity traveler = item(source);
        source.tickServer();
        helper.assertTrue(traveler.blockPosition().equals(source.getBlockPos().above()), "A removed destination fails safely");
        helper.assertTrue(source.saveWithFullMetadata(helper.getLevel().registryAccess()).getCompoundOrEmpty("Info").getLongOr("LastTeleport", 0L) == -1, "Failed travel never starts expiry");
        target = warper(helper.getLevel(), address.pos());
        helper.getLevel().setBlockAndUpdate(address.pos().above(), Blocks.STONE.defaultBlockState());
        source.loadAdditional(source.saveWithFullMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        source.tickServer();
        helper.assertTrue(traveler.blockPosition().equals(source.getBlockPos().above()), "Obstructed arrivals cannot suffocate travelers");
        helper.getLevel().removeBlock(address.pos().above(), false);
        source.tickServer();
        helper.assertTrue(traveler.blockPosition().equals(address.pos().above()), "Replacing the pad restores the existing address");
        traveler.discard();
        helper.succeed();
    }

    public static void expiryStartsOnlyAfterTravelAndResetsForLastTraveler(GameTestHelper helper) {
        WarperTile source = pad(helper, 1), target = pad(helper, 5);
        source.setDestination(address(target), new CompoundTag());
        helper.runAtTickTime(610, () -> {
            helper.assertTrue(source.getDestination() != null, "Merely setting a destination never starts the 30 second timer");
            ItemEntity first = item(source);
            source.tickServer();
            helper.assertTrue(first.blockPosition().equals(target.getBlockPos().above()), "First traveler succeeds");
            first.discard();
            source.loadAdditional(source.saveWithFullMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        });
        helper.runAtTickTime(910, () -> {
            ItemEntity last = item(source);
            source.tickServer();
            last.discard();
        });
        helper.runAtTickTime(1212, () -> helper.assertTrue(source.getDestination() != null,
                "Another traveler restarts expiry; the first travel time must no longer clear it"));
        helper.runAtTickTime(1512, () -> {
            helper.assertTrue(source.getDestination() == null, "The destination clears 30 seconds after the last successful travel");
            helper.succeed();
        });
    }

    public static void pinnedDestinationSurvivesTravelAndReload(GameTestHelper helper) {
        WarperTile source = pad(helper, 1), target = pad(helper, 5);
        source.setDestination(address(target), new CompoundTag());
        ItemEntity traveler = item(source);
        source.tickServer();
        traveler.discard();
        source.setDestinationPinned(true);
        source.loadAdditional(source.saveWithFullMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
        helper.runAtTickTime(620, () -> {
            helper.assertTrue(source.getDestination() != null && source.isDestinationPinned(), "Pin survives travel, elapsed timeout and reload");
            source.setDestinationPinned(false);
            source.tickServer();
            helper.assertTrue(source.getDestination() == null, "Unpin restores the timeout from the last successful travel");
            helper.succeed();
        });
    }

    public static void arbitraryDimensionMobTravelAndMissingDimensions(GameTestHelper helper) {
        WarperTile source = pad(helper, 1);
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        if (nether == null) {
            nether = helper.getLevel().getServer().getLevel(ArienteWorldCompat.getArienteWorld().getDimension());
        }
        final ServerLevel targetLevel = nether;
        BlockPos targetPos = new BlockPos(source.getBlockPos().getX(), 120, source.getBlockPos().getZ());
        WarperTile target = warper(nether, targetPos);
        source.setDestination(address(target), new CompoundTag());
        var mob = EntityTypes.PIG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        mob.setNoAi(true);
        mob.setNoGravity(true);
        mob.snapTo(Vec3.atCenterOf(source.getBlockPos()).add(0, .6, 0));
        helper.getLevel().addFreshEntity(mob);
        UUID id = mob.getUUID();
        source.tickServer();
        helper.succeedWhen(() -> {
            Entity arrived = targetLevel.getEntity(id);
            helper.assertTrue(arrived != null, "Wait for the asynchronous arrival chunk's entity index");
            try {
                helper.assertTrue(arrived.blockPosition().equals(targetPos.above()),
                        "Mobs travel to arbitrary dimensions, with or without Ariente World");
                arrived.discard();
                source.setDestination(new WarperDestination(ResourceKey.create(Registries.DIMENSION,
                        Identifier.fromNamespaceAndPath("ariente", "removed_dimension")), targetPos), new CompoundTag());
                ItemEntity item = item(source);
                source.tickServer();
                helper.assertTrue(item.blockPosition().equals(source.getBlockPos().above()), "Missing dimensions fail without loading or crashing");
                item.discard();
            } finally {
                targetLevel.removeBlock(targetPos, false);
            }
        });
    }

    public static void previewHasExactFootprintAndNoLiveWorldData(GameTestHelper helper) {
        WarperTile tile = pad(helper, 1);
        BlockPos origin = tile.getBlockPos();
        helper.getLevel().setBlockAndUpdate(origin.offset(-16, 1, 0), Blocks.GOLD_BLOCK.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(origin.offset(15, 1, 0), Blocks.GOLD_BLOCK.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(origin.offset(16, 1, 0), Blocks.DIAMOND_BLOCK.defaultBlockState());
        CompoundTag scene = CameraCapture.scanWarper(helper.getLevel(), origin);
        long[] blocks = scene.getLongArray("Blocks").orElseThrow();
        helper.assertTrue(Arrays.stream(blocks).allMatch(packed -> {
            BlockPos offset = CameraCapture.offset(packed);
            return offset.getX() >= -16 && offset.getX() <= 15 && offset.getZ() >= -16 && offset.getZ() <= 15;
        }), "All captured geometry stays inside the 32 by 32 footprint");
        helper.assertTrue(Arrays.stream(blocks).anyMatch(p -> CameraCapture.offset(p).equals(new BlockPos(-16, 1, 0)))
                && Arrays.stream(blocks).anyMatch(p -> CameraCapture.offset(p).equals(new BlockPos(15, 1, 0))), "Both footprint boundaries are included");
        helper.assertTrue(blocks.length <= CameraCapture.MAX_BLOCKS && !scene.contains("Entities") && !scene.contains("BlockEntities"),
                "Previews are bounded static geometry without live entities, inventories or block entity NBT");
        helper.succeed();
    }

    private static class RecordingProbeInfo extends ProbeInfo {

        private final List<String> lines = new ArrayList<>();

        @Override
        public IProbeInfo text(CompoundText text) {
            lines.add(text.get().getString());
            return this;
        }
    }

}
