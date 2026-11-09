package mcjty.ariente;

import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.FluxShipLaser;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = Ariente.MODID)
public class FluxShipCockpitTests {

    private static final Identifier TEST = Identifier.parse("ariente:pilot_and_dashboard_keep_the_original_cabin_height");

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, registry ->
                registry.register(TEST, FluxShipCockpitTests::pilotAndDashboardKeepTheOriginalCabinHeight));
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Identifier.parse("ariente:cockpit"));
        event.registerTest(TEST, data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, TEST), data),
                new TestData<>(environment, Identifier.parse("ariente:empty"), 100, 0, true));
    }

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
