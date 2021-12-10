package mcjty.ariente.cables;

import mcjty.ariente.api.ICityAI;
import mcjty.ariente.api.ICityEquipment;
import mcjty.ariente.facade.IFacadeSupport;
import mcjty.ariente.facade.MimicBlockSupport;
import mcjty.ariente.power.IPowerBlob;
import mcjty.ariente.power.PowerSenderSupport;
import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class GenericCableTileEntity extends GenericTileEntity implements IFacadeSupport, IPowerBlob, ICityEquipment {

    private final MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();
    private final PowerSenderSupport powerBlobSupport = new PowerSenderSupport();

    public GenericCableTileEntity(TileEntityType<?> type) {
        super(type);
    }

    @Nullable
    @Override
    public Map<String, Object> save() {
        if (mimicBlockSupport.getMimicBlock() == null) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        Block mimic = mimicBlockSupport.getMimicBlock().getBlock();
        data.put("mimic", mimic.getRegistryName().toString());
        // @todo 1.14 meta
//        data.put("meta", mimic.getMetaFromState(mimicBlockSupport.getMimicBlock()));
        return data;
    }

    @Override
    public void load(Map<String, Object> data) {
        if (data.get("mimic") instanceof String) {
            Block mimic = ForgeRegistries.BLOCKS.getValue(new ResourceLocation((String) data.get("mimic")));
            if (mimic == null) {
                System.out.println("Something went wrong loading mimic state for: '" + data.get("mimic") + "'!");
            } else {
//                int meta = (Integer) data.get("meta");
                // @todo 1.14 meta
                mimicBlockSupport.setMimicBlock(mimic.defaultBlockState());
            }
        }
    }

    @Override
    public void setup(ICityAI cityAI, World world, boolean firstTime) {
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        BlockState oldMimicBlock = mimicBlockSupport.getMimicBlock();

        super.onDataPacket(net, packet);

        if (level.isClientSide) {
            // If needed send a render update.
            if (mimicBlockSupport.getMimicBlock() != oldMimicBlock) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        }
    }

    @Override
    public CableColor getCableColor() {
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() instanceof GenericCableBlock) {
            return state.getValue(GenericCableBlock.COLOR);
        }
        return CableColor.COMBINED;
    }

    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }

    @Override
    public BlockState getMimicBlock() {
        return mimicBlockSupport.getMimicBlock();
    }

    @Override
    public void setMimicBlock(BlockState mimicBlock) {
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
        powerBlobSupport.fillCableId(level, worldPosition, id, getCableColor());
    }

    @Override
    public void load(CompoundNBT tagCompound) {
        super.load(tagCompound);
        mimicBlockSupport.readFromNBT(tagCompound);
        powerBlobSupport.setCableId(tagCompound.getInt("cableId"));
    }

    @Override
    public void saveAdditional(CompoundNBT tagCompound) {
        super.saveAdditional(tagCompound);
        mimicBlockSupport.writeToNBT(tagCompound);
    }
}
