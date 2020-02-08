package mcjty.ariente.blocks.utility.door;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.ariente.Ariente;
import mcjty.ariente.blocks.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;



public class InvisibleDoorRenderer extends TileEntityRenderer<InvisibleDoorTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    public InvisibleDoorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(InvisibleDoorTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = te.getWorld().getBlockState(te.getPos());
        if (state.getBlock() != ModBlocks.invisibleDoorBlock.get()) {
            return;
        }

        DoorMarkerTile doorMarkerTile = te.findDoorMarker();
        if (doorMarkerTile == null) {
            return;
        }

        int openphase = DoorMarkerRenderer.getOpenphase(doorMarkerTile);
        int iconIndex = doorMarkerTile.getIconIndex();

        GlStateManager.pushMatrix();

        Direction frontDirection = ModBlocks.invisibleDoorBlock.get().getFrontDirection(state);
        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
            // @todo 1.15
//            GlStateManager.translated(x, y, z+.5);
            GlStateManager.rotatef(90, 0, 1, 0);
        } else {
            // @todo 1.15
//            GlStateManager.translated(x + .5, y, z);
        }

        // @todo 1.15
//        bindTexture(halo);
        DoorMarkerRenderer.renderDoorSegment(openphase, iconIndex);

        GlStateManager.popMatrix();
    }

    public static void register() {
        // @todo 1.15
//        ClientRegistry.bindTileEntitySpecialRenderer(InvisibleDoorTile.class, new InvisibleDoorRenderer());
    }
}
