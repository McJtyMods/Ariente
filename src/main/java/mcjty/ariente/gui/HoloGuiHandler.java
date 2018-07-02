package mcjty.ariente.gui;

import mcjty.ariente.ModSounds;
import mcjty.ariente.entities.HoloGuiEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HoloGuiHandler {


    public static boolean openHoloGui(World world, BlockPos pos, EntityPlayer player) {
        if (world.isRemote) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), ModSounds.guiopen, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
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
        lookVec = new Vec3d(lookVec.x, 0, lookVec.z).normalize();
        x += lookVec.x;
        y += lookVec.y;
        z += lookVec.z;
        entity.setPosition(x, y, z);
        entity.setLocationAndAngles(x, y, z, player.rotationYaw, 0);
        world.spawnEntity(entity);
        return true;
    }

}
