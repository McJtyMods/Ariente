package mcjty.ariente.blocks.utility;

import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class InvisibleDoorTile extends GenericTileEntity {

    public static boolean addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof InvisibleDoorTile) {
            DoorMarkerTile door = ((InvisibleDoorTile) te).findDoorMarker();
            if (door != null && !door.isOpen()) {
                AxisAlignedBB box = Block.FULL_BLOCK_AABB.offset(pos);
                if (entityBox.intersects(box)) {
                    collidingBoxes.add(box);
                }
            }
        }
        return true;
    }

    public DoorMarkerTile findDoorMarker() {
        DoorMarkerTile doorMarkerTile = null;
        // Find the parent door marker
        BlockPos p = pos.down();
        for (int i = 0 ; i < DoorMarkerTile.MAX_DOOR_HEIGHT ; i++) {
            TileEntity marker = getWorld().getTileEntity(p);
            if (marker instanceof DoorMarkerTile) {
                doorMarkerTile = (DoorMarkerTile) marker;
                break;
            }
            p = p.down();
        }
        return doorMarkerTile;
    }


    @Override
    public boolean shouldRenderInPass(int pass) {
        // @todo Should be pass 0 but it flickers then if an entity comes into view
        return pass == 0;
    }
}
