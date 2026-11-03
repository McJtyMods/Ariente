package mcjty.ariente;

import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayAppearance;
import mcjty.ariente.display.DisplayBackground;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.DisplayGesture;
import mcjty.ariente.display.InstalledModule;
import mcjty.hologui.gui.HoloGuiEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.DisplayModuleType;
import mcjty.ariente.display.ModuleSettings;
import mcjty.ariente.display.PacketDisplayData;
import mcjty.ariente.display.PowerDisplayModule;
import mcjty.ariente.display.StorageDisplayModule;
import mcjty.ariente.items.BlueprintItem;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.recipes.BlueprintRecipeRegistry;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.components.ITextEntry;
import mcjty.hologui.api.components.IIntegerSlider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import java.util.List;

import static mcjty.hologui.setup.Registration.HOLOGUI_ENTITY_TYPE;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class HolographicDisplayTests {

    private static DisplayBaseTile base(GameTestHelper helper, BlockPos pos, Direction facing, int height) {
        helper.getLevel().setBlockAndUpdate(pos, Registration.DISPLAY_BASE.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
        DisplayBaseTile base = (DisplayBaseTile) helper.getLevel().getBlockEntity(pos);
        CompoundTag data = new CompoundTag();
        data.putInt("Height", height);
        base.load(data);
        return base;
    }

    private static ItemStack text(String value) {
        ItemStack stack = new ItemStack(Registration.DISPLAY_TEXT.get());
        DisplayModuleItem.config(stack).putString("Text", value);
        return stack;
    }

    @GameTest(template = "empty")
    public static void seamOwnershipOverlapAndRecovery(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile left = base(helper, pos, Direction.NORTH, 3);
        DisplayBaseTile right = base(helper, pos.west(), Direction.NORTH, 3);
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack stack = text("HELLO");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertTrue(left.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(left, 60, 4, 1)), "A module can cross a seam");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "Installation consumes one module");
        helper.assertTrue(left.modules().size() == 1 && right.modules().isEmpty(), "The top-left base owns the module");
        helper.assertTrue(!DisplayGeometry.canPlace(right, new DisplayModuleType.Size(32, 24), 0, 4), "Overlap is detected across owners");
        DisplayGeometry.ModuleHit hit = DisplayGeometry.moduleAt(new DisplayGeometry.Hit(right, 4, 8, 1));
        helper.assertTrue(hit != null && hit.owner() == left, "Removal targets the owner across a seam");
        CompoundTag saved = left.saveWithoutMetadata();
        DisplayBaseTile restored = new DisplayBaseTile(pos, left.getBlockState());
        restored.load(saved);
        helper.assertTrue(restored.modules().size() == 1 && DisplayModuleItem.config(restored.modules().get(0).stack()).getString("Text").equals("HELLO"), "Module settings survive saving");
        helper.getLevel().setBlockAndUpdate(pos.west(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(left.modules().isEmpty(), "Breaking a supporting column clears the spanning module");
        long recovered = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).stream()
                .filter(entity -> entity.getItem().is(Registration.DISPLAY_TEXT.get())).count();
        helper.assertTrue(recovered == 1, "A split returns exactly one configured item");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void footprintBoundsAndOrientation(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile base = base(helper, pos, Direction.NORTH, 1);
        base(helper, pos.west(), Direction.SOUTH, 1);
        helper.assertTrue(DisplayGeometry.neighbor(base, 1) == null, "Opposite-facing bases never join");
        helper.assertTrue(!DisplayGeometry.fits(base, 60, 0, new DisplayModuleType.Size(8, 24)), "A module cannot cross a missing column");
        helper.assertTrue(!DisplayGeometry.fits(base, 0, 40, new DisplayModuleType.Size(32, 24)), "The base plate is outside the display");
        helper.assertTrue(DisplayGeometry.fits(base, 0, 36, new DisplayModuleType.Size(32, 24)), "The last display pixel is usable");
        helper.assertTrue(!DisplayGeometry.overlaps(0, 0, 32, 24, 32, 0, 32, 24), "Touching edges do not overlap");
        for (var item : new DisplayModuleItem[]{Registration.DISPLAY_POWER.get(), Registration.DISPLAY_STORAGE.get(), Registration.DISPLAY_TEXT.get()}) {
            for (int count : new int[]{-1, 1, 64, Integer.MAX_VALUE}) {
                for (int columns = 1; columns <= 8; columns++) {
                    ItemStack stack = new ItemStack(item);
                    CompoundTag config = DisplayModuleItem.config(stack);
                    config.putInt("Count", count);
                    config.putInt("Columns", columns);
                    config.putBoolean("Grid", true);
                    config.putInt("Mode", Integer.MAX_VALUE);
                    config.putInt("Scale", Integer.MAX_VALUE);
                    config.putString("Text", "x".repeat(1024));
                    DisplayModuleType.Size size = item.type().size(stack);
                    helper.assertTrue(size.width() <= 512 && size.height() <= 636, "Untrusted configuration cannot exceed bounded module dimensions");
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sharedSettingsAndSafeHeight(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile left = base(helper, pos, Direction.NORTH, 3);
        DisplayBaseTile right = base(helper, pos.west(), Direction.NORTH, 3);
        Player player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, text("Low module"));
        helper.assertTrue(left.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(left, 0, 160, 1)), "Low module must install");
        var gui = right.createGui("", Ariente.guiHandler.getComponentRegistry());
        gui.findChild("display_height").orElseThrow().hit(player, null, .4, 2);
        helper.assertTrue(left.getHeight() == 3 && right.getHeight() == 3, "Height changes cannot silently clip installed modules");
        left.removeModule(player, left.modules().get(0));
        gui.findChild("display_height").orElseThrow().hit(player, null, .4, 2);
        helper.assertTrue(left.getHeight() == 2 && right.getHeight() == 2, "A setting applies to every connected base");
        for (int i = 0; i < 20; i++) {
            gui.findChild("display_height").orElseThrow().hit(player, null, 7.4, 2);
        }
        helper.assertTrue(left.getHeight() == 10 && right.getHeight() == 10, "Height is bounded to ten blocks");
        var original = left.background();
        for (int i = 0; i < 3; i++) {
            gui.findChild("background").orElseThrow().hit(player, null, 0, 0);
        }
        helper.assertTrue(left.background() == original && right.background() == original, "All three background types cycle consistently");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void surfacePresetsMigrateAndBoundSettings(GameTestHelper helper) {
        helper.assertTrue(DisplayAppearance.read(new CompoundTag()).equals(DisplayAppearance.DEFAULT), "New bases use the current pale gray and a border");
        int[] colors = {DisplayAppearance.DEFAULT_COLOR, DisplayAppearance.DEFAULT_COLOR, 0, 0xffffff, 0x679ec4, 0x81c374};
        for (int preset = 0; preset < 6; preset++) {
            CompoundTag old = new CompoundTag();
            old.putInt("Background", preset);
            DisplayAppearance appearance = DisplayAppearance.read(old);
            DisplayBackground type = preset == 0 ? DisplayBackground.INVISIBLE : preset == 1 ? DisplayBackground.TRANSPARENT : DisplayBackground.SOLID;
            helper.assertTrue(appearance.type() == type && appearance.color() == colors[preset] && appearance.border() == (preset != 0),
                    "All six legacy presets preserve their fill and frame");
            CompoundTag saved = new CompoundTag();
            appearance.write(saved);
            saved.putInt("Background", (preset + 1) % 6);
            helper.assertTrue(DisplayAppearance.read(saved).equals(appearance), "Independent settings round-trip and supersede obsolete presets");
        }
        var appearance = DisplayAppearance.DEFAULT.withTransparency(100);
        helper.assertTrue(appearance.alpha() == 0 && appearance.scanLineColor() >>> 24 == 0, "100% transparency removes the fill and scan lines");
        helper.assertTrue(appearance.withTransparency(0).alpha() == 255, "0% transparency is opaque");
        helper.assertTrue(appearance.withType(DisplayBackground.SOLID).alpha() == 255, "Solid surfaces ignore the remembered transparency");
        helper.assertTrue(appearance.withType(DisplayBackground.INVISIBLE).alpha() == 0 && appearance.withType(DisplayBackground.INVISIBLE).border(),
                "The border is independent of an invisible background");
        helper.assertTrue(appearance.withTransparency(-200).transparency() == 0 && appearance.withTransparency(200).transparency() == 100,
                "Transparency is bounded to a percentage");
        helper.assertTrue(appearance.withColor(0xff123456).color() == 0x123456, "The color picker cannot inject alpha into the surface color");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void independentSurfaceControlsShareAndRememberChoices(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile left = base(helper, pos, Direction.NORTH, 3);
        DisplayBaseTile right = base(helper, pos.west(), Direction.NORTH, 3);
        Player player = helper.makeMockSurvivalPlayer();
        HoloGuiEntity holo = new HoloGuiEntity(HOLOGUI_ENTITY_TYPE.get(), helper.getLevel());
        var registry = Ariente.guiHandler.getComponentRegistry();
        var gui = right.createGui("default", registry);
        var slider = (IIntegerSlider) gui.findChild("surface_transparency").orElseThrow();
        slider.drag(player, holo, 3.9, 5.8);
        helper.assertTrue(left.appearance().transparency() == 50 && right.appearance().equals(left.appearance()), "Transparency changes every connected base");
        gui.findChild("surface_border").orElseThrow().hit(player, holo, 5, 4.5);
        helper.assertTrue(!left.appearance().border() && !right.appearance().border(), "Border toggles across the whole display");
        gui.findChild("surface_color").orElseThrow().hit(player, holo, 1, 4.5);
        helper.assertTrue(holo.getTag().equals("surface-color"), "The color button opens the picker");
        right.createGui(holo.getTag(), registry).findChild("color_picker").orElseThrow().hit(player, holo, 4, 3);
        int color = right.appearance().color();
        helper.assertTrue(color != DisplayAppearance.DEFAULT_COLOR && left.appearance().color() == color && holo.getTag().equals("default"),
                "Selecting a color updates the row and returns to base settings");
        gui.findChild("background").orElseThrow().hit(player, holo, 1, 3.4);
        helper.assertTrue(right.background() == DisplayBackground.SOLID && gui.findChild("surface_transparency").isEmpty(),
                "Solid mode hides the transparency slider from interaction as well as rendering");
        gui.hit(player, holo, 7.4, 5.8);
        helper.assertTrue(right.appearance().transparency() == 50, "Hidden slider coordinates cannot change the stored percentage");
        gui.findChild("background").orElseThrow().hit(player, holo, 1, 3.4);
        helper.assertTrue(right.background() == DisplayBackground.INVISIBLE && right.appearance().alpha() == 0, "Invisible mode removes the surface fill");
        gui.findChild("surface_border").orElseThrow().hit(player, holo, 5, 4.5);
        helper.assertTrue(right.appearance().border() && right.background() == DisplayBackground.INVISIBLE, "An invisible surface can retain its frame");
        gui.findChild("background").orElseThrow().hit(player, holo, 1, 3.4);
        helper.assertTrue(gui.findChild("surface_transparency").isPresent() && right.appearance().transparency() == 50 && right.appearance().color() == color,
                "Returning to transparent mode restores its previous color and percentage");
        right.createGui("surface-color", registry).findChild("color_close").orElseThrow().hit(player, holo, 7.5, .2);
        helper.assertTrue(holo.getTag().equals("default") && right.appearance().color() == color, "Closing the picker preserves the selected color");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void surfaceSettingsPersistJoinAndReconcile(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile left = base(helper, pos, Direction.NORTH, 3);
        DisplayAppearance chosen = new DisplayAppearance(DisplayBackground.TRANSPARENT, 0x275ec8, 42, false);
        CompoundTag saved = left.saveWithoutMetadata();
        chosen.write(saved);
        saved.putLong("Revision", 100);
        left.load(saved);
        DisplayBaseTile right = base(helper, pos.west(), Direction.NORTH, 3);
        right.onBlockPlacedBy(helper.getLevel(), right.getBlockPos(), right.getBlockState(), helper.makeMockSurvivalPlayer(), ItemStack.EMPTY);
        helper.assertTrue(left.appearance().equals(chosen) && right.appearance().equals(chosen), "Extending a display adopts all appearance settings");
        DisplayBaseTile restored = new DisplayBaseTile(pos, left.getBlockState());
        restored.load(left.saveWithoutMetadata());
        helper.assertTrue(restored.appearance().equals(chosen), "All settings survive world saving");
        CompoundTag packet = new CompoundTag();
        left.saveClientDataToNBT(packet);
        restored.loadClientDataFromNBT(packet);
        helper.assertTrue(restored.appearance().equals(chosen), "The client receives all four appearance settings");
        CompoundTag newest = right.saveWithoutMetadata();
        DisplayAppearance changed = chosen.withType(DisplayBackground.SOLID).withColor(0x81c374).withBorder(true);
        changed.write(newest);
        newest.putLong("Revision", 200);
        right.load(newest);
        left.tickServer();
        helper.assertTrue(left.appearance().equals(changed) && right.appearance().equals(changed), "Reload reconciliation uses the complete newest appearance");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void inventoryAggregationAndRefresh(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(pos);
        chest.setItem(0, new ItemStack(Items.IRON_INGOT, 40));
        chest.setItem(1, new ItemStack(Items.IRON_INGOT, 30));
        chest.setItem(2, new ItemStack(Items.GOLD_INGOT, 8));
        ItemStack stack = new ItemStack(Registration.DISPLAY_STORAGE.get());
        link(stack, helper, pos);
        StorageDisplayModule type = (StorageDisplayModule) Registration.DISPLAY_STORAGE.get().type();
        CompoundTag initial = type.sample(helper.getLevel(), stack, new CompoundTag());
        var items = initial.getList("Items", Tag.TAG_COMPOUND);
        helper.assertTrue(items.size() == 2 && items.getCompound(0).getLong("Count") == 70, "Counts aggregate all matching stacks and sort by amount");
        chest.setItem(0, ItemStack.EMPTY);
        helper.assertTrue(type.sample(helper.getLevel(), stack, initial).equals(initial), "Source snapshots are cached within the sampling interval");
        helper.runAfterDelay(21, () -> {
            CompoundTag next = type.sample(helper.getLevel(), stack, initial);
            helper.assertTrue(next.getList("Items", Tag.TAG_COMPOUND).getCompound(0).getLong("Count") == 30, "The next sampling interval sees inventory changes");
            helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            helper.assertTrue(type.sample(helper.getLevel(), stack, next).getString("Status").equals("Target missing"), "Broken links never retain stale item data");
            helper.succeed();
        });
    }

    private static void link(ItemStack stack, GameTestHelper helper, BlockPos pos) {
        CompoundTag link = new CompoundTag();
        link.putString("Dimension", helper.getLevel().dimension().location().toString());
        link.putLong("Pos", pos.asLong());
        link.putInt("Side", Direction.UP.get3DDataValue());
        link.putString("Block", helper.getLevel().getBlockState(pos).getBlock().builtInRegistryHolder().key().location().toString());
        DisplayModuleItem.config(stack).put("Link", link);
    }

    @GameTest(template = "empty")
    public static void powerTankAndBoundedHistory(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, Registration.NEGARITE_GENERATOR.get().defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Registration.NEGARITE_TANK.get().defaultBlockState());
        var generator = (NegariteGeneratorTile) helper.getLevel().getBlockEntity(pos);
        PowerSystem system = PowerSystem.getPowerSystem(helper.getLevel());
        int id = system.allocateId();
        generator.fillCableId(id);
        system.addPower(id, 5000, PowerType.NEGARITE);
        ItemStack stack = new ItemStack(Registration.DISPLAY_POWER.get());
        link(stack, helper, pos.above());
        DisplayModuleItem.config(stack).putInt("Mode", 2);
        PowerDisplayModule type = (PowerDisplayModule) Registration.DISPLAY_POWER.get().type();
        CompoundTag data = new CompoundTag();
        for (int i = 0; i < 100; i++) {
            data = type.sample(helper.getLevel(), stack, data);
        }
        helper.assertTrue(!data.contains("Status") && data.getLong("Available") == 5000, "A tank resolves to its generator and actual network flux");
        helper.assertTrue(data.getLongArray("History").length == 32, "Power history is bounded to 32 samples");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rayOcclusionAndBackFace(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile base = base(helper, pos, Direction.NORTH, 3);
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 2);
        player.setYRot(0);
        player.setXRot(0);
        helper.assertTrue(DisplayGeometry.intersect(base, player, 5) != null, "A front-facing display can be targeted");
        helper.getLevel().setBlockAndUpdate(pos.north().above(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(DisplayGeometry.intersect(base, player, 5) == null, "Solid blocks occlude display placement");
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 3);
        player.setYRot(180);
        helper.assertTrue(DisplayGeometry.intersect(base, player, 5) == null, "The back face cannot receive mirrored interactions");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void textSessionCannotEditSwappedItem(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        ItemStack first = text("First");
        first.getOrCreateTag().putUUID("ModuleId", UUID.randomUUID());
        player.setItemInHand(InteractionHand.MAIN_HAND, first);
        var gui = Ariente.guiHandler.getGuiRegistry().createGui(ModuleSettings.guiId(InteractionHand.MAIN_HAND, false), player);
        ITextEntry entry = (ITextEntry) gui.findChild("display_text").orElseThrow();
        entry.commit(player, null, "Line 1\nLine 2\u0001");
        helper.assertTrue(DisplayModuleItem.config(first).getString("Text").equals("Line 1\nLine 2"), "Text supports line breaks and rejects control codes");
        ItemStack second = text("Second");
        second.getOrCreateTag().putUUID("ModuleId", UUID.randomUUID());
        player.setItemInHand(InteractionHand.MAIN_HAND, second);
        entry.commit(player, null, "Overwrite");
        helper.assertTrue(DisplayModuleItem.config(second).getString("Text").equals("Second"), "A GUI session cannot edit a swapped held item");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void staleReadingsCannotOverwriteNewLayout(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile server = base(helper, pos, Direction.NORTH, 2);
        Player player = helper.makeMockSurvivalPlayer();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Registration.DISPLAY_POWER.get()));
        helper.assertTrue(server.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(server, 0, 0, 1)), "Power module must install");
        CompoundTag initial = new CompoundTag();
        server.saveClientDataToNBT(initial);
        DisplayBaseTile client = new DisplayBaseTile(pos, server.getBlockState());
        client.loadClientDataFromNBT(initial);
        CompoundTag delta = new CompoundTag();
        delta.putLong("Usage", 1234);
        client.applyReadings(new PacketDisplayData(pos, initial.getLong("Layout") + 1,
                List.of(new PacketDisplayData.Entry(0, delta))));
        helper.assertTrue(!client.modules().get(0).snapshot().contains("Usage"), "Stale or reordered readings cannot target a different module layout");
        client.applyReadings(new PacketDisplayData(pos, initial.getLong("Layout"),
                List.of(new PacketDisplayData.Entry(0, delta))));
        helper.assertTrue(client.modules().get(0).snapshot().getLong("Usage") == 1234, "Matching reading deltas update the existing module");
        helper.assertTrue(!client.saveWithoutMetadata().getList("Modules", Tag.TAG_COMPOUND).getCompound(0).contains("Data"), "Live readings never bloat persistent chunk data");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void installedModuleEditingAndGrowthRecovery(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile base = base(helper, pos, Direction.NORTH, 2);
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, text("HI"));
        helper.assertTrue(base.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(base, 0, 0, 1)), "Text module must install");
        UUID id = base.modules().get(0).id();
        var gui = base.createGui(ModuleSettings.installedTag(id, false), Ariente.guiHandler.getComponentRegistry());
        ITextEntry entry = (ITextEntry) gui.findChild("display_text").orElseThrow();
        entry.commit(player, null, "HELLO");
        helper.assertTrue(base.modules().get(0).id().equals(id), "Reconfiguration preserves the installed module identity");
        helper.assertTrue(DisplayModuleItem.config(base.modules().get(0).stack()).getString("Text").equals("HELLO"), "Installed module GUI edits work with empty hands");
        helper.assertTrue(base.modules().get(0).size().width() > 32, "The cached footprint updates after editing");
        DisplayBaseTile restored = new DisplayBaseTile(pos, base.getBlockState());
        restored.load(base.saveWithoutMetadata());
        helper.assertTrue(restored.modules().get(0).id().equals(id), "Stable identities survive save/reload");
        entry.commit(player, null, "W".repeat(60));
        helper.assertTrue(base.modules().isEmpty(), "A module extending beyond the display is ejected");
        long recovered = player.getInventory().items.stream().filter(stack -> stack.is(Registration.DISPLAY_TEXT.get())
                && DisplayModuleItem.config(stack).getString("Text").equals("W".repeat(60))).count();
        helper.assertTrue(recovered == 1, "The recovered module contains the newly edited settings");
        entry.commit(player, null, "Another edit");
        helper.assertTrue(player.getInventory().items.stream().filter(stack -> stack.is(Registration.DISPLAY_TEXT.get())).count() == 1,
                "A stale GUI cannot eject a module twice");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void growthOverlapsAnotherBaseAndFullInventoryDrops(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile left = base(helper, pos, Direction.NORTH, 2);
        DisplayBaseTile right = base(helper, pos.west(), Direction.NORTH, 2);
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, text("HI"));
        helper.assertTrue(left.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(left, 32, 0, 1)), "Small module fits left of the seam");
        player.setItemInHand(InteractionHand.MAIN_HAND, text("OTHER"));
        helper.assertTrue(right.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(right, 0, 0, 1)), "Second module fits adjacent to the first");
        UUID id = left.modules().get(0).id();
        for (int index = 0; index < player.getInventory().items.size(); index++) {
            player.getInventory().items.set(index, new ItemStack(Items.STONE, 64));
        }
        left.configureModule(player, id, config -> config.putString("Text", "HELLO WORLD"));
        helper.assertTrue(left.modules().isEmpty() && right.modules().size() == 1, "Growing across a seam cannot overlap another module");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3));
        helper.assertTrue(drops.stream().filter(drop -> drop.getItem().is(Registration.DISPLAY_TEXT.get())
                        && DisplayModuleItem.config(drop.getItem()).getString("Text").equals("HELLO WORLD")).count() == 1,
                "If inventory is full, the edited module drops once at the player");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void integerSlidersClampAndStepPrecisely(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        int[] value = {5};
        IIntegerSlider slider = Ariente.guiHandler.getComponentRegistry().integerSlider(0, 0, 8, 1)
                .label("Value").range(1, 10).getter((p, e) -> value[0]).changed((p, e, next) -> value[0] = next);
        slider.hit(player, null, .2, .5);
        helper.assertTrue(value[0] == 4, "Left arrow changes by exactly one");
        slider.hit(player, null, 7.8, .5);
        helper.assertTrue(value[0] == 5, "Right arrow changes by exactly one");
        slider.drag(player, null, -100, .5);
        helper.assertTrue(value[0] == 1, "Dragging below the track clamps to the minimum");
        slider.hit(player, null, .2, .5);
        helper.assertTrue(value[0] == 1, "Precise decrement never crosses the minimum");
        slider.drag(player, null, 100, .5);
        helper.assertTrue(value[0] == 10, "Dragging beyond the track clamps to the maximum");
        slider.hit(player, null, 7.8, .5);
        helper.assertTrue(value[0] == 10, "Precise increment never crosses the maximum");
        slider.range(-80, 80);
        slider.drag(player, null, 4, .5);
        helper.assertTrue(value[0] == 0, "Signed speed sliders have an exact zero midpoint");
        slider.range(1, Integer.MAX_VALUE).logarithmic(true);
        slider.drag(player, null, 4, .5);
        helper.assertTrue(value[0] > 1000 && value[0] < 100000, "Large inventory thresholds use a useful logarithmic range");
        slider.drag(player, null, 100, .5);
        helper.assertTrue(value[0] == Integer.MAX_VALUE, "Large ranges reach their exact maximum without overflow");
        slider.hit(player, null, 7.8, .5);
        helper.assertTrue(value[0] == Integer.MAX_VALUE, "Increment at the integer maximum is safe");
        slider.hit(player, null, .2, .5);
        helper.assertTrue(value[0] == Integer.MAX_VALUE - 1, "Large values still support precise one-step decrements");
        helper.succeed();
    }

    private static ServerPlayer dragPlayer(GameTestHelper helper, BlockPos pos) {
        ServerPlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "screen-drag"));
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 2);
        return player;
    }

    private static Vec3 displayLook(DisplayBaseTile base, Player player, int x, int y) {
        Direction right = DisplayGeometry.facing(base).getCounterClockWise();
        Vec3 point = new Vec3(base.getBlockPos().getX() + .5 + right.getStepX() * (x / 64d - .5),
                base.getBlockPos().getY() + base.getHeight() - y / 64d,
                base.getBlockPos().getZ() + .5 + right.getStepZ() * (x / 64d - .5));
        return point.subtract(player.getEyePosition()).normalize();
    }

    private static long moduleGuiCount(GameTestHelper helper, BlockPos pos, UUID id) {
        return helper.getLevel().getEntitiesOfClass(HoloGuiEntity.class, new AABB(pos).inflate(8)).stream()
                .filter(gui -> gui.getTag().equals(ModuleSettings.installedTag(id, false))).count();
    }

    @GameTest(template = "empty")
    public static void moduleClickConfiguresOnlyOnRelease(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile base = base(helper, pos, Direction.NORTH, 3);
        ServerPlayer player = dragPlayer(helper, pos);
        player.setItemInHand(InteractionHand.MAIN_HAND, text("HI"));
        base.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(base, 4, 4, 1));
        InstalledModule module = base.modules().get(0);
        UUID ticket = UUID.randomUUID();
        Vec3 look = displayLook(base, player, 16, 16);
        helper.assertTrue(DisplayGesture.begin(player, pos, module.id(), ticket, look), "Press authorizes the selected module");
        helper.assertTrue(moduleGuiCount(helper, pos, module.id()) == 0, "Holding right click does not open configuration");
        DisplayGesture.finish(player, pos, module.id(), ticket, look, false, true);
        helper.assertTrue(moduleGuiCount(helper, pos, module.id()) == 1, "An unmoved release opens configuration");
        helper.assertTrue(base.findModule(module.id()) == module, "A click does not move or replace the module");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void moduleDragCrossesSeamsAndPreservesData(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile source = base(helper, pos, Direction.NORTH, 3);
        DisplayBaseTile target = base(helper, pos.west(), Direction.NORTH, 3);
        ServerPlayer player = dragPlayer(helper, pos);
        player.setItemInHand(InteractionHand.MAIN_HAND, text("HI"));
        source.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(source, 4, 4, 1));
        InstalledModule module = source.modules().get(0);
        CompoundTag data = new CompoundTag();
        data.putLong("Sample", 1234);
        module.updateSnapshot(data);
        UUID ticket = UUID.randomUUID();
        DisplayGesture.begin(player, pos, module.id(), ticket, displayLook(source, player, 16, 16));
        helper.assertTrue(source.findModule(module.id()) == module && target.modules().isEmpty(), "Press leaves persistent state untouched");
        var moved = DisplayGesture.finish(player, pos, module.id(), ticket, displayLook(source, player, 88, 40), true, true);
        helper.assertTrue(moved != null && moved.target() == target, "A legal release crosses the seam");
        InstalledModule installed = target.findModule(module.id());
        helper.assertTrue(source.modules().isEmpty() && target.modules().size() == 1, "Ownership moves to the new top-left base exactly once");
        helper.assertTrue(installed.x() == 12 && installed.y() == 28, "Dragging retains the original grab offset and snaps to the placement grid");
        helper.assertTrue(DisplayModuleItem.config(installed.stack()).getString("Text").equals("HI") && installed.snapshot().getLong("Sample") == 1234,
                "Moving preserves identity, configuration and cached readings");
        helper.assertTrue(moduleGuiCount(helper, pos, module.id()) == 0, "Dragging never opens configuration");
        DisplayBaseTile restored = new DisplayBaseTile(target.getBlockPos(), target.getBlockState());
        restored.load(target.saveWithoutMetadata());
        helper.assertTrue(restored.findModule(module.id()).x() == 12 && restored.findModule(module.id()).y() == 28, "Moved positions survive saving");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void illegalDropsAndRedPreviewsDoNotMoveModules(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile base = base(helper, pos, Direction.NORTH, 3);
        ServerPlayer player = dragPlayer(helper, pos);
        player.setItemInHand(InteractionHand.MAIN_HAND, text("HI"));
        base.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(base, 4, 4, 1));
        player.setItemInHand(InteractionHand.MAIN_HAND, text("OTHER"));
        base.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(base, 4, 64, 1));
        InstalledModule original = base.modules().get(0);
        for (int[] release : new int[][]{{16, 76}, {-32, 16}, {16, -8}}) {
            UUID ticket = UUID.randomUUID();
            DisplayGesture.begin(player, pos, original.id(), ticket, displayLook(base, player, 16, 16));
            var moved = DisplayGesture.finish(player, pos, original.id(), ticket, displayLook(base, player, release[0], release[1]), true, true);
            helper.assertTrue(moved == null && base.findModule(original.id()) == original, "Overlap or an off-screen drop keeps the last committed position");
        }
        UUID ticket = UUID.randomUUID();
        DisplayGesture.begin(player, pos, original.id(), ticket, displayLook(base, player, 16, 16));
        helper.assertTrue(DisplayGesture.finish(player, pos, original.id(), ticket, displayLook(base, player, 16, 44), true, false) == null,
                "An explicitly red client preview can never be accepted as a legal drop");
        helper.assertTrue(base.modules().size() == 2 && player.getInventory().items.stream().noneMatch(stack -> stack.is(Registration.DISPLAY_TEXT.get())),
                "Rejected drags neither eject nor duplicate module items");
        helper.assertTrue(moduleGuiCount(helper, pos, original.id()) == 0, "Illegal drag releases never open configuration");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dragSessionsRejectStaleEditsAndOldTickets(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile base = base(helper, pos, Direction.NORTH, 3);
        ServerPlayer player = dragPlayer(helper, pos);
        player.setItemInHand(InteractionHand.MAIN_HAND, text("HI"));
        base.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(base, 4, 4, 1));
        UUID id = base.modules().get(0).id();
        UUID first = UUID.randomUUID();
        DisplayGesture.begin(player, pos, id, first, displayLook(base, player, 16, 16));
        base.configureModule(player, id, config -> config.putString("Text", "NEW"));
        helper.assertTrue(DisplayGesture.finish(player, pos, id, first, displayLook(base, player, 16, 44), true, true) == null,
                "Reconfiguration invalidates the old drag rather than overwriting newer state");
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        DisplayGesture.begin(player, pos, id, second, displayLook(base, player, 16, 16));
        DisplayGesture.begin(player, pos, id, third, displayLook(base, player, 16, 16));
        DisplayGesture.cancel(player, second);
        DisplayGesture.keepAlive(player, pos, id, second);
        DisplayGesture.finish(player, pos, id, second, displayLook(base, player, 16, 44), true, true);
        helper.assertTrue(DisplayGesture.finish(player, pos, id, third, displayLook(base, player, 16, 44), true, true) != null,
                "Late release, cancellation and keepalive packets cannot replace the current drag session");
        helper.assertTrue(base.findModule(id).y() == 32 && DisplayModuleItem.config(base.findModule(id).stack()).getString("Text").equals("NEW"),
                "The current drag preserves the latest module settings");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dragCannotCrossGapsOrOverfillAnotherBase(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        DisplayBaseTile source = base(helper, pos, Direction.NORTH, 10);
        DisplayBaseTile target = base(helper, pos.west(), Direction.NORTH, 10);
        Player player = helper.makeMockSurvivalPlayer();
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() - 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, text("HI"));
        source.install(player, InteractionHand.MAIN_HAND, new DisplayGeometry.Hit(source, 4, 4, 1));
        InstalledModule module = source.modules().get(0);
        for (int index = 0; index < DisplayBaseTile.MAX_MODULES; index++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, text(""));
            helper.assertTrue(target.install(player, InteractionHand.MAIN_HAND,
                    new DisplayGeometry.Hit(target, index % 2 * 32, index / 2 * 24, 1)), "Destination fixture fills its module capacity");
        }
        helper.assertTrue(!source.moveModule(player, module, target, 0, 500), "A move to another base enforces that base's module capacity");
        DisplayBaseTile beyondGap = base(helper, pos.east(2), Direction.NORTH, 10);
        helper.assertTrue(!source.moveModule(player, module, beyondGap, 0, 0), "Separate rows cannot be joined by dragging across a gap");
        helper.assertTrue(source.findModule(module.id()) == module && target.modules().size() == DisplayBaseTile.MAX_MODULES,
                "Failed moves preserve both bases exactly");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void constructorBlueprintsExist(GameTestHelper helper) {
        for (var item : new ItemLike[]{Registration.DISPLAY_BASE.get(), Registration.DISPLAY_POWER.get(),
                Registration.DISPLAY_STORAGE.get(), Registration.DISPLAY_TEXT.get()}) {
            ItemStack output = new ItemStack(item);
            helper.assertTrue(BlueprintRecipeRegistry.findRecipe(output) != null, "Each display component needs a Constructor recipe");
            helper.assertTrue(ItemStack.isSameItem(BlueprintItem.getDestination(BlueprintItem.makeBluePrint(output)), output), "Each component has a usable blueprint item");
        }
        helper.assertTrue(BlueprintRecipeRegistry.findRecipe(new ItemStack(Registration.DISPLAY_ALIEN_TEXT.get())) == null,
                "Salvaged alien modules must stay outside the craftable display components");
        helper.succeed();
    }
}
