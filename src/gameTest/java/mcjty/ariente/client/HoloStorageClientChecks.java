package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.patchouli.client.book.gui.GuiBookEntry;

import java.nio.file.Files;
import java.util.UUID;

/** Real input gestures, hover scrolling and rendering, in an opt-in isolated client world. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class HoloStorageClientChecks {

    private static final BlockPos POS = new BlockPos(0, 200, 0);
    private static boolean started;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static int ticks;
    private static String capture;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.holoStorageClientCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.getOverlay() != null) {
            return;
        }
        require(failure == null, "Server check failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.options.hideGui = false;
            mc.setScreen(null);
            mc.mouseHandler.releaseMouse();
            UUID id = mc.player.getUUID();
            server(mc, () -> {
                var server = mc.getSingleplayerServer();
                var level = server.overworld();
                var player = server.getPlayerList().getPlayer(id);
                level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                level.setDayTime(6000);
                for (BlockPos pos : BlockPos.betweenClosed(-4, 199, -5, 4, 204, 3)) {
                    level.setBlock(pos, (pos.getY() == 199 ? Blocks.GRAY_CONCRETE : Blocks.AIR).defaultBlockState(), 2);
                }
                level.getEntitiesOfClass(HoloGuiEntity.class, new AABB(-5, 198, -5, 5, 207, 5)).forEach(HoloGuiEntity::discard);
                level.setBlockAndUpdate(POS, Registration.HOLO_STORAGE_3.get().defaultBlockState());
                StorageCubeTile tile = (StorageCubeTile) level.getBlockEntity(POS);
                tile.getItems().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 64));
                tile.getItems().setStackInSlot(1, new ItemStack(Items.OAK_LOG, 32));
                tile.getItems().setStackInSlot(10, new ItemStack(Items.DIAMOND, 12));
                tile.getItems().setStackInSlot(20, new ItemStack(Items.COBBLESTONE, 64));
                tile.getItems().setStackInSlot(35, new ItemStack(Items.GOLD_INGOT, 16));
                tile.getItems().setStackInSlot(60, new ItemStack(Items.GLASS, 20));
                tile.getItems().setStackInSlot(90, new ItemStack(Items.ENDER_PEARL, 8));
                tile.getItems().setStackInSlot(119, new ItemStack(Items.NETHER_STAR));
                player.setGameMode(GameType.CREATIVE);
                player.getInventory().clearContent();
                player.getInventory().setItem(9, new ItemStack(Items.GOLD_INGOT, 32));
                player.getInventory().setItem(10, new ItemStack(Items.APPLE, 16));
                player.getInventory().setItem(18, new ItemStack(Items.REDSTONE, 64));
                player.getInventory().setItem(26, new ItemStack(Registration.CONSTRUCTOR.get(), 64));
                player.getInventory().setItem(27, new ItemStack(Items.DIAMOND_PICKAXE));
                player.getInventory().setItem(34, new ItemStack(Items.EMERALD, 32));
                player.getInventory().setItem(35, new ItemStack(Items.AMETHYST_SHARD, 48));
                player.getInventory().setItem(8, new ItemStack(Items.BREAD, 64));
                player.teleportTo(level, .5, 200, -2.5, 0, 15);
                player.containerMenu.broadcastChanges();
                ready = true;
            });
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 20) {
            aim(mc, Vec3.atCenterOf(POS));
        } else if (ticks == 40) {
            capture = "holo-storage-cube";
        } else if (ticks == 50) {
            server(mc, () -> Ariente.guiHandler.openHoloGui(mc.getSingleplayerServer().overworld(), POS,
                    mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));
        } else if (ticks == 70) {
            aimGui(mc, .8, 1.1);
        } else if (ticks == 90) {
            aimGui(mc, 4, 4);
        } else if (ticks == 100) {
            capture = "holo-storage-panel";
        } else if (ticks == 105) {
            mc.options.keyShift.setDown(true);
        } else if (ticks == 108) {
            aimGui(mc, .8, 1.1);
        } else if (ticks == 110) {
            press(mc);
        } else if (ticks == 112) {
            mc.options.keyAttack.setDown(false);
            mc.options.keyShift.setDown(false);
        } else if (ticks == 130) {
            server(mc, () -> {
                StorageCubeTile tile = serverTile(mc);
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                require(tile.getItems().getStackInSlot(0).isEmpty() && player.getInventory().getItem(0).getCount() == 64,
                        "Sneak-click must extract into player inventory");
            });
            aimGui(mc, .8, 4.85);
        } else if (ticks == 140) {
            press(mc);
        } else if (ticks == 150) {
            aimGui(mc, .8, 2.7);
        } else if (ticks == 160) {
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 185) {
            server(mc, () -> {
                require(serverTile(mc).getItems().getStackInSlot(2).getCount() == 32,
                        "Holding, aiming and releasing must drag from player inventory into the cube");
            });
            aimGui(mc, 7.9, 1.5);
        } else if (ticks == 470) {
            capture = "holo-storage-scrolled";
        } else if (ticks == 478) {
            mc.options.keyShift.setDown(true);
        } else if (ticks == 485) {
            aimGui(mc, 7.2, 2.7);
        } else if (ticks == 490) {
            press(mc);
        } else if (ticks == 492) {
            mc.options.keyAttack.setDown(false);
            mc.options.keyShift.setDown(false);
        } else if (ticks == 515) {
            server(mc, () -> require(serverTile(mc).getItems().getStackInSlot(119).isEmpty(),
                    "Hover scrolling without clicking must make the final storage slot accessible"));
        } else if (ticks == 525) {
            mc.options.keyShift.setDown(true);
        } else if (ticks == 528) {
            aimGui(mc, 7.2, 7.55);
        } else if (ticks == 530) {
            press(mc);
        } else if (ticks == 532) {
            mc.options.keyAttack.setDown(false);
            mc.options.keyShift.setDown(false);
        } else if (ticks == 550) {
            server(mc, () -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                require(player.getInventory().getItem(8).isEmpty(), "The separated hotbar must use its new hit coordinates");
            });
            aimGui(mc, 4, 4);
        } else if (ticks == 560) {
            HoloGuiEntity holo = gui(mc);
            InputEvent.InteractionKeyMappingTriggered input = new InputEvent.InteractionKeyMappingTriggered(1,
                    mc.options.keyUse, InteractionHand.MAIN_HAND);
            MinecraftForge.EVENT_BUS.post(input);
            require(!input.isCanceled(), "Right-click must reach HoloGui's normal close handler");
            mc.gameMode.interact(mc.player, holo, InteractionHand.MAIN_HAND);
        } else if (ticks == 575) {
            require(mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(5)).isEmpty(),
                    "Right-click must dismiss the hologram");
        } else if (ticks == 580) {
            server(mc, () -> Ariente.guiHandler.openHoloGui(mc.getSingleplayerServer().overworld(), POS,
                    mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));
        } else if (ticks == 600) {
            aimGui(mc, 8.65, -.3);
        } else if (ticks == 610) {
            InputEvent.InteractionKeyMappingTriggered input = new InputEvent.InteractionKeyMappingTriggered(0,
                    mc.options.keyAttack, InteractionHand.MAIN_HAND);
            MinecraftForge.EVENT_BUS.post(input);
            require(!input.isCanceled(), "The standard question-mark button must receive its normal click");
            mc.gameMode.attack(mc.player, gui(mc));
        } else if (ticks == 635) {
            require(mc.screen instanceof GuiBookEntry book
                    && book.getEntry().getId().equals(new ResourceLocation(Ariente.MODID, "construction/holo_storage")),
                    "The question mark must open the correct Patchouli entry");
            require(failure == null, "Client checks failed: " + failure);
            try {
                Files.writeString(mc.gameDirectory.toPath().resolve("holo-storage-check.txt"),
                        "PASS: rendering, drag, sneak transfer, scrolling, separated hotbar, right-click close, Patchouli help\n");
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
            mc.stop();
        }
    }

    private static StorageCubeTile serverTile(Minecraft mc) {
        return (StorageCubeTile) mc.getSingleplayerServer().overworld().getBlockEntity(POS);
    }

    private static void server(Minecraft mc, Runnable action) {
        mc.getSingleplayerServer().execute(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure = error;
            }
        });
    }

    private static void press(Minecraft mc) {
        mc.options.keyAttack.setDown(true);
        InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(0,
                mc.options.keyAttack, InteractionHand.MAIN_HAND);
        MinecraftForge.EVENT_BUS.post(event);
        require(event.isCanceled() && !event.shouldSwingHand(), "Storage panels must consume inventory clicks");
    }

    private static void aimGui(Minecraft mc, double x, double y) {
        HoloGuiEntity holo = gui(mc);
        Vec3 right = holo.getLookAngle().cross(new Vec3(0, 1, 0));
        aim(mc, holo.position().add(right.scale((x + .8) / 10 - .5)).add(0, 1 - (y + .8) / 10, 0));
    }

    private static HoloGuiEntity gui(Minecraft mc) {
        return mc.level.getEntitiesOfClass(HoloGuiEntity.class, mc.player.getBoundingBox().inflate(5))
                .stream().findFirst().orElseThrow();
    }

    private static void aim(Minecraft mc, Vec3 target) {
        Vec3 delta = target.subtract(mc.player.getEyePosition());
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
        mc.player.yRotO = mc.player.getYRot();
        mc.player.xRotO = mc.player.getXRot();
    }

    @SubscribeEvent
    public static void screenshot(RenderGuiEvent.Post event) {
        if (capture == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        try (var screenshot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
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
