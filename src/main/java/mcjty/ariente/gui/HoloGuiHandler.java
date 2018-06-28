package mcjty.ariente.gui;

import mcjty.ariente.entities.HoloGuiEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HoloGuiHandler {


    public static boolean openHoloGui(World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            return true;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof IGuiTile)) {
            return true;
        }

        IGuiTile guiTile = (IGuiTile) te;

        HoloGuiEntity entity = new HoloGuiEntity(world);
        entity.setGuiTile(pos);
//        entity.setPosition(pos.getX()+1, pos.getY()+1, pos.getZ()+1);
        double x = player.posX;
        double y = player.posY+player.eyeHeight - .5;
        double z = player.posZ;
        Vec3d lookVec = player.getLookVec();
        x += lookVec.x;
        y += lookVec.y;
        z += lookVec.z;
        entity.setPosition(x, y, z);
        entity.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
        world.spawnEntity(entity);
        return true;
    }

}
