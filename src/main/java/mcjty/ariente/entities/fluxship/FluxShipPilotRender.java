package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.PoseStack;

import mcjty.ariente.client.RenderData;
import com.google.common.reflect.TypeToken;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Seat the rendered pilot in the cabin without changing their camera or control direction. */
public class FluxShipPilotRender {

    private static final ContextKey<Matrix4f> PILOT_POSE = new ContextKey<>(Identifier.parse("ariente:pilot_pose"));

    public static void extract(LivingEntity entity, LivingEntityRenderState state) {
            RenderData.set(state, PILOT_POSE, null);
            if (entity.getVehicle() instanceof FluxShipEntity ship) {
                float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
                float heading = ship.getFlightYaw(partialTick);
                double seatHeight = ship.getPilotSeatHeight(entity);
                Vec3 seat = ship.getPosition(partialTick).add(0, .5, 0)
                        .add(ship.rotateCabinOffset(new Vec3(0, seatHeight - .5, 0), partialTick));
                Vec3 correction = seat.subtract(entity.getPosition(partialTick));
                Matrix4f transform = new Matrix4f().translation((float) correction.x, (float) correction.y, (float) correction.z)
                        .rotate(Axis.YP.rotationDegrees(-heading))
                        .rotate(Axis.XP.rotationDegrees(ship.getFlightPitch(partialTick)))
                        .rotate(Axis.ZP.rotationDegrees(ship.getBank(partialTick)))
                        .rotate(Axis.YP.rotationDegrees(heading));
                RenderData.set(state, PILOT_POSE, transform);
                state.bodyRot = heading;
                state.yRot = 0;
                state.xRot = 0;
            }
    }

    public static void beforeLiving(LivingEntityRenderState state, PoseStack poses) {
        Matrix4f transform = RenderData.get(state, PILOT_POSE);
        if (transform != null) {
            poses.pushPose();
            poses.mulPose(transform);
        }
    }

    public static void afterLiving(LivingEntityRenderState state, PoseStack poses) {
        if (RenderData.get(state, PILOT_POSE) != null) {
            poses.popPose();
        }
    }
}
