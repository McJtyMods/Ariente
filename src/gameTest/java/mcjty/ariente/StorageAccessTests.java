package mcjty.ariente;

import mcjty.ariente.api.IStorageTile;
import mcjty.ariente.blocks.storagecube.StorageCubeTile;
import mcjty.ariente.blocks.utility.ILockTarget;
import mcjty.ariente.blocks.utility.LockTile;
import mcjty.ariente.blocks.utility.StorageTile;
import mcjty.ariente.blocks.utility.wireless.WirelessLockTile;
import mcjty.ariente.items.KeyCardItem;
import mcjty.ariente.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(Ariente.MODID)
@PrefixGameTestTemplate(false)
public class StorageAccessTests {

    @GameTest(template = "empty")
    public static void cityKeysCannotBeOverriddenByNearbyLocks(GameTestHelper helper) {
        BlockPos targetPos = helper.absolutePos(BlockPos.ZERO);
        BlockPos controllerPos = targetPos.east(3);
        for (Block block : storageBlocks()) {
            BlockEntity storage = place(helper, targetPos, block);
            setKey(storage, "city-storage");
            ILockTarget target = (ILockTarget) storage;
            target.setLocked(true);
            helper.getLevel().setBlockAndUpdate(controllerPos, Registration.LOCK.get().defaultBlockState());
            LockTile lock = (LockTile) helper.getLevel().getBlockEntity(controllerPos);
            lock.setHorizontalRange(1);
            lock.setHorizontalRange(5);
            lock.setLocked(true);
            lock.setLocked(false);
            helper.getLevel().removeBlock(controllerPos, false);
            helper.assertTrue(target.isLocked(), "A player Lock must not release keyed city storage: " + block);

            helper.getLevel().setBlockAndUpdate(controllerPos, Registration.WIRELESS_LOCK.get().defaultBlockState());
            WirelessLockTile wireless = (WirelessLockTile) helper.getLevel().getBlockEntity(controllerPos);
            wireless.setLocked(true);
            wireless.setHorizontalRange(1);
            wireless.setHorizontalRange(5);
            wireless.setLocked(false);
            helper.getLevel().removeBlock(controllerPos, false);
            helper.assertTrue(target.isLocked(), "A Wireless Lock must not release keyed city storage: " + block);

            target.setLocked(false);
            helper.getLevel().setBlockAndUpdate(controllerPos, Registration.LOCK.get().defaultBlockState());
            lock = (LockTile) helper.getLevel().getBlockEntity(controllerPos);
            lock.setLocked(true);
            helper.assertTrue(!target.isLocked(), "An unrelated controller cannot relock captured storage");
            lock.setKeyId("city-storage");
            lock.setLocked(false);
            lock.setLocked(true);
            helper.assertTrue(target.isLocked(), "A controller with the matching access key remains authorized");
            lock.setLocked(false);
            helper.assertTrue(!target.isLocked(), "An authorized controller can release its target");
            helper.getLevel().removeBlock(controllerPos, false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void locksBlockCachedAutomationHandlersOnEveryFace(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        for (Block block : storageBlocks()) {
            BlockEntity storage = place(helper, pos, block);
            ((IStorageTile) storage).setLoot(new ResourceLocation("minecraft", "diamond"), 0, false, 4, 0);
            setKey(storage, "city-storage");
            List<IItemHandler> handlers = new ArrayList<>();
            for (Direction side : Direction.values()) {
                handlers.add(storage.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElseThrow(IllegalStateException::new));
            }
            ILockTarget target = (ILockTarget) storage;
            target.setLocked(true);
            for (IItemHandler handler : handlers) {
                for (boolean simulate : List.of(true, false)) {
                    helper.assertTrue(handler.extractItem(0, 1, simulate).isEmpty(), "Locked extraction must fail: " + block);
                    helper.assertTrue(handler.insertItem(0, new ItemStack(Items.DIAMOND), simulate).getCount() == 1,
                            "Locked insertion must return its input: " + block);
                }
                helper.assertTrue(!handler.isItemValid(0, new ItemStack(Items.DIAMOND)), "Locked slots reject automation inputs");
                helper.assertTrue(handler.getStackInSlot(0).getCount() == 4, "Blocked transfers preserve the contents");
            }
            if (storage instanceof StorageTile grouped) {
                grouped.acceptKeyCard(new ItemStack(Registration.KEY_CARD.get()));
            } else {
                ((StorageCubeTile) storage).acceptKeyCard(new ItemStack(Registration.KEY_CARD.get()));
            }
            helper.assertTrue(target.isLocked(), "An unrelated card must not enable automation");
            ItemStack card = new ItemStack(Registration.KEY_CARD.get());
            KeyCardItem.addSecurityTag(card, "city-storage");
            if (storage instanceof StorageTile grouped) {
                grouped.acceptKeyCard(card);
            } else {
                ((StorageCubeTile) storage).acceptKeyCard(card);
            }
            helper.assertTrue(!target.isLocked(), "The matching card restores access");
            IItemHandler cached = handlers.get(0);
            helper.assertTrue(cached.extractItem(0, 1, false).getCount() == 1, "Cached handlers work after unlocking");
            helper.assertTrue(cached.insertItem(0, new ItemStack(Items.DIAMOND), false).isEmpty(), "Unlocked insertion works");
        }
        helper.succeed();
    }

    private static BlockEntity place(GameTestHelper helper, BlockPos pos, Block block) {
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        return helper.getLevel().getBlockEntity(pos);
    }

    private static void setKey(BlockEntity storage, String key) {
        if (storage instanceof StorageTile grouped) {
            grouped.setKeyId(key);
        } else {
            ((StorageCubeTile) storage).setKeyId(key);
        }
    }

    private static List<Block> storageBlocks() {
        return List.of(Registration.STORAGE.get(), Registration.STORAGE_3X3.get(), Registration.HOLO_STORAGE_1.get(),
                Registration.HOLO_STORAGE_2.get(), Registration.HOLO_STORAGE_3.get());
    }
}
