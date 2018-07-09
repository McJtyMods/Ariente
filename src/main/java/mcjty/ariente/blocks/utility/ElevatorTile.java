package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.entities.HoloGuiEntity;
import mcjty.ariente.gui.IGuiComponent;
import mcjty.ariente.gui.IGuiTile;
import mcjty.ariente.gui.components.HoloButton;
import mcjty.ariente.gui.components.HoloNumber;
import mcjty.ariente.gui.components.HoloPanel;
import mcjty.ariente.gui.components.HoloText;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ElevatorTile extends GenericTileEntity implements IGuiTile, ITickable {

    private AxisAlignedBB cachedBox = null;

    private int height = 9;

    @Override
    public void update() {
        if (!world.isRemote) {
            List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, getBeamBox());
            for (EntityPlayer player : players) {
                player.motionY = 1.5;
                player.isAirBorne = true;
            }
        } else {
            EntityPlayer clientPlayer = Ariente.proxy.getClientPlayer();
            if (clientPlayer.getEntityBoundingBox().intersects(getBeamBox())) {
                clientPlayer.motionY = 1.5;
                clientPlayer.isAirBorne = true;
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private void changeHeight(int dy) {
        height += dy;
        if (height < 1) {
            height = 1;
        } else if (height > 256) {
            height = 256;
        }
        cachedBox = null;
        markDirtyClient();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        int oldheight = height;

        super.onDataPacket(net, packet);

        if (getWorld().isRemote) {
            // If needed send a render update.
            if (oldheight != height) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
                cachedBox = null;
            }
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        height = tagCompound.getInteger("height");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("height", height);
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


    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getBeamBox();
    }

    private AxisAlignedBB getBeamBox() {
        if (cachedBox == null) {
            cachedBox = new AxisAlignedBB(getPos()).grow(0, height+2, 0);
        }
        return cachedBox;
    }


    @Override
    public IGuiComponent createGui(HoloGuiEntity entity) {
        return new HoloPanel(0, 0, 8, 8)
                .add(new HoloText(0, 2, 1, 1, "Height", 0xaaccff))
                .add(new HoloNumber(3, 4, 1, 1, 0xffffff, this::getHeight))

                .add(new HoloButton(1, 4, 1, 1).image(128+32, 128+16).hover(128+32+16, 128+16)
                        .hitEvent((component, player, entity1, x, y) -> changeHeight(-8)))
                .add(new HoloButton(2, 4, 1, 1).image(128+32, 128).hover(128+32+16, 128)
                        .hitEvent((component, player, entity1, x, y) -> changeHeight(-1)))
                .add(new HoloButton(5, 4, 1, 1).image(128, 128).hover(128+16, 128)
                        .hitEvent((component, player, entity1, x, y) -> changeHeight(1)))
                .add(new HoloButton(6, 4, 1, 1).image(128, 128+16).hover(128+16, 128+16)
                        .hitEvent((component, player, entity1, x, y) -> changeHeight(8)))
                ;
    }

    @Override
    public void syncToServer() {

    }
}
