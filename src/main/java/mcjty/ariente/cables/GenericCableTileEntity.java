package mcjty.ariente.cables;

import mcjty.ariente.ai.CityAI;
import mcjty.ariente.cities.ICityEquipment;
import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.facade.MimicBlockSupport;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class GenericCableTileEntity extends GenericTileEntity implements IFacadeSupport, IPowerBlob, ICityEquipment {

    private MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();
    private PowerSenderSupport powerBlobSupport = new PowerSenderSupport();

    @Nullable
    @Override
    public Map<String, Object> save() {
        if (mimicBlockSupport.getMimicBlock() == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        Block mimic = mimicBlockSupport.getMimicBlock().getBlock();
        data.put("mimic", mimic.getRegistryName().toString());
        data.put("meta", mimic.getMetaFromState(mimicBlockSupport.getMimicBlock()));
        return data;
    }

    @Override
    public void load(Map<String, Object> data) {
        if (data.get("mimic") instanceof String) {
            Block mimic = ForgeRegistries.BLOCKS.getValue(new ResourceLocation((String) data.get("mimic")));
            if (mimic == null) {
                System.out.println("Something went wrnog loading mimic state for: '" + data.get("mimic") + "'!");
            } else {
                int meta = (Integer) data.get("meta");
                mimicBlockSupport.setMimicBlock(mimic.getStateFromMeta(meta));
            }
        }
    }

    @Override
    public void setup(CityAI cityAI, World world, boolean firstTime) {
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        IBlockState oldMimicBlock = mimicBlockSupport.getMimicBlock();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            if (mimicBlockSupport.getMimicBlock() != oldMimicBlock) {
                world.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    @Override
    public CableColor getCableColor() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof GenericCableBlock) {
            return state.getValue(GenericCableBlock.COLOR);
        }
        return CableColor.COMBINED;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    public IBlockState getMimicBlock() {
        return mimicBlockSupport.getMimicBlock();
    }

    @Override
    public void setMimicBlock(IBlockState mimicBlock) {
        mimicBlockSupport.setMimicBlock(mimicBlock);
        markDirtyClient();
    }

    public void setCableId(int id) {
        if (powerBlobSupport.getCableId() != id) {
            powerBlobSupport.setCableId(id);
            markDirtyQuick();
        }
    }

    @Override
    public int getCableId() {
        return powerBlobSupport.getCableId();
    }

    @Override
    public void fillCableId(int id) {
        powerBlobSupport.fillCableId(world, pos, id, getCableColor());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        mimicBlockSupport.readFromNBT(tagCompound);
        powerBlobSupport.setCableId(tagCompound.getInteger("cableId"));
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        mimicBlockSupport.writeToNBT(tagCompound);
        tagCompound.setInteger("cableId", powerBlobSupport.getCableId());
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }
}
