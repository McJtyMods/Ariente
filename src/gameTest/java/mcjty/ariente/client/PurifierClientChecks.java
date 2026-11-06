package mcjty.ariente.client;

import mcjty.ariente.Ariente;
import mcjty.ariente.PurifierTestContent;
import mcjty.ariente.blocks.generators.NegariteGeneratorTile;
import mcjty.ariente.blocks.generators.PosiriteGeneratorTile;
import mcjty.ariente.blocks.purifier.PurifierTile;
import mcjty.ariente.blocks.purifier.client.PurifierRingParticle;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.gui.HoloGuiEntity;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;

import java.nio.file.Files;

import static mcjty.ariente.compat.jei.JeiPlugin.PURIFYING;
import static mcjty.ariente.blocks.purifier.PurifierTubeTile.RING_RADIUS;

/** Opt-in, isolated visual check of both tube lengths, cross-fading and endpoint panels. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
@JeiPlugin
public class PurifierClientChecks implements IModPlugin {

    private static boolean started;
    private static volatile boolean ready;
    private static volatile Throwable failure;
    private static int ticks;
    private static String capture;
    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Ariente.MODID, "purifier_check");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!Boolean.getBoolean("ariente.purifierClientCheck") || event.phase != TickEvent.Phase.END
                || mc.player == null || mc.level == null || mc.getSingleplayerServer() == null || mc.getOverlay() != null) {
            return;
        }
        require(failure == null, "Purifier fixture failed: " + failure);
        if (!started) {
            started = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.setScreen(null);
            mc.mouseHandler.releaseMouse();
            var id = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                try {
                    var server = mc.getSingleplayerServer();
                    var level = server.overworld();
                    var player = server.getPlayerList().getPlayer(id);
                    level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
                    level.setDayTime(6000);
                    level.getEntitiesOfClass(HoloGuiEntity.class, new AABB(-10, 190, -10, 15, 215, 15)).forEach(HoloGuiEntity::discard);
                    for (BlockPos pos : BlockPos.betweenClosed(-4, 198, -4, 9, 205, 7)) {
                        level.setBlockAndUpdate(pos, (pos.getY() == 198 ? Blocks.SEA_LANTERN : Blocks.AIR).defaultBlockState());
                    }
                    for (int z : new int[]{0, 3}) {
                        int tubes = z == 0 ? 4 : 1;
                        BlockPos start = new BlockPos(0, 200, z);
                        level.setBlockAndUpdate(start, Registration.PURIFIER_START.get().defaultBlockState());
                        for (int i = 1; i <= tubes; i++) {
                            level.setBlockAndUpdate(start.east(i), Registration.PURIFIER_TUBE.get().defaultBlockState()
                                    .setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X));
                        }
                        level.setBlockAndUpdate(start.east(tubes + 1), Registration.PURIFIER_FINISH.get().defaultBlockState());
                        level.setBlockAndUpdate(start.north(), Registration.NEGARITE_GENERATOR.get().defaultBlockState());
                        level.setBlockAndUpdate(start.north().above(), Registration.NEGARITE_TANK.get().defaultBlockState());
                        ((NegariteGeneratorTile) level.getBlockEntity(start.north())).feedDust(64);
                        level.setBlockAndUpdate(start.south(), Registration.POSIRITE_GENERATOR.get().defaultBlockState());
                        level.setBlockAndUpdate(start.south().above(), Registration.POSIRITE_TANK.get().defaultBlockState());
                        if (tubes == 4) {
                            level.setBlockAndUpdate(start.north().above(2), Registration.NEGARITE_TANK.get().defaultBlockState());
                            level.setBlockAndUpdate(start.south().above(2), Registration.POSIRITE_TANK.get().defaultBlockState());
                        }
                        ((PosiriteGeneratorTile) level.getBlockEntity(start.south())).feedDust(64);
                        PowerSenderSupport.fixNetworks(level, start);
                        ((PurifierTile) level.getBlockEntity(start)).getItems().insertItem(0,
                                new ItemStack(z == 0 ? Items.IRON_ORE : Items.DEEPSLATE_COPPER_ORE, z == 0 ? 64 : 2), false);
                    }
                    player.setGameMode(GameType.CREATIVE);
                    player.getAbilities().flying = true;
                    player.onUpdateAbilities();
                    player.teleportTo(level, 3, 201, -3, 0, 18);
                    player.getInventory().clearContent();
                    player.getInventory().setItem(0, new ItemStack(Items.IRON_ORE, 32));
                    player.getInventory().setItem(9, new ItemStack(Items.DEEPSLATE_COPPER_ORE, 16));
                    ready = true;
                } catch (Throwable error) {
                    failure = error;
                }
            });
        }
        if (!ready) {
            return;
        }
        ticks++;
        if (ticks == 20) {
            var clientTile = (PurifierTile) mc.level.getBlockEntity(new BlockPos(0, 200, 0));
            require(clientTile.findRecipe(new ItemStack(Items.DEEPSLATE_IRON_ORE)).result().is(Items.RAW_IRON),
                    "The client inventory filter must recognize vanilla ores automatically");
            require(runtime != null && runtime.getRecipeManager().createRecipeLookup(PURIFYING).get()
                    .anyMatch(recipe -> recipe.ingredient().test(new ItemStack(Items.IRON_ORE)) && recipe.result().is(Items.RAW_IRON)),
                    "JEI must display the automatic iron conversion");
            require(runtime.getRecipeManager().createRecipeLookup(PURIFYING).get()
                    .anyMatch(recipe -> recipe.ingredient().test(new ItemStack(ForgeRegistries.ITEMS.getValue(PurifierTestContent.ORE)))),
                    "JEI must display automatically discovered third-party ores");
            aim(mc, new Vec3(2.5, 200.5, .5));
        } else if (ticks == 60) {
            require(mc.hitResult instanceof BlockHitResult hit && mc.level.getBlockState(hit.getBlockPos()).is(Registration.PURIFIER_TUBE.get()),
                    "Aiming at the glass must select the tube's fitted outline");
            checkOrbit(mc, Registration.PURIFIER_RING_X.get(), Direction.Axis.X);
            checkOrbit(mc, Registration.PURIFIER_RING_Z.get(), Direction.Axis.Z);
            capture = "purifier-ore";
        } else if (ticks == 300) {
            capture = "purifier-transition";
        } else if (ticks == 600) {
            var particle = mc.particleEngine.createParticle(Registration.PURIFIER_RING_X.get(), 1.5, 200.5, 3.5, 0, RING_RADIUS, .2);
            require(particle != null, "The ring provider must be registered");
            particle.tick();
            require(!particle.isAlive(), "Particles stop when their tube is no longer processing");
            capture = "purifier-raw";
        } else if (ticks == 660) {
            aim(mc, new Vec3(.5, 200.5, .5));
            mc.player.setXRot(0);
            mc.player.xRotO = 0;
        } else if (ticks == 665) {
            open(mc, new BlockPos(0, 200, 0));
        } else if (ticks == 695) {
            capture = "purifier-start-panel";
        } else if (ticks == 705) {
            mc.getSingleplayerServer().execute(() -> mc.getSingleplayerServer().overworld()
                    .getEntitiesOfClass(HoloGuiEntity.class, new AABB(-10, 190, -10, 15, 215, 15)).forEach(HoloGuiEntity::discard));
            aim(mc, new Vec3(5.5, 200.5, .5));
            mc.player.setXRot(0);
            mc.player.xRotO = 0;
        } else if (ticks == 710) {
            open(mc, new BlockPos(5, 200, 0));
        } else if (ticks == 740) {
            capture = "purifier-finish-panel";
        } else if (ticks == 750) {
            mc.getSingleplayerServer().execute(() -> {
                try {
                    var level = mc.getSingleplayerServer().overworld();
                    ItemStack output = ((PurifierTile) level.getBlockEntity(new BlockPos(5, 200, 0))).getItems().getStackInSlot(0);
                    require(output.is(Items.RAW_IRON) && output.getCount() == 5, "The full line must produce five raw iron");
                    output = ((PurifierTile) level.getBlockEntity(new BlockPos(2, 200, 3))).getItems().getStackInSlot(0);
                    require(output.is(Items.RAW_COPPER) && output.getCount() == 4, "The short line must finish two batches of two raw copper");
                } catch (Throwable error) {
                    failure = error;
                }
            });
        } else if (ticks == 760) {
            require(failure == null, "Purifier fixture failed: " + failure);
            try {
                Files.writeString(mc.gameDirectory.toPath().resolve("purifier-check.txt"),
                        "PASS: automatic vanilla/modded JEI recipes, client ore detection, two-tank full-speed iron line, single-tank copper line, raw outputs, tube cross-fade, orbiting particles on both axes, stopped particles and endpoint panels\n");
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
            mc.stop();
        }
    }

    private static void checkOrbit(Minecraft mc, SimpleParticleType type, Direction.Axis axis) {
        var particle = mc.particleEngine.createParticle(type, 1.5, 200.5, .5, 0, RING_RADIUS, .2);
        require(particle != null, "The orbit provider must be registered");
        require(particle instanceof PurifierRingParticle ring && ring.getLightColor(0) == LightTexture.FULL_BRIGHT,
                "Sparks stay fully bright independently of world lighting");
        Vec3 center = new Vec3(1.5, 200.5, .5);
        Vec3 previous = particle.getBoundingBox().getCenter();
        for (int tick = 0; tick < 8; tick++) {
            particle.tick();
            Vec3 position = particle.getBoundingBox().getCenter();
            // The bounding box is centered horizontally, with its base at the particle's Y coordinate.
            double y = particle.getBoundingBox().minY - center.y;
            double across = axis == Direction.Axis.X ? position.z - center.z : position.x - center.x;
            double along = axis == Direction.Axis.X ? position.x - center.x : position.z - center.z;
            require(particle.isAlive() && position.distanceTo(previous) > .01, "Existing sparks must move every tick");
            require(Math.abs(along) < .0001 && Math.abs(across * across + y * y - RING_RADIUS * RING_RADIUS) < .0001,
                    "Sparks must stay on the ring perpendicular to the tube");
            previous = position;
        }
        particle.remove();
    }

    private static void open(Minecraft mc, BlockPos pos) {
        var id = mc.player.getUUID();
        mc.getSingleplayerServer().execute(() -> Ariente.guiHandler.openHoloGui(mc.getSingleplayerServer().overworld(), pos,
                mc.getSingleplayerServer().getPlayerList().getPlayer(id)));
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
        require(GL11.glGetError() == GL11.GL_NO_ERROR, "Purifier rendering must not produce OpenGL errors");
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
