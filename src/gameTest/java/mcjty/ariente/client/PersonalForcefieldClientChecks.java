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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
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
    private static boolean frontFlash;
    private static int thirdHitAt = -1;
    private static int frontHitAt = -1;
    private static int thirdFadeAt = -1;
    private static int frontFadeAt = -1;
    private static String capture;
    private static int[] baseline;
    private static Skeleton skeleton;
    private static double fadeStart;
    private static double fadeMiddle;

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
                level.getEntitiesOfClass(Skeleton.class, new AABB(-10, 200, -10, 10, 208, 10)).forEach(Skeleton::discard);
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
                if (Boolean.getBoolean("ariente.forcefieldArrowCheck")) {
                    skeleton = EntityType.SKELETON.create(level);
                    skeleton.setNoAi(true);
                    skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                    skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                    skeleton.setPos(.5, 201, -2);
                    level.addFreshEntity(skeleton);
                }
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
        } else if (ticks == 80 || ticks == 200 || ticks == 320) {
            server.execute(() -> {
                var player = server.getPlayerList().getPlayer(uuid);
                player.invulnerableTime = 0;
                if (skeleton != null) {
                    skeleton.performRangedAttack(player, 1);
                } else {
                    LaserEntity laser = LaserEntity.create(player.level(), player, 1, 0, 0);
                    player.hurt(player.damageSources().indirectMagic(laser, player), 24);
                    require(player.getHealth() == 20, "The server must block a fresh laser completely");
                }
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
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        } else if (ticks == 295) {
            capture = "third-person-front-ready";
        } else if (ticks == 380) {
            require(frontFlash && !PersonalForcefield.isCoolingDown(mc.player), "Front-facing third-person hits must also recover");
            Ariente.setup.getLogger().info("PERSONAL FORCEFIELD CLIENT CHECK PASSED: first person, front/back third person, {} block, visible cooldown tint and expiry",
                    skeleton == null ? "laser" : "skeleton arrow");
            mc.stop();
        }
        if (ticks > 80 && ticks < 120 && !firstFlash && PersonalForcefield.isCoolingDown(mc.player)) {
            firstFlash = true;
            Ariente.setup.getLogger().info("PERSONAL FIELD FIRST HIT: clientTime={}, cooldownUntil={}, hitTime={}",
                    mc.level.getGameTime(), PersonalForcefield.getCooldownUntil(mc.player), PersonalForcefield.getLastHit(mc.player));
            capture = "first-person-hit";
        } else if (ticks > 200 && ticks < 240 && !thirdFlash && PersonalForcefield.isCoolingDown(mc.player)) {
            thirdFlash = true;
            thirdHitAt = ticks;
            thirdFadeAt = ticks + (int) (PersonalForcefield.getCooldownUntil(mc.player) - mc.level.getGameTime()) - 3;
            Ariente.setup.getLogger().info("PERSONAL FIELD THIRD HIT: clientTime={}, cooldownUntil={}, hitTime={}",
                    mc.level.getGameTime(), PersonalForcefield.getCooldownUntil(mc.player), PersonalForcefield.getLastHit(mc.player));
            capture = "third-person-hit";
        } else if (ticks > 320 && ticks < 360 && !frontFlash && PersonalForcefield.isCoolingDown(mc.player)) {
            frontFlash = true;
            frontHitAt = ticks;
            frontFadeAt = ticks + (int) (PersonalForcefield.getCooldownUntil(mc.player) - mc.level.getGameTime()) - 3;
            capture = "third-person-front-hit";
        }
        if (thirdHitAt >= 0 && ticks == thirdHitAt + 6) {
            capture = "third-person-midcooldown";
        } else if (thirdFadeAt >= 0 && ticks == thirdFadeAt) {
            capture = "third-person-fade-start";
        } else if (thirdFadeAt >= 0 && ticks == thirdFadeAt + 1) {
            capture = "third-person-fade-middle";
        } else if (thirdFadeAt >= 0 && ticks == thirdFadeAt + 2) {
            capture = "third-person-fade-end";
        } else if (frontHitAt >= 0 && ticks == frontHitAt + 6) {
            capture = "third-person-front-midcooldown";
        } else if (frontFadeAt >= 0 && ticks == frontFadeAt) {
            capture = "third-person-front-fade-start";
        } else if (frontFadeAt >= 0 && ticks == frontFadeAt + 1) {
            capture = "third-person-front-fade-middle";
        } else if (frontFadeAt >= 0 && ticks == frontFadeAt + 2) {
            capture = "third-person-front-fade-end";
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
            if (capture.startsWith("third-person") && !capture.endsWith("ready")) {
                int redPixels = 0;
                int samples = 0;
                double redExcess = 0;
                for (int y = 180; y < screenshot.getHeight() - 140; y++) {
                    for (int x = screenshot.getWidth() / 2 - 220; x < screenshot.getWidth() / 2 + 220; x++) {
                        if (Math.abs(x - screenshot.getWidth() / 2) < 100) {
                            continue;
                        }
                        int pixel = screenshot.getPixelRGBA(x, y);
                        redExcess += Math.max(0, ABGR32.red(pixel) - ABGR32.green(pixel));
                        samples++;
                        if (ABGR32.red(pixel) > 80 && ABGR32.red(pixel) > ABGR32.green(pixel) * 1.5f) {
                            redPixels++;
                        }
                    }
                }
                double tint = redExcess / samples;
                if (capture.endsWith("fade-start")) {
                    fadeStart = tint;
                } else if (capture.endsWith("fade-middle")) {
                    fadeMiddle = tint;
                    require(tint > 0 && tint < fadeStart, "The red tint must start fading before cooldown ends: " + fadeStart + " -> " + tint);
                } else if (capture.endsWith("fade-end")) {
                    require(tint < fadeMiddle, "The red tint must continue fading before cooldown ends: " + fadeMiddle + " -> " + tint);
                } else {
                    require(redPixels > 200, "The third-person shield surface must stay visibly red: " + capture + "; red pixels=" + redPixels);
                    if (capture.endsWith("midcooldown")) {
                        require((double) redPixels / samples > 0.1, "The cooldown tint must remain prominent: " + capture + "; red fraction=" + (double) redPixels / samples);
                    }
                }
                Ariente.setup.getLogger().info("PERSONAL FIELD CAPTURE {}: redFraction={}, tint={}", capture, (double) redPixels / samples, tint);
            }
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
