package mcjty.ariente.blocks.utility.door;

import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class InvisibleDoorRenderer extends TileEntitySpecialRenderer<InvisibleDoorTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    public InvisibleDoorRenderer() {
    }

    @Override
    public void render(InvisibleDoorTile te, double x, double y, double z, float time, int breakTime, float alpha) {
        BlockState state = getWorld().getBlockState(te.getPos());
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

        Direction frontDirection = ModBlocks.invisibleDoorBlock.getFrontDirection(state);
        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
            GlStateManager.translate(x, y, z+.5);
            GlStateManager.rotate(90, 0, 1, 0);
        } else {
            GlStateManager.translate(x + .5, y, z);
        }

        bindTexture(halo);
        DoorMarkerRenderer.renderDoorSegment(openphase, iconIndex);

        GlStateManager.popMatrix();
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(InvisibleDoorTile.class, new InvisibleDoorRenderer());
    }
}
