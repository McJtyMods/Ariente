package mcjty.ariente.entities.fluxship;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mcjty.ariente.Ariente;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.IdentityHashMap;
import java.util.Map;

/** Seat the rendered pilot in the cabin without changing their camera or control direction. */
@Mod.EventBusSubscriber(modid = Ariente.MODID, value = Dist.CLIENT)
public class FluxShipPilotRender {

    private static final Map<Player, PilotPose> ACTIVE = new IdentityHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void beforePlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!(player.getVehicle() instanceof FluxShipEntity ship)) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        ACTIVE.put(player, new PilotPose(player.yBodyRot, player.yBodyRotO, player.yHeadRot, player.yHeadRotO,
                player.getXRot(), player.xRotO, new Matrix4f(pose.last().pose()), new Matrix3f(pose.last().normal())));
        float partialTick = event.getPartialTick();
        float heading = ship.getFlightYaw(partialTick);
        double seatHeight = ship.getPassengersRidingOffset() + player.getMyRidingOffset();
        Vec3 seat = ship.getPosition(partialTick).add(0, .5, 0)
                .add(ship.rotateCabinOffset(new Vec3(0, seatHeight - .5, 0), partialTick));
        Vec3 correction = seat.subtract(player.getPosition(partialTick));
        pose.translate(correction.x, correction.y, correction.z);
        pose.mulPose(Axis.YP.rotationDegrees(-heading));
        pose.mulPose(Axis.XP.rotationDegrees(ship.getFlightPitch(partialTick)));
        pose.mulPose(Axis.ZP.rotationDegrees(ship.getBank(partialTick)));
        pose.mulPose(Axis.YP.rotationDegrees(heading));
        player.yBodyRot = player.yBodyRotO = heading;
        player.yHeadRot = player.yHeadRotO = heading;
        player.setXRot(0);
        player.xRotO = 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void afterPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        PilotPose original = ACTIVE.remove(player);
        if (original != null) {
            player.yBodyRot = original.body();
            player.yBodyRotO = original.previousBody();
            player.yHeadRot = original.head();
            player.yHeadRotO = original.previousHead();
            player.setXRot(original.pitch());
            player.xRotO = original.previousPitch();
            event.getPoseStack().last().pose().set(original.pose());
            event.getPoseStack().last().normal().set(original.normal());
        }
    }

    private record PilotPose(float body, float previousBody, float head, float previousHead, float pitch, float previousPitch,
                             Matrix4f pose, Matrix3f normal) {
    }
}
