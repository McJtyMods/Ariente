package mcjty.ariente.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Render the real hull after world and GUI lighting changes, including a negative control. */
public class FluxShipLightingChecks {

    public static void run(FluxShipEntity ship) {
        Minecraft mc = Minecraft.getInstance();
        Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting sorting = RenderSystem.getVertexSorting();
        Matrix3f inverseView = new Matrix3f(RenderSystem.getInverseViewRotationMatrix());
        Vector3f[] originalLights = lights();
        float[] color = RenderSystem.getShaderColor().clone();
        float fogStart = RenderSystem.getShaderFogStart();
        float fogEnd = RenderSystem.getShaderFogEnd();
        RenderTarget target = new TextureTarget(320, 240, true, Minecraft.ON_OSX);
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        try {
            modelView.setIdentity();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-4, 4, -3, 3, .1f, 32), VertexSorting.ORTHOGRAPHIC_Z);
            Matrix4f view = new Matrix4f().setLookAt(7, 4, 7, 0, .6f, 0, 0, 1, 0);
            RenderSystem.setInverseViewRotationMatrix(new Matrix3f(view).invert());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderFogStart(1000);
            RenderSystem.setShaderFogEnd(2000);
            target.setClearColor(0, 0, 0, 0);
            try (NativeImage oldWorld = capture(ship, target, view, true, 0);
                 NativeImage oldGui = capture(ship, target, view, true, 1);
                 NativeImage world = capture(ship, target, view, false, 0);
                 NativeImage flatGui = capture(ship, target, view, false, 1);
                 NativeImage itemGui = capture(ship, target, view, false, 2)) {
                int oldDifference = differences(oldWorld, oldGui);
                int flatDifference = differences(world, flatGui);
                int itemDifference = differences(world, itemGui);
                require(oldDifference > 100, "The old hull render must reproduce the shared-lighting dependency");
                require(flatDifference == 0 && itemDifference == 0,
                        "GUI lighting must not change the drawn hull: flat=" + flatDifference + ", item=" + itemDifference);
                oldWorld.writeToFile(mc.gameDirectory.toPath().resolve("ship-lighting-before-world.png"));
                oldGui.writeToFile(mc.gameDirectory.toPath().resolve("ship-lighting-before-gui.png"));
                world.writeToFile(mc.gameDirectory.toPath().resolve("ship-lighting-fixed-world.png"));
                flatGui.writeToFile(mc.gameDirectory.toPath().resolve("ship-lighting-fixed-gui.png"));
                Ariente.setup.getLogger().info("FLUX SHIP LIGHTING CHECKS PASSED: old render differs at {} pixels; fixed render differs at {} / {} pixels after flat/item GUI lighting; graphics={}",
                        oldDifference, flatDifference, itemDifference, mc.options.graphicsMode().get());
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Ship lighting verification failed", exception);
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection, sorting);
            RenderSystem.setInverseViewRotationMatrix(inverseView);
            RenderSystem.setShaderLights(originalLights[0], originalLights[1]);
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            RenderSystem.setShaderFogStart(fogStart);
            RenderSystem.setShaderFogEnd(fogEnd);
            target.destroyBuffers();
            mc.getMainRenderTarget().bindWrite(true);
        }
    }

    private static NativeImage capture(FluxShipEntity ship, RenderTarget target, Matrix4f view, boolean legacy, int lighting) {
        Minecraft mc = Minecraft.getInstance();
        if (lighting == 1) {
            Lighting.setupForFlatItems();
        } else if (lighting == 2) {
            Lighting.setupFor3DItems();
        } else if (mc.level.effects().constantAmbientLight()) {
            Lighting.setupNetherLevel(view);
        } else {
            Lighting.setupLevel(view);
        }
        Vector3f[] before = lights();
        target.clear(Minecraft.ON_OSX);
        target.bindWrite(true);
        PoseStack poses = new PoseStack();
        poses.last().pose().set(view);
        poses.last().normal().set(new Matrix3f(view));
        MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(new BufferBuilder(32768));
        if (legacy) {
            poses.translate(0, .5, 0);
            poses.mulPose(Axis.YP.rotationDegrees(-ship.getFlightYaw(.5f)));
            poses.mulPose(Axis.XP.rotationDegrees(ship.getFlightPitch(.5f)));
            poses.mulPose(Axis.ZP.rotationDegrees(ship.getBank(.5f)));
            poses.mulPose(Axis.YP.rotationDegrees(-90));
            mc.getBlockRenderer().getModelRenderer().renderModel(poses.last(),
                    buffers.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), null,
                    mc.getModelManager().getModel(FluxShipRender.MODEL), 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        } else {
            mc.getEntityRenderDispatcher().getRenderer(ship).render(ship, 0, .5f, poses, buffers, LightTexture.FULL_BRIGHT);
        }
        buffers.endBatch();
        Vector3f[] after = lights();
        require(before[0].equals(after[0]) && before[1].equals(after[1]),
                "Hull rendering must restore the previous lighting for other renderers");
        return Screenshot.takeScreenshot(target);
    }

    private static Vector3f[] lights() {
        var shader = GameRenderer.getRendertypeEntityCutoutNoCullShader();
        RenderSystem.setupShaderLights(shader);
        var first = shader.LIGHT0_DIRECTION.getFloatBuffer();
        var second = shader.LIGHT1_DIRECTION.getFloatBuffer();
        return new Vector3f[]{new Vector3f(first.get(0), first.get(1), first.get(2)),
                new Vector3f(second.get(0), second.get(1), second.get(2))};
    }

    private static int differences(NativeImage first, NativeImage second) {
        int count = 0;
        for (int y = 0; y < first.getHeight(); y++) {
            for (int x = 0; x < first.getWidth(); x++) {
                if (first.getPixelRGBA(x, y) != second.getPixelRGBA(x, y)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
