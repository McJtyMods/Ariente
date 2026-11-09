package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.blocks.utility.StorageTile;
import mcjty.ariente.display.DisplayBaseTile;
import mcjty.ariente.display.DisplayAppearance;
import mcjty.ariente.display.DisplayBackground;
import mcjty.ariente.display.client.DisplayClientEvents;
import mcjty.ariente.display.client.DisplayModuleDrag;
import mcjty.ariente.display.DisplayGeometry;
import mcjty.ariente.display.DisplayModuleItem;
import mcjty.ariente.display.ModuleSettings;
import mcjty.ariente.display.PacketDisplayAction;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.hologui.gui.TextEntryScreen;
import net.minecraft.client.CameraType;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.util.UUID;

/** Opt-in real-client rendering, keyboard editing and server packet regression checks. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class DisplayClientChecks {

    private static boolean started;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static volatile boolean textSaved;
    private static int ticks;
    private static String capture;
    private static UUID installedId;
    private static UUID draggedId;
    private static int surfaceColor;
    private static GraphicsStatus graphics;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.displayClientCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.gui.overlay() != null) {
            return;
        }
        require(failure == null, "Fixture failed: " + failure);
        if (!started) {
            started = true;
            graphics = mc.options.graphicsMode().get();
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.options.hideGui = false;
            mc.gui.setScreen(null);
            mc.mouseHandler.releaseMouse();
            UUID playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                try {
                    var server = mc.getSingleplayerServer();
                    var level = server.overworld();
                    var player = server.getPlayerList().getPlayer(playerId);
                    level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
                    level.setDayTime(6000);
                    level.getEntitiesOfClass(HoloGuiEntity.class, new AABB(-10, 190, -10, 10, 210, 10)).forEach(HoloGuiEntity::discard);
                    for (BlockPos pos : BlockPos.betweenClosed(-5, 199, -7, 8, 205, 4)) {
                        var block = pos.getY() == 199 ? Blocks.GRAY_CONCRETE : Blocks.AIR;
                        if (pos.getZ() == 3 && pos.getY() > 199) {
                            block = (pos.getX() + pos.getY()) % 3 == 0 ? Blocks.LIGHT_GRAY_CONCRETE : Blocks.GRAY_CONCRETE;
                        }
                        level.setBlock(pos, block.defaultBlockState(), 2);
                    }
                    BlockPos chestPos = new BlockPos(6, 200, 1);
                    level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
                    ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
                    chest.setItem(0, new ItemStack(Items.IRON_INGOT, 64));
                    chest.setItem(1, new ItemStack(Items.IRON_INGOT, 64));
                    chest.setItem(2, new ItemStack(Items.BIRCH_STAIRS, 32));
                    chest.setItem(3, new ItemStack(Items.GOLD_INGOT, 48));
                    chest.setItem(4, new ItemStack(Items.OAK_SLAB, 64));
                    BlockPos cubePos = new BlockPos(4, 201, 0);
                    level.setBlockAndUpdate(cubePos, Registration.STORAGE.get().defaultBlockState()
                            .setValue(BlockStateProperties.FACING, Direction.NORTH));
                    StorageTile cube = (StorageTile) level.getBlockEntity(cubePos);
                    var inventory = cube.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElseThrow();
                    ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
                    sword.enchant(Enchantments.SHARPNESS, 1);
                    ItemStack[] icons = {new ItemStack(Items.BIRCH_STAIRS, 64), new ItemStack(Items.OAK_SLAB, 64),
                            new ItemStack(Items.GLASS, 64), sword};
                    for (int slot = 0; slot < icons.length; slot++) {
                        require(inventory.insertItem(slot * StorageTile.STACKS_PER_TYPE, icons[slot], false).isEmpty(),
                                "The rendering fixture must populate all four storage slots");
                    }
                    BlockPos generatorPos = new BlockPos(-3, 200, 1);
                    level.setBlockAndUpdate(generatorPos, Registration.NEGARITE_GENERATOR.get().defaultBlockState());
                    level.setBlockAndUpdate(generatorPos.above(), Registration.NEGARITE_TANK.get().defaultBlockState());
                    ((NegariteGeneratorTile) level.getBlockEntity(generatorPos)).feedDust(64);
                    for (int column = 0; column < 4; column++) {
                        BlockPos pos = new BlockPos(3 - column, 200, 0);
                        level.setBlockAndUpdate(pos, Registration.DISPLAY_BASE.get().defaultBlockState()
                                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
                        DisplayBaseTile base = (DisplayBaseTile) level.getBlockEntity(pos);
                        CompoundTag data = new CompoundTag();
                        data.putInt("Height", 3);
                        data.putInt("Background", 1);
                        ListTag modules = new ListTag();
                        if (column == 0) {
                            ItemStack heading = new ItemStack(Registration.DISPLAY_TEXT.get());
                            DisplayModuleItem.config(heading).putString("Text", "ARIENTE");
                            DisplayModuleItem.config(heading).putInt("Scale", 2);
                            modules.add(module(heading, 4, 6));
                            ItemStack subtitle = new ItemStack(Registration.DISPLAY_TEXT.get());
                            DisplayModuleItem.config(subtitle).putString("Text", "FLUX NETWORK / OPERATIONS");
                            DisplayModuleItem.config(subtitle).putInt("Color", 0x79d8ed);
                            modules.add(module(subtitle, 4, 42));
                            ItemStack power = new ItemStack(Registration.DISPLAY_POWER.get());
                            DisplayModuleItem.config(power).putInt("Mode", 2);
                            DisplayModuleItem.config(power).putBoolean("Usage", false);
                            DisplayModuleItem.config(power).putBoolean("Output", true);
                            CompoundTag powerLink = new CompoundTag();
                            powerLink.putString("Dimension", "minecraft:overworld");
                            powerLink.putLong("Pos", generatorPos.asLong());
                            powerLink.putInt("Side", Direction.UP.get3DDataValue());
                            powerLink.putString("Block", "ariente:negarite_generator");
                            DisplayModuleItem.config(power).put("Link", powerLink);
                            CompoundTag powerModule = module(power, 0, 68);
                            CompoundTag sample = new CompoundTag();
                            sample.putString("Name", "Constructor");
                            sample.putLong("Available", 18000);
                            sample.putLong("Usage", 2400);
                            long[] graph = new long[32];
                            for (int i = 0; i < graph.length; i++) {
                                graph[i] = 1000 + (long) (1400 * Math.abs(Math.sin(i * .4)));
                            }
                            sample.putLongArray("History", graph);
                            powerModule.put("Data", sample);
                            modules.add(powerModule);
                        } else if (column == 2) {
                            ItemStack storage = new ItemStack(Registration.DISPLAY_STORAGE.get());
                            DisplayModuleItem.config(storage).putInt("Count", 4);
                            CompoundTag link = new CompoundTag();
                            link.putString("Dimension", "minecraft:overworld");
                            link.putLong("Pos", chestPos.asLong());
                            link.putInt("Side", Direction.UP.get3DDataValue());
                            link.putString("Block", "minecraft:chest");
                            DisplayModuleItem.config(storage).put("Link", link);
                            modules.add(module(storage, 0, 68));
                        } else if (column == 3) {
                            ItemStack alien = new ItemStack(Registration.DISPLAY_ALIEN_TEXT.get());
                            modules.add(module(alien, 4, 8));
                        }
                        data.put("Modules", modules);
                        base.load(data);
                        base.changed();
                    }
                    level.getEntitiesOfClass(ItemEntity.class, new AABB(-10, 190, -10, 10, 210, 10)).forEach(ItemEntity::discard);
                    player.setGameMode(GameType.CREATIVE);
                    player.getInventory().clearContent();
                    player.teleportTo(level, 2, 200, -3.8, 0, 0);
                    ready = true;
                } catch (Throwable error) {
                    failure = error;
                }
            });
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 40) {
            aim(mc, new Vec3(.5, 202.5, .5));
        } else if (ticks == 45) {
            capture = "display-alien-glyphs";
        } else if (ticks == 48) {
            aim(mc, new Vec3(2, 201.62, .5));
        } else if (ticks == 50) {
            capture = "display-surface";
        } else if (ticks == 51) {
            aim(mc, new Vec3(3.5, 201.5, .5));
        } else if (ticks == 53) {
            capture = "display-storage-icons";
        } else if (ticks == 55) {
            mc.getSingleplayerServer().execute(() -> {
                var chest = (ChestBlockEntity) mc.getSingleplayerServer().overworld().getBlockEntity(new BlockPos(6, 200, 1));
                for (int slot : new int[]{0, 1, 3, 5}) {
                    chest.setItem(slot, new ItemStack(Items.GOLD_INGOT, 64));
                }
            });
        } else if (ticks == 56) {
            mc.options.graphicsMode().set(GraphicsStatus.FABULOUS);
            mc.levelRenderer.allChanged();
            aim(mc, new Vec3(4.5, 201.5, .5));
        } else if (ticks == 59) {
            capture = "display-storage-icons-fabulous";
        } else if (ticks == 60) {
            give(mc, "READY");
        } else if (ticks == 61) {
            mc.options.graphicsMode().set(graphics);
            mc.levelRenderer.allChanged();
        } else if (ticks == 80) {
            aim(mc, new Vec3(3.8, 200.54, .5));
        } else if (ticks == 85) {
            var storage = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(1, 200, 0));
            var first = storage.modules().get(0).snapshot().getList("Items", 10).getCompound(0);
            require(first.getLong("Count") == 256 && ItemStack.of(first.getCompound("Item")).is(Items.GOLD_INGOT),
                    "Reading deltas must update counts and item ordering on the client without a layout update");
        } else if (ticks == 90) {
            capture = "display-preview";
        } else if (ticks == 100) {
            give(mc, "ABCDE");
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                Ariente.guiHandler.openHoloGui(player, ModuleSettings.guiId(InteractionHand.MAIN_HAND, false), 1);
            });
        } else if (ticks == 120) {
            HoloGuiEntity holo = gui(mc);
            aimGui(mc, holo, 2, 2);
            capture = "display-text-settings";
        } else if (ticks == 125) {
            HoloGuiEntity holo = gui(mc);
            // Use the real attack path: both client focus and server edit authorization.
            mc.gameMode.attack(mc.player, holo);
        } else if (ticks == 135) {
            require(mc.gui.screen() instanceof TextEntryScreen, "Clicking a hologui text field must focus text entry");
            mc.gui.screen().keyPressed(GLFW.GLFW_KEY_HOME, 0, 0);
            mc.gui.screen().charTyped('!', 0);
            mc.gui.screen().keyPressed(GLFW.GLFW_KEY_DELETE, 0, 0);
            mc.gui.screen().keyPressed(GLFW.GLFW_KEY_END, 0, 0);
            mc.gui.screen().keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0);
            mc.gui.screen().charTyped('O', 0);
            mc.gui.screen().charTyped('K', 0);
            capture = "display-text-editor";
        } else if (ticks == 140) {
            mc.gui.screen().keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, 0);
        } else if (ticks == 150) {
            require(mc.gui.screen() == null, "Escape must leave keyboard focus");
            mc.mouseHandler.releaseMouse();
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                String text = DisplayModuleItem.config(player.getMainHandItem()).getString("Text");
                if (!text.equals("!BCDE\nOK")) {
                    failure = new IllegalStateException("Text edit did not reach server: " + text);
                } else {
                    textSaved = true;
                }
                var holo = (HoloGuiEntity) player.level().getEntity(gui(mc).getId());
                holo.switchGui(ModuleSettings.guiId(InteractionHand.MAIN_HAND, true));
            });
        } else if (ticks == 165) {
            require(textSaved, "Text edits must commit through the network");
            capture = "display-color-picker";
            aimGui(mc, gui(mc), 4, 3);
        } else if (ticks == 170) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 175) {
            require(gui(mc).getGuiId().equals(ModuleSettings.guiId(InteractionHand.MAIN_HAND, false)), "Selecting a shade returns to module settings");
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                if (!DisplayModuleItem.config(player.getMainHandItem()).contains("Color")) {
                    failure = new IllegalStateException("Color selection must reach the held module on the server");
                }
                ((HoloGuiEntity) player.level().getEntity(gui(mc).getId())).switchGui(ModuleSettings.guiId(InteractionHand.MAIN_HAND, true));
            });
        } else if (ticks == 180) {
            aimGui(mc, gui(mc), 7.5, .2);
        } else if (ticks == 185) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 190) {
            require(gui(mc).getGuiId().equals(ModuleSettings.guiId(InteractionHand.MAIN_HAND, false)), "The color picker's x returns to module settings");
            mc.getSingleplayerServer().execute(() -> {
                var level = mc.getSingleplayerServer().overworld();
                level.getEntitiesOfClass(HoloGuiEntity.class, new AABB(-10, 190, -10, 10, 210, 10)).forEach(HoloGuiEntity::discard);
            });
            aim(mc, new Vec3(3.8, 200.54, .5));
        } else if (ticks == 200) {
            DisplayGeometry.Hit hit = DisplayClientEvents.pick(mc.player);
            require(hit != null, "The client must target the display surface: eye=" + mc.player.getEyePosition()
                    + " look=" + mc.player.getLookAngle() + " loaded=" + DisplayBaseTile.LOADED.size());
            ArienteMessages.INSTANCE.sendToServer(new PacketDisplayAction(hit.tile().getBlockPos(), InteractionHand.MAIN_HAND, false));
        } else if (ticks == 215) {
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            require(base.modules().size() == 4, "A valid placement must be accepted by the server and synchronized");
            mc.options.keyShift.setDown(true);
            DisplayGeometry.Hit hit = DisplayClientEvents.pick(mc.player);
            // Check removal on the server after the client's sneak state has propagated.
        } else if (ticks == 220) {
            DisplayGeometry.Hit hit = DisplayClientEvents.pick(mc.player);
            ArienteMessages.INSTANCE.sendToServer(new PacketDisplayAction(hit.tile().getBlockPos(), InteractionHand.MAIN_HAND, false));
        } else if (ticks == 235) {
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            require(base.modules().size() == 3, "Sneak right click must remove an installed module through the network");
            mc.options.keyShift.setDown(false);
            installedId = base.modules().get(0).id();
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.containerMenu.broadcastChanges();
            });
        } else if (ticks == 245) {
            aim(mc, new Vec3(3.8, 202.7, .5));
        } else if (ticks == 250) {
            clickDisplay(mc);
        } else if (ticks == 260) {
            require(gui(mc).getTag().equals(ModuleSettings.installedTag(installedId, false)), "Right click must open the installed module GUI");
            capture = "display-installed-settings";
            aimGui(mc, gui(mc), 2, 2);
        } else if (ticks == 265) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 275) {
            require(mc.gui.screen() instanceof TextEntryScreen, "Installed text supports keyboard editing with empty hands");
            mc.gui.screen().keyPressed(GLFW.GLFW_KEY_END, 0, 0);
            for (int index = 0; index < 7; index++) {
                mc.gui.screen().keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0);
            }
            for (char letter : "STATUS".toCharArray()) {
                mc.gui.screen().charTyped(letter, 0);
            }
        } else if (ticks == 285) {
            mc.gui.screen().keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, 0);
        } else if (ticks == 295) {
            mc.mouseHandler.releaseMouse();
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            require(base.findModule(installedId) != null && DisplayModuleItem.config(base.findModule(installedId).stack()).getString("Text").equals("STATUS"),
                    "Installed text edits update the display and retain identity");
            aimGui(mc, gui(mc), 1, 6.2);
        } else if (ticks == 305) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 315) {
            require(gui(mc).getTag().equals(ModuleSettings.installedTag(installedId, true)), "Installed module color picker retains its target");
            aimGui(mc, gui(mc), 4, 3);
        } else if (ticks == 320) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 330) {
            require(gui(mc).getTag().equals(ModuleSettings.installedTag(installedId, false)), "Selecting a color returns to the installed module");
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            require(DisplayModuleItem.config(base.findModule(installedId).stack()).contains("Color"), "Installed color edits reach the display");
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                player.level().getEntitiesOfClass(HoloGuiEntity.class, player.getBoundingBox().inflate(5)).forEach(HoloGuiEntity::discard);
                Ariente.guiHandler.openHoloGuiEntity(player.level(), new BlockPos(3, 200, 0), player, "default", 1);
            });
        } else if (ticks == 345) {
            capture = "display-base-controls";
            aimGui(mc, gui(mc), 2.46, 2.2);
        } else if (ticks == 350) {
            mc.options.keyAttack.setDown(true);
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 355) {
            aimGui(mc, gui(mc), 3.61, 2.2);
        } else if (ticks == 361) {
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            require(mc.options.keyAttack.isDown() && base.getHeight() == 5, "Slider dragging updates while still held, before release: held=" + mc.options.keyAttack.isDown() + ", height=" + base.getHeight() + ", cursor=" + gui(mc).getCursorX() + "," + gui(mc).getCursorY());
        } else if (ticks == 365) {
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 375) {
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            require(base.getHeight() == 5, "Holding and dragging a slider updates its server value");
            capture = "display-slider-drag";
            aimGui(mc, gui(mc), .5, 2.1);
        } else if (ticks == 385) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 390) {
            aimGui(mc, gui(mc), 2.5, 3.8);
        } else if (ticks == 392) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 395) {
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            require(base.getHeight() == 4, "The slider's left arrow changes the server value by one");
            require(base.background().ordinal() == 2, "Clicking the combined surface choice cycles the background");
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                player.level().getEntitiesOfClass(HoloGuiEntity.class, player.getBoundingBox().inflate(5)).forEach(HoloGuiEntity::discard);
            });
            aim(mc, new Vec3(3.3, 203.7, .5));
        } else if (ticks == 410) {
            clickDisplay(mc);
        } else if (ticks == 425) {
            require(gui(mc).getTag().equals(ModuleSettings.installedTag(installedId, false)), "The edited module can be configured again");
            aimGui(mc, gui(mc), 7.3, 5.3);
        } else if (ticks == 430) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 445) {
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            require(base.findModule(installedId) == null && base.modules().size() == 2, "A growing module overlapping the subtitle is ejected");
            long recovered = mc.player.getInventory().items.stream().filter(stack -> stack.is(Registration.DISPLAY_TEXT.get())
                    && DisplayModuleItem.config(stack).getString("Text").equals("STATUS")
                    && DisplayModuleItem.config(stack).getInt("Scale") == 3).count();
            require(recovered == 1, "The recovered item contains all edited installed settings exactly once");
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var level = player.serverLevel();
                BlockPos elevator = new BlockPos(5, 200, -1);
                level.setBlockAndUpdate(elevator, Registration.ELEVATOR.get().defaultBlockState());
                Ariente.guiHandler.openHoloGuiEntity(level, elevator, player, "default", 1);
            });
        } else if (ticks == 460) {
            capture = "elevator-slider-controls";
        } else if (ticks == 465) {
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var base = (DisplayBaseTile) player.level().getBlockEntity(new BlockPos(3, 200, 0));
                var power = base.modules().stream().filter(module -> module.stack().is(Registration.DISPLAY_POWER.get())).findFirst().orElseThrow();
                player.level().getEntitiesOfClass(HoloGuiEntity.class, player.getBoundingBox().inflate(5)).forEach(HoloGuiEntity::discard);
                Ariente.guiHandler.openHoloGuiEntity(player.level(), base.getBlockPos(), player, ModuleSettings.installedTag(power.id(), false), 1);
            });
        } else if (ticks == 480) {
            capture = "power-module-controls";
            aimGui(mc, gui(mc), 2.5, 1.4);
        } else if (ticks == 485) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 495) {
            var base = (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(3, 200, 0));
            var power = base.modules().stream().filter(module -> module.stack().is(Registration.DISPLAY_POWER.get())).findFirst().orElseThrow();
            require(DisplayModuleItem.config(power.stack()).getInt("Mode") == 0 && power.size().width() == 64,
                    "An installed power module can cycle its display style and shrink in place");
        } else if (ticks == 505) {
            mc.getSingleplayerServer().execute(() -> {
                var level = mc.getSingleplayerServer().overworld();
                level.getEntitiesOfClass(HoloGuiEntity.class, new AABB(-10, 190, -10, 10, 210, 10)).forEach(HoloGuiEntity::discard);
                for (int x = 0; x < 4; x++) {
                    BlockPos pos = new BlockPos(x, 200, -1);
                    level.setBlockAndUpdate(pos, Registration.DISPLAY_BASE.get().defaultBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
                    DisplayBaseTile base = (DisplayBaseTile) level.getBlockEntity(pos);
                    CompoundTag data = new CompoundTag();
                    data.putInt("Height", 2);
                    data.putInt("Background", 1);
                    ListTag modules = new ListTag();
                    if (x >= 2) {
                        ItemStack text = new ItemStack(Registration.DISPLAY_TEXT.get());
                        DisplayModuleItem.config(text).putString("Text", x == 3 ? "DRAG" : "OBSTACLE");
                        modules.add(module(text, 4, 32));
                    }
                    data.put("Modules", modules);
                    base.load(data);
                    base.changed();
                }
            });
            give(mc, "HELD");
        } else if (ticks == 520) {
            draggedId = dragBase(mc, 3).modules().get(0).id();
            aimDisplay(mc, 3, 16, 40);
        } else if (ticks == 525) {
            pressModule(mc);
        } else if (ticks == 535) {
            requireNoGui(mc);
            require(dragBase(mc, 3).findModule(draggedId).y() == 32, "Holding must leave the committed module in place");
            capture = "display-drag-selection";
        } else if (ticks == 540) {
            aimDisplay(mc, 3, 80, 88);
        } else if (ticks == 545) {
            useEvent(mc, InteractionHand.MAIN_HAND);
            useEvent(mc, InteractionHand.OFF_HAND);
            requireNoGui(mc);
        } else if (ticks == 550) {
            capture = "display-drag-valid";
            require(dragBase(mc, 3).findModule(draggedId) != null, "The drag preview must not move the saved module");
        } else if (ticks == 555) {
            mc.options.keyUse.setDown(false);
        } else if (ticks == 570) {
            require(dragBase(mc, 3).findModule(draggedId) == null, "Dragging across a seam changes the owning base");
            requireDragPosition(mc, 4, 80);
            require(dragBase(mc, 2).modules().size() == 2, "Repeated right use must not install the held module while dragging");
            require(DisplayModuleItem.config(mc.player.getMainHandItem()).getString("Text").equals("HELD"), "Dragging preserves the held item");
        } else if (ticks == 575 || ticks == 625 || ticks == 675) {
            aimDisplay(mc, 2, 16, 88);
        } else if (ticks == 580 || ticks == 630 || ticks == 680 || ticks == 720) {
            pressModule(mc);
        } else if (ticks == 590) {
            aimDisplay(mc, 2, 16, 40);
        } else if (ticks == 600) {
            capture = "display-drag-overlap";
        } else if (ticks == 605 || ticks == 655 || ticks == 700 || ticks == 730) {
            mc.options.keyUse.setDown(false);
        } else if (ticks == 620 || ticks == 670 || ticks == 715) {
            requireDragPosition(mc, 4, 80);
            require(mc.player.getInventory().items.stream().noneMatch(stack -> stack.is(Registration.DISPLAY_TEXT.get())
                    && DisplayModuleItem.config(stack).getString("Text").equals("DRAG")), "Rejected drags must not duplicate or eject a module");
        } else if (ticks == 640) {
            aimDisplay(mc, 2, 16, -8);
        } else if (ticks == 650) {
            capture = "display-drag-outside";
        } else if (ticks == 690) {
            aimDisplay(mc, 2, 24, 104);
        } else if (ticks == 695) {
            aimDisplay(mc, 2, 16, 88);
        } else if (ticks == 725) {
            requireNoGui(mc);
        } else if (ticks == 745) {
            require(gui(mc).getTag().equals(ModuleSettings.installedTag(draggedId, false)), "Releasing without dragging opens the moved module's settings");
            require(!DisplayModuleDrag.active(), "The gesture must end after release");
        } else if (ticks == 750) {
            mc.getSingleplayerServer().execute(() -> {
                var level = mc.getSingleplayerServer().overworld();
                for (int x = 0; x < 4; x++) {
                    level.setBlockAndUpdate(new BlockPos(x, 200, 0), Blocks.AIR.defaultBlockState());
                }
                level.getEntitiesOfClass(ItemEntity.class, new AABB(-10, 190, -10, 10, 210, 10)).forEach(ItemEntity::discard);
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.containerMenu.broadcastChanges();
            });
            openSurfaceGui(mc);
        } else if (ticks == 765) {
            require(gui(mc).findComponent("surface_transparency").isPresent(), "Transparent mode exposes the percentage slider");
            require(dragBase(mc, 3).appearance().color() == DisplayAppearance.DEFAULT_COLOR, "The initial surface color is the original pale gray");
            capture = "display-surface-controls";
            aimGui(mc, gui(mc), 5.3, 4.55);
        } else if (ticks == 770 || ticks == 785 || ticks == 805 || ticks == 875 || ticks == 890 || ticks == 905
                || ticks == 920 || ticks == 965 || ticks == 1010 || ticks == 1025) {
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 780) {
            requireSurface(mc, DisplayBackground.TRANSPARENT, DisplayAppearance.DEFAULT_TRANSPARENCY, false);
            aimGui(mc, gui(mc), 1, 4.55);
        } else if (ticks == 800) {
            require(gui(mc).getTag().equals("surface-color"), "Surface color opens the holographic picker");
            capture = "display-surface-color-picker";
            aimGui(mc, gui(mc), 4, 3);
        } else if (ticks == 820) {
            require(gui(mc).getTag().equals("default"), "Selecting a surface color returns to settings");
            surfaceColor = dragBase(mc, 3).appearance().color();
            require(surfaceColor != DisplayAppearance.DEFAULT_COLOR, "The surface color selection reaches the client");
            requireSurface(mc, DisplayBackground.TRANSPARENT, DisplayAppearance.DEFAULT_TRANSPARENCY, false);
            aimGui(mc, gui(mc), 6.6, 5.8);
        } else if (ticks == 825) {
            mc.options.keyAttack.setDown(true);
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 830 || ticks == 1045) {
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 840) {
            requireSurface(mc, DisplayBackground.TRANSPARENT, 100, false);
            require(dragBase(mc, 3).appearance().alpha() == 0 && dragBase(mc, 3).appearance().scanLineColor() >>> 24 == 0,
                    "100% transparency removes scan lines as well as fill");
            closeSurfaceGui(mc);
            aimDisplay(mc, 2, 32, 64);
        } else if (ticks == 850) {
            capture = "display-clear-no-border";
        } else if (ticks == 855 || ticks == 945 || ticks == 990) {
            openSurfaceGui(mc);
        } else if (ticks == 870 || ticks == 960 || ticks == 1005) {
            aimGui(mc, gui(mc), 2.5, 3.4);
        } else if (ticks == 885) {
            requireSurface(mc, DisplayBackground.SOLID, 100, false);
            require(gui(mc).findComponent("surface_transparency").isEmpty(), "The slider disappears in solid mode");
            require(dragBase(mc, 3).appearance().alpha() == 255, "Solid mode ignores the remembered transparency");
            capture = "display-surface-solid-controls";
            aimGui(mc, gui(mc), 5.3, 4.55);
        } else if (ticks == 900) {
            requireSurface(mc, DisplayBackground.SOLID, 100, true);
            aimGui(mc, gui(mc), 1, 4.55);
        } else if (ticks == 915) {
            aimGui(mc, gui(mc), 7.5, .2);
        } else if (ticks == 930) {
            require(gui(mc).getTag().equals("default") && dragBase(mc, 3).appearance().color() == surfaceColor,
                    "The surface picker close button returns without changing the color");
            closeSurfaceGui(mc);
            aimDisplay(mc, 2, 32, 64);
        } else if (ticks == 940) {
            capture = "display-solid-color-border";
        } else if (ticks == 975) {
            requireSurface(mc, DisplayBackground.INVISIBLE, 100, true);
            require(gui(mc).findComponent("surface_transparency").isEmpty(), "The slider also disappears in invisible mode");
            closeSurfaceGui(mc);
            aimDisplay(mc, 2, 32, 64);
        } else if (ticks == 985) {
            capture = "display-invisible-border";
        } else if (ticks == 1020) {
            requireSurface(mc, DisplayBackground.TRANSPARENT, 100, true);
            require(gui(mc).findComponent("surface_transparency").isPresent(), "Returning to transparent mode restores the slider");
            aimGui(mc, gui(mc), .5, 5.8);
        } else if (ticks == 1035) {
            requireSurface(mc, DisplayBackground.TRANSPARENT, 99, true);
            aimGui(mc, gui(mc), 3.9, 5.8);
        } else if (ticks == 1040) {
            mc.options.keyAttack.setDown(true);
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 1055) {
            requireSurface(mc, DisplayBackground.TRANSPARENT, 50, true);
            closeSurfaceGui(mc);
            aimDisplay(mc, 2, 32, 64);
        } else if (ticks == 1065) {
            capture = "display-colored-transparent";
        } else if (ticks == 1075) {
            try {
                Files.writeString(mc.gameDirectory.toPath().resolve("display-check.txt"), "PASS: editing, live readings, color selection, placement, removal, installed editing/color, slider drag/precision, resize ejection, elevator controls, right-drag across seams, overlap/edge snapback, held item preservation, release-only configuration, separate surface type/color/transparency/border controls and picker return\n");
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
            Ariente.setup.getLogger().info("HOLOGRAPHIC DISPLAY CLIENT CHECK PASSED");
            mc.stop();
        }
    }

    private static void closeSurfaceGui(Minecraft mc) {
        mc.getSingleplayerServer().execute(() -> {
            var level = mc.getSingleplayerServer().overworld();
            level.getEntitiesOfClass(HoloGuiEntity.class, new AABB(-10, 190, -10, 10, 210, 10)).forEach(HoloGuiEntity::discard);
        });
    }

    private static void openSurfaceGui(Minecraft mc) {
        closeSurfaceGui(mc);
        mc.getSingleplayerServer().execute(() -> {
            var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            Ariente.guiHandler.openHoloGuiEntity(player.level(), new BlockPos(3, 200, -1), player, "default", 1);
        });
    }

    private static void requireSurface(Minecraft mc, DisplayBackground type, int transparency, boolean border) {
        DisplayAppearance appearance = dragBase(mc, 3).appearance();
        require(appearance.type() == type && appearance.transparency() == transparency && appearance.border() == border,
                "Expected surface " + type + ", " + transparency + "%, border=" + border + "; got " + appearance);
        require(surfaceColor == 0 || appearance.color() == surfaceColor, "Mode changes preserve the selected color");
        for (int x = 0; x < 4; x++) {
            require(dragBase(mc, x).appearance().equals(appearance), "All joined bases must receive every surface setting");
        }
    }

    private static void clickDisplay(Minecraft mc) {
        DisplayGeometry.Hit hit = DisplayClientEvents.pick(mc.player);
        require(hit != null, "The module surface must be in reach");
        if (DisplayGeometry.moduleAt(hit) != null && !mc.player.isShiftKeyDown()) {
            pressModule(mc);
            mc.options.keyUse.setDown(false);
        } else {
            ArienteMessages.INSTANCE.sendToServer(new PacketDisplayAction(hit.tile().getBlockPos(), InteractionHand.MAIN_HAND, false));
        }
    }

    private static void pressModule(Minecraft mc) {
        mc.options.keyUse.setDown(true);
        useEvent(mc, InteractionHand.MAIN_HAND);
        require(DisplayModuleDrag.active(), "Pressing right click on a module must start a local gesture");
    }

    private static void useEvent(Minecraft mc, InteractionHand hand) {
        InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(1, mc.options.keyUse, hand);
        MinecraftForge.EVENT_BUS.post(event);
        require(event.isCanceled() && !event.shouldSwingHand(), "Display input must consume right click for either hand");
    }

    private static DisplayBaseTile dragBase(Minecraft mc, int x) {
        return (DisplayBaseTile) mc.level.getBlockEntity(new BlockPos(x, 200, -1));
    }

    private static void aimDisplay(Minecraft mc, int owner, int x, int y) {
        DisplayBaseTile base = dragBase(mc, owner);
        Vec3 right = Vec3.atLowerCornerOf(DisplayGeometry.facing(base).getCounterClockWise().getNormal());
        Vec3 target = Vec3.atCenterOf(base.getBlockPos()).add(right.scale((x + .5) / DisplayGeometry.PIXELS - .5));
        aim(mc, new Vec3(target.x, base.getBlockPos().getY() + base.getHeight() - (y + .5) / DisplayGeometry.PIXELS, target.z));
    }

    private static void requireNoGui(Minecraft mc) {
        require(mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(8)).isEmpty(),
                "Holding or dragging must not open module configuration");
    }

    private static void requireDragPosition(Minecraft mc, int x, int y) {
        var module = dragBase(mc, 2).findModule(draggedId);
        require(module != null && module.x() == x && module.y() == y,
                "Expected dragged module at " + x + "," + y + "; got " + (module == null ? "missing" : module.x() + "," + module.y()));
        requireNoGui(mc);
    }

    private static CompoundTag module(ItemStack stack, int x, int y) {
        CompoundTag tag = new CompoundTag();
        tag.put("Item", stack.save(new CompoundTag()));
        tag.putInt("X", x);
        tag.putInt("Y", y);
        return tag;
    }

    private static void give(Minecraft mc, String text) {
        mc.getSingleplayerServer().execute(() -> {
            var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            ItemStack stack = new ItemStack(Registration.DISPLAY_TEXT.get());
            stack.getOrCreateTag().putUUID("ModuleId", UUID.randomUUID());
            DisplayModuleItem.config(stack).putString("Text", text);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            player.containerMenu.broadcastChanges();
        });
    }

    private static HoloGuiEntity gui(Minecraft mc) {
        return mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(5)).stream().findFirst().orElseThrow();
    }

    private static void aimGui(Minecraft mc, HoloGuiEntity holo, double x, double y) {
        Vec3 right = holo.getLookAngle().cross(new Vec3(0, 1, 0));
        Vec3 target = holo.position().add(right.scale((x + .8) / 10 - .5)).add(0, 1 - (y + .8) / 10, 0);
        aim(mc, target);
    }

    private static void aim(Minecraft mc, Vec3 target) {
        Vec3 direction = target.subtract(mc.player.getEyePosition());
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance())));
        mc.player.yRotO = mc.player.getYRot();
        mc.player.xRotO = mc.player.getXRot();
        mc.player.setYHeadRot(mc.player.getYRot());
        mc.player.yHeadRotO = mc.player.getYRot();
    }

    @SubscribeEvent
    public static void screenshot(RenderGuiEvent.Post event) {
        if (capture == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        try (var screenshot = Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget())) {
            screenshot.writeToFile(mc.gameDirectory.toPath().resolve(capture + ".png"));
        } catch (Exception error) {
            throw new IllegalStateException(error);
        }
        capture = null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
