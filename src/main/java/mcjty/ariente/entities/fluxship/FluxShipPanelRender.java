package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mcjty.ariente.display.client.DisplayCanvas;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.hologui.gui.HoloGuiEntityRender;
import mcjty.hologui.gui.HoloGuiRenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;

/** Rotate the cockpit instruments in the same cabin frame as the hull. */
public class FluxShipPanelRender extends EntityRenderer<HoloGuiEntity> {

    private static final MultiBufferSource.BufferSource GUI_BUFFERS = MultiBufferSource.immediate(new BufferBuilder(4096));

    public FluxShipPanelRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HoloGuiEntity panel, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        if (panel.getVehicle() instanceof FluxShipEntity ship) {
            // A passenger's independent position/rotation packets must not move the
            // dashboard relative to the cabin between client ticks.
            Vec3 correction = ship.getPanelPosition(partialTick).subtract(panel.getPosition(partialTick));
            pose.translate(correction.x, correction.y, correction.z);
            pose.mulPose(Axis.YP.rotationDegrees(-ship.getFlightYaw(partialTick)));
            pose.mulPose(Axis.XP.rotationDegrees(ship.getFlightPitch(partialTick)));
            pose.mulPose(Axis.ZP.rotationDegrees(ship.getBank(partialTick)));
            pose.mulPose(Axis.YP.rotationDegrees(180));
            renderInstruments(panel, ship, partialTick, pose);
        } else {
            HoloGuiEntityRender.doActualRender(panel, pose, buffers);
        }
        pose.popPose();
    }

    private static void renderInstruments(HoloGuiEntity panel, FluxShipEntity ship, float partialTick, PoseStack pose) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource buffers = type -> GUI_BUFFERS.getBuffer(HoloGuiRenderTools.guiRenderType(type));
        pose.translate(0, .5, 0);
        float scale = 1 - (1 - panel.getScale()) * 1.6f;
        pose.scale(scale, scale, scale);
        DisplayCanvas canvas = new DisplayCanvas(pose, buffers);
        canvas.fill(-.5f, -.5f, 1, 1, 0x30082030);
        float edge = .004f;
        canvas.fill(-.5f, -.5f, 1, edge, 0x9095e8ff);
        canvas.fill(-.5f, .5f - edge, 1, edge, 0x9095e8ff);
        canvas.fill(-.5f, -.5f, edge, 1, 0x9095e8ff);
        canvas.fill(.5f - edge, -.5f, edge, 1, 0x9095e8ff);
        Vec3 sightOffset = FluxShipLaser.sightOnPanel(ship, partialTick).subtract(FluxShipLaser.panelCenter(ship, partialTick));
        float sightX = (float) (sightOffset.dot(ship.rotateCabinOffset(new Vec3(-1, 0, 0), partialTick)) / scale);
        float sightY = (float) (sightOffset.dot(ship.rotateCabinOffset(new Vec3(0, 1, 0), partialTick)) / scale);
        pose.pushPose();
        pose.translate(0, 0, .006);
        crosshair(canvas, sightX, sightY, .008f, 0xf0081820);
        pose.translate(0, 0, .001);
        crosshair(canvas, sightX, sightY, .003f, 0xffb8f4ff);
        pose.popPose();
        GUI_BUFFERS.endBatch();
        IGuiComponent<?> gui = panel.getGui(mc.player);
        if (gui != null) {
            // The instruments are read-only. Keep their text opaque and add a
            // dark shadow so clouds and bright terrain remain visible behind it.
            pose.pushPose();
            pose.translate(.002, -.002, .009);
            gui.render(pose, type -> new VertexConsumerWrapper(buffers.getBuffer(type)) {

                @Override
                public VertexConsumer color(int red, int green, int blue, int alpha) {
                    return super.color(8, 16, 24, alpha);
                }
            }, mc.player, panel, -1, -1);
            pose.popPose();
            pose.translate(0, 0, .01);
            gui.render(pose, buffers, mc.player, panel, -1, -1);
            GUI_BUFFERS.endBatch();
        }
    }

    private static void crosshair(DisplayCanvas canvas, float x, float y, float thickness, int color) {
        canvas.fill(x - .055f, y - thickness / 2, .035f, thickness, color);
        canvas.fill(x + .02f, y - thickness / 2, .035f, thickness, color);
        canvas.fill(x - thickness / 2, y - .055f, thickness, .035f, color);
        canvas.fill(x - thickness / 2, y + .02f, thickness, .035f, color);
        canvas.fill(x - thickness / 2, y - thickness / 2, thickness, thickness, color);
    }

    @Override
    public ResourceLocation getTextureLocation(HoloGuiEntity panel) {
        return null;
    }
}
