package mcjty.ariente.blocks.utility.autofield;

import mcjty.lib.multipart.MultipartHelper;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

public class ConsumerInfo {

    public ConsumerInfo(World world, Set<Pair<BlockPos, PartSlot>> itemNodes) {
        for (Pair<BlockPos, PartSlot> pair : itemNodes) {
            TileEntity te = MultipartHelper.getTileEntity(world, pair.getKey(), pair.getRight());
            if (te instanceof ItemNodeTile) {
                ItemNodeTile itemNode = (ItemNodeTile) te;
                for (ItemStack stack : itemNode.getInputFilter()) {

                }

            }
        }

    }
}
