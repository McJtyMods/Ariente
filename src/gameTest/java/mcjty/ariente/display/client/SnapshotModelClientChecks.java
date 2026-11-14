package mcjty.ariente.display.client;

import java.util.function.Predicate;
import java.util.ArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Direction;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.ariente.blocks.utility.camera.CameraCapture;
import mcjty.ariente.setup.Registration;
import mcjty.rftoolsbuilder.modules.shield.ShieldModule;
import mcjty.rftoolsbuilder.modules.shield.ShieldRenderingMode;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldingBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.List;

/** Runs the real client model/miniature path, including destination cache replacement. */
public class SnapshotModelClientChecks implements ClientModInitializer {

    private static int ticks;
    private static int waiting;
    private static boolean done;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(SnapshotModelClientChecks::tick);
    }

    private static void tick(Minecraft mc) {
        mc.options.pauseOnLostFocus = false;
        if (done) {
            return;
        }
        if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) {
            if (++waiting % 40 == 0 && mc.screen != null) {
                for (var child : mc.screen.children()) {
                    if (child instanceof AbstractButton button && (button.getMessage().getString().startsWith("Proceed")
                            || button.getMessage().getString().equals("Continue"))) {
                        button.onPress(new KeyEvent(257, 0, 0));
                        break;
                    }
                }
            }
            require(waiting < 1800, "Disposable snapshot test world did not open");
            return;
        }
        if (++ticks < 40) {
            return;
        }
        done = true;
        Object cardPreview = new Object();
        int empty = vertices(cardPreview, new CompoundTag());
        int stone = vertices(cardPreview, scene(Blocks.STONE.defaultBlockState()));
        require(stone > empty, "Vanilla stone must remain visible in the preview");
        int checked = 0;
        for (var block : List.of(ShieldModule.SHIELDING_SOLID.get(), ShieldModule.SHIELDING_TRANSLUCENT.get(),
                ShieldModule.SHIELDING_CUTOUT.get())) {
            for (ShieldRenderingMode mode : ShieldRenderingMode.values()) {
                CompoundTag scene = scene(block.defaultBlockState().setValue(ShieldingBlock.RENDER_MODE, mode));
                int first = vertices(cardPreview, scene);
                int cached = vertices(cardPreview, scene);
                require(first == cached, "Reusing a selected destination must preserve its cached geometry");
                boolean visible = mode != ShieldRenderingMode.INVISIBLE && mode != ShieldRenderingMode.MIMIC;
                require(visible ? first > empty : first == empty,
                        "Captured shield mode must control preview geometry: " + mode);
                require(vertices(cardPreview, scene(Blocks.STONE.defaultBlockState())) == stone,
                        "Changing the destination must replace the shield preview cache");
                checked++;
            }
        }
        checkEmitOnlyModel(mc);
        vertices(cardPreview, scene(Registration.MULTIPART_BLOCK.get().defaultBlockState()));
        vertices(cardPreview, scene(Blocks.WATER.defaultBlockState()));
        CameraDisplayRenderer.clearCache();
        require(vertices(cardPreview, scene(ShieldModule.SHIELDING_SOLID.get().defaultBlockState()
                .setValue(ShieldingBlock.RENDER_MODE, ShieldRenderingMode.SHIELD))) > empty,
                "A fresh preview after resource cache clearing must still render a shield");
        String message = "SNAPSHOT MODEL CLIENT CHECKS PASSED: " + checked
                + " shield states, destination switches/cache hits, Fabric-only geometry, vanilla, multipart, fluid and cache clearing";
        try {
            Files.writeString(mc.gameDirectory.toPath().resolve("snapshot-model-results.txt"), message + "\n");
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
        System.out.println(message);
        mc.stop();
    }

    private static void checkEmitOnlyModel(Minecraft mc) {
        BlockState state = Blocks.STONE.defaultBlockState();
        BlockPos pos = new BlockPos(2, 0, 1);
        BlockStateModel stone = mc.getModelManager().getBlockStateModelSet().get(state);
        class EmittedModel implements BlockStateModel, FabricBlockStateModel {

            private boolean emitted;

            @Override
            public int materialFlags() {
                return stone.materialFlags();
            }

            @Override
            public Material.Baked particleMaterial() {
                return stone.particleMaterial();
            }

            @Override
            public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
                throw new IllegalStateException("The snapshot must support models which only emit Fabric quads");
            }

            @Override
            public void emitQuads(QuadEmitter emitter, BlockAndTintGetter view, BlockPos actualPos, BlockState actualState,
                                  RandomSource random, Predicate<Direction> cullTest) {
                require(actualState == state && actualPos.equals(pos), "Fabric-only models need the captured state and position");
                emitted = true;
                ((FabricBlockStateModel) stone).emitQuads(emitter, view, pos, state, random, cullTest);
            }
        }
        EmittedModel model = new EmittedModel();
        List<BlockStateModelPart> parts = new ArrayList<>();
        try {
            var method = CameraDisplayRenderer.class.getDeclaredMethod("collectSnapshotParts", BlockStateModel.class,
                    BlockPos.class, BlockState.class, RandomSource.class, List.class);
            method.setAccessible(true);
            method.invoke(null, model, pos, state, RandomSource.create(42), parts);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Fabric-only snapshot geometry failed", error);
        }
        int quads = 0;
        for (var part : parts) {
            quads += part.getQuads(null).size();
            for (Direction side : Direction.values()) {
                quads += part.getQuads(side).size();
            }
        }
        require(model.emitted && quads >= 6, "Fabric-only model geometry must appear in the snapshot");
    }

    private static CompoundTag scene(BlockState state) {
        CompoundTag scene = new CompoundTag();
        ListTag palette = new ListTag();
        palette.add(NbtUtils.writeBlockState(state));
        scene.put("Palette", palette);
        scene.putLongArray("Blocks", new long[]{CameraCapture.pack(new BlockPos(2, 0, 1), 0)});
        return scene;
    }

    private static int vertices(Object owner, CompoundTag scene) {
        int[] count = {0};
        VertexConsumer sink = (VertexConsumer) Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),
                new Class<?>[]{VertexConsumer.class}, (proxy, method, args) -> {
                    if (method.getName().equals("addVertex")) {
                        count[0]++;
                    }
                    return method.getReturnType() == VertexConsumer.class ? proxy : null;
                });
        // Resolve actual baked client models and build every miniature vertex without submitting a GPU draw.
        CameraDisplayRenderer.renderMiniature(new PoseStack(), type -> sink, owner, scene, 1.6f, 255);
        return count[0];
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
