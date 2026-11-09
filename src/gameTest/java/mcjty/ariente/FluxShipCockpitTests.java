package mcjty.ariente;

import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipLaser;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class FluxShipCockpitTests {

    @GameTest(template = "empty")
    public static void pilotAndDashboardKeepTheOriginalCabinHeight(GameTestHelper helper) {
        Vec3 origin = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
        FluxShipEntity ship = FluxShipEntity.create(helper.getLevel(), origin.x, origin.y, origin.z);
        Player pilot = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(pilot.startRiding(ship), "The test pilot must mount the ship");
        try {
            for (float yaw : new float[]{0, 90, 180, -90}) {
                for (float pitch : new float[]{0, -20, 20}) {
                    ship.setYRot(yaw);
                    ship.setXRot(pitch);
                    ship.positionRider(pilot);
                    Vec3 cabin = ship.position().add(0, .5, 0);
                    Vec3 up = ship.rotateCabinOffset(new Vec3(0, 1, 0));
                    Vec3 expectedEye = cabin.add(up.scale(1.17));
                    helper.assertTrue(pilot.getEyePosition().distanceTo(expectedEye) < .00001,
                            "The pilot's eyes must retain the 1.20 height of 1.67 blocks above the ship");
                    Vec3 panel = FluxShipLaser.panelCenter(ship, 1);
                    double panelHeight = panel.subtract(cabin).dot(up) / up.lengthSqr() + .5;
                    helper.assertTrue(Math.abs(panelHeight - 1.65995023) < .00001,
                            "The dashboard must retain its original height so the energy bar clears the hull: " + panelHeight
                                    + " at yaw=" + yaw + ", pitch=" + pitch);
                }
            }
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }
}
