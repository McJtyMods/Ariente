package mcjty.ariente.blocks.utility.autofield;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.ariente.Ariente;
import mcjty.ariente.setup.Registration;
import mcjty.lib.client.CustomRenderTypes;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.client.RenderSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.Random;

public class AutoFieldRenderer extends TileEntityRenderer<AutoFieldTile> {

    public static ResourceLocation BEAMS[] = new ResourceLocation[3];
    static {
        BEAMS[0] = new ResourceLocation(Ariente.MODID, "textures/effects/autobeam1.png");
        BEAMS[1] = new ResourceLocation(Ariente.MODID, "textures/effects/autobeam2.png");
        BEAMS[2] = new ResourceLocation(Ariente.MODID, "textures/effects/autobeam3.png");
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
            renderBeamBox(te, matrixStack, partialTicks, box, buffer);
        }
        if (te.isRenderItems()) {
            // @todo 1.15
//            renderItemTransfers(matrixStack, te, x, y, z);
        }
    }

    private void renderBeamBox(AutoFieldTile te, MatrixStack matrixStack, float time, AxisAlignedBB box, IRenderTypeBuffer buffer) {
        Minecraft mc = Minecraft.getInstance();

        TextureAtlasSprite sprite = mc.getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(BEAMS[random.nextInt(3)]);

        int tex = te.getBlockPos().getX();
        int tey = te.getBlockPos().getY();
        int tez = te.getBlockPos().getZ();
        Vector3d projectedView = mc.gameRenderer.getMainCamera().getPosition().add(-tex, -tey, -tez);
        Vector3f player = new Vector3f((float)projectedView.x, (float)projectedView.y, (float)projectedView.z);

        long tt = System.currentTimeMillis() / 100;

        IVertexBuilder builder = buffer.getBuffer(CustomRenderTypes.TRANSLUCENT_ADD);

        float mx = (float) box.minX - tex;
        float my = (float) box.minY - tey;
        float mz = (float) box.minZ - tez;
        float px = (float) box.maxX - tex;
        float py = (float) box.maxY - tey;
        float pz = (float) box.maxZ - tez;

        Matrix4f matrix = matrixStack.last().pose();
        // @todo move to a static final once experimentation is done
        RenderSettings settings = RenderSettings.builder()
                .width(0.1f)
                .color(255, 255, 255)
                .alpha(128)
                .brightness(100)
                .build();

        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(mx, my, mz), new Vector3f(px, my, mz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(mx, my, mz), new Vector3f(mx, py, mz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(mx, my, mz), new Vector3f(mx, my, pz), player, settings);

        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(px, py, pz), new Vector3f(mx, py, pz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(px, py, pz), new Vector3f(px, my, pz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(px, py, pz), new Vector3f(px, py, mz), player, settings);

        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(px, my, mz), new Vector3f(px, py, mz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(px, my, mz), new Vector3f(px, my, pz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(mx, py, mz), new Vector3f(px, py, mz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(mx, py, mz), new Vector3f(mx, py, pz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(mx, my, pz), new Vector3f(px, my, pz), player, settings);
        RenderHelper.drawBeam(matrix, builder, sprite, new Vector3f(mx, my, pz), new Vector3f(mx, py, pz), player, settings);
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
        ClientRegistry.bindTileEntityRenderer(Registration.AUTOFIELD_TILE.get(), AutoFieldRenderer::new);
    }
}