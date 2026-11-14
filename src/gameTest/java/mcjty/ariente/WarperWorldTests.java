package mcjty.ariente;

import mcjty.lib.varia.PersistentData;
import net.fabricmc.fabric.api.entity.FakePlayer;
import com.mojang.authlib.GameProfile;
import mcjty.ariente.blocks.utility.WarperTile;
import mcjty.ariente.compat.arienteworld.ArienteWorldCompat;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.ariente.setup.Registration;
import mcjty.ariente.teleport.WarperDestination;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.UUID;

/** Run with ArienteWorld's server, while still using Ariente's public compatibility API. */
public class WarperWorldTests {

    @GameTest(template = "ariente:warper_test", timeoutTicks = 200)
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
        FakePlayer player = FakePlayer.get(overworld, new GameProfile(UUID.randomUUID(), "WarperLegacy"));
        player.moveTo(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
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
            helper.assertTrue(BlockPos.of(PersistentData.get(player).getLong("arienteReturn")).equals(pos.above()),
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
