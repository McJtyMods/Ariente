package mcjty.ariente;

import io.netty.buffer.Unpooled;
import mcjty.ariente.entities.fluxship.FluxShipEntity;
import mcjty.ariente.entities.fluxship.PacketShipAction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class FluxShipMouseTests {

    @GameTest(template = "empty")
    public static void mouseAimTurnsClimbsAndDivesWithBoundedAttitudeAndSpeed(GameTestHelper helper) {
        var player = helper.makeMockPlayer();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        var ship = FluxShipEntity.create(helper.getLevel(), origin.getX() + .5, 220, origin.getZ() + .5);
        player.startRiding(ship);
        try {
            for (int i = 0; i < 120; i++) {
                float yaw = ship.getYRot();
                float pitch = ship.getXRot();
                ship.handleInput(player, 1, 0, 0, false, true, true, 65, -25);
                ship.tick();
                helper.assertTrue(Math.abs(Mth.wrapDegrees(ship.getYRot() - yaw)) <= 3.001
                                && Math.abs(ship.getXRot() - pitch) <= 2.001 && ship.getSpeed() <= 104.1,
                        "Mouse input must preserve smooth, bounded steering and the maximum flight speed");
            }
            helper.assertTrue(Math.abs(ship.getYRot() - 65) < 2 && ship.getXRot() < -24 && ship.getDeltaMovement().y > 1,
                    "The hull must converge on mouse aim and climb along its nose");
            for (int i = 0; i < 70; i++) {
                ship.handleInput(player, 1, 0, 0, false, true, true, 65, 25);
                ship.tick();
            }
            helper.assertTrue(ship.getXRot() > 24 && ship.getDeltaMovement().y < -1,
                    "Looking down must make an actual dive, not just turn the camera");
            float before = ship.getYRot();
            for (int i = 0; i < 30; i++) {
                ship.handleInput(player, 1, 1, 0, false, true, false, -120, 25);
                ship.tick();
            }
            helper.assertTrue(ship.getYRot() > before + 50 && Math.abs(ship.getXRot() - 25) < 1,
                    "Free look must ignore mouse heading while allowing A/D and retaining the flight pitch");
        } finally {
            player.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void malformedOrStaleMouseInputCannotSteerAnotherPilotsShip(GameTestHelper helper) {
        var pilot = helper.makeMockPlayer();
        var other = helper.makeMockPlayer();
        var ship = FluxShipEntity.create(helper.getLevel(), 0, 220, 0);
        pilot.startRiding(ship);
        try {
            ship.handleInput(other, 1, 1, 1, true, true, true, 90, 45);
            ship.handleInput(pilot, 1, 1, 1, true, true, true, Float.NaN, 45);
            ship.handleInput(pilot, 1, 1, 1, true, true, true, 90, Float.POSITIVE_INFINITY);
            ship.tick();
            helper.assertTrue(ship.getYRot() == 0 && ship.getXRot() == 0 && ship.getSpeed() == 0,
                    "Unauthorized and non-finite input must be rejected before affecting movement or firing");
            ship.handleInput(pilot, 1, 0, 0, false, true, true, 90, -25);
            for (int i = 0; i < 30; i++) {
                ship.tick();
            }
            helper.assertTrue(ship.getYRot() < 30 && Math.abs(ship.getXRot()) < 10,
                    "Mouse commands must expire when the client stops refreshing input");
        } finally {
            pilot.stopRiding();
            ship.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mouseFlightPacketPreservesAllControlsAndAim(GameTestHelper helper) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new PacketShipAction(1, -1, 1, true, true, true, 173.5f, -32.25f).toBytes(buffer);
            PacketShipAction decoded = new PacketShipAction(buffer);
            buffer.clear();
            decoded.toBytes(buffer);
            helper.assertTrue(buffer.readByte() == 1 && buffer.readByte() == -1 && buffer.readByte() == 1
                            && buffer.readBoolean() && buffer.readBoolean() && buffer.readBoolean()
                            && buffer.readFloat() == 173.5f && buffer.readFloat() == -32.25f && !buffer.isReadable(),
                    "Keyboard axes, firing, flight mode and mouse aim must survive the network round trip");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }
}
