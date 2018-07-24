package mcjty.ariente.gui;

import mcjty.ariente.ModSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HoloGuiHandler {

    public static final String TAG_DEFAULT = "default";

    public static boolean openHoloGui(World world, BlockPos pos, EntityPlayer player) {
        openHoloGuiEntity(world, pos, player, TAG_DEFAULT, 1.0);
        return true;
    }

    public static HoloGuiEntity openHoloGuiEntity(World world, BlockPos pos, EntityPlayer player, String tag, double distance) {
        if (world.isRemote) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), ModSounds.guiopen, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
            return null;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof IGuiTile)) {
            return null;
        }

        IGuiTile guiTile = (IGuiTile) te;

        HoloGuiEntity entity = new HoloGuiEntity(world);
        entity.setGuiTile(pos);
        entity.setTag(tag);
//        entity.setPosition(pos.getX()+1, pos.getY()+1, pos.getZ()+1);
        double x = player.posX;
        double y = player.posY+player.eyeHeight - .5;
        double z = player.posZ;
        Vec3d lookVec = player.getLookVec();
        lookVec = new Vec3d(lookVec.x, 0, lookVec.z).normalize();
        x += lookVec.x * distance;
        y += lookVec.y;
        z += lookVec.z * distance;
        entity.setPosition(x, y, z);
        entity.setLocationAndAngles(x, y, z, player.rotationYaw, 0);
        world.spawnEntity(entity);
        return entity;
    }

}
