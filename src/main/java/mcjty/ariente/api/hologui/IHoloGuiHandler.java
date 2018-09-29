package mcjty.ariente.api.hologui;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IHoloGuiHandler {

    boolean openHoloGui(World world, BlockPos pos, EntityPlayer player);

    IGuiComponent createNoAccessPanel();

    IHoloGuiEntity openHoloGui(World world, BlockPos pos, EntityPlayer player, String guiId, double distance);

    IHoloGuiEntity openHoloGuiRelative(World world, Entity parent, Vec3d offset, String guiId);

    IHoloGuiEntity openHoloGuiEntity(World world, BlockPos pos, EntityPlayer player, String tag, double distance);

    Class<? extends Entity> getHoloEntityClass();

    IGuiRegistry getGuiRegistry();

    IGuiComponentRegistry getComponentRegistry();
}
