package mcjty.ariente.blocks.defense;

import mcjty.ariente.gui.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ForceFieldTile extends GenericTileEntity implements IGuiTile, ITickable {

    private int[] entityIds = new int[PentakisDodecahedron.MAX_TRIANGLES];

    public ForceFieldTile() {
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            entityIds[i] = -1;
        }
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            activateShield();
        }
    }

    @Override
    public void invalidate() {
        disableShield();
        super.invalidate();
    }

    private void activateShield() {
        for (int i = 0 ; i < PentakisDodecahedron.MAX_TRIANGLES ; i++) {
            if (entityIds[i] == -1) {
                ForceFieldPanelEntity entity = new ForceFieldPanelEntity(world, i);
                double x = pos.getX();
                double y = pos.getY();
                double z = pos.getZ();
                entity.setLocationAndAngles(x, y, z, 0, 0);
                world.spawnEntity(entity);
                entityIds[i] = entity.getEntityId();
            }
        }
    }

    private void disableShield() {
        for (int i = 0; i < PentakisDodecahedron.MAX_TRIANGLES; i++) {
            if (entityIds[i] != -1) {
                Entity entity = world.getEntityByID(entityIds[i]);
                if (entity != null) {
                    entity.setDead();
                }
                entityIds[i] = -1;
            }
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        Boolean working = isWorking();
//        if (working) {
//            probeInfo.text(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (isWorking()) {
//            currenttip.add(TextFormatting.GREEN + "Producing " + getRfPerTick() + " RF/t");
//        }
    }

    @Override
    public IGuiComponent createGui(HoloGuiEntity entity, String tag) {
        return new HoloPanel(0, 0, 8, 8)
                .add(new HoloText(0, 0, 1, 1, "0", 0xffffff))
                .add(new HoloText(1, 0, 1, 1, "1", 0xffffff))
                .add(new HoloText(2, 0, 1, 1, "2", 0xffffff))
                .add(new HoloText(3, 0, 1, 1, "3", 0xffffff))
                .add(new HoloText(4, 0, 1, 1, "4", 0xffffff))
                .add(new HoloText(5, 0, 1, 1, "5", 0xffffff))
                .add(new HoloText(6, 0, 1, 1, "6", 0xffffff))
                .add(new HoloText(7, 0, 1, 1, "7", 0xffffff))
                .add(new HoloText(0, 1, 1, 1, "1", 0x00ff00))
                .add(new HoloText(0, 2, 1, 1, "2", 0x00ff00))
                .add(new HoloText(0, 3, 1, 1, "3", 0x00ff00))
                .add(new HoloText(0, 4, 1, 1, "4", 0x00ff00))
                .add(new HoloText(0, 5, 1, 1, "5", 0x00ff00))
                .add(new HoloText(0, 6, 1, 1, "6", 0x00ff00))
                .add(new HoloText(0, 7, 1, 1, "7", 0x00ff00))
                .add(new HoloText(7, 7, 1, 1, "X", 0xff0000));
    }

    @Override
    public void syncToServer() {

    }
}
