package mcjty.ariente.entities.fluxship;

import mcjty.ariente.client.ExtractedEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mcjty.ariente.display.client.DisplayCanvas;
import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.gui.HoloGuiEntity;
import mcjty.hologui.gui.HoloGuiEntityRender;
import mcjty.hologui.gui.HoloRenderBatch;
import mcjty.hologui.gui.OrderedSubmitCollector;
import java.util.function.BiConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.Minecraft;
import mcjty.lib.client.RenderBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

/** Rotate the cockpit instruments in the same cabin frame as the hull. */
public class FluxShipPanelRender extends ExtractedEntityRenderer<HoloGuiEntity> {

    public FluxShipPanelRender(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HoloGuiEntity panel, float yaw, float partialTick, PoseStack pose, RenderBufferSource buffers, int light) {
        pose.pushPose();
        if (panel.getVehicle() instanceof FluxShipEntity ship) {
            // A passenger's independent position/rotation packets must not move the
            // dashboard relative to the cabin between client ticks.
            Vec3 correction = ship.getPanelPosition(partialTick).subtract(panel.getPosition(partialTick));
            pose.translate(correction.x, correction.y, correction.z);
            pose.rotate(Axis.YP.rotationDegrees(-ship.getFlightYaw(partialTick)));
            pose.rotate(Axis.XP.rotationDegrees(ship.getFlightPitch(partialTick)));
            pose.rotate(Axis.ZP.rotationDegrees(ship.getBank(partialTick)));
            pose.rotate(Axis.YP.rotationDegrees(180));
            renderInstruments(panel, ship, partialTick, pose, buffers);
        } else {
            HoloGuiEntityRender.renderPanel(panel, partialTick, pose, buffers);
        }
        pose.popPose();
    }

    private static void renderInstruments(HoloGuiEntity panel, FluxShipEntity ship, float partialTick, PoseStack pose, RenderBufferSource buffers) {
        Minecraft mc = Minecraft.getInstance();
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
        battery(canvas, ship);
        crosshair(canvas, sightX, sightY, .008f, 0xf0081820);
        pose.translate(0, 0, .001);
        crosshair(canvas, sightX, sightY, .003f, 0xffb8f4ff);
        pose.popPose();
        IGuiComponent<?> gui = panel.getGui(mc.player);
        if (gui != null) {
            // The instruments are read-only. Keep their text opaque and add a
            // dark shadow so clouds and bright terrain remain visible behind it.
            pose.pushPose();
            pose.translate(.002, -.002, .009);
            HoloRenderBatch shadow = new HoloRenderBatch() {

                @Override
                public VertexConsumer getBuffer(RenderType type) {
                    return new VertexConsumerWrapper(buffers.getBuffer(type)) {

                        @Override
                        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
                            return super.setColor(8, 16, 24, alpha);
                        }
                    };
                }

                @Override
                public void command(PoseStack poses, BiConsumer<PoseStack, SubmitNodeCollector> command) {
                    ((HoloRenderBatch) buffers).command(poses, (base, collector) -> command.accept(base,
                            new OrderedSubmitCollector(collector) {

                                @Override
                                public void submitText(PoseStack textPose, float x, float y, FormattedCharSequence text,
                                                       boolean dropShadow, Font.DisplayMode mode, int light,
                                                       int color, int background, int outline) {
                                    FormattedCharSequence darkText = sink -> text.accept((index, style, codePoint) ->
                                            sink.accept(index, style.withColor(0x081018), codePoint));
                                    super.submitText(textPose, x, y, darkText, dropShadow, mode, light,
                                            color & 0xff000000 | 0x081018, background, outline);
                                }
                            }));
                }
            };
            gui.render(pose, shadow, mc.player, panel, -1, -1);
            pose.popPose();
            pose.translate(0, 0, .01);
            gui.render(pose, buffers, mc.player, panel, -1, -1);
        }
    }

    private static void crosshair(DisplayCanvas canvas, float x, float y, float thickness, int color) {
        canvas.fill(x - .055f, y - thickness / 2, .035f, thickness, color);
        canvas.fill(x + .02f, y - thickness / 2, .035f, thickness, color);
        canvas.fill(x - thickness / 2, y - .055f, thickness, .035f, color);
        canvas.fill(x - thickness / 2, y + .02f, thickness, .035f, color);
        canvas.fill(x - thickness / 2, y - thickness / 2, thickness, thickness, color);
    }

    private static void battery(DisplayCanvas canvas, FluxShipEntity ship) {
        float charge = ship.getChargeRatio();
        int color = charge <= .1f ? 0xffff6050 : charge <= .25f ? 0xffffcf60 : 0xff70edff;
        canvas.fill(-.465f, -.475f, .93f, .035f, 0xd0081820);
        canvas.fill(-.46f, -.47f, .92f * charge, .025f, color);
        // Text uses font pixel coordinates, while this canvas is in cockpit coordinates with Y upwards.
        canvas.poses().pushPose();
        canvas.poses().translate(-.46f, -.405f, .001f);
        canvas.poses().scale(.0035f, -.0035f, .0035f);
        canvas.text(charge == 0 ? Component.translatable("message.ariente.flux_ship.no_power").getString() + " - LAND TO RECHARGE"
                        : "CHARGE " + Math.round(charge * 100) + "%",
                0, 0, color, 1, 260);
        canvas.poses().popPose();
    }

    @Override
    public Identifier getTextureLocation(HoloGuiEntity panel) {
        return null;
    }
}
