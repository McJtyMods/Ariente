package mcjty.ariente.blocks.utility.door;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;


public class InvisibleDoorRenderer extends TileEntityRenderer<InvisibleDoorTile> {

    private ResourceLocation halo = new ResourceLocation(Ariente.MODID, "textures/gui/guielements.png");

    public InvisibleDoorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(InvisibleDoorTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = te.getWorld().getBlockState(te.getPos());
        if (state.getBlock() != Registration.INVISIBLE_DOOR.get()) {
            return;
        }

        DoorMarkerTile doorMarkerTile = te.findDoorMarker();
        if (doorMarkerTile == null) {
            return;
        }

        int openphase = DoorMarkerRenderer.getOpenphase(doorMarkerTile);
        int iconIndex = doorMarkerTile.getIconIndex();

        matrixStack.push();

        Direction frontDirection = Registration.INVISIBLE_DOOR.get().getFrontDirection(state);
        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
            matrixStack.translate(0, 0, .5);
            matrixStack.rotate(Vector3f.YP.rotationDegrees(90));
        } else {
            matrixStack.translate(.5, 0, 0);
        }

        DoorMarkerRenderer.renderDoorSegment(matrixStack, buffer, openphase, iconIndex, combinedLightIn, combinedOverlayIn);

        matrixStack.pop();
    }

    public static void register() {
        ClientRegistry.bindTileEntityRenderer(Registration.INVISIBLE_DOOR_TILE.get(), InvisibleDoorRenderer::new);
    }
}
