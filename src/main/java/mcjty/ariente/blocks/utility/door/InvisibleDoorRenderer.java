package mcjty.ariente.blocks.utility.door;

import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.ariente.setup.Registration;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraftforge.client.ClientRegistry;


public class InvisibleDoorRenderer implements BlockEntityRenderer<InvisibleDoorTile> {

    protected BlockEntityRendererProvider.Context context;

    public InvisibleDoorRenderer(BlockEntityRendererProvider.Context pContext) {
        context = pContext;
    }

    @Override
    public void render(InvisibleDoorTile te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        if (state.getBlock() != Registration.INVISIBLE_DOOR.get()) {
            return;
        }

        DoorMarkerTile doorMarkerTile = te.findDoorMarker();
        if (doorMarkerTile == null) {
            return;
        }

        int openphase = DoorMarkerRenderer.getOpenphase(doorMarkerTile);
        int iconIndex = doorMarkerTile.getIconIndex();

        matrixStack.pushPose();

        Direction frontDirection = Registration.INVISIBLE_DOOR.get().getFrontDirection(state);
        if (Direction.NORTH.equals(frontDirection) || Direction.SOUTH.equals(frontDirection)) {
            matrixStack.translate(0, 0, .5);
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(90));
        } else {
            matrixStack.translate(.5, 0, 0);
        }

        DoorMarkerRenderer.renderDoorSegment(matrixStack, buffer, openphase, iconIndex, combinedLightIn, combinedOverlayIn);

        matrixStack.popPose();
    }

    public static void register() {
        BlockEntityRenderers.register(Registration.INVISIBLE_DOOR_TILE.get(), InvisibleDoorRenderer::new);
    }
}
