package mcjty.ariente.teleport.client;

import com.mojang.blaze3d.platform.NativeImage;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.items.TeleportationCardItem;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.teleport.TeleportationCardGui;
import mcjty.ariente.teleport.WarperPreviews;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.hologui.gui.TextEntryScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

/** Real client checks for the Warper text editor and network-delivered card miniature. */
public class WarperGuiClientChecks implements ClientModInitializer {

    private static final BlockPos WARPER = new BlockPos(2, 99, 3);
    private static final String NAME = "Fabric Warper";
    private static CompletableFuture<?> work;
    private static int holoId;
    private static int phase;
    private static int ticks;
    private static int waiting;
    private static int destination;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(WarperGuiClientChecks::tick);
    }

    private static void tick(Minecraft mc) {
        mc.options.pauseOnLostFocus = false;
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) {
            if (++waiting % 40 == 0 && mc.screen != null) {
                System.out.println("WARPER GUI waiting: " + mc.screen.getClass().getName());
                screenshot(mc, "waiting-screen.png");
                for (var child : mc.screen.children()) {
                    if (child instanceof AbstractButton button && (button.getMessage().getString().startsWith("Proceed")
                            || button.getMessage().getString().equals("Continue")
                            || button.getMessage().getString().startsWith("I know what")
                            || mc.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen") && button.getMessage().getString().equals("Done"))) {
                        button.onPress();
                        break;
                    }
                }
            }
            require(waiting < 1800, "Disposable test world did not start");
            return;
        }
        if (work != null) {
            if (!work.isDone()) {
                return;
            }
            work.join();
            work = null;
        }
        require(++ticks < 600, "Warper GUI client check timed out in phase " + phase);
        switch (phase) {
            case 0 -> {
                work = mc.getSingleplayerServer().submit(() -> setUp(mc));
                next(1);
            }
            case 1 -> {
                if (ticks < 40 || !(mc.level.getEntity(holoId) instanceof HoloGuiEntity holo) || holo.getGui(mc.player) == null) {
                    return;
                }
                var field = holo.getGui(mc.player).findChild("warper_name").orElseThrow();
                field.hitClient(mc.player, holo, 2, 1.5);
                require(mc.screen instanceof TextEntryScreen, "Clicking the Warper name must open the text editor");
                for (char character : NAME.toCharArray()) {
                    mc.screen.charTyped(character, 0);
                }
                require(editor(mc).getValue().equals(NAME), "Keyboard input must reach the focused editor");
                next(2);
            }
            case 2 -> {
                if (ticks < 20) {
                    return;
                }
                screenshot(mc, "warper-name-editor.png");
                mc.screen.keyPressed(256, 0, 0);
                require(mc.screen == null, "Escape must close the editor");
                next(3);
            }
            case 3 -> {
                if (ticks < 20) {
                    return;
                }
                work = mc.getSingleplayerServer().submit(() -> {
                    var player = serverPlayer(mc);
                    var warper = (WarperTile) player.level().getBlockEntity(WARPER);
                    require(warper.getWarperName().equals(NAME), "Escape must commit the name over the network");
                    ((HoloGuiEntity) player.level().getEntity(holoId)).discard();
                    Ariente.guiHandler.openHoloGui(player, TeleportationCardGui.guiId(InteractionHand.MAIN_HAND), 1);
                    select(mc, 0);
                });
                next(4);
            }
            case 4 -> {
                var stack = mc.player.getMainHandItem();
                if (TeleportationCardItem.selectedIndex(stack) != destination && ticks < 80) {
                    return;
                }
                require(TeleportationCardItem.selectedIndex(stack) == destination, "The selected destination must synchronize");
                var scene = WarperPreviewCache.get(TeleportationCardItem.id(stack), TeleportationCardItem.selected(stack));
                if (scene == null && ticks < 80) {
                    return;
                }
                require(scene != null && !scene.contains("Status") && scene.getLongArray("Blocks").length > 0,
                        "Selected destination must arrive through the production preview receiver");
                if (ticks < 30) {
                    return;
                }
                screenshot(mc, "warper-card-destination-" + destination + ".png");
                if (destination == 0) {
                    destination = 1;
                    work = mc.getSingleplayerServer().submit(() -> select(mc, 1));
                    ticks = 0;
                } else {
                    try {
                        Files.writeString(mc.gameDirectory.toPath().resolve("warper-gui-results.txt"),
                                "PASSED: focused text input, rendered editor, Escape network commit, two network-delivered destination previews and rendered card GUI\n");
                    } catch (IOException error) {
                        throw new UncheckedIOException(error);
                    }
                    System.out.println("WARPER GUI CLIENT CHECKS PASSED");
                    next(5);
                    mc.stop();
                }
            }
        }
    }

    private static MultiLineEditBox editor(Minecraft mc) {
        return mc.screen.children().stream().filter(MultiLineEditBox.class::isInstance)
                .map(MultiLineEditBox.class::cast).findFirst().orElseThrow();
    }

    private static ServerPlayer serverPlayer(Minecraft mc) {
        return mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    }

    private static void setUp(Minecraft mc) {
        var player = serverPlayer(mc);
        var level = player.serverLevel();
        player.setGameMode(GameType.CREATIVE);
        player.teleportTo(level, .5, 100, .5, 0, 0);
        for (int x = -18; x <= 30; x++) {
            for (int z = -18; z <= 22; z++) {
                level.setBlock(new BlockPos(x, 99, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            }
        }
        var card = new ItemStack(Registration.TELEPORTATION_CARD.get());
        for (int i = 0; i < 2; i++) {
            BlockPos pos = WARPER.offset(i * 12, 0, 0);
            level.setBlock(pos, Registration.WARPER.get().defaultBlockState(), 3);
            var warper = (WarperTile) level.getBlockEntity(pos);
            warper.setWarperName(i == 0 ? "" : "Second destination");
            level.setBlock(pos.north(2).above(), (i == 0 ? Blocks.GOLD_BLOCK : Blocks.DIAMOND_BLOCK).defaultBlockState(), 3);
            TeleportationCardItem.encode(card, warper);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, card);
        TeleportationCardItem.sync(player);
        var holo = (HoloGuiEntity) Ariente.guiHandler.openHoloGuiEntity(level, WARPER, player, "default", 1);
        holoId = holo.getId();
        holo.getGui(player).findChild("warper_name").orElseThrow().hit(player, holo, 2, 1.5);
    }

    private static void select(Minecraft mc, int index) {
        var player = serverPlayer(mc);
        WarperPreviews.select(player, player.getMainHandItem(), index);
    }

    private static void screenshot(Minecraft mc, String file) {
        try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(mc.gameDirectory.toPath().resolve(file));
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private static void next(int value) {
        phase = value;
        ticks = 0;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
