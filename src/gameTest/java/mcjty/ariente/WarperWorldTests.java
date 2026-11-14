package mcjty.ariente;

import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import com.mojang.authlib.GameProfile;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.teleport.WarperDestination;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.UUID;

/** Run with ArienteWorld's server, while still using Ariente's public compatibility API. */
@EventBusSubscriber(modid = Ariente.MODID)
public class WarperWorldTests {

    @SubscribeEvent
    public static void functions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, registry -> {
            registry.register(Identifier.parse("ariente:charged_dimension_button_and_return_data_survive_card_routing"), WarperWorldTests::chargedDimensionButtonAndReturnDataSurviveCardRouting);
        });
    }

    @SubscribeEvent
    public static void tests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(Identifier.parse("ariente:recent_warperworldtests"));
        event.registerTest(Identifier.parse("ariente:charged_dimension_button_and_return_data_survive_card_routing"), data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, Identifier.parse("ariente:charged_dimension_button_and_return_data_survive_card_routing")), data), new TestData<>(environment, Identifier.parse("ariente:warper_test"), 200, 0, true));
    }


    public static void chargedDimensionButtonAndReturnDataSurviveCardRouting(GameTestHelper helper) {
        helper.assertTrue(Ariente.setup.arienteWorld, "This regression requires the companion mod");
        ServerLevel overworld = helper.getLevel().getServer().overworld();
        BlockPos pos = helper.absolutePos(new BlockPos(17, 6, 17));
        overworld.setBlockAndUpdate(pos, Registration.WARPER.get().defaultBlockState());
        overworld.removeBlock(pos.above(), false);
        overworld.removeBlock(pos.above(2), false);
        WarperTile source = (WarperTile) overworld.getBlockEntity(pos);
        source.setWarperName("Legacy entrance");
        source.setDestination(new WarperDestination(Level.NETHER, pos), new CompoundTag());
        FakePlayer player = new FakePlayer(overworld, new GameProfile(UUID.randomUUID(), "WarperLegacy")) {

            @Override
            public void teleportTo(double x, double y, double z) {
                snapTo(x, y, z);
            }
        };
        player.snapTo(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
        overworld.addFreshEntity(player);
        try {
            source.setCharges(0);
            source.warp(player);
            helper.assertTrue(player.level() == overworld, "A card must not bypass the original button's charge gate");
            source.setCharges(UtilityConfiguration.WARPER_MAX_CHARGES.get());
            source.loadAdditional(source.saveWithFullMetadata(helper.getLevel().registryAccess()), helper.getLevel().registryAccess());
            helper.assertTrue(source.canDimensionWarp(), "Overworld retains dimension travel with a card destination");
            helper.assertTrue(source.getDimensionWarpTarget().equals("Ariente"), "Overworld buttons say Warp to Ariente");
            helper.assertTrue(source.createGui("", Ariente.guiHandler.getComponentRegistry()).findChild("dimension_warp").isPresent(),
                    "The charged dimension button remains available in the redirected Warper hologram");
            source.warp(player);
            helper.assertTrue(player.level().dimension().equals(ArienteWorldCompat.getArienteWorld().getDimension()), "Warp still reaches Ariente");
            helper.assertTrue(player.getPersistentData().read("arienteReturn", BlockPos.CODEC).orElseThrow().equals(pos.above()),
                    "The released arienteReturn field keeps its original format and entrance coordinates");
            ServerLevel ariente = (ServerLevel) player.level();
            BlockPos returnPos = player.blockPosition().below();
            // Use a nearby explicit return pad so the test does not depend on the arrival building layout.
            ariente.setBlockAndUpdate(returnPos, Registration.WARPER.get().defaultBlockState());
            WarperTile receiver = (WarperTile) ariente.getBlockEntity(returnPos);
            helper.assertTrue(receiver.canDimensionWarp() && receiver.getChargePercentage() == 100, "Ariente return pads start charged");
            helper.assertTrue(receiver.getDimensionWarpTarget().equals("Overworld"), "Ariente buttons say Warp to Overworld");
            receiver.warp(player);
            helper.assertTrue(player.level() == overworld && player.blockPosition().equals(pos.above()), "The return trip honors existing entrance data");
            source.clearDestination();
            helper.assertTrue(source.getCharges() == UtilityConfiguration.WARPER_MAX_CHARGES.get()
                    && source.getWarperName().equals("Legacy entrance"), "Clear changes routing without losing charges or names");
        } finally {
            player.discard();
        }
        helper.succeed();
    }
}
