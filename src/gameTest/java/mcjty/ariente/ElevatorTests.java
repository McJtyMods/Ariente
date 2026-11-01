package mcjty.ariente;

import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class ElevatorTests {

    @GameTest(template = "empty")
    public static void levelMarkersDoNotTrapHoveringPlayers(GameTestHelper helper) {
        BlockPos shaft = new BlockPos(128, 100, 128);
        for (BlockPos pos : BlockPos.betweenClosed(shaft.offset(-2, -2, -2), shaft.offset(2, 3, 2))) {
            helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x != 0 || z != 0) {
                    helper.getLevel().setBlockAndUpdate(shaft.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                }
            }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.getLevel().setBlockAndUpdate(shaft.relative(direction), Registration.LEVEL_MARKER.get().defaultBlockState());
        }
        Player player = helper.makeMockSurvivalPlayer();
        // The reported save has feet exactly at landing height, offset toward a shaft corner,
        // and OnGround=false. Hovering players cannot use vanilla's grounded step-up behavior.
        for (double offset : new double[]{0.30000001192093, 0.5}) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                Vec3 start = new Vec3(shaft.getX() + offset, shaft.getY(), shaft.getZ() + offset);
                Vec3 movement = new Vec3(direction.getStepX() * 0.7, 0, direction.getStepZ() * 0.7);
                player.setPos(start);
                player.setOnGround(false);
                player.setDeltaMovement(Vec3.ZERO);
                player.move(MoverType.SELF, movement);
                helper.assertTrue(player.position().distanceTo(start.add(movement)) < 0.00001,
                        "A hovering player must walk through the level marker toward " + direction + " from offset " + offset);
            }
        }
        BlockPos marker = shaft.east();
        var state = helper.getLevel().getBlockState(marker);
        helper.assertTrue(state.getCollisionShape(helper.getLevel(), marker).isEmpty(), "Level markers must have no movement collision");
        helper.assertTrue(!state.getShape(helper.getLevel(), marker).isEmpty()
                        && state.getShape(helper.getLevel(), marker).bounds().maxY == 1.0 / 16,
                "Level markers must keep their thin selection shape for interaction");
        player.setPos(marker.getX() + 0.5, marker.getY() + 1, marker.getZ() + 0.5);
        player.setOnGround(false);
        player.move(MoverType.SELF, new Vec3(0, -1.5, 0));
        helper.assertTrue(player.getY() == marker.getY(), "Players must land on the floor beneath a marker, not on the marker");
        helper.succeed();
    }
}
