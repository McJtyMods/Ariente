package mcjty.ariente.blocks.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/** Front-face item slots share targeting between rendering, input and server transfers. */
public class BlockInventoryInteraction {

    public static int getSlot(Level level, BlockHitResult hit) {
        var tile = level.getBlockEntity(hit.getBlockPos());
        if (tile instanceof SmelterTile smelter) {
            return smelter.getSlot(hit);
        }
        return tile instanceof StorageTile ? StorageTile.getSlot(hit, level) : -1;
    }

    public static boolean extract(Player player, BlockPos pos, int slot) {
        if (player.level().isClientSide || !player.isAlive() || player.isSpectator() || slot < 0
                || !player.canInteractWithBlock(pos, 0)
                || !(player.pick(player.blockInteractionRange(), 1, false) instanceof BlockHitResult hit)
                || !hit.getBlockPos().equals(pos) || getSlot(player.level(), hit) != slot) {
            return false;
        }
        var tile = player.level().getBlockEntity(pos);
        if (tile instanceof SmelterTile smelter) {
            smelter.giveToPlayer(slot, player);
        } else if (tile instanceof StorageTile storage) {
            storage.giveToPlayer(slot, player);
        }
        return true;
    }
}
