package mcjty.ariente.entities.fluxship;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.joml.Matrix4f;

/** Seat the rendered pilot in the cabin without changing their camera or control direction. */
@EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipPilotRender {

    private static final ContextKey<Matrix4f> PILOT_POSE = new ContextKey<>(Identifier.parse("ariente:pilot_pose"));

    public static void registerModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {}, (entity, state) -> {
            state.setRenderData(PILOT_POSE, null);
            if (entity.getVehicle() instanceof FluxShipEntity ship) {
                float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
                float heading = ship.getFlightYaw(partialTick);
                double seatHeight = ship.getPassengersRidingOffset() - entity.getVehicleAttachmentPoint(ship).y;
                Vec3 seat = ship.getPosition(partialTick).add(0, .5, 0)
                        .add(ship.rotateCabinOffset(new Vec3(0, seatHeight - .5, 0), partialTick));
                Vec3 correction = seat.subtract(entity.getPosition(partialTick));
                Matrix4f transform = new Matrix4f().translation((float) correction.x, (float) correction.y, (float) correction.z)
                        .rotate(Axis.YP.rotationDegrees(-heading))
                        .rotate(Axis.XP.rotationDegrees(ship.getFlightPitch(partialTick)))
                        .rotate(Axis.ZP.rotationDegrees(ship.getBank(partialTick)))
                        .rotate(Axis.YP.rotationDegrees(heading));
                state.setRenderData(PILOT_POSE, transform);
                state.bodyRot = heading;
                state.yRot = 0;
                state.xRot = 0;
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforeLiving(RenderLivingEvent.Pre<?, ?, ?> event) {
        Matrix4f transform = event.getRenderState().getRenderData(PILOT_POSE);
        if (transform != null) {
            event.getPoseStack().pushPose();
            event.getPoseStack().mulPose(transform);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void afterLiving(RenderLivingEvent.Post<?, ?, ?> event) {
        if (event.getRenderState().getRenderData(PILOT_POSE) != null) {
            event.getPoseStack().popPose();
        }
    }
}
