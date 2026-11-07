package mcjty.ariente;

import mcjty.ariente.api.IGenerator;
import mcjty.ariente.blocks.generators.PowerCombinerTile;
import mcjty.ariente.cables.CableColor;
import mcjty.ariente.cables.GenericCableBlock;
import mcjty.ariente.cables.GenericCableTileEntity;
import mcjty.ariente.compat.ArienteTOPDriver;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSystem;
import mcjty.ariente.power.PowerType;
import mcjty.ariente.setup.Registration;
import mcjty.hologui.api.components.IIntegerSlider;
import mcjty.lib.tileentity.TickingTileEntity;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.ProbeHitData;
import mcjty.theoneprobe.apiimpl.ProbeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class GeneratorPowerTests {

    @GameTest(template = "empty")
    public static void cityCableSectionsRejoinAtLoadedChunkBorders(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        BlockPos pos = new BlockPos(((origin.getX() >> 4) + 2) << 4, 250, origin.getZ() + 3);
        level.getChunkAt(pos.west());
        level.getChunkAt(pos);
        level.setBlockAndUpdate(pos.west(), Registration.NETCABLE.get().defaultBlockState());
        level.setBlockAndUpdate(pos, Registration.NETCABLE.get().defaultBlockState());
        GenericCableTileEntity left = (GenericCableTileEntity) level.getBlockEntity(pos.west());
        GenericCableTileEntity right = (GenericCableTileEntity) level.getBlockEntity(pos);
        PowerSystem system = PowerSystem.getPowerSystem(level);
        try {
            for (boolean firstTime : List.of(true, false)) {
                left.setCableId(system.allocateId());
                right.setCableId(system.allocateId());
                right.setup(null, level, firstTime);
                helper.assertTrue(left.getCableId() >= 0 && left.getCableId() == right.getCableId(),
                        "City cables must reconnect newly available sections on initial load and reload");
            }
        } finally {
            level.removeBlock(pos.west(), false);
            level.removeBlock(pos, false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void negariteProbeMatchesTankOutput(GameTestHelper helper) {
        checkGenerator(helper, true);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void posiriteProbeMatchesTankOutput(GameTestHelper helper) {
        checkGenerator(helper, false);
        helper.succeed();
    }

    private static void checkGenerator(GameTestHelper helper, boolean negative) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState generatorState = (negative ? Registration.NEGARITE_GENERATOR : Registration.POSIRITE_GENERATOR).get().defaultBlockState();
        BlockState tank = (negative ? Registration.NEGARITE_TANK : Registration.POSIRITE_TANK).get().defaultBlockState();
        BlockState wrongTank = (negative ? Registration.POSIRITE_TANK : Registration.NEGARITE_TANK).get().defaultBlockState();
        level.setBlockAndUpdate(pos, generatorState);
        TickingTileEntity generator = (TickingTileEntity) level.getBlockEntity(pos);
        ((IGenerator) generator).feedDust(1);
        PowerSystem system = PowerSystem.getPowerSystem(level);
        PowerType type = negative ? PowerType.NEGARITE : PowerType.POSIRITE;
        int id = system.allocateId();
        ((IPowerBlob) generator).fillCableId(id);
        Player player = helper.makeMockSurvivalPlayer();

        checkOutput(helper, generator, player, id, type, 0);
        level.setBlockAndUpdate(pos.above(), tank);
        checkOutput(helper, generator, player, id, type, 1000);
        level.setBlockAndUpdate(pos.above(2), tank);
        level.setBlockAndUpdate(pos.above(3), tank);
        checkOutput(helper, generator, player, id, type, 3000);
        level.setBlockAndUpdate(pos.above(2), wrongTank);
        checkOutput(helper, generator, player, id, type, 1000);
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        checkOutput(helper, generator, player, id, type, 0);
    }

    private static void checkOutput(GameTestHelper helper, TickingTileEntity generator, Player player, int id, PowerType type, int expected) {
        PowerSystem system = PowerSystem.getPowerSystem(helper.getLevel());
        system.consumePower(id, system.getTotalPower(id, type), type);
        generator.tick();
        helper.assertTrue(system.getTotalPower(id, type) == expected, "The network receives the output of consecutive matching tanks");
        RecordingProbeInfo probe = new RecordingProbeInfo();
        BlockPos pos = generator.getBlockPos();
        ArienteTOPDriver.DRIVER.addProbeInfo(ProbeMode.NORMAL, probe, player, helper.getLevel(), generator.getBlockState(),
                new ProbeHitData(pos, Vec3.atCenterOf(pos), Direction.NORTH, ItemStack.EMPTY));
        helper.assertTrue(probe.lines.contains(CompoundText.createLabelInfo("Generating: ", expected + " flux").get().getString()),
                "The probe must report " + expected + " flux, matching the actual network output: " + probe.lines);
    }

    @GameTest(template = "empty")
    public static void combinerControlsSynchronizeToClient(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState state = Registration.POWER_COMBINER.get().defaultBlockState();
        helper.getLevel().setBlockAndUpdate(pos, state);
        PowerCombinerTile server = (PowerCombinerTile) helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(server.getPowerTransfer() == 1000, "New combiners default to a 1000 flux input limit");
        PowerCombinerTile client = new PowerCombinerTile(pos, state);
        var gui = server.createGui("default", Ariente.guiHandler.getComponentRegistry());
        var slider = (IIntegerSlider) gui.findChild("transfer_rate").orElseThrow();
        Player player = helper.makeMockSurvivalPlayer();

        slider.hit(player, null, 7.4, 6.2);
        helper.assertTrue(server.getPowerTransfer() == 1001, "The right button raises the server transfer rate");
        client.onDataPacket(null, server.getUpdatePacket());
        helper.assertTrue(client.getPowerTransfer() == 1001, "The button's result must reach the client through the update packet");
        slider.hit(player, null, .5, 6.2);
        client.onDataPacket(null, server.getUpdatePacket());
        helper.assertTrue(client.getPowerTransfer() == 1000, "The left button's result must reach the client");

        slider.drag(player, null, 3.9, 6.2);
        helper.assertTrue(server.getPowerTransfer() == 10000, "Dragging the midpoint selects 10000 input flux");
        client.onDataPacket(null, server.getUpdatePacket());
        helper.assertTrue(client.getPowerTransfer() == 10000, "Dragging must update the client's transfer rate too");
        PowerCombinerTile reopened = new PowerCombinerTile(pos, state);
        reopened.loadClientDataFromNBT(server.getUpdateTag());
        helper.assertTrue(reopened.getPowerTransfer() == 10000, "The transfer rate must also synchronize when the chunk is loaded");
        PowerCombinerTile restored = new PowerCombinerTile(pos, state);
        restored.load(server.saveWithoutMetadata());
        helper.assertTrue(restored.getPowerTransfer() == 10000, "The selected transfer rate survives saving and loading");
        CompoundTag legacy = new CompoundTag();
        CompoundTag info = new CompoundTag();
        info.putInt("transfer", 100);
        legacy.put("Info", info);
        restored.load(legacy);
        helper.assertTrue(restored.getPowerTransfer() == 100, "Existing saved limits are preserved when the default changes");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void combinerTransfersAvailablePairsUpToItsLimit(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlockAndUpdate(pos, Registration.POWER_COMBINER.get().defaultBlockState());
        level.setBlockAndUpdate(pos.west(), Registration.NEGARITE_GENERATOR.get().defaultBlockState());
        level.setBlockAndUpdate(pos.east(), Registration.POSIRITE_GENERATOR.get().defaultBlockState());
        BlockState connector = Registration.CONNECTOR.get().defaultBlockState().setValue(GenericCableBlock.COLOR, CableColor.COMBINED);
        level.setBlockAndUpdate(pos.north(), connector);
        level.setBlockAndUpdate(pos.south(), connector);
        PowerCombinerTile combiner = (PowerCombinerTile) level.getBlockEntity(pos);
        PowerSystem system = PowerSystem.getPowerSystem(level);
        int negative = system.allocateId();
        int positive = system.allocateId();
        int north = system.allocateId();
        int south = system.allocateId();
        ((IPowerBlob) level.getBlockEntity(pos.west())).fillCableId(negative);
        ((IPowerBlob) level.getBlockEntity(pos.east())).fillCableId(positive);
        ((IPowerBlob) level.getBlockEntity(pos.north())).fillCableId(north);
        ((IPowerBlob) level.getBlockEntity(pos.south())).fillCableId(south);

        // Limit, available Negarite, available Posirite, actual input, total output of each type.
        int[][] cases = {
                {1000, 4000, 4000, 1000, 980},
                {12157, 4000, 4000, 4000, 3920},
                {12157, 4000, 1750, 1750, 1715},
                {1500, 4000, 4000, 1500, 1470},
                {20000, 4000, 0, 0, 0},
                {20000, 0, 4000, 0, 0},
                {0, 4000, 4000, 0, 0},
                {20000, 104, 4000, 104, 101}
        };
        for (int[] scenario : cases) {
            for (int id : new int[]{negative, positive, north, south}) {
                for (PowerType type : PowerType.values()) {
                    system.consumePower(id, system.getTotalPower(id, type), type);
                }
            }
            system.addPower(negative, scenario[1], PowerType.NEGARITE);
            system.addPower(positive, scenario[2], PowerType.POSIRITE);
            combiner.setPowerTransfer(scenario[0]);
            combiner.tickServer();
            helper.assertTrue(combiner.getUsingPower() == scenario[3], "The combiner uses the available pair up to its limit");
            helper.assertTrue(system.getTotalPower(negative, PowerType.NEGARITE) == scenario[1] - scenario[3]
                    && system.getTotalPower(positive, PowerType.POSIRITE) == scenario[2] - scenario[3],
                    "Exactly the same amount is consumed from both input networks, leaving any surplus");
            for (PowerType type : PowerType.values()) {
                long first = system.getTotalPower(north, type);
                long second = system.getTotalPower(south, type);
                helper.assertTrue(first + second == scenario[4] && Math.abs(first - second) <= 1,
                        "Outputs share the actual transferred power after the 2% loss, including rounding");
            }
        }
        helper.succeed();
    }

    private static class RecordingProbeInfo extends ProbeInfo {

        private final List<String> lines = new ArrayList<>();

        @Override
        public IProbeInfo text(CompoundText text) {
            lines.add(text.get().getString());
            return this;
        }
    }
}
