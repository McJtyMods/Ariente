package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.ArmorUpgradeType;
import mcjty.ariente.entities.LaserEntity;
import mcjty.ariente.items.armor.PersonalForcefield;
import mcjty.ariente.items.modules.ModuleSupport;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Files;

/** Opt-in visual and packet checks. Run only with an isolated save: this builds a small arena. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class PersonalForcefieldClientChecks {

    private static boolean started;
    private static volatile boolean ready;
    private static int ticks;
    private static boolean firstFlash;
    private static boolean thirdFlash;
    private static String capture;
    private static int[] baseline;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.forcefieldClientCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null
                || mc.screen != null || mc.getOverlay() != null || mc.isPaused()) {
            return;
        }
        var server = mc.getSingleplayerServer();
        var uuid = mc.player.getUUID();
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.mouseHandler.releaseMouse();
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                var level = player.serverLevel();
                level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
                level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                level.setDayTime(6000);
                for (BlockPos pos : BlockPos.betweenClosed(-10, 200, -10, 10, 207, 10)) {
                    var block = pos.getY() == 200 ? Blocks.WHITE_CONCRETE : Blocks.AIR;
                    if (pos.getZ() == -5 && pos.getY() <= 205) {
                        block = (pos.getX() + pos.getY()) % 2 == 0 ? Blocks.WHITE_CONCRETE : Blocks.LIGHT_GRAY_CONCRETE;
                    }
                    level.setBlock(pos, block.defaultBlockState(), 2);
                }
                player.setGameMode(GameType.SURVIVAL);
                player.getInventory().clearContent();
                player.setHealth(20);
                ItemStack chest = new ItemStack(Registration.POWERSUIT_CHEST.get());
                chest.getOrCreateTag().putBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey(), false);
                chest.getOrCreateTag().putInt("negarite", 64);
                chest.getOrCreateTag().putInt("posirite", 64);
                player.setItemSlot(EquipmentSlot.CHEST, chest);
                player.teleportTo(level, .5, 201, .5, 180, 0);
                ready = true;
            });
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 20) {
            require(!PersonalForcefield.isActive(mc.player), "The baseline field must be disabled");
            capture = "first-person-disabled";
        } else if (ticks == 30) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                chest.getOrCreateTag().putBoolean(ArmorUpgradeType.FORCEFIELD.getModuleKey(), true);
                ModuleSupport.updateWorkingState(chest, player);
            });
        } else if (ticks == 60) {
            require(PersonalForcefield.isActive(mc.player), "Working state must reach the client");
            capture = "first-person-ready";
        } else if (ticks == 80 || ticks == 200) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                LaserEntity laser = LaserEntity.create(player.level(), player, 1, 0, 0);
                player.invulnerableTime = 0;
                player.hurt(player.damageSources().indirectMagic(laser, player), 24);
                require(player.getHealth() == 20, "The server must block a fresh laser completely");
            });
        } else if (ticks == 140) {
            require(firstFlash && !PersonalForcefield.isCoolingDown(mc.player), "The client must see the hit and cooldown expiry");
            capture = "first-person-recovered";
        } else if (ticks == 160) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        } else if (ticks == 185) {
            capture = "third-person-ready";
        } else if (ticks == 260) {
            require(thirdFlash && !PersonalForcefield.isCoolingDown(mc.player), "Third-person hit state must also recover");
            Ariente.setup.getLogger().info("PERSONAL FORCEFIELD CLIENT CHECK PASSED: first/third person, laser block, hit packet and cooldown expiry");
            mc.stop();
        }
        if (ticks > 80 && ticks < 120 && !firstFlash && PersonalForcefield.isCoolingDown(mc.player)) {
            firstFlash = true;
            capture = "first-person-hit";
        } else if (ticks > 200 && ticks < 240 && !thirdFlash && PersonalForcefield.isCoolingDown(mc.player)) {
            thirdFlash = true;
            capture = "third-person-hit";
        }
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        if (capture == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        var directory = mc.gameDirectory.toPath().resolve("screenshots/forcefield");
        try (var screenshot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            Files.createDirectories(directory);
            screenshot.writeToFile(directory.resolve(capture + ".png"));
            if (capture.equals("first-person-disabled")) {
                baseline = new int[screenshot.getWidth() * screenshot.getHeight()];
                for (int y = 0; y < screenshot.getHeight(); y++) {
                    for (int x = 0; x < screenshot.getWidth(); x++) {
                        baseline[y * screenshot.getWidth() + x] = screenshot.getPixelRGBA(x, y);
                    }
                }
            } else if (capture.equals("first-person-ready")) {
                double excess = 0;
                int samples = 0;
                for (int y = 160; y < screenshot.getHeight() - 200; y += 8) {
                    for (int x = 200; x < screenshot.getWidth() - 200; x += 8) {
                        int previous = baseline[y * screenshot.getWidth() + x];
                        int current = screenshot.getPixelRGBA(x, y);
                        excess += (ABGR32.green(current) - ABGR32.red(current))
                                - (ABGR32.green(previous) - ABGR32.red(previous));
                        samples++;
                    }
                }
                require(samples > 0 && excess / samples > 1, "The faint idle field must actually reach the framebuffer; green/red difference=" + excess / samples);
                baseline = null;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot capture the personal forcefield", e);
        }
        capture = null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Personal forcefield client check: " + message);
        }
    }
}
