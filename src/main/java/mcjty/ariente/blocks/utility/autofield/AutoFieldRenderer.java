package mcjty.ariente.blocks.utility.autofield;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.ariente.Ariente;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.client.RenderSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class AutoFieldRenderer extends TileEntityRenderer<AutoFieldTile> {

    private static ResourceLocation beams[] = new ResourceLocation[3];
    static {
        beams[0] = new ResourceLocation(Ariente.MODID, "textures/effects/autobeam1.png");
        beams[1] = new ResourceLocation(Ariente.MODID, "textures/effects/autobeam2.png");
        beams[2] = new ResourceLocation(Ariente.MODID, "textures/effects/autobeam3.png");
    }

    private Random random = new Random();

    public AutoFieldRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(AutoFieldTile te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        AxisAlignedBB box = te.getFieldBox();
        if (box == null) {
            return;
        }
        if (te.isRenderOutline()) {
            renderBeamBox(matrixStack, partialTicks, box);
        }
        if (te.isRenderItems()) {
            // @todo 1.15
//            renderItemTransfers(matrixStack, te, x, y, z);
        }
    }

    private void renderBeamBox(MatrixStack matrixStack, float time, AxisAlignedBB box) {
        Tessellator tessellator = Tessellator.getInstance();
        matrixStack.pushPose();

        GlStateManager._enableBlend();
        GlStateManager._depthMask(false);
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager._blendFunc(GL11.GL_ONE, GL11.GL_ONE);
//        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
        GlStateManager._disableCull();
        GlStateManager._enableDepthTest();

        ResourceLocation beamIcon = beams[random.nextInt(3)];
        // @todo 1.15
//        bindTexture(beamIcon);

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity p = mc.player;
        double doubleX = p.xOld + (p.getX() - p.xOld) * time;
        double doubleY = p.yOld + (p.getY() - p.yOld) * time;
        double doubleZ = p.zOld + (p.getZ() - p.zOld) * time;

        matrixStack.translate(-doubleX, -doubleY, -doubleZ);

        Vector3f player = new Vector3f((float) doubleX, (float) doubleY + p.getEyeHeight(), (float) doubleZ);

        long tt = System.currentTimeMillis() / 100;

        GlStateManager._color4f(1, 1, 1, 1);

        BufferBuilder renderer = tessellator.getBuilder();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LIGHTMAP_COLOR);

        float mx = (float) box.minX;
        float my = (float) box.minY;
        float mz = (float) box.minZ;
        float px = (float) (box.maxX + 0.0f);
        float py = (float) (box.maxY + 0.0f);
        float pz = (float) (box.maxZ + 0.0f);

        // @todo move to a static final once experimentation is done
        RenderSettings settings = RenderSettings.builder()
                .width(0.1f)
                .color(255, 255, 255)
                .alpha(128)
                .brightness(100)
                .build();

        RenderHelper.drawBeam(new Vector3f(mx, my, mz), new Vector3f(px, my, mz), player, settings);
        RenderHelper.drawBeam(new Vector3f(mx, my, mz), new Vector3f(mx, py, mz), player, settings);
        RenderHelper.drawBeam(new Vector3f(mx, my, mz), new Vector3f(mx, my, pz), player, settings);

        RenderHelper.drawBeam(new Vector3f(px, py, pz), new Vector3f(mx, py, pz), player, settings);
        RenderHelper.drawBeam(new Vector3f(px, py, pz), new Vector3f(px, my, pz), player, settings);
        RenderHelper.drawBeam(new Vector3f(px, py, pz), new Vector3f(px, py, mz), player, settings);

        RenderHelper.drawBeam(new Vector3f(px, my, mz), new Vector3f(px, py, mz), player, settings);
        RenderHelper.drawBeam(new Vector3f(px, my, mz), new Vector3f(px, my, pz), player, settings);
        RenderHelper.drawBeam(new Vector3f(mx, py, mz), new Vector3f(px, py, mz), player, settings);
        RenderHelper.drawBeam(new Vector3f(mx, py, mz), new Vector3f(mx, py, pz), player, settings);
        RenderHelper.drawBeam(new Vector3f(mx, my, pz), new Vector3f(px, my, pz), player, settings);
        RenderHelper.drawBeam(new Vector3f(mx, my, pz), new Vector3f(mx, py, pz), player, settings);

//        net.minecraft.util.math.vector.Vector3d cameraPos = net.minecraft.client.renderer.ActiveRenderInfo.getCameraPosition();
//        tessellator.getBuffer().sortVertexData((float) (player.x + doubleX), (float) (player.y + doubleY), (float) (player.z + doubleZ));
//        tessellator.getBuffer().sortVertexData((float)(cameraPos.x+doubleX), (float)(cameraPos.y+doubleY), (float)(cameraPos.z+doubleZ));
        tessellator.end();

        GlStateManager._depthMask(true);
        RenderSystem.enableLighting();
        GlStateManager._enableDepthTest();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);

        matrixStack.popPose();
    }

    private void renderItemTransfers(MatrixStack matrixStack, AutoFieldTile te, double x, double y, double z) {
        te.clientRequestRenderInfo();

        matrixStack.pushPose();
        matrixStack.translate(x, y, z);
        net.minecraft.client.renderer.RenderHelper.turnBackOn();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.enableAlphaTest();

        TransferRender[] transferRenders = te.getTransferRenders();
        for (int i = 0 ; i < transferRenders.length ; i++) {
            TransferRender render = transferRenders[i];
            if (render == null) {
                if (random.nextInt(50) == 1) {
                    AutoFieldRenderInfo renderInfo = te.getClientRenderInfo();
                    if (renderInfo != null) {
                        AutoFieldRenderInfo.TransferPath path = renderInfo.getRandomPath();
                        if (path != null) {
                            AutoFieldRenderInfo.Transfer transfer = renderInfo.getRandomTransfer(path);
                            if (transfer != null) {
                                transferRenders[i] = new TransferRender(path, transfer, te.getBlockPos());
                            }
                        }
                    }
                }
            } else {
                if (!render.render(matrixStack)) {
                    transferRenders[i] = null;
                }
            }
        }
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.disableAlphaTest();

        matrixStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(AutoFieldTile te) {
        return true;
    }

    public static void register() {
        // @todo 1.15
//        ClientRegistry.bindTileEntitySpecialRenderer(AutoFieldTile.class, new AutoFieldRenderer());
    }
}
