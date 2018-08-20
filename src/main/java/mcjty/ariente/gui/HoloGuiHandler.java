package mcjty.ariente.gui;

import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.ariente.sounds.ModSounds;
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

    public static IGuiComponent createNoAccessPanel() {
        return new HoloPanel(0, 0, 8, 8)
                .add(new HoloText(2, 3, 1, 1, "Access", 0xff0000))
                .add(new HoloText(2, 4, 1, 1, "Denied!", 0xff0000))
            ;
    }

    public static HoloGuiEntity openHoloGui(World world, BlockPos pos, EntityPlayer player, String guiId, double distance) {
        if (world.isRemote) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), ModSounds.guiopen, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
            return null;
        }
        HoloGuiEntity entity = createHoloGui(world, pos, player, "", distance);
        entity.setGuiId(guiId);
        return entity;
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

        HoloGuiEntity entity = createHoloGui(world, pos, player, tag, distance);
        entity.setGuiTile(pos);
        return entity;
    }

    private static HoloGuiEntity createHoloGui(World world, BlockPos pos, EntityPlayer player, String tag, double distance) {
        HoloGuiEntity entity = new HoloGuiEntity(world);
        entity.setTag(tag);
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
