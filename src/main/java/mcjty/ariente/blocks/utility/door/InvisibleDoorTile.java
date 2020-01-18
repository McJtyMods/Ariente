package mcjty.ariente.blocks.utility.door;

import mcjty.ariente.blocks.utility.ILockable;
import mcjty.ariente.config.UtilityConfiguration;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class InvisibleDoorTile extends GenericTileEntity implements ILockable {

    @Nullable
    public static PathNodeType getAiPathNodeType(BlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof InvisibleDoorTile) {
            DoorMarkerTile door = ((InvisibleDoorTile) te).findDoorMarker();
            if (door.isOpen()) {
                return PathNodeType.OPEN;
            }
        }
        return PathNodeType.BLOCKED;
    }

    public static AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof InvisibleDoorTile) {
            DoorMarkerTile door = ((InvisibleDoorTile) te).findDoorMarker();
            if (door != null && door.isOpen()) {
                return DoorMarkerTile.OPEN_BLOCK_AABB;
            }
        }
        return Block.FULL_BLOCK_AABB;
    }

    public static boolean addCollisionBoxToList(BlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
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

    @Override
    public boolean isLocked() {
        // An invisible door block can never be broken
        return true;
//        DoorMarkerTile tile = findDoorMarker();
//        if (tile.isLocked()) {
//            return true;
//        }
//        return false;
    }

    public DoorMarkerTile findDoorMarker() {
        DoorMarkerTile doorMarkerTile = null;
        // Find the parent door marker
        BlockPos p = pos.down();
        for (int i = 0; i < UtilityConfiguration.MAX_DOOR_HEIGHT.get() ; i++) {
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
