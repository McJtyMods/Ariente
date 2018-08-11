package mcjty.ariente.blocks.utility;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class InvisibleDoorRenderer extends TileEntitySpecialRenderer<InvisibleDoorTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    public InvisibleDoorRenderer() {
    }

    @Override
    public void render(InvisibleDoorTile te, double x, double y, double z, float time, int breakTime, float alpha) {
        IBlockState state = getWorld().getBlockState(te.getPos());
        if (state.getBlock() != ModBlocks.invisibleDoorBlock) {
            return;
        }

        DoorMarkerTile doorMarkerTile = te.findDoorMarker();
        if (doorMarkerTile == null) {
            return;
        }

        int openphase = DoorMarkerRenderer.getOpenphase(doorMarkerTile);
        int iconIndex = doorMarkerTile.getIconIndex();

        GlStateManager.pushMatrix();

        EnumFacing frontDirection = ModBlocks.invisibleDoorBlock.getFrontDirection(state);
        if (EnumFacing.NORTH.equals(frontDirection) || EnumFacing.SOUTH.equals(frontDirection)) {
            GlStateManager.translate(x, y, z+.5);
            GlStateManager.rotate(90, 0, 1, 0);
        } else {
            GlStateManager.translate(x + .5, y, z);
        }

        bindTexture(halo);
        DoorMarkerRenderer.renderDoorSegment(openphase, iconIndex);

        GlStateManager.popMatrix();
    }
}
