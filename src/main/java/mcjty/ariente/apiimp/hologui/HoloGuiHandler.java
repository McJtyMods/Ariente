package mcjty.ariente.apiimp.hologui;

import mcjty.ariente.Ariente;
import mcjty.ariente.api.hologui.*;
import mcjty.ariente.apiimp.hologui.components.GuiComponentRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HoloGuiHandler implements IHoloGuiHandler {

    public static final String TAG_DEFAULT = "default";

    private GuiRegistry guiRegistry = new GuiRegistry();
    private GuiComponentRegistry guiComponentRegistry = new GuiComponentRegistry();

    @Override
    public Class<? extends Entity> getHoloEntityClass() {
        return HoloGuiEntity.class;
    }

    @Override
    public boolean openHoloGui(World world, BlockPos pos, EntityPlayer player) {
        openHoloGuiEntity(world, pos, player, TAG_DEFAULT, 1.0);
        return true;
    }

    @Override
    public IGuiComponent createNoAccessPanel() {
        IGuiComponentRegistry registry = Ariente.guiHandler.getComponentRegistry();
        return registry.panel(0, 0, 8, 8)
                .add(registry.text(2, 3, 1, 1).text("Access").color(0xff0000))
                .add(registry.text(2, 4, 1, 1).text("Denied!").color(0xff0000))
            ;
    }

    @Override
    public IHoloGuiEntity openHoloGui(World world, BlockPos pos, EntityPlayer player, String guiId, double distance) {
        if (world.isRemote) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), HoloGuiSounds.guiopen, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
            return null;
        }
        HoloGuiEntity entity = createHoloGui(world, pos, player, "", distance);
        entity.setGuiId(guiId);
        return entity;
    }

    @Override
    public IHoloGuiEntity openHoloGuiRelative(World world, Entity parent, Vec3d offset, String guiId) {
//        if (world.isRemote) {
//            world.playSound(pos.getX(), pos.getY(), pos.getZ(), ModSounds.guiopen, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
//            return null;
//        }
        HoloGuiEntity entity = createHoloGuiRelative(world, parent, offset, "");
        entity.setGuiId(guiId);
        return entity;
    }

    @Override
    public IHoloGuiEntity openHoloGuiEntity(World world, BlockPos pos, EntityPlayer player, String tag, double distance) {
        if (world.isRemote) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), HoloGuiSounds.guiopen, SoundCategory.PLAYERS, 1.0f, 1.0f, true);
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

    private static HoloGuiEntity createHoloGuiRelative(World world, Entity parent, Vec3d offset, String tag) {
        HoloGuiEntity entity = new HoloGuiEntitySmall(world);
        entity.setTag(tag);
        double x = parent.posX + offset.x;
        double y = parent.posY + offset.y;
        double z = parent.posZ + offset.z;
        entity.setPosition(x, y, z);
        entity.setLocationAndAngles(x, y, z, parent.rotationYaw+90, parent.rotationPitch);
        world.spawnEntity(entity);
        return entity;
    }

    @Override
    public IGuiRegistry getGuiRegistry() {
        return guiRegistry;
    }

    @Override
    public IGuiComponentRegistry getComponentRegistry() {
        return guiComponentRegistry;
    }
}
