package mcjty.ariente;

import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MultipartGuiTile implements IGuiTile {

    private final World world;
    private final BlockPos pos;

    public MultipartGuiTile(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof MultipartTE) {
            PartSlot slot = PartSlot.byName(tag);
            MultipartTE.Part part = ((MultipartTE) tileEntity).getParts().get(slot);
            if (part.getTileEntity() instanceof IGuiTile) {
                return ((IGuiTile) part.getTileEntity()).createGui(tag, registry);
            }
        }
        return null;
    }

    @Override
    public void syncToClient() {

    }
}
