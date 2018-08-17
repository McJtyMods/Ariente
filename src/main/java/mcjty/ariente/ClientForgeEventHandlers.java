package mcjty.ariente;

import mcjty.ariente.blocks.ModBlocks;
import mcjty.ariente.blocks.defense.ForceFieldRenderer;
import mcjty.ariente.dimension.EditMode;
import mcjty.ariente.dimension.EditModeClient;
import mcjty.ariente.network.ArienteMessages;
import mcjty.ariente.network.PacketHitForcefield;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientForgeEventHandlers {

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = event.getTarget().getBlockPos();
            EntityPlayer player = event.getPlayer();
            IBlockState state = player.getEntityWorld().getBlockState(pos);
            if (state.getBlock() == ModBlocks.rampBlock) {
                drawSelectionBox(state, player, pos, event.getPartialTicks());
                event.setCanceled(true);
            }
        }
    }

    private static void drawSelectionBox(IBlockState state, EntityPlayer player, BlockPos pos, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        ModBlocks.rampBlock.handleAABB(state, aabb -> RenderGlobal.drawSelectionBoundingBox(aabb.offset(pos).grow(0.002D).offset(-d3, -d4, -d5), 0.0F, 0.0F, 0.0F, 0.4F));

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    @SubscribeEvent
    public void onRenderWorldEvent(RenderWorldLastEvent event) {
        ForceFieldRenderer.renderForceFields(event.getPartialTicks());
        if (EditMode.editMode) {
            EditModeClient.renderPart(event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty empty) {
        // This event is only called clientside. Tell the server
        ArienteMessages.INSTANCE.sendToServer(new PacketHitForcefield());
    }
}
