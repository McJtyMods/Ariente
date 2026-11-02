package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.gui.HoloGuiEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.nio.file.Files;

/** Exercise Minecraft's real attack key path, including held attacks and ordinary breaking. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class BlockInventoryClientChecks {

    private static final BlockPos POS = new BlockPos(0, 200, 0);
    private static boolean started;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static volatile IItemHandlerModifiable serverItems;
    private static int variant;
    private static int ticks;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.blockInventoryCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.getOverlay() != null) {
            return;
        }
        require(failure == null, "Server fixture failed: " + failure);
        if (!started) {
            started = true;
            ready = false;
            ticks = 0;
            mc.options.pauseOnLostFocus = false;
            mc.setScreen(null);
            mc.mouseHandler.releaseMouse();
            mc.options.keyAttack.setDown(false);
            var id = mc.player.getUUID();
            server(() -> {
                var level = mc.getSingleplayerServer().overworld();
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                level.getEntitiesOfClass(HoloGuiEntity.class, new AABB(-10, 190, -10, 10, 210, 10)).forEach(HoloGuiEntity::discard);
                player.setGameMode(GameType.CREATIVE);
                player.getInventory().clearContent();
                player.teleportTo(level, .5, 200, -2.5, 0, 0);
                level.removeBlock(POS, false);
                var state = block().defaultBlockState();
                if (variant == 0) {
                    state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
                } else if (variant < 3) {
                    state = state.setValue(BlockStateProperties.FACING, Direction.NORTH);
                }
                level.setBlockAndUpdate(POS, state);
                if (variant < 3) {
                    serverItems = (IItemHandlerModifiable) level.getBlockEntity(POS).getCapability(ForgeCapabilities.ITEM_HANDLER)
                            .orElseThrow(IllegalStateException::new);
                    inventory().setStackInSlot(0, new ItemStack(Items.RAW_IRON, 5));
                    if (variant == 0) {
                        inventory().setStackInSlot(1, new ItemStack(Items.IRON_INGOT, 3));
                    }
                }
                player.inventoryMenu.broadcastChanges();
                ready = true;
            });
            return;
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 15) {
            aim(mc, variant == 3 ? .5 : .77, variant == 0 ? .5 : .8);
        } else if (ticks == 25) {
            press(mc);
        } else if (ticks == 45) {
            if (variant == 3) {
                require(mc.level.isEmptyBlock(POS) && mc.getSingleplayerServer().overworld().isEmptyBlock(POS),
                        "Ordinary blocks still break in creative mode");
                mc.options.keyAttack.setDown(false);
                Files.writeString(mc.gameDirectory.toPath().resolve("block-inventory-check.txt"),
                        "PASS: real creative attack keys extract once, preserve populated/empty slots and held off-slot gestures, smelter output works, casing and ordinary blocks still break\n");
                mc.stop();
                return;
            }
            intact(mc);
            require(inventory().getStackInSlot(0).getCount() == 4, "The initial creative click extracts exactly one item");
            aim(mc, .5, .05);
        } else if (ticks == 65) {
            intact(mc);
            require(inventory().getStackInSlot(0).getCount() == 4, "Holding and drifting onto the casing never repeats or breaks");
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 70) {
            aim(mc, variant == 0 ? .23 : .77, variant == 0 ? .5 : .8);
        } else if (ticks == 80) {
            press(mc);
        } else if (ticks == 100) {
            intact(mc);
            require(inventory().getStackInSlot(variant == 0 ? 1 : 0).getCount() == (variant == 0 ? 2 : 3),
                    "A second press extracts from the smelter output or storage slot");
            mc.options.keyAttack.setDown(false);
            server(() -> inventory().setStackInSlot(0, ItemStack.EMPTY));
        } else if (ticks == 115) {
            aim(mc, .77, variant == 0 ? .5 : .8);
        } else if (ticks == 125) {
            press(mc);
        } else if (ticks == 145) {
            intact(mc);
            require(inventory().getStackInSlot(0).isEmpty(), "Empty slots also consume creative attacks");
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 150) {
            aim(mc, .5, .05);
        } else if (ticks == 160) {
            press(mc);
        } else if (ticks == 175) {
            mc.options.keyAttack.setDown(false);
        } else if (ticks == 185) {
            require(mc.level.isEmptyBlock(POS) && mc.getSingleplayerServer().overworld().isEmptyBlock(POS),
                    "A fresh click on the casing still breaks " + block());
            variant++;
            started = false;
        }
    }

    private static Block block() {
        return switch (variant) {
            case 0 -> Registration.SMELTER.get();
            case 1 -> Registration.STORAGE.get();
            case 2 -> Registration.STORAGE_3X3.get();
            default -> Blocks.STONE;
        };
    }

    private static IItemHandlerModifiable inventory() {
        return serverItems;
    }

    private static void intact(Minecraft mc) {
        require(mc.level.getBlockState(POS).is(block()) && mc.getSingleplayerServer().overworld().getBlockState(POS).is(block()),
                "Slot interactions keep the block on both client and server: " + block());
    }

    private static void aim(Minecraft mc, double x, double y) {
        Vec3 delta = Vec3.atLowerCornerOf(POS).add(x, y, 0).subtract(mc.player.getEyePosition());
        mc.player.setYRot((float) Math.toDegrees(Math.atan2(-delta.x, delta.z)));
        mc.player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())));
        mc.player.yRotO = mc.player.getYRot();
        mc.player.xRotO = mc.player.getXRot();
        mc.gameRenderer.pick(1);
    }

    private static void press(Minecraft mc) {
        mc.options.keyAttack.setDown(true);
        KeyMapping.click(mc.options.keyAttack.getKey());
    }

    private static void server(Runnable action) {
        Minecraft.getInstance().getSingleplayerServer().execute(() -> {
            try {
                action.run();
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
