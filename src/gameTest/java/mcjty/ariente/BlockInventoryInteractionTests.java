package mcjty.ariente;

import com.mojang.authlib.GameProfile;
import mcjty.ariente.blocks.utility.BlockInventoryInteraction;
import mcjty.ariente.blocks.utility.SmelterTile;
import mcjty.ariente.blocks.utility.StorageTile;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class BlockInventoryInteractionTests {

    @GameTest(template = "empty")
    public static void creativeSlotExtractionKeepsMachinesAndChecksTheTarget(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        ServerPlayer player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "slot-click"));
        player.setGameMode(GameType.CREATIVE);
        for (Block block : List.of(Registration.SMELTER.get(), Registration.STORAGE.get(), Registration.STORAGE_3X3.get())) {
            helper.getLevel().removeBlock(pos, false);
            var state = block.defaultBlockState();
            state = block == Registration.SMELTER.get() ? state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                    : state.setValue(BlockStateProperties.FACING, Direction.NORTH);
            helper.getLevel().setBlockAndUpdate(pos, state);
            var tile = helper.getLevel().getBlockEntity(pos);
            var items = (IItemHandlerModifiable) tile.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
            items.setStackInSlot(0, new ItemStack(Items.RAW_IRON, 5));
            double height = tile instanceof SmelterTile ? .5 : .8;
            aim(player, pos, .77, height);
            BlockHitResult hit = (BlockHitResult) player.pick(player.getBlockReach(), 1, false);
            helper.assertTrue(BlockInventoryInteraction.getSlot(helper.getLevel(), hit) == 0,
                    "Front slot targeting matches " + block + " at " + pos + "; hit " + hit.getBlockPos()
                            + " on " + hit.getDirection() + " at " + hit.getLocation());
            helper.assertTrue(!BlockInventoryInteraction.extract(player, pos, 99), "Invalid slot requests are rejected");
            helper.assertTrue(BlockInventoryInteraction.extract(player, pos, 0), "Creative players can extract from " + block);
            helper.assertTrue(items.getStackInSlot(0).getCount() == 4 && helper.getLevel().getBlockState(pos).is(block),
                    "One click extracts one item and preserves the machine");
            player.setShiftKeyDown(true);
            helper.assertTrue(BlockInventoryInteraction.extract(player, pos, 0) && items.getStackInSlot(0).isEmpty(),
                    "Sneak-click still extracts the remaining stack");
            player.setShiftKeyDown(false);
            helper.assertTrue(BlockInventoryInteraction.extract(player, pos, 0) && helper.getLevel().getBlockState(pos).is(block),
                    "Empty item slots remain interaction targets");
            aim(player, pos, .5, .05);
            hit = (BlockHitResult) player.pick(player.getBlockReach(), 1, false);
            helper.assertTrue(BlockInventoryInteraction.getSlot(helper.getLevel(), hit) == -1,
                    "The casing remains outside the protected slots");
            helper.assertTrue(!BlockInventoryInteraction.extract(player, pos, 0), "Slot transfers reject casing clicks");
            player.setPos(pos.getX() + .77, pos.getY() + height - player.getEyeHeight(), pos.getZ() - 20);
            helper.assertTrue(!BlockInventoryInteraction.extract(player, pos, 0), "Out-of-reach requests are rejected");
        }
        helper.succeed();
    }

    private static void aim(ServerPlayer player, BlockPos pos, double x, double y) {
        player.setPos(pos.getX() + x, pos.getY() + y - player.getEyeHeight(), pos.getZ() - 2);
        player.setYRot(0);
        player.setYHeadRot(0);
        player.setXRot(0);
    }
}
