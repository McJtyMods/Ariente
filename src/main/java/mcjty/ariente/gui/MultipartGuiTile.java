package mcjty.ariente.gui;

import mcjty.hologui.api.IGuiComponent;
import mcjty.hologui.api.IGuiComponentRegistry;
import mcjty.hologui.api.IGuiTile;
import mcjty.lib.multipart.MultipartTE;
import mcjty.lib.multipart.PartSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;

public class MultipartGuiTile implements IGuiTile {

    private final Level world;
    private final BlockPos pos;

    public MultipartGuiTile(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public IGuiComponent<?> createGui(String tag, IGuiComponentRegistry registry) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof MultipartTE) {
            String[] split = StringUtils.split(tag, ":");
            PartSlot slot = PartSlot.byName(split[0]);
            MultipartTE.Part part = ((MultipartTE) tileEntity).getParts().get(slot);
            if (part != null && part.getTileEntity() instanceof IGuiTile) {
                return ((IGuiTile) part.getTileEntity()).createGui(tag, registry);
            }
        }
        return null;
    }

    @Override
    public void syncToClient() {

    }
}
