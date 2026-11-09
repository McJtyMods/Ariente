package mcjty.ariente.client;

import com.mojang.blaze3d.platform.InputConstants;
import mcjty.ariente.Ariente;
import mcjty.ariente.api.MarbleColor;
import mcjty.ariente.blocks.decorative.DecorativeBlockModule;
import mcjty.ariente.entities.fluxship.FluxShipEnergy;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/** Place through the real right-click, client prediction, Forge hooks and server packet handler. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipPlacementClientChecks {

    private static final AABB AREA = new AABB(-20, 195, -20, 20, 210, 20);
    private static boolean started;
    private static volatile boolean ready;
    private static int ticks;
    private static int scenario;
    private static volatile Throwable failure;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.shipPlacementCheck") || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!started && mc.gui.screen() != null) {
            // Only this isolated opt-in test may confirm its own experimental-dimensions screen.
            for (var child : mc.gui.screen().children()) {
                if (child instanceof Button button && button.getMessage().getString().equals("Proceed")) {
                    button.onPress();
                    return;
                }
            }
        }
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.gui.overlay() != null) {
            return;
        }
        require(failure == null, "Server placement check failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.player.getInventory().selected = 0;
            mc.gui.setScreen(null);
            mc.mouseHandler.releaseMouse();
            prepare(mc);
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 10) {
            UUID playerId = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                // Saved dimension entities become visible only after teleporting loads their chunks.
                var level = mc.getSingleplayerServer().getPlayerList().getPlayer(playerId).serverLevel();
                level.getEntitiesOfClass(FluxShipEntity.class, AREA.inflate(100)).forEach(Entity::discard);
            });
        } else if (ticks == 25) {
            require(mc.player.getMainHandItem().is(Registration.FLUX_SHIP.get()), "The placement item must reach the client");
            String tooltip = mc.player.getMainHandItem().getTooltipLines(mc.player, TooltipFlag.Default.NORMAL)
                    .stream().map(component -> component.getString()).toList().toString();
            require(!tooltip.contains("Work in progress") && tooltip.contains("deploy"), "The release tooltip must describe deployment: " + tooltip);
            mc.player.setYRot(0);
            mc.player.setXRot(scenario == 2 ? 75 : 40);
            mc.gameRenderer.pick(1);
            require(mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK,
                    "Actual placement must target the ground: " + mc.hitResult);
            Ariente.setup.getLogger().info("SHIP PLACEMENT CLICK: scenario={}, hit={}, floor={}, player={}", scenario,
                    ((BlockHitResult) mc.hitResult).getBlockPos(), mc.level.getBlockState(((BlockHitResult) mc.hitResult).getBlockPos()), mc.player.position());
            KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(1));
        } else if (ticks == 40) {
            var ships = mc.level.getEntitiesOfClass(FluxShipEntity.class, AREA);
            require(ships.size() == 1 && (scenario == 3 ? mc.player.getMainHandItem().is(Registration.FLUX_SHIP.get()) : mc.player.getMainHandItem().isEmpty()),
                    "A real right-click must place one ship and consume one survival item: scenario=" + scenario
                            + ", ships=" + ships.size() + ", item=" + mc.player.getMainHandItem());
            require(ships.get(0).getCharge() == 4321 && ships.get(0).getY() >= 199.99,
                    "The ship must remain above ground and retain the item's battery: charge=" + ships.get(0).getCharge()
                            + ", position=" + ships.get(0).position() + ", pilot=" + ships.get(0).getControllingPassenger());
            try (var screenshot = Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget())) {
                screenshot.writeToFile(mc.gameDirectory.toPath().resolve("ship-placement-" + scenario + ".png"));
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
            if (++scenario == (Boolean.getBoolean("ariente.shipPlacementWorldCheck") ? 5 : 4)) {
                Ariente.setup.getLogger().info("FLUX SHIP PLACEMENT CLIENT CHECKS PASSED: real ground clicks on stone and Ariente marble, close placement, server spawning, item consumption, charge and release tooltip");
                mc.stop();
            } else {
                ready = false;
                ticks = 0;
                prepare(mc);
            }
        }
    }

    private static void prepare(Minecraft mc) {
        UUID playerId = mc.player.getUUID();
        mc.getSingleplayerServer().execute(() -> {
            try {
                var server = mc.getSingleplayerServer();
                var level = server.overworld();
                if (scenario == 4) {
                    for (var candidate : server.getAllLevels()) {
                        if (candidate.dimension().location().toString().equals("arienteworld:ariente")) {
                            level = candidate;
                            break;
                        }
                    }
                    require(level != server.overworld(), "The combined workspace must include the Ariente dimension");
                }
                level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                level.getEntitiesOfClass(FluxShipEntity.class, AREA.inflate(100)).forEach(Entity::discard);
                for (BlockPos pos : BlockPos.betweenClosed(-10, 200, -10, 10, 208, 10)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
                var floor = scenario == 0 ? Blocks.STONE.defaultBlockState()
                        : DecorativeBlockModule.MARBLE.get(MarbleColor.BLACK).get().defaultBlockState();
                for (BlockPos pos : BlockPos.betweenClosed(-10, 199, -10, 10, 199, 10)) {
                    level.setBlock(pos, floor, 2);
                }
                var player = server.getPlayerList().getPlayer(playerId);
                player.stopRiding();
                player.setGameMode(scenario == 3 ? GameType.CREATIVE : GameType.SURVIVAL);
                player.teleportTo(level, .5, 200, -2.5, 0, scenario == 2 ? 75 : 40);
                ItemStack item = new ItemStack(Registration.FLUX_SHIP.get());
                item.getOrCreateTag().putInt(FluxShipEnergy.CHARGE_TAG, 4321);
                player.getInventory().selected = 0;
                player.getInventory().setItem(0, item);
                ready = true;
            } catch (Throwable error) {
                failure = error;
            }
        });
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
